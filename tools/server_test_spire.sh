#!/usr/bin/env bash
set -u
sleep "${1:-55}"
# wastes grid around (992,272): chunks 56..67 x 11..22
echo "execute in minecraft:the_end run forceload add 896 176 1088 368"
sleep 2
# wilds grid around (-768,-688): chunks -54..-43 x -49..-38
echo "execute in minecraft:the_end run forceload add -864 -784 -688 -608"
sleep 2
# dragon island control
echo "execute in minecraft:the_end run forceload add -32 -32 32 32"
sleep 25
echo "stop"
sleep 5
