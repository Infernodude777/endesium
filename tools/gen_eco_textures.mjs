// Generates the End Ecology milestone textures directly as PNGs using the same
// grid + palette language as tools/generate_textures.mjs, but without the
// external DogSprite MCP server dependency. Writes into the mod's texture
// tree so the resources are ready to ship.
import { deflateSync } from 'node:zlib';
import { writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');

// Endesium palette subset (docs/ENDESIUM_VISUAL_DESIGN.md).
const PALETTE = {
  '.': null,
  B: '#111116', K: '#26232B', k: '#34313A', b: '#1B1920',
  C: '#D8D0B4', c: '#E9E2CC', s: '#B8B095', S: '#96907A',
  D: '#312A3D', V: '#5E526E', v: '#6B5F7C', u: '#463D55', U: '#2A2436',
  L: '#C4BBCD', l: '#D8D2E0', w: '#9B90A6',
  Y: '#7EA7A6', y: '#9CC4C2', n: '#5E8280',
  R: '#A9E6DF', A: '#C6A85A', a: '#D8BC6F', o: '#9A823F', W: '#F2F0E5',
};

// ---- minimal PNG writer (RGBA, zlib deflate) ----
const CRC_TABLE = (() => {
  const table = new Int32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
    table[n] = c;
  }
  return table;
})();

function crc32(buf) {
  let c = 0xFFFFFFFF;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xFF] ^ (c >>> 8);
  return (c ^ 0xFFFFFFFF) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(body));
  return Buffer.concat([len, body, crc]);
}

function writePng(path, grid) {
  const h = grid.length;
  const w = grid[0].length;
  const raw = Buffer.alloc((w * 4 + 1) * h);
  let o = 0;
  for (let y = 0; y < h; y++) {
    raw[o++] = 0; // filter: none
    for (let x = 0; x < w; x++) {
      const hex = PALETTE[grid[y][x]];
      if (!hex) { o += 4; continue; }
      const r = parseInt(hex.slice(1, 3), 16);
      const g = parseInt(hex.slice(3, 5), 16);
      const b = parseInt(hex.slice(5, 7), 16);
      raw[o++] = r; raw[o++] = g; raw[o++] = b; raw[o++] = 255;
    }
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0);
  ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8;  // bit depth
  ihdr[9] = 6;  // RGBA
  const png = Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]),
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(raw)),
    chunk('IEND', Buffer.alloc(0)),
  ]);
  writeFileSync(path, png);
}

function makeGrid(rows) {
  return rows.map((r) => r.split(''));
}

// ---- Resonant Bloom: pale violet bell flower with a faint cyan core ----
const RESONANT_BLOOM = makeGrid([
  '................',
  '......u..u......',
  '.....uVu.uVu....',
  '....uVVVuVVu....',
  '....uVVVVVVu....',
  '...uVVVVVVVVu...',
  '...uVVyYYyVVu...',
  '..uVVVyYYyVVVu..',
  '..uVVVVyYyVVVu..',
  '..uVVVVVyyVVVu..',
  '..uVVVVLyyLVVVu..',
  '..uVVVLLLLLVVVu..',
  '...uVVVVVVVVVu..',
  '...uVuVuVuVuVu..',
  '....uuuuuuuuu...',
  '................',
]);

// ---- Echo Compass: dark ring, pale dial, cyan-tipped needle, gold pivot ----
const ECHO_COMPASS = makeGrid([
  '................',
  '.......kk.......',
  '.....kkKKkk.....',
  '....kKuuuuKk....',
  '...kKuuuuuuKk...',
  '...kKuYYyYYuKk..',
  '..kKuLuuuuuLuKk.',
  '..kKuLuYYuuLuKk.',
  '..kKuLuAAuuLuKk.',
  '..kKuLuuuuuLuKk.',
  '..kKuLLLLLLLuKk.',
  '...kKuuuuuuuKk..',
  '...kKuuuuuuKk...',
  '....kKuuuuKk....',
  '.....kkKKkk.....',
  '................',
]);

// ---- Archive Sigil: a square ancient-gold seal with a cyan ring and eye ----
const ARCHIVE_SIGIL = makeGrid([
  '................',
  '................',
  '....ooooo.......',
  '...oAAAAAo......',
  '..oAAKKKAAo.....',
  '..oAKKyyKKAo....',
  '.oAAKYyyYKAAo...',
  '.oAAKYyyYKAAo...',
  '.oAAKYyyYKAAo...',
  '.oAAKKyyKKAo....',
  '..oAKKyyKKAo....',
  '..oAAKKKAAo.....',
  '...oAAAAAo......',
  '....ooooo.......',
  '................',
  '................',
]);

mkdirSync(resolve(ROOT, 'src/main/resources/assets/endesium/textures/block'), { recursive: true });
mkdirSync(resolve(ROOT, 'src/main/resources/assets/endesium/textures/item'), { recursive: true });
writePng(resolve(ROOT, 'src/main/resources/assets/endesium/textures/block/resonant_bloom.png'), RESONANT_BLOOM);
writePng(resolve(ROOT, 'src/main/resources/assets/endesium/textures/item/echo_compass.png'), ECHO_COMPASS);
writePng(resolve(ROOT, 'src/main/resources/assets/endesium/textures/item/archive_sigil.png'), ARCHIVE_SIGIL);
console.log('wrote resonant_bloom.png, echo_compass.png, archive_sigil.png');
