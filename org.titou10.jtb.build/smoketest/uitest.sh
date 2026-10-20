#!/usr/bin/env bash
# Runs the SWTBot UI tests (org.titou10.jtb.uitest): starts the brokers, builds JMSToolBox with the "uitest" profile,
# which starts JMSToolBox and lets SWTBot click through it, and stops the brokers at the end.
# Don't use mouse and keyboard while the tests run: the JMSToolBox window shows up and SWTBot needs the focus.
# On Linux without a display, the tests run in a virtual one (xvfb-run).
# Each step is logged ("UI: ...") and shown with a picture of the windows in the report org.titou10.jtb.uitest/target/uitest-report.
# Options:
#   --keep  leave the brokers running afterwards (stop them with: docker compose down -v)
#   --slow  pause 500 ms between SWTBot actions, to watch the tests
set -euo pipefail
cd "$(dirname "$0")"

keep=false
delay=0
for arg in "$@"; do
   case "$arg" in
      --keep) keep=true ;;
      --slow) delay=500 ;;
      *) echo "Unknown option $arg"; exit 1 ;;
   esac
done

if ! $keep; then
   trap 'echo "=== Stopping the brokers"; docker compose down -v' EXIT
fi

echo "=== Starting the brokers"
./setup.sh

echo "=== Building JMSToolBox and running the UI tests"
mvn=(../../mvnw -B -ntp -Puitest -Duitest.delay=$delay -f ../pom.xml clean verify)
if [ "$(uname -s)" = "Linux" ] && [ -z "${DISPLAY:-}" ]; then
   xvfb-run -a "${mvn[@]}"
else
   "${mvn[@]}"
fi
echo "=== UI tests passed. Report: $(cd ../.. && pwd)/org.titou10.jtb.uitest/target/uitest-report/index.html"
