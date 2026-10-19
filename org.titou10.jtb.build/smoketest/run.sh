#!/usr/bin/env bash
# Starts the JMSToolBox built by "./mvnw -f org.titou10.jtb.build/pom.xml clean verify" with a separate workspace
# (target/workspace) that already contains the sessions of config.xml. Your own workspace (~/.jtb) is not touched.
# Options:
#   --reset       start again from a fresh workspace
#   --wait        return only when JMSToolBox has been closed
#   --rest        start the REST services of JMSToolBox automatically (port 9998)
#   --background  start JMSToolBox without waiting and write its process id to target/app.pid
#                 (used by headless.sh; on Linux without a display, JMSToolBox runs in a virtual one via xvfb-run)
set -euo pipefail
cd "$(dirname "$0")"
here=$(pwd)
products=../../org.titou10.jtb.product/target/products/org.titou10.jtb.product

wait=false
rest=false
background=false
for arg in "$@"; do
   case "$arg" in
      --reset)      rm -rf target/workspace ;;
      --wait)       wait=true ;;
      --rest)       rest=true ;;
      --background) background=true ;;
      *) echo "Unknown option $arg"; exit 1 ;;
   esac
done

case "$(uname -s)-$(uname -m)" in
   Darwin-arm64)  built_app=$products/macosx/cocoa/aarch64/JMSToolBox.app; ini=Contents/Eclipse/JMSToolBox.ini; exe=Contents/MacOS/JMSToolBox ;;
   Darwin-x86_64) built_app=$products/macosx/cocoa/x86_64/JMSToolBox.app;  ini=Contents/Eclipse/JMSToolBox.ini; exe=Contents/MacOS/JMSToolBox ;;
   Linux-x86_64)  built_app=$products/linux/gtk/x86_64/JMSToolBox;         ini=JMSToolBox.ini;                 exe=JMSToolBox ;;
   *) echo "Unsupported platform $(uname -s)-$(uname -m)"; exit 1 ;;
esac
[ -d "$built_app" ] || { echo "No build found in $built_app. Run: ./mvnw -f org.titou10.jtb.build/pom.xml clean verify"; exit 1; }

# Start a copy of the built app, so that its .ini can point to the smoke test workspace without changing the build
rm -rf target/app && mkdir -p target/app target/workspace/JMSToolBox
cp -R "$built_app" target/app/
app=target/app/$(basename "$built_app")
sed -i.bak "s#^@user.home/.jtb\$#$here/target/workspace#" "$app/$ini" && rm "$app/$ini.bak"

workspace=target/workspace/JMSToolBox
[ -f $workspace/config.xml ] || cp config.xml $workspace/
if $rest; then
   touch $workspace/jmstoolbox.properties
   grep -v -E '^rest\.(autostart|port)=' $workspace/jmstoolbox.properties > $workspace/jmstoolbox.properties.new || true
   printf 'rest.autostart=true\nrest.port=9998\n' >> $workspace/jmstoolbox.properties.new
   mv $workspace/jmstoolbox.properties.new $workspace/jmstoolbox.properties
fi

echo "Log: $here/$workspace/jmstoolbox.log"
if $background; then
   # Start the executable directly (not via "open"), so that its process id is known
   if [ "$(uname -s)" = "Linux" ] && [ -z "${DISPLAY:-}" ]; then
      xvfb-run -a "$app/$exe" > target/app.out 2>&1 &
   else
      "$app/$exe" > target/app.out 2>&1 &
   fi
   echo $! > target/app.pid
elif [ "$(uname -s)" = "Darwin" ]; then
   # "open" returns immediately, "open -W" when the app has been closed
   if $wait; then open -W "$app"; else open "$app"; fi
else
   if $wait; then "$app/$exe"; else "$app/$exe" & fi
fi
