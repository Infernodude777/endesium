# Endesium

A restrained, ancient continuation of the End.

Endesium is a Fabric mod for Minecraft Java Edition 1.21.1 that expands the End into a quiet, weathered frontier. It adds the sparse End Wastes biome, handcrafted End Ruins, the Void Shard and Resonance Lens, and the Void Stalker — a predator that watches before it strikes.

The mod is built around a simple idea: the End should reward observation and interpretation, not map markers and particle storms. Signals are quiet, sources are server-authoritative, and every visual accent means something.

> **Status:** first vertical slice. The scope is deliberately small, and the design is locked before gameplay implementation (see `docs/ENDESIUM_VERTICAL_SLICE_DESIGN.md`).

## Table of contents

- [Features](#features)
- [The first vertical slice](#the-first-vertical-slice)
- [Getting started](#getting-started)
- [Building from source](#building-from-source)
- [Project structure](#project-structure)
- [Design principles](#design-principles)
- [Technical notes](#technical-notes)
- [Documentation](#documentation)
- [License](#license)

## Features

### End Wastes

A sparse transition biome layered on top of vanilla End generation rather than replacing it. End islands remain familiar, but shelves become fractured, void gaps grow wider, and occasional dark seams cut through the ground. End Wastes currently occupies a small share of eligible outer-End biome samples, keeping the central island and Dragon arena untouched.

### End Ruin

A small, handcrafted observation station built into an End island shelf — broken pillars, a collapsed corner, and a low resonant plate. The ruin is placed by the mod on world tick at a fixed origin in the End, using End Ruin Block and vanilla End Stone Bricks. It is a place to look around, not a quest destination.

### Void Shard

A fractured, inert fragment of an older End material. It is not glowing crystal and not ore: it is a physical remnant that becomes useful only when paired with the Resonance Lens. Shards are scarce; the first slice does not provide a farm. They can be crafted from an Ender Pearl as a bridge for early testing, but the intended source is ruin loot.

### Resonance Lens

A small, dark-framed instrument with a pale mineral aperture. Crafted from four Void Shards around an Ender Eye, the Lens reads nearby resonance and reports it as a qualitative pulse — never as coordinates, an arrow, or a distance. It works in the End, is reusable, and has a short activation cooldown.

### Void Stalker

A low, four-legged predator that prefers to observe before it engages. The Void Stalker keeps a preferred combat distance, uses a readable side reposition to punish simple backpedaling, and only commits when it has line of sight. Its body stays dark mineral and desaturated; a single cyan signal appears only during attack commitment. Modeled and animated with GeckoLib.

### Resonance system

A small server-side registry of discoverable signals. A player's resonance level persists as an attachment; the Lens reports a bounded, qualitative response. The client never learns exact source positions, and one player's discovery never reveals coordinates to another.

### Discovery advancement

Entering the End Wastes earns "A Resonance in the Wastes", rewarding the first step of exploration with a named confirmation of the discovery.

## The first vertical slice

In scope:

- End Wastes biome (extension of vanilla End selection)
- End Ruin Block and the first End Ruin structure
- Void Shard and Resonance Lens items with recipes
- Void Stalker entity with GeckoLib geometry, textures, and animations
- Basic Resonance system with persistent player state
- "A Resonance in the Wastes" advancement
- Generated language, loot tables, and recipe data

Out of scope (deliberately):

- Deep End, new bosses, or post-dragon progression
- Complete End replacement or a custom terrain generator
- Additional mobs and structures
- Renewable shard farming

## Getting started

Requirements:

- Minecraft Java Edition 1.21.1
- Fabric Loader 0.19.3+
- Fabric API 0.116.15+1.21.1
- Java 21
- GeckoLib 4.9.2 (bundled as a dependency)

Install the mod like any Fabric mod: drop the built JAR into your `mods` folder, along with Fabric API and GeckoLib, and launch through the Fabric loader profile.

## Building from source

The project uses Fabric Loom with official Mojang mappings.

```sh
# Generate Minecraft sources and set up the workspace
./gradlew genSources

# Run a client or dedicated server
./gradlew runClient
./gradlew runServer

# Regenerate data (recipes, loot tables, advancements, lang)
./gradlew runDatagen

# Build the mod JAR
./gradlew build
```

On Windows, use `gradlew.bat` or run from Git Bash as shown above.

## Project structure

```
src/
  main/java/com/infernodude777/endesium/
    entity/          Void Stalker entity and AI
    item/            Void Shard and Resonance Lens item behavior
    mixin/           TheEndBiomeSourceMixin (End Wastes selection)
    registry/        Blocks, items, entities, sounds registration
    resonance/       Resonance system and persistent player state
    world/           End Wastes biome definition and End world extensions
  main/resources/
    assets/endesium/ Models, textures, GeckoLib geometry and animations
    data/endesium/   Worldgen biome definition and advancement
    fabric.mod.json  Mod metadata
  client/java/com/infernodude777/endesium/client/
    entity/          Void Stalker GeckoLib model and renderer
    datagen/         Language, loot table, and recipe data providers
docs/
  ENDESIUM_VISUAL_DESIGN.md          Visual language, palette, and material rules
  ENDESIUM_VERTICAL_SLICE_DESIGN.md  Vertical slice design specification
```

## Design principles

- **Restraint.** The palette stays desaturated — charcoal, End gray, and End stone cream dominate. Saturated purple, pure black, and neon effects are not part of the working palette.
- **Meaningful signals.** Resonance Cyan means active energy. Ancient Gold means rare history. A bright accent must have a gameplay or lore reason.
- **Vanilla relationship.** The vanilla End remains recognizable: its stone, its emptiness, its scale. Endesium adds things that do not belong, quietly.
- **Server authority.** Detection, signal strength, and rewards are computed server-side. Clients receive bounded responses, never source coordinates.
- **Readable movement.** The Void Stalker's intent is readable through animation — observe, position, commit — before any damage happens.

## Technical notes

- **Biome selection:** a mixin extends the vanilla End biome source instead of replacing it, so vanilla End generation, the Dragon fight, and Enderman behavior remain unchanged.
- **Resonance state:** per-player resonance is stored with the Fabric attachment API using a persistent `Codec.INT` attachment, surviving reloads.
- **Mob rendering:** the Void Stalker uses GeckoLib 4.9.2 with a Blockbench geometry model and authored animations.
- **Data generation:** recipes, loot tables, advancements, and language entries are generated by the `fabric-datagen` entrypoint and committed under `src/main/generated`.

## Documentation

- `docs/ENDESIUM_VISUAL_DESIGN.md` — the finalized palette, material language, and pixel-art rules for every asset.
- `docs/ENDESIUM_VERTICAL_SLICE_DESIGN.md` — the locked design for the first slice, including mechanics, AI behavior, and the resonance discovery sequence.

## License

This project is licensed under CC0-1.0 (public domain dedication), matching the template it was built from. See `fabric.mod.json`.