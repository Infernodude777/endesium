#!/usr/bin/env bash
set -u
sleep "${1:-50}"
echo "execute in minecraft:the_end run forceload add 640 -896 800 -800"
echo "execute in minecraft:the_end run forceload add 960 240 1088 368"
echo "execute in minecraft:the_end run forceload add -800 -720 -688 -608"
sleep 20
# city/highlands area
for cx in $(seq 40 49); do
  for cz in $(seq -56 -50); do
    x=$(( cx * 16 )); z=$(( cz * 16 ))
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace minecraft:end_stone"
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace minecraft:chorus_plant"
  done
done
# wastes chunk 62,17 and neighbors
for ch in "62 17" "63 17" "62 18" "63 18"; do
  set -- $ch
  x=$(( $1 * 16 )); z=$(( $2 * 16 ))
  echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace minecraft:end_stone"
  echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace minecraft:chorus_plant"
done
sleep 3
echo "stop"
sleep 5
