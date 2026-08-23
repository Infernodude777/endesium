#!/usr/bin/env bash
set -u
sleep "${1:-50}"
# force-load two 8x8 chunk grids (wastes around 992/272, wilds around -768/-688)
echo "execute in minecraft:the_end run forceload add 960 240 1088 368"
echo "execute in minecraft:the_end run forceload add -800 -720 -688 -608"
sleep 20

# wastes grid: chunks 60-67 x 15-22 -> blocks 960..1088 / 240..368
for cx in $(seq 60 67); do
  for cz in $(seq 15 22); do
    x=$(( cx * 16 )); z=$(( cz * 16 ))
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace endesium:end_gray"
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace endesium:resonant_slate"
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace endesium:dormant_resonant_crystal"
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace endesium:resonant_mechanism"
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace minecraft:barrel"
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace minecraft:chorus_plant"
  done
done

# wilds grid: chunks -50..-43 x -45..-38 -> blocks -800..-688 / -720..-608
for cx in $(seq -50 -43); do
  for cz in $(seq -45 -38); do
    x=$(( cx * 16 )); z=$(( cz * 16 ))
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace endesium:chorus_sprout"
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace endesium:wild_tendril"
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace minecraft:chorus_plant"
    echo "execute in minecraft:the_end run fill $x 40 $z $(( x + 15 )) 140 $(( z + 15 )) minecraft:air replace endesium:end_gray"
  done
done
sleep 3
echo "stop"
sleep 5
