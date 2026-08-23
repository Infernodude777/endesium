// Scans End region files for arena blocks and reports counts + chunk locations.
// usage: node tools/scan_arena.mjs <worldDir>
import { readFileSync, existsSync } from 'node:fs';
import zlib from 'node:zlib';

const worldDir = process.argv[2] || 'run/world';
const regionDir = `${worldDir}/DIM1/region`;

const targets = [
  'endesium:resonant_slate',
  'endesium:end_gray',
  'endesium:inscribed_slate',
  'endesium:resonant_pillar',
  'endesium:dormant_resonant_crystal',
  'endesium:cracked_spire_stone',
  'minecraft:end_stone_bricks',
  'minecraft:obsidian',
  'minecraft:bedrock',
];

const counts = Object.fromEntries(targets.map((t) => [t, 0]));
const inChunks = Object.fromEntries(targets.map((t) => [t, []]));

for (let rx = -5; rx <= 5; rx++) {
  for (let rz = -5; rz <= 5; rz++) {
    const file = `${regionDir}/r.${rx}.${rz}.mca`;
    if (!existsSync(file)) continue;
    const buf = readFileSync(file);
    for (let lx = 0; lx < 32; lx++) {
      for (let lz = 0; lz < 32; lz++) {
        const off = buf.readUInt32BE(4 * (lx + lz * 32));
        if (!off) continue;
        const so = (off >> 8) * 4096;
        const len = buf.readUInt32BE(so);
        const comp = buf.readUInt8(so + 4);
        let data;
        try {
          data = comp === 1
            ? zlib.gunzipSync(buf.subarray(so + 5, so + 5 + len - 1))
            : zlib.inflateSync(buf.subarray(so + 5, so + 5 + len - 1));
        } catch (e) {
          continue;
        }
        const s = data.toString('latin1');
        const wx = rx * 32 + lx;
        const wz = rz * 32 + lz;
        for (const t of targets) {
          let i = 0;
          let n = 0;
          while ((i = s.indexOf(t, i)) !== -1) {
            n++;
            i += t.length;
          }
          if (n > 0) {
            counts[t] += n;
            inChunks[t].push(`${wx},${wz}`);
          }
        }
      }
    }
  }
}

for (const t of targets) {
  const chunks = inChunks[t];
  const sample = chunks.length > 12 ? `${chunks.slice(0, 12).join(' ')} ...` : chunks.join(' ');
  console.log(`${t} -> ${counts[t]} block refs (chunks: ${sample || '-'})`);
}
