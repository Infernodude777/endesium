// Generates the Resonant Wings textures: a 16x16 inventory icon and the 64x32
// worn elytra texture (wings drawn in the ElytraModel's UV region x22..42).
import { deflateSync } from 'node:zlib';
import { writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');

const PALETTE = {
  '.': null,
  B: '#111116', K: '#26232B', k: '#34313A', b: '#1B1920',
  C: '#D8D0B4', c: '#E9E2CC', s: '#B8B095', S: '#96907A',
  D: '#312A3D', V: '#5E526E', u: '#463D55', U: '#2A2436',
  L: '#C4BBCD', l: '#D8D2E0', w: '#9B90A6',
  Y: '#7EA7A6', y: '#9CC4C2', n: '#5E8280',
  R: '#A9E6DF', A: '#C6A85A', a: '#D8BC6F', W: '#F2F0E5',
};

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
    raw[o++] = 0;
    for (let x = 0; x < w; x++) {
      const hex = PALETTE[grid[y][x]];
      if (!hex) { o += 4; continue; }
      raw[o++] = parseInt(hex.slice(1, 3), 16);
      raw[o++] = parseInt(hex.slice(3, 5), 16);
      raw[o++] = parseInt(hex.slice(5, 7), 16);
      raw[o++] = 255;
    }
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0);
  ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  const png = Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]),
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(raw)),
    chunk('IEND', Buffer.alloc(0)),
  ]);
  writeFileSync(path, png);
}

const makeGrid = (rows) => rows.map((r) => r.split(''));

// 16x16 inventory icon: a folded pair of dark wings with a pale cyan edge.
const RESONANT_WINGS_ITEM = makeGrid([
  '................',
  '....KKKK........',
  '...KKKKKK.......',
  '..KKKKkkKK......',
  '..KKkkkkKK......',
  '..KkkkkkKK......',
  '..KkkkkkKK......',
  '..KkYYkkKK......',
  '..KkYYkKKK......',
  '..KkYYkKKK......',
  '...KkkKKK.......',
  '...KkkKKK.......',
  '....kKKK........',
  '.....KK.........',
  '................',
  '................',
]);

// 64x32 worn texture: the ElytraModel reads both wings from x22..42, y0..20.
// Left wing drawn here; the model mirrors it for the right wing.
const WING = [
  '..KKKKKKKK..',
  '.KKkkkkKKKK.',
  '.KkkkkkKKKK.',
  '.KkkkkkKKKK.',
  '.KkkYYkkKKK.',
  '.KkkYYkkKKK.',
  '.KkkkkkKKKK.',
  '.KkkkkkKKKK.',
  '..kkkkkKKK..',
  '..kkkkkKKK..',
  '..kYYkKKK...',
  '..kYYkKKK...',
  '..kkkkKKK...',
  '...kkkKKK...',
  '...kkkKKK...',
  '...kkkKK....',
  '....kkKK....',
  '....kkKK....',
  '.....kK.....',
  '.....kK.....',
];

const ENTITY = [];
for (let y = 0; y < 32; y++) {
  let row = '';
  for (let x = 0; x < 64; x++) {
    const wx = x - 22;
    const wy = y - 1;
    if (wx >= 0 && wx < 10 && wy >= 0 && wy < 20) {
      row += WING[wy][wx];
    } else {
      row += '.';
    }
  }
  ENTITY.push(row);
}

mkdirSync(resolve(ROOT, 'src/main/resources/assets/endesium/textures/item'), { recursive: true });
mkdirSync(resolve(ROOT, 'src/main/resources/assets/endesium/textures/entity'), { recursive: true });
writePng(resolve(ROOT, 'src/main/resources/assets/endesium/textures/item/resonant_wings.png'), RESONANT_WINGS_ITEM);
writePng(resolve(ROOT, 'src/main/resources/assets/endesium/textures/entity/resonant_wings.png'), ENTITY);
console.log('wrote resonant_wings.png (item) and resonant_wings.png (entity)');
