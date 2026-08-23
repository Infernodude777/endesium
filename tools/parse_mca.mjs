// Minimal .mca region parser: finds a chunk, decompresses it, and prints its
// status + block palette + section info. Used to verify End chunk generation.
import { readFileSync } from 'node:fs';
import zlib from 'node:zlib';

const [, , regionPath, chunkX, chunkZ] = process.argv;
if (!regionPath || chunkX === undefined) {
  console.error('usage: node parse_mca.mjs <region.mca> <chunkX> <chunkZ>');
  process.exit(1);
}
const buf = readFileSync(regionPath);
const cx = Number(chunkX), cz = Number(chunkZ);
const lx = cx & 31, lz = cz & 31;
const offsetEntry = buf.readUInt32BE(4 * (lx + lz * 32));
if (!offsetEntry) {
  console.log(`chunk (${cx},${cz}) not present in region`);
  process.exit(0);
}
const sectorOffset = (offsetEntry >> 8) * 4096;
const length = buf.readUInt32BE(sectorOffset);
const compression = buf.readUInt8(sectorOffset + 4);
const data = buf.slice(sectorOffset + 5, sectorOffset + 5 + length - 1);
let nbt;
if (compression === 1) {
  nbt = zlib.gunzipSync(data);
} else if (compression === 2) {
  nbt = zlib.inflateSync(data);
} else if (compression === 3) {
  nbt = zlib.unzipSync(data);
} else {
  console.error(`unsupported compression ${compression}`);
  process.exit(1);
}
// crude NBT walk: find Status string and Level palette
const ascii = nbt.toString('latin1');
function extractString(name) {
  const idx = ascii.indexOf(name + '\u0000');
  if (idx < 0) return null;
  const start = idx + name.length + 1;
  const len = nbt.readUInt16BE(start);
  return nbt.slice(start + 2, start + 2 + len).toString();
}
const status = extractString('Status');
console.log(`chunk (${cx},${cz}): status=${status}`);
const palettes = new Set();
// scan for block palette strings (minecraft:xxx) outside NBT names
const re = /minecraft:[a-z_]+/g;
let m;
const skip = new Set(['minecraft:air']);
while ((m = re.exec(ascii)) !== null) {
  const s = m[0];
  // crude: ignore strings that are part of known names
  if (!['minecraft:minecraft', 'minecraft:air'].includes(s)) palettes.add(s);
}
console.log('block ids seen:', [...palettes].slice(0, 40).join(', '));
