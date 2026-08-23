# Endesium Performance Notes

This document records the performance characteristics of Endesium and the
best practices for keeping the mod running smoothly.

## General expectations

Endesium adds ten biomes, nine mobs, and dozens of blocks and items to the End.
The additional worldgen and entities have a measurable but modest cost on
modern hardware. A machine that runs vanilla 1.21.1 comfortably will run
Endesium without major issues.

## Worldgen cost

The themed biome layout is implemented through a `TheEndBiomeSource` mixin.
Terrain features (EndRuinFeature, ShatteredSpireFeature, ResonantArchiveFeature,
ResonantMonolithFeature, WildsSanctumFeature, DragonArenaFeature) add structure
noise. To keep chunk generation reasonable:

- Structures are spaced apart and do not generate in every chunk.
- Plant blocks are lightweight and use the existing `EndPlantBlock`.
- Ashen Crust and Void Glass are simple transparent blocks.

## Entity cost

Nine mobs (AshWraith, ChorusStalker, CrystalBurrower, DustCrawler, LumenMoth,
MarshCrawler, Nullwalker, VoidRay, VoidStalker) use GeckoLib animations.
GeckoLib animation is efficient, but large numbers of animated mobs in one
area can add up. The mobs spawn at vanilla-style rates and despawn normally.

## Rendering cost

- Resonant particles (`ResonanceMoteParticle`) are lightweight point sprites.
- The guidebook screen is a static GUI with text; it renders only when open.
- The Sonic Boom handler and Resonant Wings passives add no per-frame cost
  unless active.

## Known hot spots

- The Luminous Groves' light levels (Lumen Stone emits light) can cause block
  light recalculation in denser areas; this is localized and bounded.
- The Void Skirts' many Void Glass blocks are transparent and slightly more
  expensive than opaque blocks; build with them sparingly.

## Recommendations

- Keep the render distance at 10-12 chunks for large End exploration.
- If using Sodium, Endesium's mixins are designed to be compatible; report
  any incompatibilities as issues.
- For a stress test, fly across each biome at render distance 12 and watch
  the frame time. See `docs/PERFORMANCE_NOTES.md` for baseline numbers.
