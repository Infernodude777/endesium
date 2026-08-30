#!/usr/bin/env bash
# QA: dragon egg altar -> End Golem summon + egg immobility + golem portal-refusal.
set -u
LOG="${1:-/tmp/endesium_golem_egg.log}"
sleep "${2:-40}"

echo "=== QA: force-load End island ==="
echo "execute in minecraft:the_end run forceload add -4 -4 4 4"
sleep 4

echo "=== QA: place a dragon egg on the fountain (x=z=0) ==="
echo "execute in minecraft:the_end run setblock 0 69 0 minecraft:dragon_egg"
sleep 3

echo "=== QA: piston attempts to push the egg -> egg should stay ==="
echo "execute in minecraft:the_end run setblock -3 69 0 minecraft:piston[facing=east]"
sleep 2
echo "execute in minecraft:the_end run setblock -4 69 0 minecraft:redstone_block"
sleep 3
echo "execute in minecraft:the_end run data get block 0 69 0"
sleep 2
echo "dragonfight"
sleep 2

echo "=== QA: wait ~15s, confirm egg still present (not fallen/teleported) ==="
sleep 15
echo "execute in minecraft:the_end run data get block 0 69 0"
sleep 2

echo "=== QA: countdown to golem summon (~keep alive 70s) ==="
echo "dragonfight"
sleep 70

echo "=== QA: is the golem present? ==="
echo "execute in minecraft:the_end run data get entity @e[type=endesium:end_golem,limit=1,sort=nearest] Health"
sleep 2
echo "execute in minecraft:the_end run data get entity @e[type=endesium:end_golem,limit=1,sort=nearest] Pos"
sleep 2

echo "=== QA: stop ==="
echo "stop"
sleep 5