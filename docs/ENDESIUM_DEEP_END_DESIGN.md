# Endesium — Deep End Design

Status: design only. No Deep End code is implemented yet. This document is the
basis for the first playable vertical slice and must be reviewed before that
slice is built.

## 1. Core concept

**The End has depth.** The player spent the whole game believing the void under
the End islands was empty. After the Ender Dragon is defeated and the Resonant
Archive is awakened, they learn the void hides a deeper region.

The Deep End must feel ancient, alien, dangerous, quiet, enormous, mysterious,
and progressively disconnected from the familiar End. It is **not** a generic
cave dimension, **not** a purple Nether, and **not** a normal underground cave
system with End stone.

## 2. Entry method

Entry is **discovered**, **gated on post-Dragon progression**, and tied to the
Archive Sigil.

- The entry is a **natural descent** — a "void channel" that opens below a
  Resonant Archive after the transformation is active.
- The Archive Sigil is the key the player carries to recognize the channel: the
  Lens, near a channel, reads a new `DEPTH_GATE` resonance instead of the usual
  ruin/archive signals.
- No `/portal → new dimension` command. The transition is environmental: stand
  on the channel and descend (a slow fall, not a teleport).

The player should feel they are descending into something that was always there.

## 3. World model

**Decision: a hybrid — a separate dimension that mirrors the End's coordinates.**

Reasoning:

- The End world height (Y 0–255) is too shallow to stack a meaningful vertical
  layer under existing terrain without colliding with vanilla generation and
  player builds.
- A **separate dimension** (`endesium:deep_end`) with its own chunk generator
  is the safest architecture: no retroactive terrain writes, no build damage,
  full compatibility with existing worlds.
- The dimension reuses the End's X/Z coordinate space so the entry point and
  the Deep End align positionally, making navigation feel continuous.

The vanilla End generator is **not** replaced.

## 4. Vertical structure

Four depth bands, each with a distinct environmental identity:

1. **Descent** — the void channel: a narrow shaft of suspended End stone that
   falls away into darkness. Vertical, quick, transitional.
2. **Upper Deep End** — broken End geology: shattered shelves, floating slabs,
   thin bridges. Familiar material, wrong orientation. Cool, dim.
3. **Lower Deep End** — strange ecosystems: resonance-fed growth, still air,
   faint light. Warmer in color, more alive.
4. **Ancient Depths** — the oldest layer: monolithic structures, near-silence,
   sparse but powerful hazards. The last band before (future) Endgame.

The player must feel increasing depth through lighting, material, and silence.

## 5. Terrain generation

Clearly different from the surface End:

- Enormous hollow chambers rather than floating islands.
- Vertical cliffs and deep shafts rather than flat shelves.
- Suspended stone masses and resonance caverns.
- Ancient geological layers, not ordinary Overworld caves or Nether terrain.

Recognizable landmarks (a single vast chamber, a hanging monolith, a glow
source) replace the surface's repeating islands. Noise is kept low-frequency so
spaces read as deliberate rather than chaotic.

## 6. Caves and chambers

The Deep End is **not** an endless maze. Deliberate spaces only:

- Caverns, tunnels, shafts, chambers, ruins, and natural landmarks.
- Navigation stays possible; players form mental maps from landmark shapes,
  lighting, and material changes — never UI markers.

## 7. Biomes

Two to four strong biomes, not ten similar ones. Candidates (names provisional):

- **Resonance Caverns** — dim chambers where dormant crystals pulse with the
  Lens; the "intro" biome of the layer.
- **Hollow Gardens** — the biologically active band: pale flora fed by weak
  resonance, the Deep End's answer to Chorus Wilds.
- **Shattered Depths** — broken geology, long falls, the vertical-cliff biome.
- **Ancient Vaults** — monolithic sealed structures, nearly silent, the deepest
  band before future content.

Each biome defines terrain, blocks, flora, particles, ambient behavior,
resources, creatures, hazards, and structures.

## 8. Ecology

Life that could not exist on the surface, built around **roles** rather than
"stronger versions of existing mobs":

- A passive cave organism (ambient).
- A resonance-sensitive creature that reacts to Lens use.
- A territorial predator (the depth's own pressure, distinct from the Void
  Stalker).
- An environmental organism (immobile, reactive).

Add the minimum number of entities. Strong identities over mob count.

## 9. Resonance in the Deep End

Resonance becomes **environmental information**. It indicates ancient
structures, dangerous areas, hidden chambers, resources, living organisms, and
deeper paths. It never becomes a GPS: no exact coordinates, no per-block glow.
The player learns to interpret the environment.

## 10. Resources

Only what the Deep End requires, each with purpose, rarity, biome relationship,
visual identity, and progression purpose. **No armor tier, no dozen ores.** The
first slice provides components and discoveries, not equipment.

## 11. New capability

One meaningful capability for navigation/understanding. Candidate: **resonance
interpretation** — the Lens gains a qualitative "depth echo" readout that
distinguishes the four bands and reveals hidden chambers. Exploration-first,
not damage-first. (The Void Grapple is out of scope.)

## 12. Hazards

Danger beyond stronger mobs, and always **readable**:

- Unstable terrain (crumbling shelves).
- Resonance disturbances (areas the Lens warns are "wrong").
- Environmental void exposure (slow, visible, avoidable).
- Darkness with meaningful points of light.

No invisible instant death. The player learns *why* a place is dangerous.

## 13. Lighting and atmosphere

Not simply dark caves. Light comes from resonance crystals, dormant structures,
biological glow, distant energy, and ancient machinery. Darkness is used
intentionally; the player sees meaningful distant lights. No constant glowing
purple.

## 14. Structures

A small number of strong structures. The first is an **ancient observatory** —
the civilization that built the surface ruins was watching something below.
Every structure tells the player something. No giant dungeons yet.

## 15. Storytelling

No quest chain. Architecture, environmental clues, fragments, Resonance, item
descriptions, structure placement, and progression discoveries together reveal:
*the surface civilization did not understand everything below them.*

## 16. Player navigation

Must work for returning to the surface, finding the entry point, moving between
chambers, locating structures, escaping danger, and multiplayer. Landmarks,
lighting, material differences, Resonance, and architecture — not coordinates.

## 17. Death and recovery

Dangerous but fair. Defined: fall behavior, void behavior, recovery, item
retrieval, spawn behavior, escape routes. No unavoidable death traps. Item
recovery is tested explicitly.

## 18. World-generation compatibility

The Deep End must not touch existing End terrain, old worlds, End Cities, End
Ruins, Archives, or player builds. The separate dimension guarantees this. Test
both fresh and existing worlds.

## 19. Performance

Chunk generation must be bounded: no per-block expensive logic, no full-chunk
repeated scans, no global searches, no huge structure templates, no excessive
spawning. Profile large chambers.

## 20. Multiplayer

Server-authoritative. Test: entering together, separate exploration, Resonance,
structures, creatures, hazards, death, item recovery, progression, save/load.

## 21. Commands

Development-only where necessary (locate entry, locate structures, inspect
biome/depth, test hazards, test progression). Every command: permission
validation, argument validation, safe limits, documentation.

## 22. Assets

DogSprite for pixel art, Jimbibo for Blockbench. Production-quality only, no
placeholders. 16×16 RGBA item textures, hard pixel edges, limited palette,
Endesium visual language. Entities need coherent silhouettes and readable
GeckoLib animations.

## 23. Audio

A Deep End audio identity built from silence, distant ambience, low resonance,
environmental sounds, creature sounds, and structure sounds. No constant
background noise. Acoustically distinct from the surface.

## 24. First playable vertical slice

Do not build the whole Deep End. The first slice is:

ENTRY → DESCENT → first biome → one major cavern → one ecological system → one
structure → one new resource → one new capability → discovery of a deeper path.

## 25. Test the slice

Play it without commands and answer: Is descending exciting? Does it feel
different? Is navigation understandable? Is the first cavern memorable? Is the
ecology interesting? Is the structure worth investigating? Does Resonance feel
useful? Does the resource feel meaningful? Does the capability change
exploration? **Do I want to go deeper?** If the last answer is no, stop
expanding and improve the slice.

## 26. Regression

The Deep End must not break: vanilla End, End Wastes, Chorus Wilds, End Ruins,
Shattered Spire, Resonance, Resonance Lens, Echo Compass, Void Stalker,
ecology, PostDragonState, Dragon transformation, Resonant Archive, Archive
Sigil, commands, save/load, multiplayer.

## 27. Scope limit

Do **not** implement: final Endgame, final boss, End Golem, End Serpent,
Endesium armor, Voidsteel armor, Momentum combat, Resonance Storms, Meteor
Showers, final Resonance Gates, a new unrelated dimension, or a complete End
replacement. The Deep End is an exploration milestone, not the final game.
