#!/usr/bin/env bash
# Automated smoke test, locally and in CI: builds JMSToolBox, starts the brokers, starts JMSToolBox with its REST services
# and tests every session of config.xml through them (headless_test.py). Also checks the GraalJS engine used by the
# visualizers. Stops JMSToolBox and the brokers at the end. The exit code is 0 only if all checks passed.
# Options:
#   --skip-build  use the existing build
#   --keep        leave the brokers running afterwards (stop them with: docker compose down -v)
set -euo pipefail
cd "$(dirname "$0")"

build=true
keep=false
for arg in "$@"; do
   case "$arg" in
      --skip-build) build=false ;;
      --keep)       keep=true ;;
      *) echo "Unknown option $arg"; exit 1 ;;
   esac
done

stop_app() {
   [ -f target/app.pid ] || return 0
   local pid
   pid=$(cat target/app.pid)
   # On Linux, the pid is the one of xvfb-run: stop its children (JMSToolBox, Xvfb) too
   pkill -TERM -P "$pid" 2>/dev/null || true
   kill -TERM "$pid" 2>/dev/null || true
   rm -f target/app.pid
}

cleanup() {
   local status=$?
   echo "=== Stopping JMSToolBox"
   stop_app
   # Show why a broker failed before its container (and its log) is removed
   if [ $status -ne 0 ]; then
      for service in $(docker compose ps -a --services 2>/dev/null); do
         state=$(docker compose ps -a --format '{{.State}} {{.Health}}' "$service" 2>/dev/null)
         case "$state" in
            "running healthy" | "running ") ;;
            *) echo "=== Log of $service ($state)"; docker compose logs --tail 30 "$service" ;;
         esac
      done
   fi
   if ! $keep; then
      echo "=== Stopping the brokers"
      docker compose down -v
   fi
}

if $build; then
   echo "=== Building JMSToolBox"
   ../../mvnw -B -ntp -q -f ../pom.xml clean verify
fi

trap cleanup EXIT

echo "=== Starting the brokers"
./setup.sh

echo "=== Starting JMSToolBox with its REST services"
./run.sh --reset --rest --background
log=target/workspace/JMSToolBox/jmstoolbox.log
for i in $(seq 1 120); do
   curl -s -o /dev/null --max-time 2 http://localhost:9998/ && break
   if ! kill -0 "$(cat target/app.pid)" 2>/dev/null; then
      echo "JMSToolBox has stopped:"; tail -20 target/app.out; exit 1
   fi
   sleep 1
done
curl -s -o /dev/null --max-time 2 http://localhost:9998/ || { echo "The REST services of JMSToolBox did not start within 120 s"; exit 1; }
echo "REST services up after ${i} s"

failed=0

echo "=== Testing the sessions"
python3 headless_test.py config.xml || failed=1

echo "=== Checking the JavaScript engine of the visualizers"
# 1. In JMSToolBox: the engine is looked up at startup, VisualizersManager fails to initialize without it
if grep -q "VisualizersManager initialized" $log; then
   echo "PASS  JavaScript engine found by JMSToolBox at startup"
else
   echo "FAIL  JavaScript engine: 'VisualizersManager initialized' missing in $log"; failed=1
fi
# 2. The GraalJS jars shipped in org.titou10.jtb.core, run with the Java runtime shipped with JMSToolBox
java=$(find target/app -path '*justj*/jre/bin/java' -type f | head -1)
core=$(find target/app -name 'org.titou10.jtb.core_*.jar' | head -1)
rm -rf target/graal && unzip -q -o "$core" 'lib/*' -d target/graal
if "$java" --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow \
      -Dpolyglot.engine.WarnInterpreterOnly=false -cp "$(ls target/graal/lib/*.jar | tr '\n' ':')" GraalCheck.java; then
   echo "PASS  GraalJS runs a visualizer-like script"
else
   echo "FAIL  GraalJS check"; failed=1
fi

echo "=== Checking the log for errors"
# The TIBCO and IBM MQ plugins always fail to start without their proprietary jars
errors=$(grep ' ERROR ' $log | grep -v -E "instatiating 'org\.titou10\.jtb\.qm\.(tibco|ibmmq)'" || true)
if [ -z "$errors" ]; then
   echo "PASS  no unexpected errors in the log"
else
   echo "FAIL  unexpected errors in the log:"; echo "$errors" | head -20; failed=1
fi

if [ $failed -eq 0 ]; then echo "=== All checks passed"; else echo "=== Some checks FAILED. Log: $(pwd)/$log"; fi
exit $failed
