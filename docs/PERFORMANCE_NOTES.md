# Performance Notes

Endesium avoids per-tick biome assignment and post-generation world scans, but
its current worldgen is not free. The ten-biome pass, support checks, enlarged
landmarks, and ecology entities need to be measured rather than assumed cheap.

## Current costs

- `TheEndBiomeSourceMixin` performs one deterministic biome decision per biome
  sample and does not scan the world.
- `BiomeTerrainFeature` visits each column of its generating chunk once and may
  raise or carve a bounded number of blocks.
- `BiomeVegetationFeature` performs a small number of heightmap-based placement
  attempts per chunk.
- `BiomeStructureFeature` performs a support-footprint check and some builders
  call `flattenGround`, which samples a local square through the heightmap.
- Crystal Resonator scans a bounded 49x13x49-style sparse sample around the
  player on use, not every tick. The Null Quill's inscription lookup is bounded
  to a 25x11x25 cube and only runs on deliberate use; the Marsh Clapper checks
  a 9x9 footing window.
- Resonance sources are registered by ticking mechanism block entities every
  20 ticks; unloaded sources are skipped during sampling.
- The transformed Dragon's particle and attack effects are intentionally heavy
  during combat and need live profiling. Fracture geometry is now cached per
  Dragon state instead of rebuilt once per player pulse.
- Expanded biome Feature writes now skip protected portal/arena blocks and
  existing block entities, avoiding destructive overlap without adding a broad
  world scan.

## Feature density

- End Ruins use a 3x3 chunk cell plus a placed-feature rarity filter and support
  test.
- Shattered Spires use a 9x9 chunk cell and a 23x23 support test.
- Resonant Archives use a 7x7 chunk cell and a 23x23 support test.
- Resonant Monoliths use a 5x5 chunk cell and a 9x9 support test.
- Biome landmarks are gated by the shared placed-feature rarity filter and a
  region-sized support test.
- Vegetation is intentionally sparse; Chorus Wilds has rare elder growth rather
  than a solid forest.

## Known risks

The Spire and Archive builders now have 23x23 footprints, which cross chunk
boundaries even though the code is still implemented as Features. Candidate
spacing prevents neighboring landmarks from intentionally overlapping, but
cross-chunk writes and generation order require multi-seed region-file testing.
The support checks also trade visual safety for extra heightmap work and may
make large landmarks rarer than the design intends on fragmented islands.

## Measurement plan

Use a profiler or `/tick` while force-generating a large End grid and record:

- average and p95 chunk-generation time in each region;
- heightmap/support-check time for structure candidates;
- number of generated landmarks per 1,000 qualifying chunks;
- region-file writes touching neighboring chunks;
- entity count and AI time in populated ecology biomes;
- packet/particle volume near active mechanisms and during the transformed
  Dragon fight.

Do not lower density blindly. First identify whether the cost is heightmap
sampling, block writes, entity AI, or client particle rendering.
