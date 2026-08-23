#!/bin/sh
set -eu

SERVER_JAR=${1:?Aufruf: ./build.sh HytaleServer.jar LuckPerms.jar GlymeraPlotWorld.jar VaultUnlocked.jar}
LUCKPERMS_JAR=${2:?Aufruf: ./build.sh HytaleServer.jar LuckPerms.jar GlymeraPlotWorld.jar VaultUnlocked.jar}
PLOTWORLD_JAR=${3:?Aufruf: ./build.sh HytaleServer.jar LuckPerms.jar GlymeraPlotWorld.jar VaultUnlocked.jar}
VAULT_JAR=${4:?Aufruf: ./build.sh HytaleServer.jar LuckPerms.jar GlymeraPlotWorld.jar VaultUnlocked.jar}
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BUILD_DIR="$ROOT_DIR/build"

mkdir -p "$BUILD_DIR/classes" "$BUILD_DIR/resources/defaults"
find "$BUILD_DIR/classes" -type f -delete

javac -encoding UTF-8 -cp "$SERVER_JAR:$LUCKPERMS_JAR:$PLOTWORLD_JAR:$VAULT_JAR" -d "$BUILD_DIR/classes" \
  $(find "$ROOT_DIR/src/main/java" -name '*.java' -print)

cp "$ROOT_DIR/manifest.json" "$BUILD_DIR/resources/manifest.json"
cp "$ROOT_DIR/../../config/onboarding/questions.json" "$BUILD_DIR/resources/defaults/questions.json"
cp "$ROOT_DIR/../../config/plots/property-pricing.json" "$BUILD_DIR/resources/defaults/property-pricing.json"

jar --create --file "$BUILD_DIR/NonSinnPublicCore-0.3.0.jar" \
  -C "$BUILD_DIR/classes" . \
  -C "$BUILD_DIR/resources" .

echo "$BUILD_DIR/NonSinnPublicCore-0.3.0.jar"
