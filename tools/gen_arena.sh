#!/usr/bin/env bash
# Generates a fresh central End island and renders it top-down so the arena
# silhouette can be inspected for radial symmetry / repetition.
# Usage: tools/gen_arena.sh <seed-tag>
set -u
cd /c/Users/Nikhil/Desktop/endesium
export JAVA_HOME="/c/Users/Nikhil/.jdks/temurin-21.0.12"
TAG="${1:-a}"
SEED="${2:-123456789}"
LOG="/tmp/endesium_arena_${TAG}.log"

rm -rf run/world run/world_nether run/world_the_end
sed -i "s/^level-seed=.*/level-seed=${SEED}/" run/server.properties

# /forceload takes BLOCK coordinates and caps at 256 chunks per call, so split
# the central 24x24-chunk island into three z-bands.
( sleep 20
  echo "execute in minecraft:the_end run forceload add -192 -192 192 -64"
  sleep 30
  echo "execute in minecraft:the_end run forceload add -192 -64 192 64"
  sleep 30
  echo "execute in minecraft:the_end run forceload add -192 64 192 192"
  sleep 30
  echo "stop" ) | ./gradlew runServer --console=plain > "$LOG" 2>&1

echo "=== errors ==="
grep -nE "far chunk|Exception|ERROR|Mixin transform|Failed to" "$LOG" | head -20 || true
echo "=== forceload confirmations ==="
grep -nE "Marked .* chunks|forceload" "$LOG" | head -8 || true
echo "=== render ==="
node tools/render_island.mjs run/world 3
