// Generates item textures + models for the Endesium ecology drops and spawn
// eggs that were registered by the ecology milestone but shipped without
// assets. Uses the same grid + palette language as tools/gen_eco_textures.mjs.
import { deflateSync } from 'node:zlib';
import { writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');

const PALETTE = {
  '.': null,
  B: '#111116', K: '#26232B', k: '#34313A', b: '#1B1920',
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
  writeFileSync(path, png);
}

const makeGrid = (rows) => rows.map((r) => r.split(''));

// ---- 16x16 item icons (transparent background) ----
const DUST_CHITIN = makeGrid([
  '................',
  '................',
  '......sss.......',
  '.....sSSSs......',
  '....sSSSSSs.....',
  '....sSSsSSs.....',
  '....sSSSSSs.....',
  '.....sSSSs......',
  '......sss.......',
  '......sss.......',
  '.....sss........',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const WASTES_SEED_POD = makeGrid([
  '................',
  '................',
  '.......cc.......',
  '......cCCc......',
  '.....cCCCCc.....',
  '.....cCCCCc.....',
  '.....cCCCCc.....',
  '......cCCc......',
  '.......cc.......',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const STALKER_TENDRIL = makeGrid([
  '................',
  '................',
  '................',
  '..........u.....',
  '.........uVu....',
  '........uVVu....',
  '.......uVVu.....',
  '......uVVu......',
  '.....uVVu.......',
  '....uVVu........',
  '...uVVu.........',
  '..uVu...........',
  '.uu.............',
  '................',
  '................',
  '................',
]);

const CHORUS_EYE = makeGrid([
  '................',
  '................',
  '.......uu.......',
  '......uVVu......',
  '.....uVyyVu.....',
  '.....uVyyVu.....',
  '.....uVyyVu.....',
  '......uVVu......',
  '.......uu.......',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const VOID_MEMBRANE = makeGrid([
  '................',
  '................',
  '................',
  '................',
  '....KKKKKKKK....',
  '...KKKKKKKKKK...',
  '..KKKKKKKKKKKK..',
  '..KKKKKKKKKKKK..',
  '..KKKKKKKKKKKK..',
  '...KKKKKKKKKK...',
  '....KKKKKKKK....',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const HIGHLAND_FEATHER = makeGrid([
  '................',
  '................',
  '.........cc.....',
  '........ccCc....',
  '.......ccCCc....',
  '......ccCCc.....',
  '.....ccCCc......',
  '....ccCCc.......',
  '...ccCCc........',
  '..ccCCc.........',
  '..cCCc..........',
  '..Kk............',
  '................',
  '................',
  '................',
  '................',
]);

const VOID_SAP = makeGrid([
  '................',
  '................',
  '.......nn.......',
  '......nYYn......',
  '.....nYYYYn.....',
  '.....nYYYYn.....',
  '.....nYYYYn.....',
  '......nYYn......',
  '.......nn.......',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const MARSH_TENDRIL = makeGrid([
  '................',
  '................',
  '................',
  '..........n.....',
  '.........nYn....',
  '........nYYn....',
  '.......nYYn.....',
  '......nYYn......',
  '.....nYYn.......',
  '....nYYn........',
  '...nYYn.........',
  '..nYn...........',
  '.nn.............',
  '................',
  '................',
  '................',
]);

const CRAWLER_EYE = makeGrid([
  '................',
  '................',
  '.......nn.......',
  '......nYYn......',
  '.....nYyyYn.....',
  '.....nYyyYn.....',
  '.....nYyyYn.....',
  '......nYYn......',
  '.......nn.......',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const LUMEN_DUST = makeGrid([
  '................',
  '................',
  '................',
  '................',
  '......W.........',
  '........W.......',
  '....W....W......',
  '......W.........',
  '........W.......',
  '....W...........',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const LUMEN_WING = makeGrid([
  '................',
  '................',
  '................',
  '.....WWWW.......',
  '....WWWWWW......',
  '...WWWWWWWW.....',
  '...WWWWWWWW.....',
  '...WWWWWWWW.....',
  '....WWWWWW......',
  '.....WWWW.......',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const WRAITH_ASH = makeGrid([
  '................',
  '................',
  '.......MM.......',
  '......MMMM......',
  '.....MMMMMM.....',
  '.....MMMMMM.....',
  '.....MMMMMM.....',
  '......MMMM......',
  '.......MM.......',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const ASH_CORE = makeGrid([
  '................',
  '................',
  '.......MM.......',
  '......MKKM......',
  '.....MKKKKM.....',
  '.....MKKKKM.....',
  '.....MKKKKM.....',
  '......MKKM......',
  '.......MM.......',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const BURROWER_PLATE = makeGrid([
  '................',
  '................',
  '......KKKK......',
  '.....KVVVVK.....',
  '....KVVVVVVK....',
  '....KVVKVVVK....',
  '....KVVVVVVK....',
  '.....KVVVVK.....',
  '......KKKK......',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const CRYSTAL_CORE = makeGrid([
  '................',
  '................',
  '.......yy.......',
  '......yyyy......',
  '.....yyyyyy.....',
  '.....yyyyyy.....',
  '.....yyyyyy.....',
  '......yyyy......',
  '.......yy.......',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

const CRYSTAL_FANG = makeGrid([
  '................',
  '................',
  '................',
  '.........yy.....',
  '........yyyy....',
  '.......yyyyy....',
  '......yyyyy.....',
  '.....yyyyy......',
  '....yyyyy.......',
  '...yyyyy........',
  '..yyyyy.........',
  '..yyyy..........',
  '................',
  '................',
  '................',
  '................',
]);

const NULL_FRAGMENT = makeGrid([
  '................',
  '................',
  '................',
  '................',
  '.....KKKKKK.....',
  '....KKKKKKKK....',
  '...KKKKKKKKKK...',
  '...KKKKKKKKKK...',
  '...KKKKKKKKKK...',
  '....KKKKKKKK....',
  '.....KKKKKK.....',
  '................',
  '................',
  '................',
  '................',
  '................',
]);

// Spawn eggs: vanilla-style two-tone egg with speckles.
function spawnEgg(base, spots) {
  const g = [
    '................',
    '................',
    '......KKKK......',
    '.....KKKKKK.....',
    '....KKKKKKKK....',
    '...KKKKKKKKKK...',
    '...KKKKKKKKKK...',
    '...KKKKKKKKKK...',
    '...KKKKKKKKKK...',
    '...KKKKKKKKKK...',
    '....KKKKKKKK....',
    '.....KKKKKK.....',
    '......KKKK......',
    '................',
    '................',
    '................',
  ].map(r => r.split(''));
  // base fill
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    if (g[y][x] === 'K') g[y][x] = base;
  }
  // spots
  for (const [x, y] of spots) {
    if (x >= 0 && y >= 0 && x < 16 && y < 16) g[y][x] = spotsColor;
  }
  return g;
}

let spotsColor = 'K';
function egg(base, spots, spot = 'K') {
  spotsColor = spot;
  return spawnEgg(base, spots);
}

const EGGS = {
  dust_crawler: egg('s', [[3,5],[7,4],[11,6],[5,9],[9,10],[12,8]], 'k'),
  chorus_stalker: egg('V', [[4,5],[8,4],[11,6],[6,9],[10,10]], 'U'),
  void_ray: egg('k', [[4,4],[8,3],[12,5],[6,8],[10,9],[13,11]], 'Y'),
  marsh_crawler: egg('n', [[3,5],[7,4],[11,6],[5,9],[9,10]], 'B'),
  lumen_moth: egg('y', [[4,4],[8,3],[11,5],[6,8],[10,9]], 'W'),
  ash_wraith: egg('M', [[4,5],[8,4],[11,6],[6,9],[10,10]], 'g'),
  crystal_burrower: egg('u', [[3,5],[7,4],[11,6],[5,9],[9,10],[12,8]], 'V'),
  nullwalker: egg('B', [[4,4],[8,3],[12,5],[6,8],[10,9]], 'g'),
};

const ITEMS = {
  dust_chitin: DUST_CHITIN,
  wastes_seed_pod: WASTES_SEED_POD,
  stalker_tendril: STALKER_TENDRIL,
  chorus_eye: CHORUS_EYE,
  void_membrane: VOID_MEMBRANE,
  highland_feather: HIGHLAND_FEATHER,
  void_sap: VOID_SAP,
  marsh_tendril: MARSH_TENDRIL,
  crawler_eye: CRAWLER_EYE,
  lumen_dust: LUMEN_DUST,
  lumen_wing: LUMEN_WING,
  wraith_ash: WRAITH_ASH,
  ash_core: ASH_CORE,
  burrower_plate: BURROWER_PLATE,
  crystal_core: CRYSTAL_CORE,
  crystal_fang: CRYSTAL_FANG,
  null_fragment: NULL_FRAGMENT,
};

const itemTexDir = resolve(ROOT, 'src/main/resources/assets/endesium/textures/item');
const itemModelDir = resolve(ROOT, 'src/main/resources/assets/endesium/models/item');
mkdirSync(itemTexDir, { recursive: true });
mkdirSync(itemModelDir, { recursive: true });

for (const [name, grid] of Object.entries(ITEMS)) {
  writePng(resolve(itemTexDir, `${name}.png`), grid);
  writeFileSync(resolve(itemModelDir, `${name}.json`),
    JSON.stringify({ parent: 'minecraft:item/generated', textures: { layer0: `endesium:item/${name}` } }, null, 2) + '\n');
  console.log(`wrote ${name}.png + ${name}.json`);
}

for (const [name, grid] of Object.entries(EGGS)) {
  writePng(resolve(itemTexDir, `${name}_spawn_egg.png`), grid);
  writeFileSync(resolve(itemModelDir, `${name}_spawn_egg.json`),
    JSON.stringify({ parent: 'minecraft:item/template_spawn_egg' }, null, 2) + '\n');
  console.log(`wrote ${name}_spawn_egg.png + ${name}_spawn_egg.json`);
}
console.log('done');
