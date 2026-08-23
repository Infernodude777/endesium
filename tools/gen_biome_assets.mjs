// Generates the Endesium ecology-milestone assets directly: 26 block textures,
// 10 item textures, plus blockstate/model/item JSONs. Self-contained PNG writer
// (RGBA + zlib), no external pixel-art server dependency.
import { deflateSync } from 'node:zlib';
import { writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');

// Palette (docs/ENDESIUM_VISUAL_DESIGN.md + ecology additions).
const PALETTE = {
  '.': null,
  B: '#111116', K: '#26232B', k: '#34313A', b: '#1B1920',
  X: '#0E0D10', x: '#19171D',
  G: '#77747D', g: '#8A8791', h: '#57555E', H: '#3E3D44',
  C: '#D8D0B4', c: '#E9E2CC', s: '#B8B095', S: '#96907A', P: '#CFC9BD',
  D: '#312A3D', V: '#5E526E', v: '#6B5F7C', u: '#463D55', U: '#2A2436',
  L: '#C4BBCD', l: '#D8D2E0', w: '#9B90A6',
  Y: '#7EA7A6', y: '#9CC4C2', n: '#5E8280', R: '#A9E6DF',
  M: '#94647C', m: '#6E4A5C',
  A: '#C6A85A', a: '#D8BC6F', o: '#9A823F', W: '#F2F0E5',
  E: '#3F4A43', e: '#55645B', f: '#2B342F',
};

// ---- PNG writer ----
const CRC_TABLE = (() => {
  const t = new Int32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c;
  }
  return t;
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
  const h = grid.length, w = grid[0].length;
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
  ihdr[8] = 8; ihdr[9] = 6;
  writeFileSync(path, Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]),
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(raw)),
    chunk('IEND', Buffer.alloc(0)),
  ]));
}

// ---- helpers ----
function rng(seed) {
  let a = seed >>> 0;
  return () => {
    a |= 0; a = (a + 0x6D2B79F5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}
function grid() { return Array.from({ length: 16 }, () => Array(16).fill('.')); }
function set(g, x, y, ch) { if (x >= 0 && x < 16 && y >= 0 && y < 16) g[y][x] = ch; }
function rect(g, x0, y0, x1, y1, ch) { for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) set(g, x, y, ch); }
function line(g, x0, y0, x1, y1, ch) {
  const steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0), 1);
  for (let i = 0; i <= steps; i++) {
    const x = Math.round(x0 + ((x1 - x0) * i) / steps);
    const y = Math.round(y0 + ((y1 - y0) * i) / steps);
    set(g, x, y, ch);
  }
}
function scatter(g, rand, count, chars, x0 = 0, y0 = 0, x1 = 15, y1 = 15) {
  for (let i = 0; i < count; i++) {
    const x = x0 + Math.floor(rand() * (x1 - x0 + 1));
    const y = y0 + Math.floor(rand() * (y1 - y0 + 1));
    set(g, x, y, chars[Math.floor(rand() * chars.length)]);
  }
}

// Stone: horizontal value bands + flecks + optional strata/cracks + vein.
function stone(seed, bands, flecks, opts = {}) {
  const rand = rng(seed);
  const g = grid();
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) g[y][x] = bands[y];
  scatter(g, rand, opts.flecks ?? 60, flecks);
  if (opts.strata) {
    line(g, 0, 5, 15, 5, opts.strata, 1);
    line(g, 0, 12, 15, 12, opts.strata, 1);
  }
  if (opts.cracks) {
    line(g, 4, 1, 4, 7, opts.cracks, 1);
    line(g, 4, 7, 7, 9, opts.cracks, 1);
    line(g, 12, 3, 11, 9, opts.cracks, 1);
  }
  if (opts.vein) line(g, 1, 4, 12, 11, opts.vein, 1);
  if (opts.glints) for (const [x, y] of opts.glints) set(g, x, y, opts.glint ?? 'c');
  return g;
}

// Bark: vertical stripes + occasional knot.
function bark(seed, stripes, flecks) {
  const rand = rng(seed);
  const g = grid();
  for (let x = 0; x < 16; x++) for (let y = 0; y < 16; y++) g[y][x] = stripes[x];
  scatter(g, rand, 40, flecks);
  if (rand() < 0.6) {
    const kx = 3 + Math.floor(rand() * 10), ky = 3 + Math.floor(rand() * 10);
    rect(g, kx - 1, ky - 1, kx + 1, ky + 1, flecks[0]);
    set(g, kx, ky, flecks[flecks.length - 1]);
  }
  return g;
}

// Crystal: dark base + geometric shard facets + a bright edge.
function crystal(seed, base, facets, edge) {
  const rand = rng(seed);
  const g = grid();
  rect(g, 0, 0, 15, 15, base);
  scatter(g, rand, 40, [base, ...facets]);
  // three shards
  line(g, 6, 3, 6, 12, facets[0], 1);
  line(g, 7, 3, 7, 13, facets[1], 1);
  line(g, 8, 4, 8, 13, facets[0], 1);
  line(g, 3, 6, 5, 12, facets[1], 1);
  line(g, 4, 5, 6, 12, facets[0], 1);
  line(g, 11, 4, 13, 12, edge, 1);
  line(g, 12, 6, 14, 13, edge, 1);
  set(g, 7, 4, edge);
  set(g, 4, 5, edge);
  return g;
}

// Soil: soft base + speckle + a couple of clods.
function soil(seed, base, speckles) {
  const rand = rng(seed);
  const g = grid();
  rect(g, 0, 0, 15, 15, base);
  scatter(g, rand, 90, speckles);
  scatter(g, rand, 4, speckles.slice(0, 1), 2, 2, 13, 13);
  return g;
}

// Plant cross: stem + two leaves + a tip.
function plant(seed, stem, leaf, tip) {
  const g = grid();
  line(g, 8, 2, 8, 14, stem, 1);
  line(g, 8, 8, 5, 6, leaf, 1);
  line(g, 8, 8, 5, 7, leaf, 1);
  line(g, 8, 10, 11, 8, leaf, 1);
  line(g, 8, 10, 11, 9, leaf, 1);
  line(g, 8, 5, 7, 4, leaf, 1);
  set(g, 8, 1, tip);
  set(g, 8, 2, tip);
  return g;
}

const bands = (top, mid, dark) => [dark, dark, top, top, top, mid, mid, mid, mid, mid, mid, mid, mid, top, top, dark];

// ---- Blocks ----
const BLOCKS = [];
const CUBE = (id, gen) => BLOCKS.push({ id, kind: 'cube', gen });
const PLANT = (id, gen) => BLOCKS.push({ id, kind: 'plant', gen });

// End Wastes
CUBE('wastes_stone', () => stone(0x11, bands('S', 's', 'S'), ['S', 's', 'C', 'G'], { strata: 'h', glints: [[11, 3], [4, 12]] }));
CUBE('wastes_gravel', () => soil(0x12, 'S', ['s', 'C', 'G', 'h']));
PLANT('dust_reed', () => plant(0x13, 'S', 's', 'C'));
PLANT('void_grass', () => plant(0x14, 'K', 'k', 'U'));

// Chorus Wilds
CUBE('elder_chorus_wood', () => stone(0x21, bands('v', 'V', 'u'), ['V', 'u', 'v', 'D'], { strata: 'U', glints: [[7, 7]] }));
CUBE('elder_chorus_bark', () => bark(0x22, ['U', 'u', 'V', 'V', 'v', 'v', 'V', 'V', 'u', 'U', 'u', 'V', 'v', 'v', 'V', 'u'], ['U', 'V', 'v']));
CUBE('chorus_root', () => bark(0x23, ['D', 'u', 'V', 'u', 'D', 'u', 'V', 'v', 'V', 'u', 'D', 'u', 'V', 'u', 'D', 'u'], ['D', 'V', 'U']));
CUBE('chorus_moss', () => soil(0x24, 'V', ['v', 'u', 'L', 'D']));
CUBE('hollow_chorus_wood', () => stone(0x25, bands('V', 'u', 'D'), ['V', 'u', 'D'], { cracks: 'B', vein: 'U' }));

// Shattered Highlands
CUBE('highland_stone', () => stone(0x31, bands('g', 'G', 'h'), ['G', 'g', 'h', 'H', 'C'], { strata: 'H', cracks: 'H' }));
CUBE('highland_slate', () => stone(0x32, bands('h', 'H', 'H'), ['H', 'h', 'G', 'B'], { strata: 'B', vein: 'V' }));

// Void Marshes
CUBE('void_marsh_soil', () => soil(0x41, 'U', ['u', 'D', 'E', 'V']));
PLANT('void_reed', () => plant(0x42, 'u', 'V', 'L'));
CUBE('marsh_moss', () => soil(0x43, 'e', ['E', 'f', 'V', 'u']));

// Luminous Groves
CUBE('lumen_stone', () => stone(0x51, bands('Y', 'n', 'K'), ['Y', 'n', 'K', 'y'], { vein: 'R', glints: [[6, 5], [10, 10], [13, 7]], glint: 'R' }));
PLANT('lumen_moss', () => plant(0x52, 'n', 'Y', 'R'));
PLANT('lumen_bloom', () => plant(0x53, 'Y', 'n', 'R'));

// Ashen Expanse
CUBE('ash_stone', () => stone(0x61, bands('h', 'H', 'H'), ['H', 'h', 'G', 'X'], { strata: 'B', vein: 'K' }));
CUBE('ashen_soil', () => soil(0x62, 'H', ['h', 'G', 'X', 'K']));

// Crystal Barrens
CUBE('crystal_shard_block', () => crystal(0x71, 'K', ['w', 'L', 'l'], 'c'));
CUBE('dark_crystal_block', () => crystal(0x72, 'b', ['V', 'u', 'U'], 'Y'));
CUBE('pale_crystal_block', () => crystal(0x73, 'k', ['l', 'L', 'W'], 'R'));
PLANT('crystal_cluster', () => plant(0x74, 'L', 'l', 'R'));

// Deep / common
CUBE('resonant_basalt', () => stone(0x81, bands('K', 'k', 'b'), ['K', 'k', 'b', 'V'], { vein: 'Y', glints: [[5, 6], [12, 11]], glint: 'y' }));
CUBE('end_clay', () => soil(0x82, 'U', ['u', 'D', 'V', 'L']));
CUBE('voidstone', () => stone(0x83, bands('b', 'K', 'B'), ['b', 'K', 'B', 'X'], { cracks: 'B', vein: 'V' }));

// ---- Items (hand-authored 16x16) ----
const ITEMS = [];
function item(id, rows) { ITEMS.push({ id, grid: rows.map((r) => r.split('')) }); }

item('wastes_compass', [
  '................', '.....kkkkk......', '....kKKKKKk.....', '...kKuuuuuKk....', '..kKuYYYYYuKk...', '..kKuYLLLYuKk...',
  '..kKuYL.ALYuKk...', '..kKuYL.ALYuKk...', '..kKuYLLLYuKk...', '..kKuYYYYYuKk...', '...kKuuuuuKk....', '....kKKKKKk.....',
  '.....kkkkk......', '................', '................', '................',
]);
item('highland_grappler', [
  '................', '.....kk.........', '....kVVk........', '....kVVk........', '...kVVVk........', '...kVVVVk.......',
  '..kVVVVVVk......', '..kDVVVVk.......', '...kDVVVk.......', '....kDVVk.......', '.....kDVk.......', '.....kVk........',
  '....kVk.........', '...kA.k.........', '..kA...........', '..k............',
]);
item('lumen_lantern', [
  '................', '......kkk.......', '.....kYYk.......', '....kYYYYk......', '...kYYyYYk......', '...kYRRyYk......',
  '...kYyyyYk......', '....kYYYk.......', '.....kkk........', '......k.........', '......K.........', '......K.........',
  '.....kKk........', '.....kKk........', '................', '................',
]);
item('void_filter', [
  '................', '......kkkk......', '.....kYYYYk.....', '....kYyRRyYk....', '...kYyRRRRyYk...', '...kYYyRRyYYk...',
  '...kYYYYYYYYk...', '....kYYYYYYk....', '.....kkkkkk.....', '................', '................', '................',
  '................', '................', '................', '................',
]);
item('crystal_resonator', [
  '................', '......kkk.......', '.....kLklk......', '....kLLklLk.....', '...kLlRRllLk....', '...kLlRRllLk....',
  '...kLLlllLLk....', '....kLLLk......', '.....kkk.......', '......kk........', '......k.........', '......k.........',
  '.....kkk........', '................', '................', '................',
]);
item('ash_sifter', [
  '................', '.....HHHHHH.....', '....HhhhhhhH....', '....HhhhhhhH....', '...HhhhhhhhhH...', '...HhhhhhhhhH...',
  '...HhhhhhhhhH...', '....HhhhhhhH....', '....HHHHHHHH....', '......k.........', '......k.........', '.....kKk........',
  '.....kKk........', '................', '................', '................',
]);
item('chorus_pruner', [
  '................', '.....kVk.kVk....', '....kVVk.kVVk...', '...kVVk...kVVk..', '..kVVk.....kVVk.', '..kVVk.....kVVk.',
  '...kVk.......kVk', '....k........k..', '.....kk....kk...', '......kkkkk.....', '................', '................',
  '................', '................', '................', '................',
]);
item('archive_key', [
  '................', '......kkkk......', '.....kAAAAk.....', '.....kAAYAk.....', '.....kAAYAk.....', '.....kAAAAk.....',
  '......kYYk......', '.......k........', '.......k........', '.......kk.......', '.......k........', '......k.........',
  '......k.........', '................', '................', '................',
]);
item('void_flare', [
  '................', '......kkk.......', '.....kRRRk......', '....kRRRRRk.....', '...kRRyRyRRk....', '...kRRyRyRRk....',
  '...kRRyRyRRk....', '...kRRRRRRRk....', '....kRRRRRk.....', '.....kRRRk......', '......kkk.......', '......k.........',
  '......k.........', '.....kKk........', '.....kKk........', '................',
]);
item('end_cartographer', [
  '................', '...ooooo........', '..oAAAAAo.......', '..oALALAAo......', '..oALALAAo......', '..oALALAAo......',
  '..oAAAAAAo......', '...ooooo........', '...oAAAAo.......', '..oAYYAYAo......', '..oAYYAYAo......', '..oAAAAAAo......',
  '...ooooo........', '................', '................', '................',
]);

// ---- write ----
const TEX = resolve(ROOT, 'src/main/resources/assets/endesium/textures');
const BS = resolve(ROOT, 'src/main/resources/assets/endesium/blockstates');
const BM = resolve(ROOT, 'src/main/resources/assets/endesium/models/block');
const IM = resolve(ROOT, 'src/main/resources/assets/endesium/models/item');
for (const d of [TEX + '/block', TEX + '/item', BS, BM, IM]) mkdirSync(d, { recursive: true });

for (const b of BLOCKS) {
  writePng(resolve(TEX, `block/${b.id}.png`), b.gen());
  if (b.kind === 'cube') {
    writeFileSync(resolve(BS, `${b.id}.json`), JSON.stringify({ variants: { '': { model: `endesium:block/${b.id}` } } }, null, 2) + '\n');
    writeFileSync(resolve(BM, `${b.id}.json`), JSON.stringify({ parent: 'minecraft:block/cube_all', textures: { all: `endesium:block/${b.id}` } }, null, 2) + '\n');
    writeFileSync(resolve(IM, `${b.id}.json`), JSON.stringify({ parent: `endesium:block/${b.id}` }, null, 2) + '\n');
  } else {
    writeFileSync(resolve(BS, `${b.id}.json`), JSON.stringify({ variants: { '': { model: `endesium:block/${b.id}` } } }, null, 2) + '\n');
    writeFileSync(resolve(BM, `${b.id}.json`), JSON.stringify({ parent: 'minecraft:block/cross', textures: { cross: `endesium:block/${b.id}` } }, null, 2) + '\n');
    writeFileSync(resolve(IM, `${b.id}.json`), JSON.stringify({ parent: 'minecraft:item/generated', textures: { layer0: `endesium:block/${b.id}` } }, null, 2) + '\n');
  }
}
for (const it of ITEMS) {
  writePng(resolve(TEX, `item/${it.id}.png`), it.grid);
  writeFileSync(resolve(IM, `${it.id}.json`), JSON.stringify({ parent: 'minecraft:item/generated', textures: { layer0: `endesium:item/${it.id}` } }, null, 2) + '\n');
}

console.log(`wrote ${BLOCKS.length} block assets and ${ITEMS.length} item assets`);
