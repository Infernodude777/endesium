#!/usr/bin/env bash
# Ecology milestone verification: boot a fresh world, locate all seven
# Endesium biomes, and force-generate a broad outer-End band so biome terrain,
# vegetation, and structures all run. Usage: tools/server_test_ecology.sh <seed>
set -u
cd /c/Users/Nikhil/Desktop/endesium
export JAVA_HOME="/c/Users/Nikhil/.jdks/temurin-21.0.12"
SEED="${1:-123456789}"
LOG="/tmp/endesium_eco_${SEED}.log"

rm -rf run/world run/world_nether run/world_the_end
sed -i "s/^level-seed=.*/level-seed=${SEED}/" run/server.properties

(
  sleep 25
  echo "execute in minecraft:the_end run locate biome endesium:end_wastes"
  sleep 6
  echo "execute in minecraft:the_end run locate biome endesium:chorus_wilds"
  sleep 6
  echo "execute in minecraft:the_end run locate biome endesium:shattered_highlands"
  sleep 6
  echo "execute in minecraft:the_end run locate biome endesium:void_marshes"
  sleep 6
  echo "execute in minecraft:the_end run locate biome endesium:luminous_groves"
  sleep 6
  echo "execute in minecraft:the_end run locate biome endesium:ashen_expanse"
  sleep 6
  echo "execute in minecraft:the_end run locate biome endesium:crystal_barrens"
  sleep 6
  # Force-generate broad outer-End bands so terrain/vegetation/structure
  # features all execute (block coords; each call stays under the chunk cap).
  echo "execute in minecraft:the_end run forceload add 1400 1400 1600 1600"
  sleep 18
  echo "execute in minecraft:the_end run forceload add 1400 -1600 1600 -1400"
  sleep 18
  echo "execute in minecraft:the_end run forceload add -1600 1400 -1400 1600"
  sleep 18
  echo "execute in minecraft:the_end run forceload add -1600 -1600 -1400 -1400"
  sleep 20
  echo "stop"
) | ./gradlew runServer --console=plain > "$LOG" 2>&1

echo "=== locate results ==="
grep -nE "The nearest|endesium:" "$LOG" | head -20 || true
echo "=== errors ==="
grep -nE "far chunk|Exception|ERROR|Mixin transform|Failed to|missing sound|Unable to" "$LOG" | head -30 || true
echo "=== biome registry ==="
grep -nE "Registered Endesium biome|biome holders could not" "$LOG" | head -5 || true
