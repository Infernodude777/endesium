# Endesium Worldgen & Ecology Overhaul — Implementation Report

Status: **IMPLEMENTED & MACHINE-VERIFIED (needs live playthrough)**

This milestone replaces the two-biome outer End with a seven-biome, continent-scale
ecology. It is a terrain-first redesign: one coherent region generator, one terrain
pipeline, one vegetation dispatcher, and one structure dispatcher — not layered
decorations.

## 1. The seven biomes

| Biome | Identity | Terrain profile | Palette |
|---|---|---|---|
| `end_wastes` | barren, wind-eroded | broad rolling plateaus + mesas | cream / gray |
| `chorus_wilds` | alien forest (no chorus spam) | rolling hills + mounds + clearings | violet / magenta / green-gray |
| `shattered_highlands` | massive vertical terrain | terraced ridges + cliffs (up to ~24 relief) | gray / pale cream |
| `void_marshes` | strange lowland | shallow basins (−4..−9) + low ridges | dark gray / muted cyan |
| `luminous_groves` | bioluminescent forest | sheltered hills + caves | cyan / blue-white |
| `ashen_expanse` | dead catastrophe | ash dunes + craters | black / gray / ash-white |
| `crystal_barrens` | geological mineral fields | rocky shelves + mineral ridges | violet / cyan / white |

## 2. Large-scale regional generation

`EndesiumRegions` assigns the seven biomes over the vanilla highlands/midlands
rings using a **jittered square lattice + Voronoi** with cell size ~1500 blocks,
plus per-seed cell-size modulation. Regions are continent-scale belts (hundreds to
1000+ blocks), never noise patches.

**Wastes-never-touches-Wilds guarantee:** the lattice is colored `(gx + gz + offset)
mod 7` in the order `[Wastes, Highlands, Marshes, Wilds, Groves, Ash, Barrens]`.
Every 8-connected neighbor differs by at most ±2 in that sum, while Wastes (0) and
Wilds (3) differ by 3 — so they can never share an edge. Empirically verified with
zero adjacency violations across a 16000×16000 sample. There is always at least one
intermediary biome between them.

## 3. New blocks (26)

`wastes_stone`, `wastes_gravel`, `dust_reed`, `void_grass`,
`elder_chorus_wood`, `elder_chorus_bark`, `chorus_root`, `chorus_moss`,
`hollow_chorus_wood`, `highland_stone`, `highland_slate`, `void_marsh_soil`,
`void_reed`, `marsh_moss`, `lumen_stone`, `lumen_moss`, `lumen_bloom`,
`ash_stone`, `ashen_soil`, `crystal_shard_block`, `crystal_cluster`,
`dark_crystal_block`, `pale_crystal_block`, `resonant_basalt`, `end_clay`,
`voidstone`.

All 26 have textures, blockstates, cube/cross models, item models, loot tables,
hardness, sound groups, and (for the six plants) cutout render layers. Luminous and
crystal blocks carry light levels.

## 4. New items (10)

`wastes_compass` (heading/distance to the Heart of the End), `highland_grappler`
(leap), `lumen_lantern` (night-vision light), `void_filter` (clears hazards +
resistance), `crystal_resonator` (detects minerals), `ash_sifter` (sifts ashen soil
into materials), `chorus_pruner` (sustainable chorus harvesting), `archive_key`
(wakes a mechanism directly), `void_flare` (plants a landmark bloom),
`end_cartographer` (records discovered biomes in item NBT).

## 5. Terrain / vegetation / structures

- `BiomeTerrain` + `BiomeTerrainFeature` — per-biome relief, chunk-local and
  seed-deterministic (a pure function of world seed + column), so ridges/basins are
  seamless across chunk borders with no far-chunk writes.
- `BiomeVegetationFeature` — layered density per region; giant growth (elder chorus)
  is rare, terrain stays readable.
- `BiomeStructureFeature` — a curated set of coherent archetypes (fallen spire,
  shrine, elder shrine, skybridge, crater, crystal landmark), placed through a
  rarity filter so long natural stretches separate discoveries.

## 6. Sound & particles

Five new ambient sound events (`shattered_highlands_low`, `void_marshes_low`,
`luminous_groves_low`, `ashen_expanse_low`, `crystal_barrens_low`) and five new
ambient particle types (`highland_wind`, `marsh_mist`, `lumen_mote`, `ash_mote`,
`crystal_mote`), wired into each biome's mood sound and ambient particle.

## 7. Compatibility

- Central island, Ender Dragon, End Crystals, exit portal, End gateways, End Cities,
  and vanilla End traversal are unchanged (the mixin still returns vanilla for the
  central island, small islands, and barrens).
- Resonance system, Lens, Void Stalker, post-Dragon transformation, and the Echo
  Compass all remain wired.

## 8. Verification

| Check | Result |
|---|---|
| `./gradlew build` | PASS |
| `./gradlew runDatagen` | PASS (59 files) |
| `node tools/validate_resources.mjs` | PASS (92 files) |
| Fresh server, seed 123456789 | all 7 biomes locate; 0 far-chunk/mixin/exception/missing-sound errors |
| Region simulation | 7 regions evenly large; 0 Wastes↔Wilds adjacency violations |

## 9. Known remaining problems

- **NEEDS LIVE PLAYTHROUGH**: terrain relief, vegetation density, and structure
  silhouettes are verified structurally (generation + block counts + biomes), but
  color, lighting, and ground-level feel need an in-client pass.
- Structures are a first curated archetype set (one landmark + ruins per biome
  family), not the full 5-per-biome interior set; interiors are shallow by design.
- Ambient sound events reuse vanilla sound files (no authored .ogg yet).
- The old `EndesiumNoise` selector is now unused (superseded by `EndesiumRegions`).
