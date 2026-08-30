# Endesium

An ancient continuation of the End - ten regions, guarded by wardens, and
ruled by something older than the dragon.

I built this because the End never felt like a place. It was always just the
loot chest at the end of the hallway - a boring island, one boss, and nothing
to come back for. Endesium is my attempt to fix that. It's a Fabric mod for
Minecraft Java Edition 1.21.1 that turns the End into a hand-built frontier:
ten regions layered onto vanilla generation, structures in three tiers, a
progression system you learn by exploring instead of grinding, a roster of
creatures that all fight differently, and boss fights whose rewards
permanently change your character.

> **Where it's at:** very playable, still being worked on. The most recent
> passes made the bosses properly mean - they heal, they stop flinching, they
> hit a lot harder - the vanilla stronghold is now a citadel built for this
> mod, and there's a companion dragon you can hatch, tame, and ride. It's not
> finished, and I don't think it ever fully will be. That's kind of the point
> of a mod like this.

## Table of contents

- [The ten regions](#the-ten-regions)
- [Structures](#structures)
- [Resonance and progression](#resonance-and-progression)
- [Creatures](#creatures)
- [Bosses and permanent rewards](#bosses-and-permanent-rewards)
- [The companion dragon](#the-companion-dragon)
- [Post-Dragon transformation](#post-dragon-transformation)
- [Getting started](#getting-started)
- [Building from source](#building-from-source)
- [Project structure](#project-structure)
- [Design principles](#design-principles)
- [Documentation](#documentation)
- [License](#license)

## The ten regions

The outer End is split into ten continent-scale regions, each with its own
blocks, plants, particles, ambience, creatures, and structures. A seed-driven
lattice decides which one you get where:

| Region | What it is |
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

`/locate biome endesium:<region_name>` will find them.

## Structures

Everything is authored by hand - no random assembly. Three tiers:

- **Flagships** - one grand build per region (Dust Cathedral, Elderwood
  Sanctum, Skyrend Keep, Great Caldera, ...). Multi-level interiors,
  environmental hazards, curated loot, and an End Warden guarding the vault.
  The vault bars only open when the warden falls.
- **Landmarks** - medium builds on a ~256-block grid per region: fossil
  arches, windvane watchtowers, mire bell cairns, shard spire clusters. Each
  carries loot and a wakeable mini-mechanism.
- **Ruins and micro-sites** - weighted ruin variants (Intact / Fractured /
  Sunken) and small scatter sites keep every walk interesting.

### The stronghold is ours now

The vanilla stronghold is gone. The one the eyes of ender lead to is now a
hand-built Endesium citadel - a buried structure three times the old size,
with a Great Descent Hall, a portal cathedral (all twelve frames, always
eyeless, so progression stays intact), plus libraries, crypts, a forge, a
dining hall, and a prison. Sixteen room kinds, all connected, no dead ends.
And the one rule that matters: no Endesium gear in any chest, so you can't
grind the stronghold to skip the mod's progression. I redressed every room
twice before I was happy with it - shelves, candle runs, coffin rows, the
boring stuff that makes a place read as lived in.

## Resonance and progression

Progression here is understanding, not mining tiers. The loop looks like this:

```
EXPLORE -> NOTICE a structure -> CRAFT the Resonance Lens
  -> READ the signal (band + direction, never coordinates)
  -> WAKE the mechanism (Token + fragment + loot)
  -> BUILD the Echo Compass to track stronger sources
  -> GEAR UP from the regions -> KILL THE DRAGON
  -> the End transforms, permanently
```

Waking mechanisms earns Resonance Tokens, and tokens gate the Echo Compass and
other tools. The whole system is server-authoritative - the client asks, the
server decides.

## Creatures

Fourteen creatures now, and they don't all fight the same. Thirteen have
their own GeckoLib models and combat identities; the last one is the
companion dragon, who rides the vanilla dragon model because he literally is
a dragon.

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
- **Crown Sentinel** - construct guardian with a telegraphed area slam and a grab-and-hurl if you get too close. The grab is escapable if you deal enough damage mid-hold, and below half health it reforges its plating, so the armor you cracked open comes back unless you finish it.
- **End Warden** and **End Golem** - see below.
- **Companion Dragon** - hatches from the dragon egg, grows up, and bonds to whoever feeds it pearls. There's a whole section on it further down.

## Bosses and permanent rewards

Every boss drops something you keep forever. That's the whole point. And after
the last balance pass, they all fight for it now - the numbers below are the
honest ones, not the ones from before the difficulty pass.

### End Wardens (minibosses, x10)

One per flagship vault, wearing its region's colors and accessory bones, with
a region-tinted boss bar and a signature attack per region. It raises guard
(frontal immunity - flank it), calls local kin at 66% health, and enrages
below half. Recent pass: 560 health, 26 damage a hit, real armor, and it no
longer flinches from chip damage. Below half it regenerates, so the back half
of the fight is a fight instead of a victory lap.

Its **Warden Sigil** is the temptation: carry it and you regenerate in the
End. Use it and you permanently gain +1 heart - up to +10. Attune all ten
regions to become a hidden **Warden Ascendant** with a visible aura and a
lasting regeneration pulse.

### The End Golem (major boss)

Wakes where the dragon falls. Three phases (purple / yellow / red bar):
slams, homing resonance barrages with minion summons, beam sweeps,
shockwaves, an arena tether, and burning ground. Deal 60+ damage within 8
seconds to trigger the **stagger** - five seconds of double damage while it
kneels. It has 720 health and hits for 26, and once it drops below half it
starts repairing itself mid-fight, so you can't just chip it down from range.

It drops **Golem Cores**: carry them for Resistance in the End, or absorb them
for a permanent +1 heart and +0.25 attack damage (max +10 / +4). Ten cores
unlock **Golem's Resolve** - once per day, death refuses you. Craft a **Golem
Effigy** to summon another golem whenever one is ready to fall. The Golem also
guarantees a **Void Pearl** - a safe short-range teleport with durability that
accepts Unbreaking and Mending.

### The Dragon (the real fight)

The first kill is the tutorial. After that, the respawned dragon fights a
four-phase Endesium controller with its own mechanics - and it scales properly
with how much health it's lost, enraging in four stages (60% / 35% / 15%) that
escalate spawns, breath cadence, and scripted set-pieces. You've fought it
before. Now it's fought back. The fights are also quieter than they used to be
- I stripped the on-screen boss titles; the arena and the boss's behavior tell
you what's happening.

## The companion dragon

After you kill the dragon, the egg sitting on the fountain isn't just a
trophy anymore. Pick it up, place it back on top of the fountain, and it
starts working - ten minutes later it cracks and a baby dragon hatches.
She's called Ember, she's small, and she's not going anywhere.

Ember grows through three stages - baby, teen, adult, about five minutes
each. Feed her ender pearls and she bonds to you, and once she's an adult
you can hop on.

Riding is the fun part. She steers by where you look: WASD to fly, look up to
climb, look down to dive. Hold space for three seconds and she charges up and
fires a magic ball in the direction she's facing. It's a lot of power for a
mount, but honestly, you earned it.

A bonded dragon is safe to bring home. She won't run the vanilla dragon's
hostile arena behavior, and once she's tamed only you - or the void, or /kill
- can hurt her, so she won't get shredded by random mobs. Her growth and tame
state survive reloads, so the adult you raised stays an adult.

## Post-Dragon transformation

The first dragon death is a world event: a permanent, restart-proof
transformation stored in `SavedData`. Mechanisms reach farther, the Resonant
Archive unseals, the respawned dragon fights the four-phase controller above,
the Golem awakens - and the dragon egg becomes a door instead of a souvenir
(see the companion dragon section).

## Getting started

- Minecraft Java Edition 1.21.1
- Fabric Loader 0.19.3+, Fabric API 0.116.15+1.21.1
- Java 21, GeckoLib 4.9.2 (declared dependency)

Drop the built JAR into `mods` with Fabric API and launch.

In-game, craft the **Guidebook** (book + Resonance Token) and the
**Progression Guide** - they cover the whole mod.

## Building from source

```sh
./gradlew genSources      # Minecraft sources
./gradlew runClient       # dev client
./gradlew runDatagen      # regenerate recipes/loot/lang
./gradlew build           # mod JAR
```

Windows: use `gradlew.bat`.

## Project structure

```
src/
  main/java/com/infernodude777/endesium/
    block/       plant blocks, inscribed slate, resonant mechanism
    command/     development-only /endesium commands
    dragon/      dragon fight controller, companion dragon, wing passives
    entity/      the custom entities incl. End Warden and End Golem
    item/        lenses, tokens, relics, sigils, cores, effigies
    mixin/       biome source extension, dragon fight hooks, an input accessor
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

These are the rules I keep coming back to:

- **Restraint.** A desaturated working palette; saturated color has to mean something.
- **Observation over waypoints.** Qualitative signals, never coordinates.
- **Server authority.** The client asks; the server decides.
- **Rewards you keep.** Permanent hearts, damage, and saves - all heavily guarded.
- **Readable combat.** Every attack telegraphs before it lands.
- **Fights stay quiet.** No screen titles telling you what's happening; the arena speaks for itself.
- **Vanilla stays vanilla underneath.** Mixins extend, don't replace.

## Documentation

The docs are kept deliberately small:

- `docs/ROADMAP.md` - what's built and what's next
- `docs/CHANGELOG.md` - the feature history (honestly a mess of "unreleased"
  sections at this point, but it's the history)
- `docs/DEVLOG_*.md` - the devlogs, in order (stronghold takeover, the boss
  difficulty pass, the companion dragon, and everything after)
- `ABOUT.md` - the short version of why this mod exists

## License

CC0-1.0 (public domain), matching the template this project grew from. See `fabric.mod.json`.
