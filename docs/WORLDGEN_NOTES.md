# Endesium Worldgen Notes

This document summarizes the world generation layout of Endesium and how the
new biomes fit together around the End's central island.

## Biome layout

The End's outer islands are reorganized into a ring of themed biomes, each
with its own geology, flora, and structures:

- **End Wastes** — a dusty, featureless plain of Wastes Stone and Wastes
  Gravel dotted with Dust Reed and Void Grass. Fractured Endesium stations
  are buried here.
- **Chorus Wilds** — dense Elder Chorus Wood and Chorus Moss, home to Chorus
  Stalkers and the Chorus Sprout flora.
- **Shattered Highlands** — tall cliffs of Highland Stone and Highland Slate
  with Cracked Spire Stone formations and the Shattered Spire structure.
- **Void Marshes** — low-lying Void Marsh Soil and Marsh Moss with Void Reed,
  patrolled by Marsh Crawlers.
- **Luminous Groves** — Lumen Stone glows softly; Lumen Moss and Lumen Bloom
  light the way for Lumen Moths.
- **Ashen Expanse** — Ash Stone and Ashen Soil with Ashen Crust over lava;
  Ash Wraiths drift here. Dormant volcanoes can be found.
- **Crystal Barrens** — Crystal Shard Blocks, Crystal Clusters, and Dark and
  Pale Crystal Blocks; Crystal Burrowers tunnel beneath.
- **Void Skirts** — the deep edge of the End, with Void Slate, Void Gravel,
  Void Soil, Void Glass, and Void Brick ruins. Void Stalkers roam here.
- **Void Crown** — the highest point, home to the Void Spire and the dragon
  arena.
- **Umbral Reach** — the deepest layer, with Umbral Stone and the Resonant
  Archive.

## Structures

- **Endesium stations** — small ruins with Resonant Mechanisms that the
  Resonance Lens activates.
- **The Shattered Spire** — a tall tower in the Highlands.
- **The Resonant Archive** — a buried vault in the Umbral Reach, opened with
  an Archive Key.
- **The Dragon Arena** — the post-dragon boss arena in the Void Crown.

## Generation notes

Biome placement uses a custom `TheEndBiomeSource` mixin. Terrain features are
registered as Fabric worldgen features in the `world` package. See
`docs/WORLDGEN_REFERENCE.md` for the full feature list.
