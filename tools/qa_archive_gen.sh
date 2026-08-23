#!/usr/bin/env bash
# Headless Resonant Archive generation QA.
# 1) fresh world -> locate both biomes (capture coordinates)
# 2) same world -> forceload a 15x15 chunk grid around each biome -> stop
# 3) scan DIM1 decompressed chunks for the raw ARCHIVE variant NBT string
# Usage: tools/qa_archive_gen.sh <tag>
set -u
cd /c/Users/Nikhil/Desktop/endesium
export JAVA_HOME="/c/Users/Nikhil/.jdks/temurin-21.0.12"
TAG="${1:-arc}"
LOC="/tmp/endesium_${TAG}_locate.log"
GEN="/tmp/endesium_${TAG}_gen.log"

rm -rf run/world run/world_nether run/world_the_end

# Phase 1: locate both biomes.
( sleep 18
  echo "execute in minecraft:the_end run locate biome endesium:end_wastes"
  sleep 6
  echo "execute in minecraft:the_end run locate biome endesium:chorus_wilds"
  sleep 6
  echo "stop" ) | ./gradlew runServer --console=plain > "$LOC" 2>&1

wastes="$(grep -oE 'endesium:end_wastes is at \[-?[0-9]+, [0-9]+, -?[0-9]+\]' "$LOC" | head -1)"
wilds="$(grep -oE 'endesium:chorus_wilds is at \[-?[0-9]+, [0-9]+, -?[0-9]+\]' "$LOC" | head -1)"
echo "located: $wastes"
echo "located: $wilds"

wx=$(echo "$wastes" | grep -oE '\-?[0-9]+' | head -1); wz=$(echo "$wastes" | grep -oE '\-?[0-9]+' | tail -1)
gx=$(echo "$wilds"  | grep -oE '\-?[0-9]+' | head -1); gz=$(echo "$wilds"  | grep -oE '\-?[0-9]+' | tail -1)
wcx=$(( wx / 16 )); wcz=$(( wz / 16 ))
gcx=$(( gx / 16 )); gcz=$(( gz / 16 ))
echo "wastes chunk ($wcx,$wcz), wilds chunk ($gcx,$gcz)"

# Phase 2: same world, force-generate a 15x15 chunk grid around each biome
# (/forceload caps at 256 chunks per call; 225 keeps us under it, and takes
# BLOCK coordinates).
( sleep 18
  echo "execute in minecraft:the_end run forceload add $((wx-120)) $((wz-120)) $((wx+120)) $((wz+120))"
  sleep 30
  echo "execute in minecraft:the_end run forceload add $((gx-120)) $((gz-120)) $((gx+120)) $((gz+120))"
  sleep 30
  echo "stop" ) | ./gradlew runServer --console=plain > "$GEN" 2>&1

echo "=== forceload confirmations ==="
grep -nE "Marked .* chunks|forceload" "$GEN" | head -6

echo "=== archive variant scan (DIM1) ==="
node -e "
const { readFileSync, existsSync } = require('node:fs');
const zlib = require('node:zlib');
const worldDir = 'run/world';
let archives = 0, mechanisms = 0;
for (let rx = -4; rx <= 4; rx++) for (let rz = -4; rz <= 4; rz++) {
  const file = worldDir + '/DIM1/region/r.' + rx + '.' + rz + '.mca';
  if (!existsSync(file)) continue;
  const buf = readFileSync(file);
  if (buf.length < 8192) continue;
  for (let lx = 0; lx < 32; lx++) for (let lz = 0; lz < 32; lz++) {
    const off = buf.readUInt32BE(4 * (lx + lz * 32));
    if (!off) continue;
    const sectorOffset = (off >> 8) * 4096;
    const sectorCount = off & 0xff;
    if (sectorOffset < 8192 || sectorCount === 0 || sectorOffset + 5 > buf.length) continue;
    const len = buf.readUInt32BE(sectorOffset);
    const comp = buf.readUInt8(sectorOffset + 4);
    if (len < 1 || len > sectorCount * 4096 - 4 || sectorOffset + 5 + len - 1 > buf.length) continue;
    const data = buf.slice(sectorOffset + 5, sectorOffset + 5 + len - 1);
    let nbt;
    try {
      if (comp === 1) nbt = zlib.gunzipSync(data);
      else if (comp === 2) nbt = zlib.inflateSync(data);
      else if (comp === 3) nbt = data;
      else continue;
    } catch {
      continue;
    }
    const s = nbt.toString('latin1');
    if (s.includes('ARCHIVE')) archives++;
    if (s.includes('endesium:resonant_mechanism')) mechanisms++;
  }
}
console.log('ARCHIVE variant mechanisms:', archives);
console.log('total mechanisms:', mechanisms);
"
