# Code Walkthrough

A tour of the Endesium source tree, package by package, so a new contributor can
find where a given behavior lives without reading every file.

## Entry points

- `Endesium` (`ModInitializer`) — registers blocks, items, entities, sounds,
  particles, block entities, and item groups, and kicks off worldgen setup.
- `EndesiumClient` (`ClientModInitializer`) — registers the Void Stalker
  renderer and the particle factories. This class is client-only and is never
  loaded on a dedicated server.

## registry

- `ModBlocks` — every block, wired to `BlockItem`s where appropriate.
- `ModItems` — Void Shard, Resonance Lens, Resonance Token, spawn egg.
- `ModEntities` — the Void Stalker entity type.
- `ModSounds` — sound event constants.
- `ModBlockEntities` — the Resonant Mechanism block entity type.
- `ModItemGroups` — the single "Endesium" creative tab.

Registration order matters: block entities must register after their block, and
items after their dependencies. Keeping registration in one place avoids
ordering bugs.

## block

- Resonant Mechanism block + block entity: holds the dormant/active state, the
  activation that the Lens performs, and the reward-claim flag. Persisted via
  NBT so activation survives reload.
- Inscribed Slate: four symbol variants driven by a blockstate property, each
  with its own model.

## item

- `ProductionVoidShardItem` — plain item; its scarcity comes from loot and drop
  tables rather than item code.
- `ProductionResonanceLensItem` — right-click use queries `ResonanceManager` for
  the nearest source and shows a qualitative reading. Cooldown enforced here.
- `ResonanceTokenItem` — reward item.

## entity

- `ProductionVoidStalkerEntity` — the mob, its attributes, and the AI goal
  wiring for observe/approach/reposition/attack states. Attack damage is applied
  at the impact frame, not on anticipation.

## resonance

- `ResonanceType` — enum of signal kinds.
- `ResonanceSource` — a source of resonance (position + type).
- `ResonanceManager` — server-side registry of sources; answers "nearest source
  strength" for the Lens. No client-visible coordinates.

## world

- `EndesiumBiomes` — the two `ResourceKey`s.
- `EndesiumBiomeHolders` — static holder capture shared between worldgen paths.
- `EndesiumWorldgenSeeds` — captured world seed (seed 0 is valid).
- `EndesiumNoise` — deterministic noise used to split Wastes vs Wilds.
- `ModWorldgen` — resolves holders and registers configured/placed features.
- `EndesiumWorld` — biome definition helpers.
- `EndWastesFeature`, `ChorusWildsTerrainFeature`, `ChorusWildsVegetationFeature`
  — terrain/vegetation decoration.
- `EndRuinFeature`, `EndRuinLayouts`, `EndRuinVariant` — ruin family + variants.
- `ShatteredSpireFeature`, `ShatteredSpireBuilder` — the rare spire landmark.

## mixin

- `TheEndBiomeSourceMixin` — overwrites `getNoiseBiome` to add Wastes/Wilds while
  preserving vanilla End thresholds and the central island.
- `RandomStateMixin` — captures the world seed.

## client

- `client.entity` — GeckoLib renderer + model for the Void Stalker.
- `client.particle` — particle factories (ResonanceMoteParticle and variants).
- `client.datagen` — language, loot, model, and recipe providers feeding
  `src/main/generated`.
