# World Generation JSON Reference

Endesium worldgen lives under `data/endesium/worldgen`. This documents the
shape and intent of each file so contributors can tune generation without
reading the Java feature code.

## Biomes

### `biome/end_wastes.json`

- Identity: dead, fractured, sparse.
- `precipitation: none`, End-like temperature/downfall so it reads as part of
  the End.
- `spawners`/`spawn_costs` kept minimal; the Void Stalker spawns through its own
  rule, not a dense spawn table.
- `effects`: muted fog/sky, no harsh color grading.

### `biome/chorus_wilds.json`

- Identity: alive, organic, spreading.
- Same End base values as Wastes, but with different vegetation features and a
  slightly different palette so the two biomes are distinguishable on sight.

## Configured / placed features

| Feature | Type | Intent |
|---|---|---|
| `end_wastes_surface` | surface | fractured End stone formations |
| `end_ruin` | structure-like | ruin variants A/B/C |
| `shattered_spire` | structure-like | rare major landmark |
| `chorus_wilds_terrain` | terrain | rolling elevated terrain |
| `chorus_wilds_vegetation` | vegetation | `chorus_sprout`, `wild_tendril` |

## Rarity mechanics

- Ruins use a `rarity_filter` plus a chunk-coordinate gate, tuned to roughly 1
  in 7 qualifying chunks, with a terrain-support check.
- The spire uses a 7x7 cell gate (both chunk coordinates congruent to 0 mod 7)
  so it appears as a landmark rather than a common structure.

## Editing checklist

1. Keep JSON valid; the resource validator parses every worldgen JSON.
2. Prefer adjusting `rarity_filter` probability over adding more features.
3. Never hard-code absolute coordinates; generation must stay seed-dependent.
4. After editing, run `./gradlew runDatagen` and `node tools/validate_resources.mjs`,
   then `tools/qa_run.sh qa` to confirm biomes still locate.
