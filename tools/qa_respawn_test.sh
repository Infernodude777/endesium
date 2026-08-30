#!/usr/bin/env bash
# QA: true respawned (transformed) dragon. Activate the post-Dragon state,
# summon the respawned boss, then confirm it has ~400 HP (not half), responds
# to its scale, and is actually flying (moving).
set -u
LOG="${1:-/tmp/endesium_respawn.log}"
sleep "${2:-40}"

echo "=== QA: force-load the End island ==="
echo "execute in minecraft:the_end run forceload add -3 -3 3 3"
sleep 4

echo "=== QA: activate post-Dragon transformation ==="
echo "endesium dragonstate set true"
sleep 3

echo "=== QA: spawn the respawned boss dragon ==="
echo "execute in minecraft:the_end run summon ender_dragon 0 71 0 {PersistenceRequired:1b}"
sleep 4
echo "execute in minecraft:the_end run data get entity @e[type=ender_dragon,limit=1,sort=nearest] Health"
sleep 2
echo "dragonfight"
sleep 2

echo "=== QA: read position twice to detect motion ==="
echo "execute in minecraft:the_end run data get entity @e[type=ender_dragon,limit=1,sort=nearest] Pos"
sleep 6
echo "execute in minecraft:the_end run data get entity @e[type=ender_dragon,limit=1,sort=nearest] Pos"
sleep 2
echo "dragonfight"
sleep 2

echo "=== QA: stop ==="
echo "stop"
sleep 5