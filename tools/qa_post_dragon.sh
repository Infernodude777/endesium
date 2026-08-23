#!/usr/bin/env bash
# Post-Dragon state QA: phase 1 activates the transformation on a fresh world,
# phase 2 restarts the SAME world and verifies the state persisted.
# Usage: tools/qa_post_dragon.sh <tag>
set -u
cd /c/Users/Nikhil/Desktop/endesium
export JAVA_HOME="/c/Users/Nikhil/.jdks/temurin-21.0.12"
TAG="${1:-pd}"
LOG1="/tmp/endesium_${TAG}_phase1.log"
LOG2="/tmp/endesium_${TAG}_phase2.log"

# Phase 1: fresh world, activate the transformation, stop.
rm -rf run/world run/world_nether run/world_the_end
( sleep 10
  echo "execute in minecraft:the_end run locate biome endesium:end_wastes"
  sleep 6
  echo "endesium dragonstate get"
  sleep 3
  echo "endesium dragonstate set true"
  sleep 3
  echo "endesium dragonstate get"
  sleep 3
  echo "stop" ) | ./gradlew runServer --console=plain > "$LOG1" 2>&1

# Phase 2: same world, verify persistence.
( sleep 10
  echo "endesium dragonstate get"
  sleep 3
  echo "endesium dragonstate set true"
  sleep 3
  echo "endesium dragonstate get"
  sleep 3
  echo "stop" ) | ./gradlew runServer --console=plain > "$LOG2" 2>&1

echo "=== phase1: $LOG1 ==="
grep -nE "Endesium post-Dragon|The End answers|A deep resonance|dragonstate|The nearest" "$LOG1" | head -20
echo "=== phase2: $LOG2 ==="
grep -nE "Endesium post-Dragon|dragonstate|already active|activated" "$LOG2" | head -20
