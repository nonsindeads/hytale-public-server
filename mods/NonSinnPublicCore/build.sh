#!/bin/sh
set -eu

SERVER_JAR=${1:?Aufruf: ./build.sh /pfad/zu/HytaleServer.jar /pfad/zu/LuckPerms.jar}
LUCKPERMS_JAR=${2:?Aufruf: ./build.sh /pfad/zu/HytaleServer.jar /pfad/zu/LuckPerms.jar}
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BUILD_DIR="$ROOT_DIR/build"

mkdir -p "$BUILD_DIR/classes" "$BUILD_DIR/resources/defaults"
find "$BUILD_DIR/classes" -type f -delete

javac -encoding UTF-8 -cp "$SERVER_JAR:$LUCKPERMS_JAR" -d "$BUILD_DIR/classes" \
  $(find "$ROOT_DIR/src/main/java" -name '*.java' -print)

cp "$ROOT_DIR/manifest.json" "$BUILD_DIR/resources/manifest.json"
cp "$ROOT_DIR/../../config/onboarding/questions.json" "$BUILD_DIR/resources/defaults/questions.json"

jar --create --file "$BUILD_DIR/NonSinnPublicCore-0.2.1.jar" \
  -C "$BUILD_DIR/classes" . \
  -C "$BUILD_DIR/resources" .

echo "$BUILD_DIR/NonSinnPublicCore-0.2.1.jar"
