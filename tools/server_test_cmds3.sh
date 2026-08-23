#!/usr/bin/env bash
set -u
sleep "${1:-50}"
# Force-generate chunks around the located Endesium biomes and the End City
echo "execute in minecraft:the_end run summon minecraft:armor_stand 992 68 272"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 1000 68 272"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 992 68 280"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 1008 68 288"
echo "execute in minecraft:the_end run summon minecraft:armor_stand -768 68 -688"
echo "execute in minecraft:the_end run summon minecraft:armor_stand -776 68 -696"
echo "execute in minecraft:the_end run summon minecraft:armor_stand -760 68 -680"
echo "execute in minecraft:the_end run summon minecraft:armor_stand 688 68 -864"
sleep 6
echo "execute in minecraft:the_end run locate structure minecraft:end_gateway"
sleep 3
echo "execute in minecraft:the_end run locate structure minecraft:end_city"
sleep 2
echo "stop"
sleep 5
