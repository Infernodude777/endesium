#!/usr/bin/env bash
# QA: riding mixin loads cleanly + non-End-Golem bosses nerfed ~25%.
set -u
LOG="${1:-/tmp/endesium_rideboss.log}"
sleep "${2:-40}"

echo "=== QA: force-load the End island ==="
echo "execute in minecraft:the_end run forceload add -3 -3 3 3"
sleep 4

echo "=== QA: companion spawn (should be adult/tamed) ==="
echo "execute in minecraft:the_end run summon endesium:companion_dragon 0 71 0 {PersistenceRequired:1b}"
sleep 4
echo "execute in minecraft:the_end run data get entity @e[type=endesium:companion_dragon,limit=1,sort=nearest] Health"
sleep 2

echo "=== QA: Crown Sentinel effective stats (difficulty pass) ==="
echo "execute in minecraft:the_end run summon endesium:crown_sentinel 5 71 0"
sleep 4
echo "execute in minecraft:the_end run data get entity @e[type=endesium:crown_sentinel,limit=1,sort=nearest] Health"
sleep 1
echo "execute in minecraft:the_end run data get entity @e[type=endesium:crown_sentinel,limit=1,sort=nearest] Attributes"
sleep 2

echo "=== QA: End Warden effective stats (difficulty pass) ==="
echo "execute in minecraft:the_end run summon endesium:end_warden 10 71 0"
sleep 4
echo "execute in minecraft:the_end run data get entity @e[type=endesium:end_warden,limit=1,sort=nearest] Health"
sleep 1
echo "execute in minecraft:the_end run data get entity @e[type=endesium:end_warden,limit=1,sort=nearest] Attributes"
sleep 2

echo "=== QA: stop ==="
echo "stop"
sleep 5