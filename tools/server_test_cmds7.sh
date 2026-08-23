#!/usr/bin/env bash
set -u
sleep "${1:-50}"
echo "execute in minecraft:the_end run forceload add 992 272"
echo "execute in minecraft:the_end run forceload add 1040 272"
echo "execute in minecraft:the_end run forceload add 960 240"
echo "execute in minecraft:the_end run forceload add 1040 320"
echo "execute in minecraft:the_end run forceload add -768 -688"
echo "execute in minecraft:the_end run forceload add -800 -720"
echo "execute in minecraft:the_end run forceload add -736 -656"
echo "execute in minecraft:the_end run forceload add -800 -656"
echo "execute in minecraft:the_end run forceload add 688 -864"
echo "execute in minecraft:the_end run forceload add 720 -864"
echo "execute in minecraft:the_end run forceload add 656 -832"
sleep 12
echo "execute in minecraft:the_end run fill 880 40 160 1136 120 400 minecraft:air replace endesium:end_gray"
echo "execute in minecraft:the_end run fill 880 40 160 1136 120 400 minecraft:air replace endesium:resonant_slate"
echo "execute in minecraft:the_end run fill 880 40 160 1136 120 400 minecraft:air replace endesium:dormant_resonant_crystal"
echo "execute in minecraft:the_end run fill 880 40 160 1136 120 400 minecraft:air replace endesium:resonant_mechanism"
echo "execute in minecraft:the_end run fill 880 40 160 1136 120 400 minecraft:air replace endesium:chorus_sprout"
echo "execute in minecraft:the_end run fill 880 40 160 1136 120 400 minecraft:air replace endesium:wild_tendril"
echo "execute in minecraft:the_end run fill 880 40 160 1136 120 400 minecraft:air replace minecraft:chorus_plant"
echo "execute in minecraft:the_end run fill 880 40 160 1136 120 400 minecraft:air replace minecraft:barrel"
echo "execute in minecraft:the_end run fill -896 40 -800 -640 120 -560 minecraft:air replace endesium:chorus_sprout"
echo "execute in minecraft:the_end run fill -896 40 -800 -640 120 -560 minecraft:air replace endesium:wild_tendril"
echo "execute in minecraft:the_end run fill -896 40 -800 -640 120 -560 minecraft:air replace minecraft:chorus_plant"
sleep 3
echo "stop"
sleep 5
