#!/usr/bin/env bash
# Maven Wrapper script
# This script will download Maven wrapper jar if not present and execute Maven.
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$DIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper..."
  curl -L -o "$WRAPPER_JAR" https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar
fi
java -jar "$WRAPPER_JAR" "$@"
