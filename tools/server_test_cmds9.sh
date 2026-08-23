#!/usr/bin/env bash
set -u
sleep "${1:-50}"
# wastes rectangle around (992,272): blocks 800-1200 / 160-432
echo "execute in minecraft:the_end run forceload add 800 160 1200 432"
# wilds rectangle around (-768,-688): blocks -912..-560 / -784..-560
echo "execute in minecraft:the_end run forceload add -912 -784 -560 -560"
# city area
echo "execute in minecraft:the_end run forceload add 600 -900 800 -800"
sleep 20
echo "execute in minecraft:the_end run fill 800 30 160 1200 160 432 minecraft:air replace endesium:end_gray"
echo "execute in minecraft:the_end run fill 800 30 160 1200 160 432 minecraft:air replace endesium:resonant_slate"
echo "execute in minecraft:the_end run fill 800 30 160 1200 160 432 minecraft:air replace endesium:dormant_resonant_crystal"
echo "execute in minecraft:the_end run fill 800 30 160 1200 160 432 minecraft:air replace endesium:resonant_mechanism"
echo "execute in minecraft:the_end run fill 800 30 160 1200 160 432 minecraft:air replace endesium:chorus_sprout"
echo "execute in minecraft:the_end run fill 800 30 160 1200 160 432 minecraft:air replace endesium:wild_tendril"
echo "execute in minecraft:the_end run fill 800 30 160 1200 160 432 minecraft:air replace minecraft:chorus_plant"
echo "execute in minecraft:the_end run fill 800 30 160 1200 160 432 minecraft:air replace minecraft:barrel"
echo "execute in minecraft:the_end run fill -912 30 -784 -560 160 -560 minecraft:air replace endesium:chorus_sprout"
echo "execute in minecraft:the_end run fill -912 30 -784 -560 160 -560 minecraft:air replace endesium:wild_tendril"
echo "execute in minecraft:the_end run fill -912 30 -784 -560 160 -560 minecraft:air replace minecraft:chorus_plant"
echo "execute in minecraft:the_end run fill -912 30 -784 -560 160 -560 minecraft:air replace endesium:end_gray"
echo "execute in minecraft:the_end run fill -912 30 -784 -560 160 -560 minecraft:air replace endesium:resonant_slate"
echo "execute in minecraft:the_end run fill 600 30 -900 800 160 -800 minecraft:air replace minecraft:chorus_plant"
sleep 3
echo "stop"
sleep 5
