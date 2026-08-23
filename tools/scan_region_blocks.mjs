// Scans End region files for block ids and biomes across a chunk grid.
// usage: node scan_region_blocks.mjs <worldDir> <minCx> <minCz> <maxCx> <maxCz>
import { readFileSync, existsSync } from 'node:fs';
import zlib from 'node:zlib';

const [, , worldDir, minCx, minCz, maxCx, maxCz] = process.argv;
const cx0 = Number(minCx), cz0 = Number(minCz), cx1 = Number(maxCx), cz1 = Number(maxCz);

const TARGETS = ['endesium:resonant_mechanism', 'endesium:inscribed_slate', 'endesium:resonant_pillar',
  'endesium:cracked_spire_stone', 'minecraft:barrel', 'endesium:resonant_slate', 'endesium:dormant_resonant_crystal'];
const found = Object.fromEntries(TARGETS.map((t) => [t, []]));
const biomeCounts = {};

for (let cx = cx0; cx <= cx1; cx++) {
  for (let cz = cz0; cz <= cz1; cz++) {
    const rx = Math.floor(cx / 32), rz = Math.floor(cz / 32);
    const file = `${worldDir}/DIM1/region/r.${rx}.${rz}.mca`;
    if (!existsSync(file)) continue;
    const buf = readFileSync(file);
    const lx = cx & 31, lz = cz & 31;
    const offsetEntry = buf.readUInt32BE(4 * (lx + lz * 32));
    if (!offsetEntry) continue;
    const sectorOffset = (offsetEntry >> 8) * 4096;
    const sectorCount = offsetEntry & 0xff;
    if (sectorOffset < 8192 || sectorCount === 0 || sectorOffset + 5 > buf.length) continue;
    const length = buf.readUInt32BE(sectorOffset);
    const compression = buf.readUInt8(sectorOffset + 4);
    if (length < 1 || length > sectorCount * 4096 - 4 || sectorOffset + 5 + length - 1 > buf.length) continue;
    const data = buf.slice(sectorOffset + 5, sectorOffset + 5 + length - 1);
    let nbt;
    if (compression === 1) nbt = zlib.gunzipSync(data);
    else if (compression === 2) nbt = zlib.inflateSync(data);
    else if (compression === 3) nbt = data;
    else continue;
    const ascii = nbt.toString('latin1');
    // biome: capture any registry-style biome id in the biomes palette
    const biomes = ascii.match(/(?:endesium|minecraft):[a-z_]+/g) ?? [];
    for (const b of biomes) biomeCounts[b] = (biomeCounts[b] ?? 0) + 1;
    for (const t of TARGETS) {
      if (ascii.includes(t)) found[t].push(`(${cx},${cz})`);
    }
  }
}
console.log('=== biome mentions across scanned chunks ===');
for (const [b, n] of Object.entries(biomeCounts).sort((a, b) => b[1] - a[1])) console.log(b, n);
console.log('=== target blocks ===');
for (const [t, cells] of Object.entries(found)) {
  console.log(t.padEnd(34), cells.length, cells.slice(0, 10).join(' '));
}
