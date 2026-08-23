#!/usr/bin/env bash
set -u
sleep "${1:-50}"
echo "execute in minecraft:the_end run forceload add -64 -64 64 64"
echo "execute in minecraft:the_end run forceload add 960 240 1088 368"
sleep 15
echo "execute in minecraft:the_end run fill -16 0 -16 16 255 16 minecraft:air replace minecraft:end_stone"
echo "execute in minecraft:the_end run fill 992 0 272 1007 255 287 minecraft:air replace minecraft:end_stone"
echo "execute in minecraft:the_end run fill 992 0 272 1007 255 287 minecraft:air replace minecraft:chorus_plant"
echo "execute in minecraft:the_end run fill 992 0 272 1007 255 287 minecraft:air replace minecraft:end_gateway"
echo "execute in minecraft:the_end run fill 992 0 272 1007 255 287 minecraft:air replace endesium:end_gray"
echo "execute in minecraft:the_end run data get block 992 70 272"
sleep 3
echo "stop"
sleep 5
