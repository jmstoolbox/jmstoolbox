#!/usr/bin/env bash
# Does the whole smoke test in one go: builds JMSToolBox, starts the brokers, starts JMSToolBox with the
# preconfigured sessions and, once JMSToolBox has been closed, stops the brokers again.
# Options:
#   --skip-build  use the existing build
#   --reset       start from a fresh JMSToolBox workspace (see run.sh)
#   --keep        leave the brokers running afterwards (stop them with: docker compose down -v)
set -euo pipefail
cd "$(dirname "$0")"

build=true
keep=false
run_args=(--wait)
for arg in "$@"; do
   case "$arg" in
      --skip-build) build=false ;;
      --keep)       keep=true ;;
      --reset)      run_args+=(--reset) ;;
      *) echo "Unknown option $arg"; exit 1 ;;
   esac
done

if $build; then
   echo "=== Building JMSToolBox"
   ../../mvnw -B -ntp -q -f ../pom.xml clean verify
fi

# Stop the brokers on every exit: normal end, error or Ctrl+C
if ! $keep; then
   trap 'echo "=== Stopping the brokers"; docker compose down -v' EXIT
fi

echo "=== Starting the brokers"
./setup.sh

echo "=== Starting JMSToolBox. Close it to end the smoke test"
./run.sh "${run_args[@]}"
