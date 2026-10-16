#!/usr/bin/env bash
# Starts the JMSToolBox built by "./mvnw -f org.titou10.jtb.build/pom.xml clean verify" with a separate workspace (target/workspace) that already
# contains the sessions of config.xml. Your own workspace (~/.jtb) is not touched.
# Options:
#   --reset  start again from a fresh workspace
#   --wait   return only when JMSToolBox has been closed
set -euo pipefail
cd "$(dirname "$0")"
here=$(pwd)
products=../../org.titou10.jtb.product/target/products/org.titou10.jtb.product

wait=false
for arg in "$@"; do
   case "$arg" in
      --reset) rm -rf target ;;
      --wait)  wait=true ;;
      *) echo "Unknown option $arg"; exit 1 ;;
   esac
done

case "$(uname -s)-$(uname -m)" in
   Darwin-arm64)  built_app=$products/macosx/cocoa/aarch64/JMSToolBox.app; ini=Contents/Eclipse/JMSToolBox.ini ;;
   Darwin-x86_64) built_app=$products/macosx/cocoa/x86_64/JMSToolBox.app;  ini=Contents/Eclipse/JMSToolBox.ini ;;
   Linux-x86_64)  built_app=$products/linux/gtk/x86_64/JMSToolBox;         ini=JMSToolBox.ini ;;
   *) echo "Unsupported platform $(uname -s)-$(uname -m)"; exit 1 ;;
esac
[ -d "$built_app" ] || { echo "No build found in $built_app. Run: ./mvnw -f org.titou10.jtb.build/pom.xml clean verify"; exit 1; }

# Start a copy of the built app, so that its .ini can point to the smoke test workspace without changing the build
rm -rf target/app && mkdir -p target/app target/workspace/JMSToolBox
cp -R "$built_app" target/app/
app=target/app/$(basename "$built_app")
sed -i.bak "s#^@user.home/.jtb\$#$here/target/workspace#" "$app/$ini" && rm "$app/$ini.bak"

[ -f target/workspace/JMSToolBox/config.xml ] || cp config.xml target/workspace/JMSToolBox/

echo "Log: $here/target/workspace/JMSToolBox/jmstoolbox.log"
if [ "$(uname -s)" = "Darwin" ]; then
   # "open" returns immediately, "open -W" when the app has been closed
   if $wait; then open -W "$app"; else open "$app"; fi
else
   if $wait; then "$app/JMSToolBox"; else "$app/JMSToolBox" & fi
fi
