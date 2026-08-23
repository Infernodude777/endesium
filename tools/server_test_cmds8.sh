#!/usr/bin/env bash
set -u
sleep "${1:-50}"
# wastes area
echo "execute in minecraft:the_end run forceload add 992 272"
echo "execute in minecraft:the_end run forceload add 1040 272"
echo "execute in minecraft:the_end run forceload add 960 240"
echo "execute in minecraft:the_end run forceload add 1040 320"
# wilds area
echo "execute in minecraft:the_end run forceload add -768 -688"
echo "execute in minecraft:the_end run forceload add -800 -720"
echo "execute in minecraft:the_end run forceload add -736 -656"
echo "execute in minecraft:the_end run forceload add -800 -656"
# city area
echo "execute in minecraft:the_end run forceload add 688 -864"
echo "execute in minecraft:the_end run forceload add 720 -864"
echo "execute in minecraft:the_end run forceload add 656 -832"
sleep 12
for ch in "992 272" "1040 272" "960 240" "1040 320"; do
  set -- $ch
  echo "execute in minecraft:the_end run fill $1 30 $2 $(( $1 + 15 )) 140 $(( $2 + 15 )) minecraft:air replace endesium:end_gray"
  echo "execute in minecraft:the_end run fill $1 30 $2 $(( $1 + 15 )) 140 $(( $2 + 15 )) minecraft:air replace endesium:resonant_slate"
  echo "execute in minecraft:the_end run fill $1 30 $2 $(( $1 + 15 )) 140 $(( $2 + 15 )) minecraft:air replace endesium:dormant_resonant_crystal"
  echo "execute in minecraft:the_end run fill $1 30 $2 $(( $1 + 15 )) 140 $(( $2 + 15 )) minecraft:air replace endesium:resonant_mechanism"
  echo "execute in minecraft:the_end run fill $1 30 $2 $(( $1 + 15 )) 140 $(( $2 + 15 )) minecraft:air replace minecraft:barrel"
done
for ch in "-768 -688" "-800 -720" "-736 -656" "-800 -656"; do
  set -- $ch
  echo "execute in minecraft:the_end run fill $1 30 $2 $(( $1 + 15 )) 140 $(( $2 + 15 )) minecraft:air replace endesium:chorus_sprout"
  echo "execute in minecraft:the_end run fill $1 30 $2 $(( $1 + 15 )) 140 $(( $2 + 15 )) minecraft:air replace endesium:wild_tendril"
  echo "execute in minecraft:the_end run fill $1 30 $2 $(( $1 + 15 )) 140 $(( $2 + 15 )) minecraft:air replace minecraft:chorus_plant"
done
for ch in "688 -864" "720 -864" "656 -832"; do
  set -- $ch
  echo "execute in minecraft:the_end run fill $1 30 $2 $(( $1 + 15 )) 140 $(( $2 + 15 )) minecraft:air replace minecraft:chorus_plant"
done
sleep 3
echo "stop"
sleep 5
