#!/usr/bin/env bash
set -u
sleep "${1:-50}"
echo "execute in minecraft:the_end run locate biome endesium:end_wastes"
sleep 8
echo "execute in minecraft:the_end run locate biome endesium:chorus_wilds"
sleep 8
echo "execute in minecraft:the_end run locate biome minecraft:end_highlands"
sleep 5
# force-load the previously located wastes column (992,272) and wilds column (-768,-688)
echo "execute in minecraft:the_end run forceload add 976 256 1008 288"
sleep 2
echo "execute in minecraft:the_end run forceload add -784 -704 -752 -672"
sleep 2
# dragon island control
echo "execute in minecraft:the_end run forceload add -32 -32 32 32"
sleep 10
echo "stop"
sleep 5
