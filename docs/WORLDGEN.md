# World Generation

Endesium extends the vanilla End rather than replacing the Dragon island,
vanilla gateways, or End Cities. The outer End is routed through ten
seed-deterministic Endesium biomes. The central island remains vanilla
`minecraft:the_end`.

## Biome selection

`TheEndBiomeSourceMixin` preserves the vanilla central-island and terrain-ring
checks, then assigns eligible outer highlands/midlands columns through
`EndesiumRegions.regionAt(seed, blockX, blockZ)`.

Current regions:

- End Wastes
- Chorus Wilds
- Shattered Highlands
- Void Marshes
- Luminous Groves
- Ashen Expanse
- Crystal Barrens
- Void Skirts
- Void Crown
- Umbral Reach

The seed is captured from the active world, including seed `0`. Biome holders
are resolved from the server registry at startup. A missing holder causes a
logged fallback rather than silently pretending the custom geography exists.

## Feature architecture

Production worldgen is data-driven through configured and placed Features:

- `BiomeTerrainFeature` — deterministic per-column relief and region geology.
- `BiomeVegetationFeature` — region-specific plants and ambient growth.
- `BiomeStructureFeature` — region-specific scenery, shrines, temples,
  archives, bridges, volcanoes, and landmarks.
- `EndRuinFeature` / `EndRuinLayouts` — Intact, Fractured, and Sunken ruins.
- `ShatteredSpireFeature` / `ShatteredSpireBuilder` — rare large landmark.
- `ResonantArchiveFeature` / `ResonantArchiveBuilder` — rare post-Dragon
  archive landmark that is generated sealed and awakened at runtime.
- `ResonantMonolithFeature` / `ResonantMonolithBuilder` — uncommon way-marker.
- `WildsSanctumFeature` — Chorus Wilds landmark.

The landmarks are deliberately hand-built Features rather than registered
Minecraft Structures. This keeps discovery organic, but means they do not
appear in `/locate structure`, have structure bounding boxes, or receive all of
Minecraft's structure lifecycle guarantees.

## Placement and safety

- End Ruins use a 3x3 chunk cell, a safe interior origin, and a broad support
  check before writing their 13x9 footprint.
- The Shattered Spire uses a 9x9 chunk cell, a centered 23x23 platform, and a
  23x23 support check.
- Resonant Archives use a 7x7 chunk cell, a centered 23x23 hall, and a broad
  support check.
- Resonant Monoliths use a 5x5 chunk cell and a 9x9 support check.
- Biome landmarks perform region-sized support checks and flatten only their
  local surface where the builder allows it.
- Rotations are deterministic per feature invocation and use thread-local
  builder state so parallel chunk generation does not leak orientation.

The enlarged Spire and Archive footprints cross chunk boundaries. Candidate
spacing and support checks reduce overlap and edge failures, but this remains a
known engineering risk until a large multi-seed region-file stress test proves
that cross-chunk Feature writes are safe in practice. A future migration to
registered Structures remains an option for the flagship landmarks.

## Compatibility

- Vanilla End thresholds, the central island, gateways, End Cities, chorus, and
  Endermen remain available.
- No fixed-coordinate world tick pass rewrites existing player terrain.
- Post-Dragon state changes resonance and runtime activation; it does not
  regenerate terrain or alter already-generated structures.
- Natural ecology spawns are biome-scoped in `ModEntities`, rather than merely
  registering entity types and spawn predicates.

## What to test

Test at least two seeds and inspect both fresh and already-generated worlds:

1. locate all ten biomes;
2. force-generate a large End grid;
3. inspect region files for landmark overlap and chunk-border corruption;
4. verify hostile/passive ecology populations;
5. confirm the central island and vanilla End Cities remain intact;
6. repeat after the Dragon transformation and after server restart.
