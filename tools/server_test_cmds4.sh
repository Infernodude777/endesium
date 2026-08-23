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
sleep 6
echo "execute in minecraft:the_end run execute if block 992 68 272 endesium:resonant_mechanism run say RUIN_MECHANISM_FOUND"
echo "execute in minecraft:the_end run execute if block 992 68 272 endesium:end_gray run say END_GRAY_FOUND"
echo "execute in minecraft:the_end run execute if block 992 68 272 endesium:chorus_sprout run say SPROUT_FOUND"
echo "execute in minecraft:the_end run execute if block 992 68 272 endesium:wild_tendril run say TENDRIL_FOUND"
echo "execute in minecraft:the_end run execute if block -768 68 -688 endesium:chorus_sprout run say SPROUT_FOUND"
echo "execute in minecraft:the_end run execute if block -768 68 -688 endesium:wild_tendril run say TENDRIL_FOUND"
echo "execute in minecraft:the_end run execute if block -768 68 -688 endesium:resonant_mechanism run say RUIN_IN_WILDS"
sleep 3
echo "stop"
sleep 5
