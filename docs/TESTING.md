# Testing

Endesium is tested headlessly for world generation, resource integrity, and
server stability, plus manually in a client for visuals, audio, and feel. The
automation is deliberately small: a handful of Node/Java scripts plus Gradle.

## Acceptance gates

A change is considered done only when:

1. `./gradlew build` is green.
2. `./gradlew runDatagen` is green and produces no unexpected generated diffs.
3. `node tools/validate_resources.mjs` exits 0.
4. A fresh-world dedicated-server run locates `endesium:end_wastes` and
   `endesium:chorus_wilds` and places ruin + spire blocks.
5. The Echo Compass recipe and Resonant Bloom loot generate without errors, and
   a client resource reload reports no missing models or textures.
6. The post-Dragon state activates exactly once, persists across server
   restart, is idempotent on re-activation, and the Resonant Archive
   (`ARCHIVE`-variant mechanisms) generates in both biomes
   (`tools/qa_post_dragon.sh` covers the state; `tools/qa_archive_gen.sh`
   covers the archive generation).

## Build and datagen

```bash
./gradlew build
./gradlew runDatagen
```

Datagen writes committed output under `src/main/generated` (blockstates, models,
loot tables, recipes, language). Review `git status` after datagen to catch
unexpected churn.

## Resource validation

```bash
node tools/validate_resources.mjs
```

This validates model/blockstate/texture/sound/particle/worldgen/geometry
references within the `endesium` namespace and exits nonzero on the first broken
reference. Vanilla `minecraft:` references are intentionally skipped.

## Headless world-generation test

```bash
tools/qa_run.sh qa
```

The wrapper deletes the test world, starts the dedicated server with a known
seed, and pipes `tools/qa_server_test.sh`. The script locates both biomes,
summons a Void Stalker, gives the Void Shard and Resonance Lens, and teleports
across a wide grid to force generation. On-disk verification uses
`tools/parse_mca.mjs` and `tools/scan_region_blocks.mjs` to confirm the actual
saved biomes and feature blocks.

## What to look for in logs

```bash
grep -nE "The nearest|Could not find|ERROR|Exception|Invalid|missing" run/logs/latest.log
```

Treat every Endesium-related error as real even if the server keeps running.

## Manual client checks

The headless loop cannot judge visuals or audio. Manually verify:

- item/block textures are not black or missing,
- the Void Stalker model and animations load,
- particles render,
- sounds play without irritating repetition,
- the Resonance Lens gives a qualitative (not coordinate) reading.

## Post-Dragon state test

```bash
tools/qa_post_dragon.sh pd
```

Phase 1 boots a fresh world, locates a biome, and sets the transformation via
the dev command; phase 2 restarts the same world and confirms the state
persisted and re-activation is idempotent.

## Resonant Archive generation test

```bash
tools/qa_archive_gen.sh arc
```

Locates both biomes, force-loads a 15x15 chunk grid around each (`/forceload`
caps at 256 chunks per call and takes BLOCK coordinates), then scans the
decompressed `DIM1` region data for the raw `ARCHIVE` variant string. Note that
raw-string scans of compressed region files are meaningless; the script
decompresses each chunk first.

## Seeds

Regeneration is seed-dependent. Test at least two distinct seeds and compare
Wastes/Wilds distribution, ruin and spire placement, and transitions.
