#!/usr/bin/env bash
set -u
sleep "${1:-50}"
echo "execute in minecraft:the_end run forceload add -64 -64 64 64"
sleep 8
# dragon island control: quarter column
echo "execute in minecraft:the_end run fill 0 0 0 15 127 15 minecraft:air replace minecraft:end_stone"
# wastes chunk 62,17 top half
echo "execute in minecraft:the_end run fill 992 0 272 1007 127 287 minecraft:air replace minecraft:end_stone"
echo "execute in minecraft:the_end run fill 992 0 272 1007 127 287 minecraft:air replace minecraft:chorus_plant"
echo "execute in minecraft:the_end run fill 992 0 272 1007 127 287 minecraft:air replace endesium:end_gray"
echo "execute in minecraft:the_end run fill 992 0 272 1007 127 287 minecraft:air replace endesium:resonant_mechanism"
# wilds chunk -48,-43
echo "execute in minecraft:the_end run fill -768 0 -688 -753 127 -673 minecraft:air replace minecraft:chorus_plant"
echo "execute in minecraft:the_end run fill -768 0 -688 -753 127 -673 minecraft:air replace endesium:chorus_sprout"
echo "execute in minecraft:the_end run fill -768 0 -688 -753 127 -673 minecraft:air replace endesium:wild_tendril"
# city chunk 43,-54
echo "execute in minecraft:the_end run fill 688 0 -864 703 127 -849 minecraft:air replace minecraft:end_stone"
# probes
echo "execute in minecraft:the_end run execute if block 992 60 272 minecraft:end_stone run say PROBE_60"
echo "execute in minecraft:the_end run execute if block 992 70 272 minecraft:end_stone run say PROBE_70"
echo "execute in minecraft:the_end run execute if block 992 80 272 minecraft:end_stone run say PROBE_80"
echo "execute in minecraft:the_end run execute if block 992 100 272 minecraft:end_stone run say PROBE_100"
sleep 3
echo "stop"
sleep 5
