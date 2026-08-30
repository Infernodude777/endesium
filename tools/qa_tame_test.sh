#!/usr/bin/env bash
# QA: companion spawns pre-tamed/adult without errors; sanity check.
set -u
LOG="${1:-/tmp/endesium_tame.log}"
sleep "${2:-40}"

echo "=== QA: force-load the End island ==="
echo "execute in minecraft:the_end run forceload add -3 -3 3 3"
sleep 4

echo "=== QA: companion dragon spawn ==="
echo "execute in minecraft:the_end run summon endesium:companion_dragon 0 71 0 {PersistenceRequired:1b}"
sleep 3
echo "execute in minecraft:the_end run data get entity @e[type=endesium:companion_dragon,limit=1,sort=nearest] Pos"
sleep 2
echo "execute in minecraft:the_end run data get entity @e[type=endesium:companion_dragon,limit=1,sort=nearest] Motion"
sleep 2
echo "execute in minecraft:the_end run data get entity @e[type=endesium:companion_dragon,limit=1,sort=nearest] Id"
sleep 2

echo "=== QA: stop ==="
echo "stop"
sleep 5