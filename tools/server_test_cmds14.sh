#!/usr/bin/env bash
set -u
sleep "${1:-50}"
echo "execute in minecraft:the_end run forceload add -64 -64 64 64"
echo "execute in minecraft:the_end run forceload add 960 240 1088 368"
echo "execute in minecraft:the_end run forceload add -800 -720 -688 -608"
echo "execute in minecraft:the_end run forceload add 640 -896 800 -800"
sleep 20
# control: dragon island column
echo "execute in minecraft:the_end run fill -16 0 -16 16 255 16 minecraft:air replace minecraft:end_stone"
# wastes chunk 62,17 (992-1007, 272-287)
echo "execute in minecraft:the_end run fill 992 0 272 1007 255 287 minecraft:air replace minecraft:end_stone"
echo "execute in minecraft:the_end run fill 992 0 272 1007 255 287 minecraft:air replace minecraft:chorus_plant"
echo "execute in minecraft:the_end run fill 992 0 272 1007 255 287 minecraft:air replace endesium:end_gray"
echo "execute in minecraft:the_end run fill 992 0 272 1007 255 287 minecraft:air replace endesium:resonant_mechanism"
# wilds chunk -48,-43 (-768..-753, -688..-673)
echo "execute in minecraft:the_end run fill -768 0 -688 -753 255 -673 minecraft:air replace minecraft:chorus_plant"
echo "execute in minecraft:the_end run fill -768 0 -688 -753 255 -673 minecraft:air replace endesium:chorus_sprout"
echo "execute in minecraft:the_end run fill -768 0 -688 -753 255 -673 minecraft:air replace endesium:wild_tendril"
# city chunk 43,-54 (688-703, -864..-849)
echo "execute in minecraft:the_end run fill 688 0 -864 703 255 -849 minecraft:air replace minecraft:end_stone"
# targeted probes at the wastes column
echo "execute in minecraft:the_end run execute if block 992 60 272 minecraft:end_stone run say PROBE_992_60_272_ENDSTONE"
echo "execute in minecraft:the_end run execute if block 992 70 272 minecraft:end_stone run say PROBE_992_70_272_ENDSTONE"
echo "execute in minecraft:the_end run execute if block 992 80 272 minecraft:end_stone run say PROBE_992_80_272_ENDSTONE"
echo "execute in minecraft:the_end run execute if block 992 100 272 minecraft:end_stone run say PROBE_992_100_272_ENDSTONE"
sleep 3
echo "stop"
sleep 5
