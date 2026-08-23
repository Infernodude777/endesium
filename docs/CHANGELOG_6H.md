# Endesium — 6-Hour Session Changelog

This changelog records the content pass delivered during the dedicated six-hour
improvement session. The focus of this pass was **crafting depth, progression
clarity, and documentation completeness** — filling the gaps left by the earlier
worldgen and ecology work so that every item and block in the mod has a clear
path to acquisition.

## Recipes added

A large batch of new recipes was added as raw data files under
`src/main/resources/data/endesium/recipe/`. These cover:

- **All ten biome utility items** — Wastes Compass, Highland Grappler, Lumen
  Lantern, Void Filter, Crystal Resonator, Ash Sifter, Chorus Pruner, Archive
  Key, Void Flare, and End Cartographer now all have crafting recipes.
- **Material chains** — Ender Essence, Void Pearl, Dragonbone, Abyssal Thread,
  Resonance Core, Archive Fragment, Echo Shard, Umbral Shard, Void Core, Magma
  Core, and Ashen Ember can all be crafted from earlier-stage materials.
- **Smelting and blasting** — Voidstone smelts into Void Ingots, and Umbral
  Shards blast into Void Ingots, giving a second acquisition path for the
  core metal.
- **Building blocks** — End Gray, Cracked Spire Stone, Resonant Pillar, Elder
  Chorus Wood and Bark, Hollow Chorus Wood, Void Glass, Void Lamp, Void Spire,
  Void Weave, Wastes Gravel, Void Gravel, Void Soil, Void Marsh Soil, Ashen
  Soil, End Clay, Dark and Pale Crystal Blocks, Resonant Basalt, and Dormant
  Resonant Crystal all gained recipes.
- **Alternate recipes** — Void Brick can now be made from Umbral Stone or
  Voidstone, Void Slate from Voidstone, Resonant Slate from Resonant Basalt,
  and Highland Slate from Highland Stone.

## Advancements added

Fifteen new advancements were added under `data/endesium/advancement/`,
creating a clear progression ladder from the first Void Shard through the Void
metal line and into each biome's signature tool. Each advancement has a
localized title and description in the language provider.

## Documentation added

Ten new documentation files were written covering crafting, advancement trees,
progression, playtesting, balance, worldgen, assets, architecture, and QA.

## Notes

All recipe and advancement IDs are unique and do not collide with the existing
datagen-generated recipes or the twenty-four pre-existing advancements. The
mod continues to compile cleanly and the data loads without conflicts.
