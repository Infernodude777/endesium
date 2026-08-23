// Generates the Void Skirts milestone assets directly as PNGs + JSON using the
// same grid + palette language as tools/gen_eco_textures.mjs. Writes block and
// item textures, the checked-in slab/stairs/wall blockstates and models, the
// plant cross models, and the item models for the void materials/tools/armor.
import { deflateSync } from 'node:zlib';
import { writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const ASSETS = `${ROOT}/src/main/resources/assets/endesium`;

const PALETTE = {
  '.': null,
  B: '#0B0B10', K: '#1B1B22', k: '#26262E', b: '#14141A',
  C: '#D8D0B4', c: '#E9E2CC', s: '#B8B095', S: '#96907A',
  D: '#312A3D', V: '#5E526E', v: '#6B5F7C', u: '#463D55', U: '#2A2436',
  L: '#C4BBCD', l: '#D8D2E0', w: '#9B90A6',
  Y: '#7EA7A6', y: '#9CC4C2', n: '#5E8280',
  R: '#A9E6DF', A: '#C6A85A', a: '#D8BC6F', o: '#9A823F', W: '#F2F0E5',
  G: '#6E6E6E', g: '#8A8A8A', M: '#4A4A4A', m: '#5A5A5A',
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
  mkdirSync(dirname(path), { recursive: true });
  writeFileSync(path, png);
}

const makeGrid = (rows) => rows.map((r) => r.split(''));
const writeJson = (path, obj) => {
  mkdirSync(dirname(path), { recursive: true });
  writeFileSync(path, JSON.stringify(obj, null, 2));
};

// ═══════════════════════════════════════════════════════════════════════════
// 1. BLOCK TEXTURES (16x16)
// ═══════════════════════════════════════════════════════════════════════════

// Void Slate: dark, faintly violet slate with a few pale mineral flecks.
const VOID_SLATE = makeGrid([
  'KKKKKKKKKKKKKKKK',
  'KkKkKkKkKkKkkKk',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KKKKKKKKKKKKKKKK',
]);

// Void Gravel: loose dark gravel.
const VOID_GRAVEL = makeGrid([
  'KkKbKkKbKkKbKkK',
  'kMkKmKkMkKmKkMk',
  'KkKbKkKbKkKbKkK',
  'kMkKmKkMkKmKkMk',
  'KkKbKkKbKkKbKkK',
  'kMkKmKkMkKmKkMk',
  'KkKbKkKbKkKbKkK',
  'kMkKmKkMkKmKkMk',
  'KkKbKkKbKkKbKkK',
  'kMkKmKkMkKmKkMk',
  'KkKbKkKbKkKbKkK',
  'kMkKmKkMkKmKkMk',
  'KkKbKkKbKkKbKkK',
  'kMkKmKkMkKmKkMk',
  'KkKbKkKbKkKbKkK',
  'kMkKmKkMkKmKkMk',
]);

// Void Soil: dark, slightly violet dirt.
const VOID_SOIL = makeGrid([
  'KkKkKkKkKkKkKkK',
  'kDkDkDkDkDkDkDk',
  'KkKkKkKkKkKkKkK',
  'kDkDkDkDkDkDkDk',
  'KkKkKkKkKkKkKkK',
  'kDkDkDkDkDkDkDk',
  'KkKkKkKkKkKkKkK',
  'kDkDkDkDkDkDkDk',
  'KkKkKkKkKkKkKkK',
  'kDkDkDkDkDkDkDk',
  'KkKkKkKkKkKkKkK',
  'kDkDkDkDkDkDkDk',
  'KkKkKkKkKkKkKkK',
  'kDkDkDkDkDkDkDk',
  'KkKkKkKkKkKkKkK',
  'kDkDkDkDkDkDkDk',
]);

// Void Glass: mostly transparent with a dark frame and a faint cyan glint.
const VOID_GLASS = makeGrid([
  'KKKKKKKKKKKKKKKK',
  'K.........n....K',
  'K..n............K',
  'K...........n..K',
  'K.....n........K',
  'K............n.K',
  'K..n...........K',
  'K........n.....K',
  'K.....n........K',
  'K..........n...K',
  'K...n..........K',
  'K........n.....K',
  'K.....n........K',
  'K..........n...K',
  'K..n...........K',
  'KKKKKKKKKKKKKKKK',
]);

// Void Brick: dark bricks with pale mortar and occasional violet bricks.
const VOID_BRICK = makeGrid([
  'KKKKKKKKKKKKKKKK',
  'kkkkkkkkkkkkkkkk',
  'KkKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkKk',
  'kkkkkkkkkkkkkkkk',
  'KkKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkKk',
  'kkkkkkkkkkkkkkkk',
  'KkKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkKk',
  'kkkkkkkkkkkkkkkk',
  'KkKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkKk',
  'kkkkkkkkkkkkkkkk',
  'KkKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkKk',
]);

// Void Lamp: dark frame with a bright cold center.
const VOID_LAMP = makeGrid([
  'KKKKKKKKKKKKKKKK',
  'KkkkkkkkkkkkkkkK',
  'KkYYYYYYYYYYYYkK',
  'KkyRRRRRRRRRRykK',
  'KkyRWWWWWWWWRykK',
  'KkyRWRRRRRRWRykK',
  'KkyRWRYYYYRWRykK',
  'KkyRWRYRRYRWRykK',
  'KkyRWRYRRYRWRykK',
  'KkyRWRYYYYRWRykK',
  'KkyRWRRRRRRWRykK',
  'KkyRWWWWWWWWRykK',
  'KkyRRRRRRRRRRykK',
  'KkYYYYYYYYYYYYkK',
  'KkkkkkkkkkkkkkkK',
  'KKKKKKKKKKKKKKKK',
]);

// Void Ore: void slate with glowing cyan-violet gem clusters.
const VOID_ORE = makeGrid([
  'KKKKKKKKKKKKKKKK',
  'KkKkKkKkKkKkKKk',
  'kKkKkKkKkKkKkKk',
  'KkKkKkYkKkKkKkK',
  'kKkKkyYyKkKkKkK',
  'KkKkKkYkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKnkKkKkKkKkK',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KKKKKKKKKKKKKKKK',
]);

// Void Spire: dark stone with a glowing cyan seam running down the center.
const VOID_SPIRE = makeGrid([
  'KKKKKKKKKKKKKKKK',
  'KkKkKkYkKkKkKkK',
  'kKkKkKyYyKkKkKk',
  'KkKkKkYkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KkKkKkKkKkKkKkK',
  'kKkKkKkKkKkKkKk',
  'KKKKKKKKKKKKKKKK',
]);

// Void Weave: dark woven fabric with a subtle thread pattern.
const VOID_WEAVE = makeGrid([
  'KKKKKKKKKKKKKKKK',
  'KkKkKkKkKkKkKkK',
  'KKKKKKKKKKKKKKKK',
  'kKkKkKkKkKkKkKk',
  'KKKKKKKKKKKKKKKK',
  'KkKkKkKkKkKkKkK',
  'KKKKKKKKKKKKKKKK',
  'kKkKkKkKkKkKkKk',
  'KKKKKKKKKKKKKKKK',
  'KkKkKkKkKkKkKkK',
  'KKKKKKKKKKKKKKKK',
  'kKkKkKkKkKkKkKk',
  'KKKKKKKKKKKKKKKK',
  'KkKkKkKkKkKkKkK',
  'KKKKKKKKKKKKKKKK',
  'kKkKkKkKkKkKkKk',
]);

// Umbral Stone: the darkest stone, with faint violet seams.
const UMBRAL_STONE = makeGrid([
  'BBBBBBBBBBBBBBBB',
  'BKBKBKBKBKBKBKBK',
  'KBKBKBKBKBKBKBKB',
  'BKBKBKBKBKBKBKBK',
  'KBKBKBKBKBKBKBKB',
  'BKBKBKBKBKBKBKBK',
  'KBKBKBKBKBKBKBKB',
  'BKBKBKBKBKBKBKBK',
  'KBKBKBKBKBKBKBKB',
  'BKBKBKBKBKBKBKBK',
  'KBKBKBKBKBKBKBKB',
  'BKBKBKBKBKBKBKBK',
  'KBKBKBKBKBKBKBKB',
  'BKBKBKBKBKBKBKBK',
  'KBKBKBKBKBKBKBKB',
  'BBBBBBBBBBBBBBBB',
]);

// Voidstone: deep void stone.
const VOIDSTONE = makeGrid([
  'BBBBBBBBBBBBBBBB',
  'BbBbBbBbBbBbBbBb',
  'bBbBbBbBbBbBbBbB',
  'BbBbBbBbBbBbBbBb',
  'bBbBbBbBbBbBbBbB',
  'BbBbBbBbBbBbBbBb',
  'bBbBbBbBbBbBbBbB',
  'BbBbBbBbBbBbBbBb',
  'bBbBbBbBbBbBbBbB',
  'BbBbBbBbBbBbBbBb',
  'bBbBbBbBbBbBbBbB',
  'BbBbBbBbBbBbBbBb',
  'bBbBbBbBbBbBbBbB',
  'BbBbBbBbBbBbBbBb',
  'bBbBbBbBbBbBbBbB',
  'BBBBBBBBBBBBBBBB',
]);

// Plant textures (cross models).
const VOID_CRYSTAL = makeGrid([
  '................',
  '................',
  '.......K........',
  '......KYK.......',
  '......kYk.......',
  '.....kYyk.......',
  '.....kYYk.......',
  '.....kYyk.......',
  '....kYyyk.......',
  '....kYYyk.......',
  '....kYyyk.......',
  '....kYyk........',
  '....kYyk........',
  '.....kk.........',
  '................',
  '................',
]);

const UMBRAL_GRASS = makeGrid([
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '..K.......K.....',
  '.Kk......Kk.....',
  '.kU.....Kk......',
  '.kU....kU.......',
  '..kU..kU........',
  '...kkkk.........',
  '................',
  '................',
]);

const VOID_FERN = makeGrid([
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '.....K..........',
  '....KDK.........',
  '...KkDK.........',
  '..Kk.KDk........',
  '.Kk...KDk.......',
  'Kk.....KDk......',
  'k.......Kk......',
  '........k.......',
  '................',
  '................',
]);

// ═══════════════════════════════════════════════════════════════════════════
// 2. ITEM TEXTURES (16x16)
// ═══════════════════════════════════════════════════════════════════════════

const VOID_INGOT = makeGrid([
  '................',
  '................',
  '......KKKK......',
  '.....KYYYKK.....',
  '....KYYYYYKK....',
  '....KYYYYYKK....',
  '....KYYYYYKK....',
  '....KYYYYYKK....',
  '....KYYYYYKK....',
  '....KYYYYYKK....',
  '....KYYYYYKK....',
  '.....KYYYYKK....',
  '......KKKKK.....',
  '................',
  '................',
  '................',
]);

const VOID_NUGGET = makeGrid([
  '................',
  '................',
  '................',
  '................',
  '................',
  '.....KKKK.......',
  '....KYYYK......',
  '....KYYYK......',
  '....KYYYK......',
  '....KYYYK......',
  '.....KKK.......',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const VOID_GEM = makeGrid([
  '................',
  '................',
  '.......K........',
  '......KYK.......',
  '.....KYYYK......',
  '....KYYYYYK.....',
  '....KYYYYYK.....',
  '....KYYYYYK.....',
  '.....KYYYYK.....',
  '......KYYK......',
  '.......KK.......',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const UMBRAL_SHARD = makeGrid([
  '................',
  '................',
  '.......B........',
  '......BKB.......',
  '.....BKB........',
  '....BKB.........',
  '...BKB..........',
  '..BKB...........',
  '..BKB...........',
  '...BKB..........',
  '....BKB.........',
  '.....BKB........',
  '......BB........',
  '................',
  '................',
  '................',
]);

const VOID_CORE = makeGrid([
  '................',
  '................',
  '....KKKKKK......',
  '...KKKKKKKK.....',
  '..KKYYYYYYKK....',
  '..KYRRRRRRYK....',
  '..KYRWWWWRYK....',
  '..KYRWRYRWYK....',
  '..KYRWWWWRYK....',
  '..KYRRRRRRYK....',
  '..KKYYYYYYKK....',
  '...KKKKKKKK.....',
  '....KKKKKK......',
  '................',
  '................',
  '................',
]);

const VOID_SWORD = makeGrid([
  '................',
  '..........K.....',
  '.........KY.....',
  '........KYk.....',
  '.......KYk......',
  '......KYk.......',
  '.....KYk........',
  '....KYk.........',
  '...KYk..........',
  '..KYk...........',
  '..Kk............',
  '..kk............',
  '..Kk............',
  '..Kk............',
  '..Kk............',
  '................',
]);

const VOID_PICKAXE = makeGrid([
  '................',
  '..KKKKKK........',
  '.KYYYYYK........',
  '.KYYYYYK........',
  '.KYYYYYK........',
  '..KKKKKK........',
  '......KK........',
  '.....KKk........',
  '....KKk.........',
  '...KKk..........',
  '..KKk...........',
  '..Kk............',
  '..Kk............',
  '..Kk............',
  '..Kk............',
  '................',
]);

const VOID_AXE = makeGrid([
  '................',
  '..KKKKKK........',
  '.KYYYYYK........',
  '.KYYYYYK........',
  '.KYYYYYK........',
  '..KKKKKK........',
  '......KK........',
  '.....KKk........',
  '....KKk.........',
  '...KKk..........',
  '..KKk...........',
  '..Kk............',
  '..Kk............',
  '..Kk............',
  '..Kk............',
  '................',
]);

const VOID_SHOVEL = makeGrid([
  '................',
  '................',
  '.....KKKK.......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '.....KKKK.......',
  '......KK........',
  '.....KKk........',
  '....KKk.........',
  '...KKk..........',
  '..KKk...........',
  '..Kk............',
  '..Kk............',
  '..Kk............',
  '................',
]);

const VOID_HOE = makeGrid([
  '................',
  '..KKKKKK........',
  '.KYYYYYK........',
  '.KYYYYYK........',
  '.KYYYYYK........',
  '.KYYYYYK........',
  '..KKKKKK........',
  '......KK........',
  '.....KKk........',
  '....KKk.........',
  '...KKk..........',
  '..KKk...........',
  '..Kk............',
  '..Kk............',
  '..Kk............',
  '................',
]);

const VOID_HELMET = makeGrid([
  '................',
  '....KKKKKK......',
  '...KYYYYYYK.....',
  '..KYYYYYYYYK....',
  '..KYYYYYYYYK....',
  '..KYYYYYYYYK....',
  '..KYYYYYYYYK....',
  '...KYYYYYYK.....',
  '....KKKKKK......',
  '.....KYYK.......',
  '.....KYYK.......',
  '......KK........',
  '................',
  '................',
  '................',
  '................',
]);

const VOID_CHESTPLATE = makeGrid([
  '................',
  '....KKKKKK......',
  '...KYYYYYYK.....',
  '...KYYYYYYK.....',
  '...KYYYYYYK.....',
  '...KYYYYYYK.....',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '.....KYYK.......',
  '.....KKKK.......',
  '.....KKKK.......',
  '................',
  '................',
  '................',
]);

const VOID_LEGGINGS = makeGrid([
  '................',
  '.....KKKK.......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '.....KYYK.......',
  '.....KYYK.......',
  '.....KYYK.......',
  '.....KYYK.......',
  '.....KYYK.......',
  '.....KKKK.......',
  '................',
  '................',
  '................',
]);

const VOID_BOOTS = makeGrid([
  '................',
  '................',
  '.....KKKK.......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '.....KKKK.......',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const VOID_COMPASS = makeGrid([
  '................',
  '................',
  '.....KKKK.......',
  '....KkKKkK......',
  '...KkKYYKkK.....',
  '...KKKYYKKK.....',
  '..KkKKYYKKkK....',
  '..KkKKYYKKkK....',
  '..KkKKYYKKkK....',
  '..KkKKYYKKkK....',
  '...KKKYYKKK.....',
  '...KkKYYKkK.....',
  '....KkKKkK......',
  '.....KKKK.......',
  '................',
  '................',
]);

const VOID_ANCHOR = makeGrid([
  '................',
  '......KK........',
  '.....KYYK.......',
  '......KK........',
  '.....KKKK.......',
  '.....KYYK.......',
  '.....KYYK.......',
  '.....KYYK.......',
  '.....KYYK.......',
  '.....KYYK.......',
  '.....KYYK.......',
  '..K..KYYK..K....',
  '..KKKKKKKKKK....',
  '..K..KKKK..K....',
  '................',
  '................',
]);

const VOID_LANTERN = makeGrid([
  '................',
  '.....KKKK.......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '....KYYYYK......',
  '.....KKKK.......',
  '......KK........',
  '.....KKKK.......',
  '................',
  '................',
  '................',
  '................',
]);

const VOID_DASH = makeGrid([
  '................',
  '................',
  '................',
  '................',
  '........K.......',
  '.......KK.......',
  '......KYK.......',
  '.....KYYK.......',
  '....KYYYK.......',
  '...KYYYYK.......',
  '..KYYYYK........',
  '.KYYYYK.........',
  'KYYYYK..........',
  'KYYK...........',
  'KK.............',
  '................',
]);

// ═══════════════════════════════════════════════════════════════════════════
// 3. ARMOR LAYER TEXTURES (64x32) — procedural dark plates with cyan seams
// ═══════════════════════════════════════════════════════════════════════════

function armorLayer(seed) {
  const rows = [];
  let s = seed >>> 0;
  const rnd = () => {
    s = (s * 1664525 + 1013904223) >>> 0;
    return s / 4294967296;
  };
  for (let y = 0; y < 32; y++) {
    let row = '';
    for (let x = 0; x < 64; x++) {
      const isSeam = (x % 16 === 0 || y % 8 === 0) && rnd() < 0.5;
      const isPlate = (x + y) % 7 < 2;
      if (isSeam) row += 'Y';
      else if (isPlate) row += 'k';
      else row += 'K';
    }
    rows.push(row);
  }
  return rows;
}

// ═══════════════════════════════════════════════════════════════════════════
// 4. WRITE EVERYTHING
// ═══════════════════════════════════════════════════════════════════════════

const blockTextures = {
  void_slate: VOID_SLATE,
  void_gravel: VOID_GRAVEL,
  void_soil: VOID_SOIL,
  void_glass: VOID_GLASS,
  void_brick: VOID_BRICK,
  void_lamp: VOID_LAMP,
  void_ore: VOID_ORE,
  void_spire: VOID_SPIRE,
  void_weave: VOID_WEAVE,
  umbral_stone: UMBRAL_STONE,
  voidstone: VOIDSTONE,
  void_crystal: VOID_CRYSTAL,
  umbral_grass: UMBRAL_GRASS,
  void_fern: VOID_FERN,
};

const itemTextures = {
  void_ingot: VOID_INGOT,
  void_nugget: VOID_NUGGET,
  void_gem: VOID_GEM,
  umbral_shard: UMBRAL_SHARD,
  void_core: VOID_CORE,
  void_sword: VOID_SWORD,
  void_pickaxe: VOID_PICKAXE,
  void_axe: VOID_AXE,
  void_shovel: VOID_SHOVEL,
  void_hoe: VOID_HOE,
  void_helmet: VOID_HELMET,
  void_chestplate: VOID_CHESTPLATE,
  void_leggings: VOID_LEGGINGS,
  void_boots: VOID_BOOTS,
  void_compass: VOID_COMPASS,
  void_anchor: VOID_ANCHOR,
  void_lantern: VOID_LANTERN,
  void_dash: VOID_DASH,
};

let count = 0;
for (const [name, grid] of Object.entries(blockTextures)) {
  writePng(`${ASSETS}/textures/block/${name}.png`, grid);
  count++;
}
for (const [name, grid] of Object.entries(itemTextures)) {
  writePng(`${ASSETS}/textures/item/${name}.png`, grid);
  count++;
}
writePng(`${ASSETS}/textures/models/armor/void_layer_1.png`, armorLayer(0xC0FFEE));
count++;
writePng(`${ASSETS}/textures/models/armor/void_layer_2.png`, armorLayer(0xDEADBEE));
count++;

// ── Checked-in blockstates + models for the slab/stairs/wall ──
const brick = 'endesium:block/void_brick';

writeJson(`${ASSETS}/blockstates/void_brick_slab.json`, {
  variants: {
    'type=bottom': { model: 'endesium:block/void_brick_slab' },
    'type=double': { model: 'endesium:block/void_brick' },
    'type=top': { model: 'endesium:block/void_brick_slab_top' },
  },
});
writeJson(`${ASSETS}/models/block/void_brick_slab.json`, {
  parent: 'minecraft:block/slab',
  textures: { bottom: brick, top: brick, side: brick },
});
writeJson(`${ASSETS}/models/block/void_brick_slab_top.json`, {
  parent: 'minecraft:block/slab_top',
  textures: { bottom: brick, top: brick, side: brick },
});
writeJson(`${ASSETS}/models/item/void_brick_slab.json`, {
  parent: 'endesium:block/void_brick_slab',
});

// Stairs blockstate (24 variants).
const stairVariants = {};
const shapes = ['straight', 'inner', 'outer'];
for (const facing of ['east', 'west', 'south', 'north']) {
  for (const half of ['bottom', 'top']) {
    for (const shape of shapes) {
      const y = { east: 0, west: 180, south: 90, north: 270 }[facing];
      const model = shape === 'straight'
        ? 'endesium:block/void_brick_stairs'
        : `endesium:block/void_brick_stairs_${shape}`;
      stairVariants[`facing=${facing},half=${half},shape=${shape},waterlogged=false`] = {
        model, y, uvlock: true,
      };
    }
  }
}
writeJson(`${ASSETS}/blockstates/void_brick_stairs.json`, { variants: stairVariants });
const stairTex = { bottom: brick, top: brick, side: brick };
writeJson(`${ASSETS}/models/block/void_brick_stairs.json`, {
  parent: 'minecraft:block/stairs', textures: stairTex,
});
writeJson(`${ASSETS}/models/block/void_brick_stairs_inner.json`, {
  parent: 'minecraft:block/inner_stairs', textures: stairTex,
});
writeJson(`${ASSETS}/models/block/void_brick_stairs_outer.json`, {
  parent: 'minecraft:block/outer_stairs', textures: stairTex,
});
writeJson(`${ASSETS}/models/item/void_brick_stairs.json`, {
  parent: 'endesium:block/void_brick_stairs',
});

// Wall blockstate (multipart).
const wallMultipart = [
  { when: { up: 'true' }, apply: { model: 'endesium:block/void_brick_wall_post' } },
  { when: { north: 'low' }, apply: { model: 'endesium:block/void_brick_wall_side', uvlock: true } },
  { when: { east: 'low' }, apply: { model: 'endesium:block/void_brick_wall_side', y: 90, uvlock: true } },
  { when: { south: 'low' }, apply: { model: 'endesium:block/void_brick_wall_side', y: 180, uvlock: true } },
  { when: { west: 'low' }, apply: { model: 'endesium:block/void_brick_wall_side', y: 270, uvlock: true } },
  { when: { north: 'tall' }, apply: { model: 'endesium:block/void_brick_wall_side_tall', uvlock: true } },
  { when: { east: 'tall' }, apply: { model: 'endesium:block/void_brick_wall_side_tall', y: 90, uvlock: true } },
  { when: { south: 'tall' }, apply: { model: 'endesium:block/void_brick_wall_side_tall', y: 180, uvlock: true } },
  { when: { west: 'tall' }, apply: { model: 'endesium:block/void_brick_wall_side_tall', y: 270, uvlock: true } },
];
writeJson(`${ASSETS}/blockstates/void_brick_wall.json`, { multipart: wallMultipart });
writeJson(`${ASSETS}/models/block/void_brick_wall_post.json`, {
  parent: 'minecraft:block/template_wall_post',
  textures: { wall: brick },
});
writeJson(`${ASSETS}/models/block/void_brick_wall_side.json`, {
  parent: 'minecraft:block/template_wall_side',
  textures: { wall: brick },
});
writeJson(`${ASSETS}/models/block/void_brick_wall_side_tall.json`, {
  parent: 'minecraft:block/template_wall_side_tall',
  textures: { wall: brick },
});
writeJson(`${ASSETS}/models/block/void_brick_wall_inventory.json`, {
  parent: 'minecraft:block/wall_inventory',
  textures: { wall: brick },
});
writeJson(`${ASSETS}/models/item/void_brick_wall.json`, {
  parent: 'endesium:block/void_brick_wall_inventory',
});

// ── Plant blockstates + models + item models ──
for (const plant of ['void_crystal', 'umbral_grass', 'void_fern']) {
  writeJson(`${ASSETS}/blockstates/${plant}.json`, {
    variants: { '': { model: `endesium:block/${plant}` } },
  });
  writeJson(`${ASSETS}/models/block/${plant}.json`, {
    parent: 'minecraft:block/cross',
    textures: { cross: `endesium:block/${plant}` },
  });
  writeJson(`${ASSETS}/models/item/${plant}.json`, {
    parent: 'minecraft:item/generated',
    textures: { layer0: `endesium:block/${plant}` },
  });
}

// ── Item models for the void materials/tools/armor/functional items ──
for (const name of Object.keys(itemTextures)) {
  writeJson(`${ASSETS}/models/item/${name}.json`, {
    parent: 'minecraft:item/generated',
    textures: { layer0: `endesium:item/${name}` },
  });
}

console.log(`Wrote ${count} textures + slab/stairs/wall + plant + item models to ${ASSETS}`);
