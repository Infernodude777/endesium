#!/usr/bin/env bash
# QA run wrapper: fresh world -> start server -> feed commands via console stdin.
# Usage: tools/qa_run.sh <tag>
set -u
cd /c/Users/Nikhil/Desktop/endesium
export JAVA_HOME="/c/Users/Nikhil/.jdks/temurin-21.0.12"
TAG="${1:-qa}"
LOG="/tmp/endesium_${TAG}.log"

# Fresh world
rm -rf run/world run/world_nether run/world_the_end

# Pipe the test script's echo'd commands into gradle runServer's stdin, which
# the server console reads as commands. Log to file.
( sleep 8; ./tools/qa_server_test.sh "$LOG" 55 ) | ./gradlew runServer --console=plain > "$LOG" 2>&1

echo "=== done: $LOG ==="
