# Endesium

An ancient, restrained continuation of the End - grown into ten regions, guarded by wardens, and ruled by something older than the dragon.

Endesium is a Fabric mod for Minecraft Java Edition 1.21.1 that expands the End into a hand-authored frontier: ten distinct regions layered onto vanilla End generation, structures in three tiers, a discovery-driven resonance system, thirteen custom creatures with individual combat identities, and two boss fights with rewards that permanently change your character.

> **Status:** the 10x overhaul is complete and the worldgen backbone now runs
> on registered vanilla Structures (native `/locate`, chunk-safe generation).
> See `docs/ROADMAP.md` for what is next and `docs/TESTING_RUNBOOK.md` for QA
> gates.

## Table of contents

- [The ten regions](#the-ten-regions)
- [Structures](#structures)
- [Resonance and progression](#resonance-and-progression)
- [Creatures](#creatures)
- [Bosses and permanent rewards](#bosses-and-permanent-rewards)
- [Post-Dragon transformation](#post-dragon-transformation)
- [Getting started](#getting-started)
- [Building from source](#building-from-source)
- [Project structure](#project-structure)
- [Design principles](#design-principles)
- [Documentation](#documentation)
- [License](#license)

## The ten regions

A seed-driven lattice divides the outer End into ten continent-scale biomes, each with its own blocks, plants, particles, ambience, creatures, and structures:

| Region | Identity |
|---|---|
| End Wastes | The mineral grave: fractured shelves, dead mechanisms, dust reeds |
| Chorus Wilds | The living forest: elder chorus trees, blooms, blink-stalking predators |
| Shattered Highlands | Wind and stone: sky lakes, lensstone towers, gliding rays |
| Void Marshes | Drowned ground: black water, tide iron, tendril-pulling ambushers |
| Luminous Groves | Starlit atoll: glowing flora, prism canopies, lumen moths |
| Ashen Expanse | The caldera country: volcanoes, ember shrines, ash wraiths |
| Crystal Barrens | Shattered geodes: crystal spires, burrowers, shard volleys |
| Void Skirts | The prison yard: void slate flats, wisps, the tallest spire in the End |
| Void Crown | The sealed ziggurat: crown needles, sentinels, observatory orbs |
| Umbral Reach | The deepest dark: null archives, sound-mimicking watchers |

Find them with `/locate biome endesium:<region_name>`.

## Structures

Three tiers, all hand-authored block by block:

- **Flagships** - one grand build per region (Dust Cathedral, Elderwood Sanctum, Skyrend Keep, Great Caldera, ...), each with multi-level interiors, environmental hazards, curated loot, and an End Warden guarding its vault. Vault bars retract only when the warden falls.
- **Landmarks** - medium builds on a ~256-block grid per region: fossil arches, windvane watchtowers, mire bell cairns, shard spire clusters, and more. Each carries loot plus a wakeable mini-mechanism.
- **Ruins and micro-sites** - weighted ruin variants (Intact / Fractured / Sunken) and small scatter sites keep every walk interesting.

## Resonance and progression

Progression is understanding, not mining tiers:

```
EXPLORE -> NOTICE a structure -> CRAFT the Resonance Lens
  -> READ the signal (band + direction, never coordinates)
  -> WAKE the mechanism (Token + fragment + loot)
  -> BUILD the Echo Compass to track stronger sources
  -> GEAR UP from the regions -> KILL THE DRAGON
  -> the End transforms, permanently
```

Waking mechanisms earns Resonance Tokens; tokens gate the Echo Compass and other tools. The system is fully server-authoritative.

## Creatures

Thirteen GeckoLib-modeled creatures, each with its own silhouette and combat identity:

- **Void Stalker** - the flagship predator: observes, flanks, commits. Ten-state AI.
- **Dust Crawler** - armored scarab; kicks up blinding dust, burrow-escapes at low health.
- **Chorus Stalker** - blinks behind you through the wilds.
- **Marsh Crawler** - crocodilian ambusher; tendril pull and pounce.
- **Lumen Moth** - luminous ambience; follows lantern light.
- **Ash Wraith** - telegraphed ash bolts; enrages below half health and its bolts ignite.
- **Crystal Burrower** - erupts from the ground, fires homing crystal shards.
- **Void Ray** - wildlife glider that dive-bombs when provoked.
- **Nullwalker** - rare watcher of the Umbral Reach; mimics sounds, vanishes when approached, suppresses mortal vigor.
- **Void Wisp** - lure predator of the Skirts; drifts like a mote, lunges and drags.
- **Crown Sentinel** - construct guardian with a telegraphed area slam.
- **End Warden** and **End Golem** - see below.

## Bosses and permanent rewards

### End Wardens (minibosses, x10)

One per flagship vault, wearing its region's colors and accessory bones, with a region-tinted boss bar and a signature attack per region. It raises guard (frontal immunity - flank it), calls local kin at 66% health, and enrages below half.

Its **Warden Sigil** is the temptation: carried, it grants regeneration in the End. Used, it permanently grants +1 heart - up to +10. Attune all ten regions to become a hidden **Warden Ascendant** with a visible aura and a lasting regeneration pulse.

### The End Golem (major boss)

Wakes where the dragon falls. Three phases (purple / yellow / red bar): slams, homing resonance barrages with minion summons, beam sweeps, shockwaves, an arena tether, and burning ground. Deal 60+ damage within 8 seconds to trigger the **stagger** - five seconds of double damage while it kneels.

It drops **Golem Cores**: carried, they grant Resistance in the End; absorbed, they permanently grant +1 heart and +0.25 attack damage (max +10 / +4). Ten cores unlock **Golem's Resolve** - once per day, death refuses you. Craft a **Golem Effigy** to summon another golem whenever one is ready to fall. The Golem also guarantees a **Void Pearl** - a safe short-range teleport with durability that accepts Unbreaking and Mending.

## Post-Dragon transformation

The first dragon death is a world event: a permanent, restart-proof transformation stored in `SavedData`. Mechanisms reach farther, the Resonant Archive unseals, the respawned dragon fights a four-phase Endesium controller, and the Golem awakens.

## Getting started

- Minecraft Java Edition 1.21.1
- Fabric Loader 0.19.3+, Fabric API 0.116.15+1.21.1
- Java 21, GeckoLib 4.9.2 (declared dependency)

Drop the built JAR into `mods` with Fabric API and launch.

In-game, craft the **Guidebook** (book + Resonance Token) and the **Progression Guide** for full coverage.

## Building from source

```sh
./gradlew genSources      # Minecraft sources
./gradlew runClient       # dev client
./gradlew runDatagen      # regenerate recipes/loot/lang
./gradlew test            # pure-logic unit tests
./gradlew build           # mod JAR
```

Windows: use `gradlew.bat`.

## Project structure

```
src/
  main/java/com/infernodude777/endesium/
    block/       plant blocks, inscribed slate, resonant mechanism
    command/     development-only /endesium commands
    dragon/      respawned-dragon fight controller
    entity/      13 custom entities incl. End Warden and End Golem
    item/        lenses, tokens, relics, sigils, cores, effigies
    mixin/       biome source extension + dragon fight hooks
    registry/    blocks, items, entities, sounds, tabs
    resonance/   signal manager and source types
    state/       persistent world + player-reward state
    world/       regions, terrain, flagship + landmark features
  main/resources/assets/endesium/   models, textures, GeckoLib geo/animations
  main/resources/data/endesium/     biomes, features, loot, recipes, advancements
  client/java/...                   renderers, models, screens, datagen providers
docs/             design docs, devlogs, runbooks (see Documentation)
```

## Design principles

- **Restraint.** A desaturated working palette; saturated color must mean something.
- **Observation over waypoints.** Qualitative signals, never coordinates.
- **Server authority.** The client asks; the server decides.
- **Rewards you keep.** Permanent hearts, damage, and saves - all heavily guarded.
- **Readable combat.** Every attack telegraphs before it lands.
- **Vanilla stays vanilla underneath.** Mixins extend, not replace.

## Documentation

Start here:

- `docs/GAMEPLAY_GUIDE.md` and `docs/PROGRESSION_GUIDE.md`
- `docs/FEATURE_REFERENCE.md` and `docs/CRAFTING_REFERENCE.md`

Then the deep cuts:

- `docs/ARCHITECTURE.md` and `docs/CODE_WALKTHROUGH.md`
- `docs/WORLDGEN.md` - regions, spacing grids, feature wiring
- `docs/VOID_STALKER_AI.md` - the 10-state combat machine
- `docs/MEANINGFUL_STRUCTURES_PLAN.md` and `docs/10X_OVERHAUL_PLAN.md`
- `docs/BALANCE_NOTES.md`, `docs/TESTING_RUNBOOK.md`, `docs/QA_REPORT.md`
- `docs/CHANGELOG.md` and `docs/DEVLOG_12.md` / `DEVLOG_13.md`
- `ABOUT.md` - the short version of why this mod exists

## License

CC0-1.0 (public domain), matching the template this project grew from. See `fabric.mod.json`.
