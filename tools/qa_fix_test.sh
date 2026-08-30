#!/usr/bin/env bash
# QA: companion dragon + first-kill hoard.
set -u
LOG="${1:-/tmp/endesium_fix.log}"
sleep "${2:-40}"

echo "=== QA: force-load the End island ==="
echo "execute in minecraft:the_end run forceload add -3 -3 3 3"
sleep 4

echo "=== QA: companion dragon spawn (persistent) ==="
echo "execute in minecraft:the_end run summon endesium:companion_dragon 0 71 0 {PersistenceRequired:1b}"
sleep 3
echo "execute in minecraft:the_end run data get entity @e[type=endesium:companion_dragon,limit=1,sort=nearest] Pos"
sleep 5
echo "execute in minecraft:the_end run data get entity @e[type=endesium:companion_dragon,limit=1,sort=nearest] Pos"
sleep 3

echo "=== QA: boss dragon (frozen at spawn), killed -> hoard should spawn ==="
echo "execute in minecraft:the_end run summon ender_dragon 0 71 0 {PersistenceRequired:1b,NoAI:1b}"
sleep 4
echo "dragonfight"
sleep 2
echo "execute in minecraft:the_end run kill @e[type=ender_dragon,limit=1]"
sleep 5
echo "dragonfight"
sleep 3

echo "=== QA: locate hoard beacon clouds ==="
echo "execute in minecraft:the_end run data get entity @e[type=area_effect_cloud,sort=nearest,limit=3] Pos"
sleep 2

echo "=== QA: scan chest column at 0,0 ==="
for y in $(seq 55 80); do
  echo "execute in minecraft:the_end run data get block 0 $y 0 Items"
  sleep 0.4
done

echo "=== QA: stop ==="
echo "stop"
sleep 5
