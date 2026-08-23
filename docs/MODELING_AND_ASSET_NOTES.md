# Modeling and Asset Notes

Working notes on how Endesium's entities and item art are built, and the lessons paid for the hard way. Read this before touching anything under `assets/endesium/geo` or spawning a new creature.

## Pipeline overview

1. Entities use **GeckoLib 4.9.2** (`software.bernie.geckolib`). Each mob needs four files plus registration:
   - `assets/endesium/geo/entity/<mob>.geo.json` - Bedrock geometry
   - `assets/endesium/animations/entity/<mob>.animation.json`
   - `assets/endesium/textures/entity/<mob>.png`
   - a `GeoModel` + `GeoEntityRenderer` pair under `src/client/java/.../client/entity/`, registered in `EndesiumClient`.
2. Server side: entity class (usually extends `Monster implements GeoEntity`), entry in `ModEntities` with dimensions and track range, attributes via `FabricDefaultAttributeRegistry` in `Endesium`, spawn placement rules, spawn egg in `ModItems`, lang via datagen, loot table JSON.
3. Animation names referenced by entity code MUST exist in the animation JSON. The controller plays by name; a missing name silently idles instead of crashing, which makes it easy to miss.

## Hard rules learned from bugs

- **Keyframes must fit inside `animation_length`.** Keyframes past the declared length cause glitchy snapping. There is a validator pattern in the devlog; check `maxKey <= length` for every animation after editing. The ash wraith cinder bug came from exactly this.
- **Never write files with PowerShell 5.1 `-Encoding UTF8`.** It emits a BOM, and Java rejects `﻿package ...` while GSON rejects BOM'd datapack JSON. Use `[System.IO.File]::WriteAllBytes` or strip the first three bytes afterward.
- **Glowmasks (`*_glowmask.png`) light up EVERY pixel matching their mask across the whole UV sheet.** On noisy procedural textures this reads as random glowing confetti. They were removed from all entity textures; if reintroducing emissive eyes, mask only the exact eye UV islands.
- **Synced flags beat server-only fields.** Any boolean that changes the played animation (casting, guarding, staggering, slamming) must be a `SynchedEntityData` entry, otherwise the client never sees the pose. This bit three mobs before it became a rule.
- **Goals need `stop()` overrides if they set state.** A casting goal interrupted by melee left a golem frozen mid-animation forever.
- **Boss spawns must settle.** Always run new boss spawns through `BossPlacement.settleOnGround` (heightmap snap + lift-until-clear) and check `BossPlacement.duplicateNearby`. Flagship tiers are SOLID fills; several early wardens spawned entombed inside them.

## Model design conventions

- One strong silhouette per mob. The fastest way to make thirteen mobs feel different is different BODY PLANS: specter (nullwalker), beetle (dust crawler), crocodilian (marsh crawler), manta (void ray), construct (sentinel/golem).
- Bones parented for secondary motion: robe tails, wing tips, tail streamers, crown flames each animate independently so idle loops feel alive.
- Death animations collapse the signature part (core shatters, crown pops, shards fly).
- Texture sizes are declared in the geo description (`texture_width/height`) and UVs must stay inside them.
- Textures are generated procedurally with System.Drawing scripts (see devlog 13): vertical top-lit gradient plus palette noise beats flat noise. Palettes: nullwalker cold void-black with pale accents, ash wraith charcoal + ember, dust crawler sand + gold seams, marsh crawler bog green, void ray slate + cyan.

## Region-adaptive rendering (End Warden)

The warden is one mesh with ten looks:

- A synced region byte (`DATA_REGION`) resolves from the biome on the server.
- `EndWardenModel.getTextureResource` picks `end_warden_<region>.png`.
- `setCustomAnimations` shows/hides accessory bone groups (`crest_spine`, hood tendrils, `halo`, horns) via `GeoBone.setHidden` based on region - Umbral wears none.
- The boss bar tint maps from the same region byte.

When adding an eleventh region: add a texture, extend the two switches, pick an accessory group. No geometry changes required.

## Item art rules

- Item models either parent to a block model (renders 3D) or to `endesium:item/endesium_gui` / `minecraft:item/generated` with a `layer0` sprite.
- Sprite textures must NOT be fully opaque squares - that reads as a placeholder. Icons should be shaped with transparency.
- Enchantable gear items override `isEnchantable`/`getEnchantmentValue` and take durability via `stack.hurtAndBreak(n, player, slot)`.
- Spawn eggs use `minecraft:item/template_spawn_egg`; colors come from the `SpawnEggItem` constructor, no texture file needed.

## Validation checklist before committing assets

1. All JSON parses (geo, animations, blockstates, loot tables, recipes).
2. Animation keyframe times fit their lengths.
3. Every animation name referenced in entity code exists.
4. No BOMs anywhere under `resources/data` or `src/main/java`.
5. New mobs: renderer registered in `EndesiumClient`, attributes registered, egg + lang + loot table present.
6. `gradlew build` and `gradlew runDatagen` green.
