#!/usr/bin/env node
// Validates Endesium's asset tree: every item/block model, blockstate, texture,
// sound, particle, worldgen JSON, and GeckoLib geometry reference must resolve.
// Exits nonzero on the first broken reference so it can gate a release build.
//
//   node tools/validate_resources.mjs
//
// Namespace handling: references are "namespace:path". Only the "endesium"
// namespace is validated here. "minecraft:" references (vanilla model parents,
// vanilla sounds, vanilla particle textures) are trusted and skipped, because
// vanilla assets are validated by the game itself and are not part of this
// mod's asset tree. References with no namespace default to vanilla.

import { readFileSync, existsSync, readdirSync, statSync } from 'node:fs';
import { join, dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '..');
const ASSETS = join(ROOT, 'src', 'main', 'resources', 'assets', 'endesium');
const GENERATED_ASSETS = join(ROOT, 'src', 'main', 'generated', 'assets', 'endesium');
const DATA = join(ROOT, 'src', 'main', 'resources', 'data', 'endesium');

const problems = [];
const seen = new Set();

function error(msg) {
  problems.push(msg);
  console.error('\u2717 ' + msg);
}

function walk(dir, cb) {
  if (!existsSync(dir)) return;
  for (const name of readdirSync(dir)) {
    const p = join(dir, name);
    if (statSync(p).isDirectory()) walk(p, cb);
    else cb(p);
  }
}

function jsonOf(p) {
  try {
    return JSON.parse(readFileSync(p, 'utf8'));
  } catch (e) {
    error(`malformed JSON: ${p} (${e.message})`);
    return null;
  }
}

// Split "namespace:path" into [namespace, path]. A colonless string is an
// implicit vanilla reference and defaults to the "minecraft" namespace.
function splitRef(ref) {
  const i = ref.indexOf(':');
  if (i === -1) return ['minecraft', ref];
  return [ref.slice(0, i), ref.slice(i + 1)];
}

function isVanilla(ref) {
  return splitRef(ref)[0] === 'minecraft';
}

// Resolve an "endesium:block/..." or "endesium:item/..." model reference to a
// filesystem path inside the endesium asset tree. Returns null for vanilla refs.
function firstExisting(...paths) {
  return paths.find((path) => existsSync(path)) ?? paths[0];
}

function endesiumModelPath(ref) {
  const [ns, path] = splitRef(ref);
  if (ns !== 'endesium') return null;
  return firstExisting(
    join(ASSETS, 'models', path + '.json'),
    join(GENERATED_ASSETS, 'models', path + '.json'),
  );
}

function endesiumTexturePath(ref) {
  const [ns, path] = splitRef(ref);
  if (ns !== 'endesium') return null;
  return firstExisting(
    join(ASSETS, 'textures', path + '.png'),
    join(GENERATED_ASSETS, 'textures', path + '.png'),
  );
}

function modelReferences(json) {
  if (!json || typeof json !== 'object') return { parents: [], textures: [] };
  const parents = [];
  const textures = [];
  const nodes = Array.isArray(json) ? json : [json];
  for (const node of nodes) {
    if (!node || typeof node !== 'object') continue;
    if (typeof node.parent === 'string') parents.push(node.parent);
    if (node.textures && typeof node.textures === 'object') {
      for (const v of Object.values(node.textures)) {
        if (typeof v === 'string' && v.includes(':')) textures.push(v);
      }
    }
  }
  return { parents, textures };
}

function resolveModel(path, chain = []) {
  if (seen.has(path)) return;
  seen.add(path);
  if (chain.includes(path)) {
    error(`model parent cycle: ${chain.concat(path).join(' -> ')}`);
    return;
  }
  if (!existsSync(path)) {
    error(`missing model: ${path}`);
    return;
  }
  const json = jsonOf(path);
  const { parents, textures } = modelReferences(json);
  for (const ref of parents) {
    if (isVanilla(ref)) continue; // vanilla parent — trusted
    const mp = endesiumModelPath(ref);
    if (mp) resolveModel(mp, chain.concat(path));
  }
  for (const ref of textures) {
    if (isVanilla(ref)) continue; // vanilla texture — trusted
    const tp = endesiumTexturePath(ref);
    if (tp && !existsSync(tp)) error(`missing texture: ${ref}`);
  }
}

// 1. Models resolve, textures exist, no parent cycles.
for (const modelRoot of [join(ASSETS, 'models'), join(GENERATED_ASSETS, 'models')]) {
  walk(modelRoot, (p) => p.endsWith('.json') && resolveModel(p));
}

// 2. Blockstates reference existing model variants (endesium namespace only).
walk(join(ASSETS, 'blockstates'), (p) => {
  const json = jsonOf(p);
  if (!json) return;
  const refs = [];
  const collect = (obj) => {
    if (typeof obj !== 'object' || obj === null) return;
    if (obj.model) refs.push(obj.model);
    for (const k of Object.keys(obj)) collect(obj[k]);
  };
  collect(json);
  for (const m of refs) {
    if (isVanilla(m)) continue;
    const mp = endesiumModelPath(m);
    if (mp && !existsSync(mp)) error(`blockstate ${p} references missing model: ${m}`);
  }
});

// 3. sounds.json entries point at real .ogg files (endesium namespace only).
const sounds = jsonOf(join(ASSETS, 'sounds.json'));
if (sounds) {
  // These paths belonged to pre-1.13 resource layouts. Minecraft accepts the
  // JSON, but silently fails to play them in current versions, so catch them
  // here instead of waiting for a client smoke test to expose missing audio.
  const retiredVanillaPrefixes = [
    'minecraft:mob/enderdragon/',
    'minecraft:mob/warden/',
    'minecraft:random/explode',
    'minecraft:block/amethyst/',
  ];
  for (const [id, def] of Object.entries(sounds)) {
    for (const entry of (def.sounds ?? [])) {
      const name = typeof entry === 'string' ? entry : entry.name;
      if (!name) continue;
      if (retiredVanillaPrefixes.some((prefix) => name.startsWith(prefix))) {
        error(`sound event ${id} uses a retired vanilla sound path: ${name}`);
        continue;
      }
      if (isVanilla(name)) continue; // vanilla sound — trusted
      const [, path] = splitRef(name);
      if (!existsSync(join(ASSETS, 'sounds', path + '.ogg'))) {
        error(`sound event ${id} missing file: ${name}.ogg`);
      }
    }
  }
}

// 4. Particle definitions have textures under textures/particle (endesium only).
walk(join(ASSETS, 'particles'), (p) => {
  const json = jsonOf(p);
  if (!json) return;
  for (const tex of (json.textures ?? [])) {
    if (isVanilla(tex)) continue;
    const [, path] = splitRef(tex);
    if (!existsSync(join(ASSETS, 'textures', 'particle', path + '.png'))) {
      error(`particle ${p} missing texture: ${tex}.png`);
    }
  }
});

// 5. Every worldgen biome/feature/placed_feature JSON is valid.
for (const sub of ['biome', 'configured_feature', 'placed_feature']) {
  walk(join(DATA, 'worldgen', sub), (p) => p.endsWith('.json') && jsonOf(p));
}

// 6. GeckoLib geometry referenced by a model exists (endesium namespace only).
walk(join(ASSETS, 'models'), (p) => {
  const json = jsonOf(p);
  if (!json || !json.geometry) return;
  if (isVanilla(json.geometry)) return;
  const [, path] = splitRef(json.geometry);
  if (!existsSync(join(ASSETS, 'geo', path + '.json'))) {
    error(`model ${p} references missing geometry: ${json.geometry}`);
  }
});

if (problems.length) {
  console.error(`\n${problems.length} problem(s) found.`);
  process.exit(1);
}
console.log(`Endesium asset tree is valid (${seen.size} model/geometry files checked).`);
