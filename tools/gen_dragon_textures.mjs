// Generates Endesium Dragon-milestone textures: 16x16 item icons for the ten
// dragon materials, an upgraded Resonant Wings icon + worn wing texture, and
// the item model JSONs. All art is hand-authored pixel grids in the Endesium
// palette (charcoal/violet body, pale cyan resonance, muted bone).
import { deflateSync } from 'node:zlib';
import { writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');

const PALETTE = {
  '.': null,
  // charcoal / dark
  B: '#111116', K: '#26232B', k: '#34313A', b: '#1B1920',
  // bone / pale
  C: '#D8D0B4', c: '#E9E2CC', s: '#B8B095', S: '#96907A',
  // violet
  D: '#312A3D', V: '#5E526E', u: '#463D55', U: '#2A2436',
  // lavender
  L: '#C4BBCD', l: '#D8D2E0', w: '#9B90A6',
  // resonance cyan
  Y: '#7EA7A6', y: '#9CC4C2', n: '#5E8280', R: '#A9E6DF',
  // gold
  A: '#C6A85A', a: '#D8BC6F',
  // white
  W: '#F2F0E5',
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

// ---------------------------------------------------------------------------
// Item icons (16x16)
// ---------------------------------------------------------------------------

// Overlapping dragon scale plates: charcoal scales with a pale cyan rim.
const RESONANT_DRAGON_SCALE = makeGrid([
  '................',
  '................',
  '................',
  '.....KKKKKK.....',
  '....KKKKKKKK....',
  '...KKkkkkkkKK...',
  '..KKkkkkkkkkKK..',
  '..KkYYkYYkYYkK..',
  '..KkYYkYYkYYkK..',
  '..KKkkkkkkkkKK..',
  '...KKkkkkkkKK...',
  '....KKKKKKKK....',
  '.....KKKKKK.....',
  '.......KK.......',
  '................',
  '................',
]);

// A curved ancient bone shard with a pale tip.
const DRAGONBONE = makeGrid([
  '................',
  '.....LLLLLL.....',
  '....LLLLLLLL....',
  '...LLllllllLL...',
  '..LLllLLllllLL..',
  '.LLllLLLlllllLL.',
  '.LLllLLLlllllLL.',
  '.LLllLLLlllllLL.',
  '.LLllLLLlllllLL.',
  '..LLllLLLllllLL.',
  '...LLllLLlllLL..',
  '....LLllllLLL...',
  '.....LLlllLL....',
  '......LLLLLL....',
  '................',
  '................',
]);

// A sharp curved fang, ivory with a violet root.
const DRAGON_FANG = makeGrid([
  '................',
  '............UUU.',
  '...........UUUU.',
  '..........UUUUU.',
  '.........UUUUUU.',
  '........UUUllUU.',
  '.......UUllllUU.',
  '......UUlllllUU.',
  '.....UUllllllUU.',
  '....UUlllllllUU.',
  '...UUllllllllUU.',
  '..UUlllllllllUU.',
  '.UUllllllllllUU.',
  '.UUUUUUUUUUUUUU.',
  '................',
  '................',
]);

// A beating crystal heart: deep violet with a cyan core.
const DRAGON_HEART = makeGrid([
  '................',
  '....UUU..UUU....',
  '...UUUUU.UUUU...',
  '..UUUUUUUUUUUU..',
  '..UUUUUuUUUUUU..',
  '..UUUUYuuUUUUU..',
  '...UUUYuuuUUU...',
  '....UUuuuuUU....',
  '.....UuuuuuU....',
  '......uuuuu.....',
  '.....YuuuuuY....',
  '....YYuuuuuYY...',
  '....YYuuuuuYY...',
  '.....YYYYYY.....',
  '................',
  '................',
]);

// A droplet of condensed End energy with a pale swirling core.
const ENDER_ESSENCE = makeGrid([
  '................',
  '.......UUU......',
  '......UUUUU.....',
  '.....UUuUUUU....',
  '....UUuuuUUU....',
  '...UUuuuuuUUU...',
  '...UuYuuuuuUU...',
  '..UuYYuuuuuuUU..',
  '..UuYYYuuuuuUU..',
  '..UuuYYuuuuuUU..',
  '..UuuuuuuuuuUU..',
  '...UuuuuuuuUU...',
  '...UUuuuuuUUU...',
  '....UUUUUUUU....',
  '.....UUUUUU.....',
  '................',
]);

// A cyan glass shard with concentric sound rings.
const ECHO_SHARD = makeGrid([
  '......YY........',
  '.....YYYY.......',
  '....YYYYYY......',
  '...YYYYYYYY.....',
  '..YYYyYYyYY.....',
  '.YYYyYYYYyYY....',
  'YYYyYYYYYYyYY...',
  'YYyYYYYYYYYyY...',
  'YYyYYYYYYYYyY...',
  'YYyYYyYYyYYyY...',
  '.YYyYYyYYyYY....',
  '..YYyYYYYyYY....',
  '...YYyYYyYY.....',
  '....YYyYYY......',
  '.....YYY........',
  '......Y.........',
]);

// A dark pearl with a void swirl.
const VOID_PEARL = makeGrid([
  '................',
  '....VVVVVVV.....',
  '...VVuuuuuVV....',
  '..VVuuuuuuuVV...',
  '..VuuuuuuuuuV...',
  '.VuYYuuuuuuuuV..',
  '.VuYYYuuuuuuuV..',
  '.VuYYYuuuuuuuV..',
  '.VuuYYuuuuuuuV..',
  '.VuuuYYuuuuuuV..',
  '..VuuuuuuuuuV...',
  '..VVuuuuuuuVV...',
  '...VVVVVVVVV....',
  '................',
  '................',
  '................',
]);

// A coil of dark thread with a faint sheen.
const ABYSSAL_THREAD = makeGrid([
  '................',
  '.....BBBBBB.....',
  '....BBbBBBBB....',
  '...BBbBBBBbBB...',
  '..BBbBBBBbBBBB..',
  '..BBbBBBBbBBBB..',
  '...BBBBbBBBbB...',
  '....BBBBbBBB....',
  '.....BBbBBBB....',
  '....BBBbBBBBB...',
  '...BBbBBBBbBB...',
  '..BBbBBBBbBBBB..',
  '..BBBBBBbBBBB...',
  '....BBBBBBBB....',
  '................',
  '................',
]);

// A small mechanism core with a glowing resonance center.
const RESONANCE_CORE = makeGrid([
  '................',
  '.....kkkkkk.....',
  '....kkkYYkkk....',
  '...kkYYYYYYkk...',
  '...kYYYYYYYYk...',
  '..kkYyYYYYyYkk..',
  '..kkYyYYYYyYkk..',
  '..kkYYYYYYYYkk..',
  '..kkYYYYYYYYkk..',
  '...kYYYkkYYYk...',
  '...kkkYkkYkkk...',
  '....kkkkkkkk....',
  '.....kkkkkk.....',
  '................',
  '................',
  '................',
]);

// A torn tablet fragment with a glowing rune and a broken edge.
const ARCHIVE_FRAGMENT = makeGrid([
  '................',
  '..AAAAAAAAAAA...',
  '.AAAAAAAAAAAAA..',
  '.AAA......AAAA..',
  '.AA..YYY...AAA..',
  '.AA..YAY...AA...',
  '.AA..YYY...AA...',
  '.AA.......AA....',
  '.AA......AA.....',
  '.AAA....AA......',
  '.AAA...AA.......',
  '.AAAA.AA........',
  '.AAAAAAAAA......',
  '..AAAAAAA.......',
  '................',
  '................',
]);

// Upgraded Resonant Wings icon: folded wings with cyan veins.
const RESONANT_WINGS_ITEM = makeGrid([
  '................',
  '....KKKK........',
  '...KKKKKK.......',
  '..KKKkkKKK......',
  '..KKkkkkKKK.....',
  '.KKkkkkkkKKK....',
  '.KKkYYkkkKKK....',
  '.KKkYYkkkKKK....',
  '.KKkkYYkkKKK....',
  '.KKkkkYYkKKK....',
  '.KKkkkkkkKKK....',
  '..KKkkkkKKK.....',
  '..KKkkkKKKK.....',
  '...KKKKKKK......',
  '....KKKK........',
  '................',
]);

// ---------------------------------------------------------------------------
// Worn wing texture (64x32). ElytraModel reads both wings from x22..42, y0..20.
// A longer wing with a pale membrane and cyan resonance veins.
// ---------------------------------------------------------------------------

const WING = [
  '.KKKKKKKKKKKKKKK.',
  'KKkkkkkkkkkkkkKK.',
  'KkkkkkkkkkkkkkKK.',
  'KkkYYkkkkkkkkkKK.',
  'KkkYYkkkkkkkkKKK.',
  'KkkkkYYkkkkkkKKK.',
  'KkkkkYYkkkkkKKKK.',
  'KkkkkkkYYkkkKKKK.',
  'KkkkkkkYYkkKKKKK.',
  'KkkkkkkkkYYkKKKK.',
  'KkkkkkkkkkYYKKKK.',
  '.kkkkkkkkkkkKKKK.',
  '.kkkkkkkkkkKKKK..',
  '.kkkkkkkkkKKKK...',
  '.kkkkkkkkKKKK....',
  '.kkkkkkkKKKK.....',
  '..kkkkkKKKK......',
  '..kkkkKKKK.......',
  '...kkKKKK........',
  '...kkKKK.........',
];

const ENTITY = [];
for (let y = 0; y < 32; y++) {
  let row = '';
  for (let x = 0; x < 64; x++) {
    const wx = x - 22;
    const wy = y - 1;
    if (wx >= 0 && wx < 17 && wy >= 0 && wy < 20) {
      row += WING[wy][wx];
    } else {
      row += '.';
    }
  }
  ENTITY.push(row);
}

// ---------------------------------------------------------------------------
// Write everything
// ---------------------------------------------------------------------------

const ITEMS = {
  resonant_dragon_scale: RESONANT_DRAGON_SCALE,
  dragonbone: DRAGONBONE,
  dragon_fang: DRAGON_FANG,
  dragon_heart: DRAGON_HEART,
  ender_essence: ENDER_ESSENCE,
  echo_shard: ECHO_SHARD,
  void_pearl: VOID_PEARL,
  abyssal_thread: ABYSSAL_THREAD,
  resonance_core: RESONANCE_CORE,
  archive_fragment: ARCHIVE_FRAGMENT,
};

const itemDir = resolve(ROOT, 'src/main/resources/assets/endesium/textures/item');
const modelDir = resolve(ROOT, 'src/main/resources/assets/endesium/models/item');
const entityDir = resolve(ROOT, 'src/main/resources/assets/endesium/textures/entity');
mkdirSync(itemDir, { recursive: true });
mkdirSync(modelDir, { recursive: true });
mkdirSync(entityDir, { recursive: true });

for (const [id, grid] of Object.entries(ITEMS)) {
  writePng(resolve(itemDir, `${id}.png`), grid);
  const model = {
    parent: 'minecraft:item/generated',
    textures: { layer0: `endesium:item/${id}` },
  };
  writeFileSync(resolve(modelDir, `${id}.json`), JSON.stringify(model, null, 2));
  console.log(`wrote ${id}.png + ${id}.json`);
}

writePng(resolve(itemDir, 'resonant_wings.png'), RESONANT_WINGS_ITEM);
writePng(resolve(entityDir, 'resonant_wings.png'), ENTITY);
console.log('wrote resonant_wings.png (item) + resonant_wings.png (entity)');
