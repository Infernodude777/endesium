// Renders the central End island from region files as two ASCII maps:
//   1. HEIGHT map  — terrain silhouette + relief (checks for circular/symmetric shape)
//   2. STRUCTURE map — man-made / non-end-stone surface blocks (checks density + patterns)
// usage: node tools/render_island.mjs <worldDir> [scale]
import { readFileSync, existsSync } from 'node:fs';
import zlib from 'node:zlib';

const worldDir = process.argv[2] || 'run/world';
const SCALE = Number(process.argv[3] || 3);

// ---------------------------------------------------------------------------
// NBT parser (network / big-endian)
// ---------------------------------------------------------------------------
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
    case 9: { const et = r.u8(); const n = r.i32(); const arr = []; for (let i = 0; i < n; i++) arr.push(readPayload(r, et)); return arr; }
    case 10: { const obj = {}; for (;;) { const t = readTag(r, true); if (t === null) break; obj[t.name] = t.value; } return obj; }
    case 11: { const n = r.i32(); const arr = []; for (let i = 0; i < n; i++) arr.push(r.i32()); return arr; }
    case 12: { const n = r.i32(); const arr = []; for (let i = 0; i < n; i++) arr.push(r.i64()); return arr; }
    default: throw new Error(`unhandled tag type ${type}`);
  }
}

function readTag(r, named) {
  const type = r.u8();
  if (type === 0) return null;
  const name = named ? r.str() : '';
  return { type, name, value: readPayload(r, type) };
}

// ---------------------------------------------------------------------------
// Region -> top-solid block map
// ---------------------------------------------------------------------------
const RANGE = 192; // block coordinate half-extent to render

function blockAt(section, x, y, z) {
  const palette = section.block_states.palette;
  const data = section.block_states.data || [];
  const bits = palette.length <= 1 ? 4 : Math.ceil(Math.log2(palette.length));
  const idx = ((y & 15) * 16 + z) * 16 + x;
  if (palette.length === 1) return palette[0].Name;
  const bitOffset = idx * bits;
  const longIdx = Math.floor(bitOffset / 64);
  const bitInLong = bitOffset % 64;
  const mask = bits >= 64 ? (1n << 64n) - 1n : (1n << BigInt(bits)) - 1n;
  let val = (data[longIdx] >> BigInt(bitInLong)) & mask;
  if (bitInLong + bits > 64 && longIdx + 1 < data.length) {
    val |= (data[longIdx + 1] & ((1n << BigInt(bits - (64 - bitInLong))) - 1n)) << BigInt(64 - bitInLong);
  }
  const idx0 = Number(val);
  return palette[idx0] ? palette[idx0].Name : 'minecraft:air';
}

function loadRegion(rx, rz, map) {
  const file = `${worldDir}/DIM1/region/r.${rx}.${rz}.mca`;
  if (!existsSync(file)) return;
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
        data = comp === 1 ? zlib.gunzipSync(buf.subarray(so + 5, so + 5 + len - 1))
          : comp === 2 ? zlib.inflateSync(buf.subarray(so + 5, so + 5 + len - 1))
            : zlib.unzipSync(buf.subarray(so + 5, so + 5 + len - 1));
      } catch (e) { continue; }
      const root = readTag(new Reader(data), true);
      if (!root || !root.value) continue;
      // 1.18+ flat format: level fields live directly on the root compound.
      const level = root.value.Level || root.value;
      const chunkX = level.xPos;
      const chunkZ = level.zPos;
      if (chunkX === undefined || chunkZ === undefined) continue;
      const sections = level.sections || [];
      // For each column, find the top non-air block.
      for (let cx = 0; cx < 16; cx++) {
        for (let cz = 0; cz < 16; cz++) {
          const wx = chunkX * 16 + cx;
          const wz = chunkZ * 16 + cz;
          if (wx >= RANGE || wx < -RANGE || wz >= RANGE || wz < -RANGE) continue;
          let topY = -1;
          let topBlock = 'minecraft:air';
          for (let s = sections.length - 1; s >= 0; s--) {
            const section = sections[s];
            const baseY = section.Y * 16;
            for (let y = 15; y >= 0; y--) {
              const name = blockAt(section, cx, y, cz);
              if (name !== 'minecraft:air' && name !== 'minecraft:cave_air' && name !== 'minecraft:void_air') {
                topY = baseY + y;
                topBlock = name;
                break;
              }
            }
            if (topY >= 0) break;
          }
          map[wx + RANGE][wz + RANGE] = { y: topY, block: topBlock };
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Render
// ---------------------------------------------------------------------------
const size = RANGE * 2;
const map = Array.from({ length: size }, () => new Array(size).fill({ y: -1, block: 'minecraft:air' }));

for (let rx = Math.floor(-RANGE / 512); rx <= 0; rx++) {
  for (let rz = Math.floor(-RANGE / 512); rz <= 0; rz++) {
    loadRegion(rx, rz, map);
  }
}

function heightChar(v) {
  if (v.y < 0) return ' ';
  if (v.y <= 55) return '.';
  if (v.y <= 62) return '-';
  if (v.y <= 67) return '=';
  if (v.y <= 74) return 'o';
  if (v.y <= 82) return '#';
  return '@';
}

function structureChar(v) {
  if (v.y < 0) return ' ';
  const b = v.block;
  if (b === 'minecraft:end_stone') return ' ';
  if (b === 'minecraft:air' || b === 'minecraft:cave_air') return ' ';
  if (b.startsWith('endesium:dormant_resonant_crystal')) return 'c';
  if (b.startsWith('endesium:')) return 'r';
  if (b === 'minecraft:obsidian') return 'P';
  if (b === 'minecraft:bedrock') return 'B';
  if (b === 'minecraft:end_stone_bricks') return '#';
  if (b.startsWith('minecraft:chorus')) return '!';
  return '+';
}

function render(fn) {
  const rows = [];
  for (let z = -RANGE; z < RANGE; z += SCALE) {
    let row = '';
    for (let x = -RANGE; x < RANGE; x += SCALE) {
      // Max height (or most interesting) in the cell.
      let best = map[x + RANGE][z + RANGE];
      for (let dx = 0; dx < SCALE; dx++) {
        for (let dz = 0; dz < SCALE; dz++) {
          const v = map[x + RANGE + dx]?.[z + RANGE + dz] || { y: -1, block: 'minecraft:air' };
          if (v.y > best.y) best = v;
        }
      }
      row += fn(best);
    }
    rows.push(row);
  }
  return rows.join('\n');
}

console.log('=== HEIGHT MAP (  .=low  -=basin  ==arena  o=mid  #=high  @=ridge ) ===');
console.log(render(heightChar));
console.log('\n=== STRUCTURE MAP ( r=endesium  c=crystal  #=bricks  P=obsidian  B=bedrock ) ===');
console.log(render(structureChar));
