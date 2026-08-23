#!/usr/bin/env bash
# Comprehensive QA dedicated-server test for Endesium.
# Usage: tools/qa_server_test.sh <logfile>
set -u
LOG="${1:-/tmp/endesium_qa.log}"
sleep "${2:-55}"
echo "=== QA: locate biomes ==="
echo "execute in minecraft:the_end run locate biome endesium:end_wastes"
sleep 6
echo "execute in minecraft:the_end run locate biome endesium:chorus_wilds"
sleep 6
# Endesium intentionally replaces vanilla highlands/midlands with its ten
# regional biomes. Barrens remain vanilla and are the correct compatibility
# control for this geography test.
echo "execute in minecraft:the_end run locate biome minecraft:end_barrens"
sleep 4
echo "execute in minecraft:the_end run locate structure endesium:end_ruin"
sleep 6
echo "=== QA: force-load wastes grid (chunks 34..89 x -14..41) ==="
CX0=34; CZ0=-14
for cy in 0 1 2 3; do
  for cx in 0 1 2 3; do
    x0=$(( (CX0 + cx * 14) * 16 ))
    z0=$(( (CZ0 + cy * 14) * 16 ))
    x1=$(( (CX0 + cx * 14 + 13) * 16 + 15 ))
    z1=$(( (CZ0 + cy * 14 + 13) * 16 + 15 ))
    echo "execute in minecraft:the_end run forceload add $x0 $z0 $x1 $z1"
    sleep 1
  done
done
sleep 20
echo "=== QA: force-load wilds grid (chunks -55..0 x -55..0) ==="
for cy in 0 1 2 3; do
  for cx in 0 1 2 3; do
    x0=$(( (-55 + cx * 14) * 16 ))
    z0=$(( (-55 + cy * 14) * 16 ))
    x1=$(( (-55 + cx * 14 + 13) * 16 + 15 ))
    z1=$(( (-55 + cy * 14 + 13) * 16 + 15 ))
    echo "execute in minecraft:the_end run forceload add $x0 $z0 $x1 $z1"
    sleep 1
  done
done
sleep 20
echo "=== QA: dragon island control ==="
echo "execute in minecraft:the_end run forceload add -32 -32 32 32"
sleep 6
echo "=== QA: stop ==="
echo "stop"
sleep 5
