#!/usr/bin/env bash
set -u
sleep "${1:-50}"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 992 68 272"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 1040 68 272"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 960 68 240"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 1040 68 320"
echo "execute in minecraft:the_end run summon minecraft:armor_stand -768 68 -688"
echo "execute in minecraft:the_end run summon minecraft:armor_stand -800 68 -720"
echo "execute in minecraft:the_end run summon minecraft:armor_stand -736 68 -656"
echo "execute in minecraft:the_end run summon minecraft:armor_stand -800 68 -656"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 688 68 -864"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 720 68 -864"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 656 68 -832"
sleep 8
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
