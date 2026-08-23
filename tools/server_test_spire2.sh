#!/usr/bin/env bash
set -u
sleep "${1:-50}"
# Force-load a 56x56 chunk grid centered on the wastes biome at (992, 272).
# Block coords for forceload; the cap is 256 chunks per area, so split into
# 16 areas of 14x14 chunks (196 chunks each).
CX0=34; CZ0=-14; CX1=89; CZ1=41
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
sleep 30
echo "stop"
sleep 5
