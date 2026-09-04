# Endesium

An ancient continuation of the End, split into ten regions, guarded by wardens, and ruled by something older than the dragon.

I made Endesium because I thought that the End felt too barren, simple, and lacked any real integrity. 

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

The outer End is divided into ten large regions. Each one has its own blocks, plants, particles, atmosphere, creatures, and structures. Their locations are determined by a seed-based lattice, so every world is different.

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

The structures in Endesium are all built by hand. I didn't want them to feel like randomly generated rooms thrown together, so there are three main levels of structures.

- **Flagships** - the big structures, with one in each region. They have multiple floors, environmental hazards, custom loot, and an End Warden protecting the main vault. The vault doesn't open until the Warden is defeated.
- **Landmarks** - smaller structures spread throughout each region. These include things like fossil arches, watchtowers, bell cairns, and crystal formations. They also contain loot and small mechanisms that can be activated.
- **Ruins and micro-sites** - smaller locations scattered around the world. There are several variants, including Intact, Fractured, and Sunken ruins, so exploring never feels completely empty.

### Stronghold

The vanilla stronghold is gone. Instead, the stronghold that the Eyes of Ender lead you to is now a large Endesium citadel. It's roughly three times the size of the original and includes a Great Descent Hall, a portal cathedral with all twelve frames, libraries, crypts, a forge, a dining hall, and a prison. There are sixteen different room types, all connected without dead ends.One important rule is that you won't find Endesium gear in the stronghold. I don't want players to be able to farm the structure and completely skip the mod's progression. I also spent a lot of time on the small details. Things like shelves, candle arrangements, coffins, and other little environmental details might not matter mechanically, but they make the stronghold feel like an actual place instead of a collection of rooms.

## Resonance and progression

Endesium isn't supposed to be about constantly mining for better materials. Most of the progression comes from exploring the new structues and biomes and other stuff

```
EXPLORE -> NOTICE a structure -> CRAFT the Resonance Lens
  -> READ the signal (band + direction, never coordinates)
  -> WAKE the mechanism (Token + fragment + loot)
  -> BUILD the Echo Compass to track stronger sources
  -> GEAR UP from the regions -> KILL THE DRAGON
  -> the End transforms, permanently
```

Waking mechanisms earns Resonance Tokens, and tokens gate the Echo Compass and
other tools.

## Creatures

There are fourteen creatures in Endesium, and they aren't just different models with the same AI. Most of them have their own behavior and combat style.

- **Void Stalker** - The main predator of the End. It watches you, flanks you, and commits when it sees an opening.
- **Dust Crawler** - An armored scarab that creates blinding dust and burrows away when it's close to death.
- **Chorus Stalker** - Can blink behind players while moving through the Chorus Wilds.
- **Marsh Crawler** - A crocodilian ambusher that can pull players with tendrils before pouncing.
- **Lumen Moth** - Mostly peaceful wildlife that follows lantern light.
- **Ash Wraith** - Fires telegraphed ash bolts and becomes more aggressive below half health.
- **Crystal Burrower** - Bursts out of the ground and fires homing crystal shards.
- **Void Ray** - A flying creature that normally glides around but dive-bombs players when provoked.
- **Nullwalker** - A rare creature found in the Umbral Reach. It can mimic sounds, disappear when approached, and suppress mortal vigor.
- **Void Wisp** - A small predator that lures players in before lunging and dragging them.
- **Crown Sentinel** - A large construct with an area slam and a grab attack. You can escape the grab by dealing enough damage, and below half health it repairs its armor.
- **End Warden** - The guardian of each region's flagship structure.
- **End Golem** - A major boss that appears later in progression.
- **Companion Dragon** - A dragon that can hatch from the Ender Dragon's egg and eventually become a mount.

## Bosses and permanent rewards

After defeating a boss, you get cool rewards like dragon's wings, dragon fangs, etc.

### End Wardens (minibosses, x10)

There are ten End Wardens, one for each flagship vault.

Each Warden has the visual style of its region, a region-specific boss bar, and its own signature attack. They can raise their guard to become immune from the front, summon nearby creatures at 66% health, and become enraged below half health.

They have 560 health, deal 26 damage per hit, and have real armor. They also don't simply get staggered by small amounts of damage. Below half health, they regenerate, making the second half of the fight just as important as the first.

Defeating a Warden gives you a Warden Sigil. Carrying one gives you regeneration in the End, while using one permanently gives you an extra heart, up to ten additional hearts.

Attuning all ten regions unlocks Warden Ascendant, which gives you a visible aura and a permanent regeneration pulse.

### The End Golem (major boss)

The End Golem appears where the dragon dies.

It has three phases and attacks using slams, resonance barrages, minions, beam sweeps, shockwaves, arena tethers, and burning ground.

There's also a stagger mechanic. If you deal at least 60 damage within eight seconds, the Golem staggers and takes double damage for five seconds.

The Golem has 720 health and deals 26 damage. Below half health, it starts repairing itself, so simply staying at a distance and slowly chipping away at it isn't enough.

It drops Golem Cores. You can carry them for Resistance in the End or absorb them for permanent bonuses to health and attack damage.

Collecting ten cores unlocks Golem's Resolve, which gives you one chance per day to survive what would otherwise be a death.

The Golem also guarantees a Void Pearl, which lets you perform a short-range teleport. The pearl has durability and works with Unbreaking and Mending.

### The Dragon

After the first kill, the dragon uses a four-phase Endesium controller. Its behavior changes as its health drops, with major enrages at 60%, 35%, and 15%.

Each stage increases things like creature spawns, breath attacks, and scripted events.

You've fought the Ender Dragon before. This time, it fights back.

I also removed the giant on-screen boss titles. The goal is for the arena and the boss's behavior to tell you what's happening rather than having the game constantly announce it.

## The companion dragon

After defeating the dragon, the egg on the fountain isn't just a trophy anymore.

Pick it up, put it back on top of the fountain, and wait ten minutes. Eventually, it cracks open and a baby dragon hatches.

Her name is Ember.

Ember grows through three stages: baby, teenager, and adult. Each stage takes roughly five minutes. Feeding her Ender Pearls allows her to bond with you, and once she's an adult, you can ride her.

Riding her works based on where you're looking. WASD controls her movement, looking up makes her climb, and looking down makes her dive.

Hold space for three seconds and she charges up a magic projectile that fires in the direction she's facing.

Once Ember is bonded to you, she's safe to bring home. She won't behave like the hostile Ender Dragon, and only you, the void, or /kill can hurt her. Her growth and tamed state also survive reloads, so you don't have to worry about losing all that progress when you restart the game.

## Post-Dragon transformation

The first time the dragon dies, the world permanently changes.

This isn't just a temporary event. The transformation is saved in SavedData, so it persists even after restarting the world.

Resonance mechanisms reach farther, the Resonant Archive becomes accessible, the respawned dragon gets its new four-phase fight, the End Golem awakens, and the dragon egg becomes something you can actually use rather than just a trophy.

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



## License

CC0-1.0 (public domain), matching the template this project grew from. See `fabric.mod.json`.
