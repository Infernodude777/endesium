# Endesium Architecture Summary

This document gives a high-level tour of the Endesium codebase for developers
new to the project.

## Source sets

Endesium uses two source sets:

- `src/main/java` — common code: blocks, items, entities, worldgen, resonance
  systems, and mixins that run on both sides.
- `src/client/java` — client-only code: rendering, screens, particles, and the
  Fabric datagen providers.

## Package layout

- `com.infernodude777.endesium` — the mod entrypoint (`Endesium`).
- `registry` — `ModItems` and `ModBlocks` register all content and expose
  static fields used everywhere else.
- `block` — custom block classes (AshenCrustBlock, EndPlantBlock,
  InscribedSlateBlock, ResonantMechanismBlock, VoidGlassBlock, VoidOreBlock).
- `item` — custom item classes, including the ten biome utility items, the
  Void tool/armor materials, and the guidebook item.
- `entity` — the nine mobs (AshWraith, ChorusStalker, CrystalBurrower,
  DustCrawler, LumenMoth, MarshCrawler, Nullwalker, VoidRay, VoidStalker).
- `dragon` — the post-dragon fight controller, loot, and Resonant Wings
  passives.
- `resonance` — the Resonance system: sources, types, and the manager that
  tracks signals across the world.
- `state` — post-dragon events and persistent state.
- `world` — biome, terrain, and structure features.
- `mixins` — mixins into vanilla classes (EndDragonFight, EnderDragon,
  LivingEntity, RandomState, TheEndBiomeSource).
- `client.datagen` — Fabric datagen providers for recipes, loot tables,
  models, and language.
- `client.screen` — the guidebook screen and content.

## Data flow

Content is registered in `ModItems`/`ModBlocks` and referenced by datagen
providers and raw data files. Recipes and advancements added as raw JSON in
`src/main/resources/data/endesium/` load alongside datagen output. The
resonance system drives the Echo Compass and the guidebook's narrative.

## Build

Fabric Loader >= 0.19.3, Minecraft 1.21.1, Java >= 21, Fabric API
0.116.15+1.21.1, GeckoLib >= 4.9.2. Official mappings. See `build.gradle`.
