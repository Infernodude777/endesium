// Histogram of top-solid terrain heights on the central End island, plus a
// cleaner silhouette render that ignores the obsidian pillars so the actual
// island shape and relief are visible.
// usage: node tools/terrain_hist.mjs <worldDir> [scale]
import { readFileSync, existsSync } from 'node:fs';
import zlib from 'node:zlib';

const worldDir = process.argv[2] || 'run/world';
const SCALE = Number(process.argv[3] || 3);
const RANGE = 160;

class Reader {
  constructor(buf) { this.buf = buf; this.o = 0; }
  u8() { return this.buf[this.o++]; }
  i16() { const v = this.buf.readInt16BE(this.o); this.o += 2; return v; }
  i32() { const v = this.buf.readInt32BE(this.o); this.o += 4; return v; }
  i64() { const v = this.buf.readBigInt64BE(this.o); this.o += 8; return v; }
  f32() { const v = this.buf.readFloatBE(this.o); this.o += 4; return v; }
  f64() { const v = this.buf.readDoubleBE(this.o); this.o += 8; return v; }
  bytes(n) { const v = this.buf.subarray(this.o, this.o + n); this.o += n; return v; }
  str() { const len = this.buf.readUInt16BE(this.o); this.o += 2; const s = this.buf.subarray(this.o, this.o + len).toString('utf8'); this.o += len; return s; }
}
function readPayload(r, type) {
  switch (type) {
    case 1: return r.u8();
    case 2: return r.i16();
    case 3: return r.i32();
    case 4: return r.i64();
    case 5: return r.f32();
    case 6: return r.f64();
    case 7: { const n = r.i32(); return r.bytes(n); }
    case 8: return r.str();
    case 9: { const et = r.u8(); const n = r.i32(); const a = []; for (let i = 0; i < n; i++) a.push(readPayload(r, et)); return a; }
    case 10: { const o = {}; for (;;) { const t = readTag(r, true); if (t === null) break; o[t.name] = t.value; } return o; }
    case 11: { const n = r.i32(); const a = []; for (let i = 0; i < n; i++) a.push(r.i32()); return a; }
    case 12: { const n = r.i32(); const a = []; for (let i = 0; i < n; i++) a.push(r.i64()); return a; }
    default: throw new Error('tag ' + type);
  }
}
function readTag(r, named) {
  const t = r.u8(); if (t === 0) return null;
  const name = named ? r.str() : '';
  return { type: t, name, value: readPayload(r, t) };
}

function blockAt(section, x, y, z) {
  const pal = section.block_states.palette;
  const data = section.block_states.data || [];
  if (pal.length === 1) return pal[0].Name;
  const bits = Math.max(4, Math.ceil(Math.log2(pal.length)));
  const idx = ((y & 15) * 16 + z) * 16 + x;
  const bitOff = idx * bits;
  const li = Math.floor(bitOff / 64);
  const bi = bitOff % 64;
  const mask = bits >= 64 ? (1n << 64n) - 1n : (1n << BigInt(bits)) - 1n;
  let v = (data[li] >> BigInt(bi)) & mask;
  if (bi + bits > 64 && li + 1 < data.length) v |= (data[li + 1] & ((1n << BigInt(bits - (64 - bi))) - 1n)) << BigInt(64 - bi);
  return pal[Number(v)] ? pal[Number(v)].Name : 'minecraft:air';
}

const map = Array.from({ length: RANGE * 2 }, () => new Array(RANGE * 2).fill({ y: -1, block: 'air' }));
const hist = {};
const pillar = new Set();
for (const [px, pz] of [[42,0],[0,42],[-42,0],[0,-42],[30,30],[-30,30],[30,-30],[-30,-30],[15,30],[30,15],[-15,30],[30,-15],[15,-30],[-15,-30],[-30,15],[-30,-15],[-42,-42],[42,-42],[-42,42],[42,42]]) {
  pillar.add(px + ',' + pz);
}

for (let rx = -2; rx <= 1; rx++) for (let rz = -2; rz <= 1; rz++) {
  const file = `${worldDir}/DIM1/region/r.${rx}.${rz}.mca`;
  if (!existsSync(file)) continue;
  const buf = readFileSync(file);
  for (let lx = 0; lx < 32; lx++) for (let lz = 0; lz < 32; lz++) {
    const off = buf.readUInt32BE(4 * (lx + lz * 32));
    if (!off) continue;
    const so = (off >> 8) * 4096;
    const len = buf.readUInt32BE(so);
    const comp = buf.readUInt8(so + 4);
    let data;
    try { data = comp === 2 ? zlib.inflateSync(buf.subarray(so + 5, so + 5 + len - 1)) : zlib.gunzipSync(buf.subarray(so + 5, so + 5 + len - 1)); }
    catch (e) { continue; }
    const root = readTag(new Reader(data), true);
    if (!root || !root.value) continue;
    const level = root.value.Level || root.value;
    if (level.xPos === undefined || level.zPos === undefined) continue;
    const cx = level.xPos, cz = level.zPos;
    const sections = level.sections || [];
    for (let x = 0; x < 16; x++) for (let z = 0; z < 16; z++) {
      const wx = cx * 16 + x, wz = cz * 16 + z;
      if (wx >= RANGE || wx < -RANGE || wz >= RANGE || wz < -RANGE) continue;
      let topY = -1, topBlock = 'air';
      for (let s = sections.length - 1; s >= 0; s--) {
        const sec = sections[s];
        if (!sec || !sec.block_states) continue;
        for (let y = 15; y >= 0; y--) {
          const nm = blockAt(sec, x, y, z);
          if (nm !== 'minecraft:air' && nm !== 'minecraft:cave_air' && nm !== 'minecraft:void_air') { topY = sec.Y * 16 + y; topBlock = nm; break; }
        }
        if (topY >= 0) break;
      }
      map[wx + RANGE][wz + RANGE] = { y: topY, block: topBlock };
      if (topY >= 0 && !pillar.has(wx + ',' + wz) && topBlock === 'minecraft:end_stone') {
        const bucket = Math.floor(topY / 4) * 4;
        hist[bucket] = (hist[bucket] || 0) + 1;
      }
    }
  }
}

console.log('=== end_stone surface height histogram (4-block buckets, excluding pillar columns) ===');
const keys = Object.keys(hist).map(Number).sort((a, b) => a - b);
const max = Math.max(...keys.map((k) => hist[k]));
for (const k of keys) {
  const bar = '#'.repeat(Math.round((hist[k] / max) * 60));
  console.log(String(k).padStart(3) + '..' + String(k + 3).padStart(3) + '  ' + String(hist[k]).padStart(5) + '  ' + bar);
}

function c(v) {
  if (v.y < 0) return ' ';
  if (v.block !== 'minecraft:end_stone') return ' ';
  if (v.y <= 56) return '.';
  if (v.y <= 62) return '-';
  if (v.y <= 68) return '=';
  if (v.y <= 76) return 'o';
  if (v.y <= 88) return '#';
  return '@';
}
console.log('\n=== SILHOUETTE (end_stone only, ignores pillars/structures) ===');
const rows = [];
for (let z = -RANGE; z < RANGE; z += SCALE) {
  let row = '';
  for (let x = -RANGE; x < RANGE; x += SCALE) {
    let best = map[x + RANGE][z + RANGE];
    for (let dx = 0; dx < SCALE; dx++) for (let dz = 0; dz < SCALE; dz++) {
      const v = map[x + RANGE + dx]?.[z + RANGE + dz] || { y: -1, block: 'air' };
      if (v.y > best.y) best = v;
    }
    row += c(best);
  }
  rows.push(row);
}
console.log(rows.join('\n'));
