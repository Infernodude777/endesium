#!/usr/bin/env bash
# QA: companion spawns at 30 hearts (60 HP) and takes damage.
set -u
LOG="${1:-/tmp/endesium_health.log}"
sleep "${2:-40}"

echo "=== QA: force-load the End island ==="
echo "execute in minecraft:the_end run forceload add -3 -3 3 3"
sleep 4

echo "=== QA: companion spawn ==="
echo "execute in minecraft:the_end run summon endesium:companion_dragon 0 71 0 {PersistenceRequired:1b}"
sleep 4
echo "execute in minecraft:the_end run data get entity @e[type=endesium:companion_dragon,limit=1,sort=nearest] Health"
sleep 2

echo "=== QA: damage companion 10, then read health again ==="
echo "execute in minecraft:the_end run damage @e[type=endesium:companion_dragon,limit=1] 10 minecraft:mob_attack"
sleep 3
echo "execute in minecraft:the_end run data get entity @e[type=endesium:companion_dragon,limit=1,sort=nearest] Health"
sleep 2

echo "=== QA: stop ==="
echo "stop"
sleep 5