# Architecture

Endesium is a Fabric mod for Minecraft 1.21.1 on Java 21. It uses the official
Mojang mappings, Fabric API, and GeckoLib 4.9.2 for the Void Stalker. Mod ID is
`endesium`, package root `com.infernodude777.endesium`.

## Source split

- `src/main/java` — server/common code: worldgen, resonance, entities, blocks,
  items, registry, mixins.
- `src/client/java` — client-only code: renderers, models, particles, datagen.
- `src/main/resources` — assets (models, textures, sounds, particles, GeckoLib
  geometry/animations), data (worldgen, loot, advancements), `fabric.mod.json`,
  mixins config.
- `src/main/generated` — datagen output (blockstates, models, loot, recipes,
  language).

## Packages

| Package | Responsibility |
|---|---|
| `registry` | blocks, items, entities, sounds, particles, block entities, item groups |
| `block` | block + block-entity behavior (resonant mechanism, inscribed slate) |
| `item` | Void Shard, Resonance Lens, Resonance Token behavior |
| `entity` | Void Stalker entity and AI |
| `resonance` | `ResonanceSource`, `ResonanceType`, `ResonanceManager` |
| `world` | biome keys, holders, seeds, noise, features, layout/variant builders |
| `particle` | particle type registration |
| `mixin` | `TheEndBiomeSourceMixin`, `RandomStateMixin` |
| `client.entity` | GeckoLib renderer + model for the Void Stalker |
| `client.particle` | particle factories |
| `client.datagen` | language/loot/model/recipe providers |

## Registration

Registration happens in `Endesium` (the `ModInitializer`). Client-only
registration (renderers, particle factories) happens in `EndesiumClient`
(`ClientModInitializer`). Nothing client-only is referenced from common/server
code, so a dedicated server never loads rendering classes.

## Server authority

Gameplay state is server-authoritative. The Resonance Lens reports a qualitative
reading computed server-side; the client never learns exact source positions.
Mechanism activation and reward claiming persist in the mechanism block entity.
Particles and visuals are client-side and carry no gameplay state.

## Mixins

- `TheEndBiomeSourceMixin` — deterministic overwrite of `getNoiseBiome` to
  introduce Wastes/Wilds while preserving vanilla End behavior.
- `RandomStateMixin` — captures the world seed for deterministic biome
  assignment.

Mixin count is kept minimal; new behavior prefers registry/feature systems over
mixins wherever possible.

## Data flow

1. Server starts; `EndesiumWorldgenSeeds` captures the seed; `ModWorldgen`
   resolves the biome holders into `EndesiumBiomeHolders`.
2. Chunk generation calls `TheEndBiomeSourceMixin.getNoiseBiome`, which assigns
   vanilla/wastes/wilds deterministically.
3. Placed features decorate qualifying chunks (ruins, spire, vegetation).
4. Players interact with mechanisms; `ResonanceManager` updates block-entity
   state and fires advancements/particles.
5. On save, block entities persist; on load, they restore without duplicate
   rewards.
