#!/usr/bin/env bash
# QA: egg altar cleanup (egg removed -> bar cleared) + full summon still works.
set -u
sleep "${1:-18}"

echo "=== QA: force-load End island (fountain chunk) ==="
echo "execute in minecraft:the_end run forceload add -1 -1 1 1"
sleep 4

echo "execute in minecraft:the_end run forceload -1 -1 1 1"
sleep 3

echo "=== QA: place egg, let altar register ==="
echo "execute in minecraft:the_end run setblock 0 69 0 minecraft:dragon_egg"
sleep 6

echo "=== QA: remove egg (simulates relocated/teleported egg) -> bar must clear ==="
echo "execute in minecraft:the_end run setblock 0 69 0 minecraft:air"
sleep 6
echo "execute in minecraft:the_end run setblock 1 69 1 minecraft:air"
sleep 2

echo "=== QA: re-place egg and wait for full 60s summon ==="
echo "execute in minecraft:the_end run setblock 0 69 0 minecraft:dragon_egg"
sleep 4
echo "execute in minecraft:the_end run setblock -3 69 0 minecraft:piston[facing=east]"
sleep 2
echo "execute in minecraft:the_end run setblock -4 69 0 minecraft:redstone_block"
sleep 3
sleep 62

echo "=== QA: is the golem present? ==="
echo "execute in minecraft:the_end run data get entity @e[type=endesium:end_golem,limit=1,sort=nearest] Health"
sleep 2
echo "execute in minecraft:the_end run data get entity @e[type=endesium:end_golem,limit=1,sort=nearest] Pos"
sleep 2

echo "=== QA: stop ==="
echo "stop"
sleep 5