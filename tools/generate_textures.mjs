// Generates the improved Endesium 16x16 texture set by driving the DogSprite
// MCP pixel-art server (tools/DogSprite/mcp-server). Items and plants are
// hand-authored pixel grids; blocks are built procedurally with value
// gradients, seeded noise, veins, and flecks in the Minecraft style. PNGs are
// exported to tools/DogSprite/mcp-server/output/.
import { spawn } from 'node:child_process';
import { mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const SERVER = resolve(__dirname, 'DogSprite/mcp-server/dist/index.js');
const OUTPUT = resolve(__dirname, 'DogSprite/mcp-server/output');

// ---- Endesium palette (docs/ENDESIUM_VISUAL_DESIGN.md) with gradient ramps ----
const PALETTE = {
  '.': null,
  B: '#111116', K: '#26232B', k: '#34313A', b: '#1B1920',
  G: '#77747D', g: '#8A8791', h: '#57555E', H: '#3E3D44',
  C: '#D8D0B4', c: '#E9E2CC', s: '#B8B095', S: '#96907A',
  D: '#312A3D', V: '#5E526E', v: '#6B5F7C', u: '#463D55', U: '#2A2436',
  L: '#C4BBCD', l: '#D8D2E0', w: '#9B90A6',
  Y: '#7EA7A6', y: '#9CC4C2', n: '#5E8280',
  R: '#A9E6DF', M: '#94647C', m: '#6E4A5C',
  A: '#C6A85A', a: '#D8BC6F', o: '#9A823F', W: '#F2F0E5',
};

// ---- deterministic RNG (mulberry32) ----
function rng(seed) {
  let a = seed >>> 0;
  return () => {
    a |= 0; a = (a + 0x6D2B79F5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

// ---- tiny grid builder ----
function makeGrid() {
  return Array.from({ length: 16 }, () => Array(16).fill('.'));
}
function set(g, x, y, ch) {
  if (x >= 0 && x < 16 && y >= 0 && y < 16 && PALETTE[ch]) g[y][x] = ch;
}
function row(g, y, str) {
  for (let x = 0; x < 16; x++) set(g, x, y, str[x] ?? '.');
}
function rect(g, x0, y0, x1, y1, ch) {
  for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) set(g, x, y, ch);
}
function line(g, x0, y0, x1, y1, ch, thick = 1) {
  const steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0), 1);
  for (let i = 0; i <= steps; i++) {
    const x = Math.round(x0 + ((x1 - x0) * i) / steps);
    const y = Math.round(y0 + ((y1 - y0) * i) / steps);
    for (let dx = 0; dx < thick; dx++) for (let dy = 0; dy < thick; dy++) set(g, x + dx, y + dy, ch);
  }
}
function scatter(g, rand, count, chars, x0 = 0, y0 = 0, x1 = 15, y1 = 15) {
  for (let i = 0; i < count; i++) {
    const x = x0 + Math.floor(rand() * (x1 - x0 + 1));
    const y = y0 + Math.floor(rand() * (y1 - y0 + 1));
    set(g, x, y, chars[Math.floor(rand() * chars.length)]);
  }
}

// ---- texture definitions ----
const TEXTURES = {};

// --- Items: hand-authored silhouettes ---

TEXTURES.void_shard = {
  kind: 'item',
  grid: (() => {
    const g = makeGrid();
    const art = [
      '................',
      '...........kk...',
      '..........kCCk..',
      '.........kCCCCCk',
      '........kVCCCCCk',
      '.......kVVCCCCVk',
      '......kVVVCCCVVk',
      '.....kVVVsCCCVVk',
      '.....kVVVsYCCVVk',
      '....kVVVsYCCCVVk',
      '...kDVVsYCCCVVk.',
      '...kDVVsYCCCVVk.',
      '..kDDVsYCCCVVk..',
      '..kDDsYCCCVVk...',
      '...kDsYCCCk.....',
      '....kYYCk.......',
    ];
    art.forEach((r, y) => row(g, y, r));
    set(g, 9, 12, 'A'); // ancient gold inclusion in the lower body
    set(g, 8, 5, 'c');  // bright facet catch-light
    return g;
  })(),
};

TEXTURES.resonance_lens = {
  kind: 'item',
  grid: (() => {
    const g = makeGrid();
    const art = [
      '................',
      '......kkkkkk....',
      '....kkKKKKKKkk..',
      '...kKKVVVVVVKKk.',
      '..kKVVVVVVVVVKk.',
      '..kVVVVVVLVVVVk.',
      '..kVVVLLyYYyLLVk',
      '..kVVLLyRRRyLLVk',
      '..kVVVLLyYYyLLVk',
      '..kVVVVVVLVVVVk.',
      '..kVVVVVVVVVVVk.',
      '..kKVVVVVVVVVKk.',
      '...kKKVVVVVKKk..',
      '....kkKKKKKKkk..',
      '......kkkkkk....',
      '................',
    ];
    art.forEach((r, y) => row(g, y, r));
    return g;
  })(),
};

// --- Plants: hand-authored crosses ---

TEXTURES.chorus_sprout = {
  kind: 'plant',
  grid: (() => {
    const g = makeGrid();
    const art = [
      '................',
      '................',
      '................',
      '......kk........',
      '.....kVVk.......',
      '....kVVVVk......',
      '....kVVVVk.k....',
      '.....kVkk.kVk...',
      '......kk.kVVk...',
      '.......kVVVk....',
      '......kVVVVk....',
      '.....kVVkkVk....',
      '....kVVk.kVk....',
      '....kVk..kk.....',
      '.....k..........',
      '................',
    ];
    art.forEach((r, y) => row(g, y, r));
    return g;
  })(),
};

TEXTURES.wild_tendril = {
  kind: 'plant',
  grid: (() => {
    const g = makeGrid();
    const art = [
      '................',
      '................',
      '................',
      '....k.......k...',
      '...kVk.....kVk..',
      '..kVVk.....kVVk.',
      '..kVVk.....kVVk.',
      '...kVk.....kVk..',
      '....k.......k...',
      '....V.......V...',
      '...kV......kV...',
      '..kVVk....kVVk..',
      '..kVVk....kVVk..',
      '...kVk....kVk...',
      '....k......k....',
      '................',
    ];
    art.forEach((r, y) => row(g, y, r));
    return g;
  })(),
};

// --- Blocks: procedural, Minecraft-style ---

// Resonant Slate: charcoal stone, vertical value gradient, violet-cyan seam,
// cream flecks. Light from the top.
TEXTURES.resonant_slate = {
  kind: 'block',
  grid: (() => {
    const rand = rng(0x51A7E);
    const g = makeGrid();
    // Wrapping value bands keep the tile seamless: light at the top and
    // bottom edges, a darker band through the middle where the seam sits.
    const bands = ['k', 'k', 'K', 'K', 'K', 'K', 'b', 'b', 'b', 'b', 'b', 'K', 'K', 'k', 'k', 'k'];
    for (let y = 0; y < 16; y++) {
      for (let x = 0; x < 16; x++) g[y][x] = bands[y];
    }
    scatter(g, rand, 45, ['K', 'k', 'b', 'K'], 0, 0, 15, 15);
    scatter(g, rand, 30, ['b', 'B', 'K'], 0, 6, 15, 15);
    // a clean diagonal resonance seam: dark violet border, violet body,
    // desaturated-cyan core with one brighter glint where it ends
    line(g, 1, 4, 11, 10, 'u', 1);
    line(g, 2, 4, 12, 10, 'V', 1);
    line(g, 3, 4, 12, 9, 'Y', 1);
    set(g, 12, 10, 'y');
    set(g, 10, 9, 'n');
    // rare mineral flecks
    set(g, 10, 2, 'C'); set(g, 4, 13, 'C'); set(g, 13, 14, 'c'); set(g, 1, 10, 's');
    return g;
  })(),
};

// End Gray: weathered gray stone with strata and a couple of cracks.
TEXTURES.end_gray = {
  kind: 'block',
  grid: (() => {
    const rand = rng(0xE6A4E);
    const g = makeGrid();
    // Wrapping value bands keep the tile seamless.
    const bands = ['g', 'g', 'g', 'G', 'G', 'G', 'G', 'G', 'h', 'h', 'h', 'h', 'G', 'g', 'g', 'g'];
    for (let y = 0; y < 16; y++) {
      for (let x = 0; x < 16; x++) g[y][x] = bands[y];
    }
    scatter(g, rand, 80, ['G', 'g', 'h', 'G', 'g', 'h', 'H'], 0, 0, 15, 15);
    // strata lines
    line(g, 0, 5, 15, 5, 'h', 1);
    line(g, 0, 11, 15, 11, 'H', 1);
    line(g, 0, 12, 15, 12, 'h', 1);
    // two cracks
    line(g, 4, 1, 4, 7, 'H', 1);
    line(g, 4, 7, 7, 9, 'H', 1);
    line(g, 12, 3, 11, 9, 'H', 1);
    line(g, 11, 9, 11, 14, 'H', 1);
    // dry cream dust in the deepest recesses
    scatter(g, rand, 6, ['C', 's'], 4, 8, 7, 14);
    return g;
  })(),
};

// Dormant Resonant Crystal: dark stone with an embedded lavender-cyan cluster.
TEXTURES.dormant_resonant_crystal = {
  kind: 'block',
  grid: (() => {
    const rand = rng(0xC0A5E);
    const g = makeGrid();
    rect(g, 0, 0, 15, 15, 'K');
    scatter(g, rand, 50, ['K', 'b', 'B', 'k'], 0, 0, 15, 15);
    // dark separators keep the three shards readable as separate crystals
    // central tall shard
    line(g, 6, 3, 6, 12, 'L', 1);
    line(g, 7, 3, 7, 13, 'l', 1);
    line(g, 8, 4, 8, 13, 'L', 1);
    line(g, 9, 5, 9, 12, 'w', 1);
    set(g, 7, 4, 'W');
    line(g, 5, 5, 5, 12, 'B', 1); // left edge separator
    // left shard, leaning left
    line(g, 3, 6, 5, 12, 'w', 1);
    line(g, 4, 5, 6, 12, 'L', 1);
    line(g, 5, 5, 7, 11, 'l', 1);
    set(g, 4, 5, 'c');
    // right shard with cyan edge, separated from the center
    line(g, 10, 5, 12, 12, 'n', 1);
    line(g, 11, 4, 13, 12, 'Y', 1);
    line(g, 12, 6, 14, 13, 'y', 1);
    line(g, 10, 4, 10, 11, 'B', 1); // right edge separator
    set(g, 11, 4, 'y');
    // buried base shadow
    line(g, 4, 13, 11, 13, 'U', 1);
    return g;
  })(),
};

// Resonant Mechanism: a dark plate with corner rivets, a central cyan seam,
// and an ancient gold contact, seen from above.
TEXTURES.end_ruin_mechanism = {
  kind: 'block',
  grid: (() => {
    const g = makeGrid();
    const art = [
      'HHHHHHHHHHHHHHHH',
      'HHGggGGggGGggGHH',
      'HGkkkkkkkkkkkkGH',
      'HGkkkkkkkkkkkkGH',
      'HGkkUUVkkkUUVkGH',
      'HGkkUVVkkkUVVkGH',
      'HGkkkkknnkkkkkGH',
      'HGkkkknYYnkkkkGH',
      'HGkkkknRRnkkkkGH',
      'HGkkkknYYnkkkkGH',
      'HGkkkkknnkkkkkGH',
      'HGkkkkkUUVkkkkGH',
      'HGkkkkkUVVkkkkGH',
      'HGkkkkkkkkkkkkGH',
      'HHGggGGggGGggGHH',
      'HHHHHHHHHHHHHHHH',
    ];
    art.forEach((r, y) => row(g, y, r));
    // ancient gold rivets at the four inner corners
    const rivets = [[2, 2], [13, 2], [2, 13], [13, 13]];
    for (const [rx, ry] of rivets) {
      set(g, rx, ry, 'o');
      set(g, rx + 1, ry, 'A');
      set(g, rx, ry + 1, 'A');
      set(g, rx + 1, ry + 1, 'a');
    }
    return g;
  })(),
};

// --- Discovery / landmark assets ---

// Shared dark slate base for the inscribed tiles.
function slateBase() {
  const g = makeGrid();
  const bands = ['k', 'k', 'K', 'K', 'K', 'K', 'b', 'b', 'b', 'b', 'K', 'K', 'K', 'k', 'k', 'k'];
  for (let y = 0; y < 16; y++) {
    for (let x = 0; x < 16; x++) g[y][x] = bands[y];
  }
  const rand = rng(0x51A7F);
  scatter(g, rand, 55, ['K', 'k', 'b', 'B', 'u'], 0, 0, 15, 15);
  return g;
}

// Inscribed Slate, ring motif: a carved ring with one surviving arm.
TEXTURES.inscribed_slate_0 = {
  kind: 'block',
  grid: (() => {
    const g = slateBase();
    const outer = [[7, 2], [11, 3], [13, 7], [11, 11], [7, 13], [3, 11], [2, 7], [3, 3]];
    for (let i = 0; i < outer.length; i++) {
      const [x0, y0] = outer[i];
      const [x1, y1] = outer[(i + 1) % outer.length];
      line(g, x0, y0, x1, y1, 'u', 1);
    }
    const inner = [[7, 4], [10, 5], [11, 8], [10, 11], [7, 12], [4, 11], [3, 8], [4, 5]];
    for (let i = 0; i < inner.length; i++) {
      const [x0, y0] = inner[i];
      const [x1, y1] = inner[(i + 1) % inner.length];
      line(g, x0, y0, x1, y1, 'V', 1);
    }
    // one broken arm hangs lower, hinting at the shattered ring
    line(g, 13, 7, 15, 9, 'u', 1);
    line(g, 11, 11, 13, 13, 'u', 1);
    set(g, 14, 9, 'B');
    // pale catch-light on the upper-left arc
    set(g, 5, 3, 'l'); set(g, 6, 2, 'l');
    return g;
  })(),
};

// Inscribed Slate, spire motif: a pointed tower with a ring at its crown.
TEXTURES.inscribed_slate_1 = {
  kind: 'block',
  grid: (() => {
    const g = slateBase();
    line(g, 7, 2, 7, 12, 'u', 1);
    line(g, 8, 2, 8, 12, 'u', 1);
    line(g, 6, 6, 6, 12, 'u', 1);
    line(g, 9, 6, 9, 12, 'u', 1);
    set(g, 7, 1, 'V'); set(g, 8, 1, 'V');
    // the ring motif around the crown
    line(g, 5, 4, 10, 4, 'V', 1);
    set(g, 4, 5, 'V'); set(g, 11, 5, 'V');
    // a thin cyan seam where the core shows through the broken face
    set(g, 8, 7, 'Y'); set(g, 7, 8, 'Y'); set(g, 8, 8, 'n'); set(g, 8, 9, 'Y');
    return g;
  })(),
};

// Inscribed Slate, eye motif: the watching eye above the ring.
TEXTURES.inscribed_slate_2 = {
  kind: 'block',
  grid: (() => {
    const g = slateBase();
    line(g, 3, 8, 7, 5, 'u', 1);
    line(g, 7, 5, 11, 8, 'u', 1);
    line(g, 11, 8, 7, 11, 'u', 1);
    line(g, 7, 11, 3, 8, 'u', 1);
    line(g, 4, 8, 7, 6, 'V', 1);
    line(g, 7, 6, 10, 8, 'V', 1);
    line(g, 10, 8, 7, 10, 'V', 1);
    line(g, 7, 10, 4, 8, 'V', 1);
    // cyan iris, dark pupil, one catch-light
    set(g, 6, 7, 'Y'); set(g, 7, 7, 'y'); set(g, 8, 7, 'Y');
    set(g, 6, 8, 'Y'); set(g, 7, 8, 'Y'); set(g, 8, 8, 'Y');
    set(g, 7, 7, 'n');
    set(g, 7, 8, 'n');
    set(g, 6, 7, 'R');
    return g;
  })(),
};

// Inscribed Slate, plain: grooved border, no motif.
TEXTURES.inscribed_slate_3 = {
  kind: 'block',
  grid: (() => {
    const g = slateBase();
    line(g, 1, 1, 14, 1, 'B', 1);
    line(g, 1, 14, 14, 14, 'B', 1);
    line(g, 1, 1, 1, 14, 'B', 1);
    line(g, 14, 1, 14, 14, 'B', 1);
    line(g, 2, 2, 13, 2, 'U', 1);
    line(g, 2, 13, 13, 13, 'U', 1);
    line(g, 2, 2, 2, 13, 'U', 1);
    line(g, 13, 2, 13, 13, 'U', 1);
    return g;
  })(),
};

// Resonant Pillar: a vertical column face with a glowing cyan core.
TEXTURES.resonant_pillar = {
  kind: 'block',
  grid: (() => {
    const rand = rng(0x51A8E);
    const g = makeGrid();
    const bands = ['b', 'b', 'K', 'K', 'k', 'k', 'k', 'k', 'k', 'k', 'K', 'K', 'b', 'b', 'b', 'b'];
    for (let y = 0; y < 16; y++) {
      for (let x = 0; x < 16; x++) g[y][x] = bands[x];
    }
    scatter(g, rand, 40, ['K', 'k', 'b', 'B'], 0, 0, 15, 15);
    // the luminous core seam down the middle
    line(g, 7, 0, 7, 15, 'n', 1);
    line(g, 8, 0, 8, 15, 'Y', 1);
    set(g, 7, 3, 'y'); set(g, 8, 3, 'R'); set(g, 7, 11, 'y'); set(g, 8, 11, 'R');
    // dark edge shading keeps the column rounded
    line(g, 1, 0, 1, 15, 'B', 1);
    line(g, 14, 0, 14, 15, 'B', 1);
    return g;
  })(),
};

// Cracked Spire Stone: dark weathered stone with violet fractures.
TEXTURES.cracked_spire_stone = {
  kind: 'block',
  grid: (() => {
    const rand = rng(0xC0A7E);
    const g = makeGrid();
    const bands = ['k', 'k', 'K', 'K', 'K', 'K', 'K', 'b', 'b', 'b', 'b', 'K', 'K', 'k', 'k', 'k'];
    for (let y = 0; y < 16; y++) {
      for (let x = 0; x < 16; x++) g[y][x] = bands[y];
    }
    scatter(g, rand, 90, ['K', 'k', 'b', 'B', 'K', 'k'], 0, 0, 15, 15);
    // a web of fractures, some tinted with dormant violet
    line(g, 2, 2, 6, 6, 'H', 1);
    line(g, 6, 6, 6, 11, 'H', 1);
    line(g, 6, 11, 10, 14, 'H', 1);
    line(g, 10, 2, 8, 7, 'H', 1);
    line(g, 8, 7, 12, 9, 'H', 1);
    line(g, 12, 9, 14, 8, 'H', 1);
    line(g, 3, 12, 7, 9, 'u', 1);
    line(g, 9, 4, 13, 3, 'u', 1);
    // sparse violet residue inside the deepest cracks
    scatter(g, rand, 4, ['V', 'u'], 2, 2, 13, 13);
    // dry cream dust
    set(g, 11, 14, 'C'); set(g, 3, 3, 's');
    return g;
  })(),
};

// Resonance Token: a small ancient-gold medallion with a cyan ring and eye.
TEXTURES.resonance_token = {
  kind: 'item',
  grid: (() => {
    const g = makeGrid();
    const art = [
      '................',
      '......ooooo.....',
      '....ooAAAAAoo...',
      '...oAAaaaaaAAo..',
      '..oAaaKKKKKaaAo.',
      '..oAaKKYyYKKaAo.',
      '.oAaaKYnnYKaaAo.',
      '.oAaaKYYyYKaaAo.',
      '.oAaaKYYyYKaaAo.',
      '.oAaaKYnnYKaaAo.',
      '..oAaKKYyYKKaAo.',
      '..oAaaKKKKKaaAo.',
      '...oAAaaaaaAAo..',
      '....ooAAAAAoo...',
      '......ooooo.....',
      '................',
    ];
    art.forEach((r, y) => row(g, y, r));
    set(g, 7, 6, 'n'); set(g, 7, 8, 'n'); set(g, 8, 7, 'n'); // pupil
    set(g, 6, 7, 'R'); // iris catch-light
    set(g, 4, 6, 'W'); // metal highlight
    return g;
  })(),
};

// ---- MCP client ----
function mcpClient() {
  const child = spawn('node', [SERVER], { stdio: ['pipe', 'pipe', 'pipe'] });
  let buffer = '';
  const pending = new Map();
  let id = 0;
  child.stdout.on('data', (chunk) => {
    buffer += chunk.toString();
    let idx;
    while ((idx = buffer.indexOf('\n')) >= 0) {
      const line = buffer.slice(0, idx).trim();
      buffer = buffer.slice(idx + 1);
      if (!line) continue;
      let msg;
      try { msg = JSON.parse(line); } catch { continue; }
      if (msg.id && pending.has(msg.id)) {
        const { resolve, reject } = pending.get(msg.id);
        pending.delete(msg.id);
        if (msg.error) reject(new Error(JSON.stringify(msg.error)));
        else resolve(msg.result);
      }
    }
  });
  child.stderr.on('data', () => {});
  function call(method, params) {
    const rid = ++id;
    return new Promise((resolve, reject) => {
      pending.set(rid, { resolve, reject });
      child.stdin.write(JSON.stringify({ jsonrpc: '2.0', id: rid, method, params }) + '\n');
    });
  }
  function notify(method, params) {
    child.stdin.write(JSON.stringify({ jsonrpc: '2.0', method, params }) + '\n');
  }
  return { call, notify, child };
}

async function tool(client, name, args) {
  const result = await client.call('tools/call', { name, arguments: args });
  if (result.isError) throw new Error(`tool ${name} failed: ${JSON.stringify(result.content)}`);
  return (result.content ?? []).map((c) => c.text).join('\n');
}

function gridToPixels(grid) {
  const pixels = [];
  grid.forEach((rowArr, y) => {
    for (let x = 0; x < rowArr.length; x++) {
      const color = PALETTE[rowArr[x]];
      if (color) pixels.push({ x, y, color });
    }
  });
  return pixels;
}

const results = [];
const client = mcpClient();
try {
  await client.call('initialize', {
    protocolVersion: '2024-11-05',
    capabilities: {},
    clientInfo: { name: 'endesium-texture-gen', version: '1.0.0' },
  });
  client.notify('notifications/initialized', {});
  await new Promise((r) => setTimeout(r, 300));

  mkdirSync(OUTPUT, { recursive: true });
  for (const [name, def] of Object.entries(TEXTURES)) {
    const created = await tool(client, 'create_sprite', { width: 16, height: 16, name });
    const brace = created.indexOf('{');
    if (brace < 0) throw new Error(`could not parse create_sprite result: ${created}`);
    const projectId = JSON.parse(created.slice(brace)).projectId;
    if (!projectId) throw new Error(`could not parse project id from: ${created}`);
    const pixels = gridToPixels(def.grid);
    await tool(client, 'set_pixels', { projectId, pixels });
    const exported = await tool(client, 'export_png', { projectId, scale: 1, filename: `${name}.png` });
    await tool(client, 'export_png', { projectId, scale: 8, filename: `${name}_prev.png` });
    results.push(exported.trim());
  }
} finally {
  client.child.kill();
}
console.log(results.join('\n'));
