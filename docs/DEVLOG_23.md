# Devlog 23 - The Stronghold Is Ours Now

hey guys.

for a while now the Endesium identity stopped at the end. everything past the
portal was ours - the island, the dragon, the resonance, the gear - but the
road *to* it was still vanilla's stronghold. a cramped maze of hallways and
libraries copy-pasted a hundred times, holding the exact same end portal the
game shipped with in 2011. it never matched. the mod's big gate got the
smallest room in the game.

that's done now. the vanilla stronghold is gone and the **Endesium
stronghold** replaced it: a buried citadel that previews the end without
spoiling it. this one took a while, so let me walk the whole thing.

## the approach: replace, don't add

the first question was whether to build a *new* structure and shove it
somewhere, or actually take over the stronghold slot. taking over won, and it
was the right call - because the stronghold is the **one structure every
player is guaranteed to visit**. it's the progression gate to the end. making
it ours means making the mod's thesis visible before the portal, not after.

so `EndStrongholdStructure` was written as a full code-built structure (one
`Piece` class, absolute world coordinates, NBT round-trip, same convention as
the ten flagships), and then the override went in where it counts:

- `endesium:end_stronghold` registered as a structure type plus its piece
  serializer in `EndesiumStructureTypes`.
- `data/minecraft/worldgen/structure/stronghold.json` **replaced** - the
  vanilla stronghold now *is* the Endesium one. `/locate structure stronghold`
  finds it, the eyes lead to it, nothing else changes.

that json override had its own little bug hunt: my first draft referenced a
biome tag that doesn't exist in 1.21.1 (`has_structure_stronghold`), which
meant an empty biome set and a stronghold that would never spawn. the real
tag lives at `#minecraft:has_structure/stronghold` in a subfolder, and the
step is `surface_structures` with `bury` terrain adaptation - matching
vanilla's own file exactly, then pointing the type at us.

## the layout: a citadel, not a maze

vanilla strongholds are random. rooms are scattered, corridors dead-end,
libraries repeat, and the portal is wherever the RNG put it. the Endesium
stronghold is **authored**: a fixed, connected, three-times-bigger floor plan
that always works.

- a **Great Descent Hall** buries you - a switchback staircase that drops
  from the surface as much as 90 blocks down into the stone, red runner
  carpet down the aisle, end rods lighting the way, two vindicator sentinels
  waiting at the top.
- a sealed **Arrival Vault** is what the eye of ender actually points at: a
  quiet chamber with a radial purple sigil on the floor and exactly one
  grounded door - the route onward, always.
- the **Hub** is the spine: an atrium with a carpeted floor, a central
  resonance dais (amethyst block, end rod, the works), purpur pillars at the
  cardinal axes, bookshelves and barrels lining every wall, bats circling the
  ceiling, and a *real* staircase up to the gallery instead of a visual
  platform.
- junctions are readable - four grounded route markers plus a carpet cross -
  so you never need signs to find your way.

## the rooms

sixteen distinct room kinds, every one hand-dressed:

- **Library** - bookshelves three shelves high around the walls, two
  lecterns, a chiseled-bookshelf reading dais, blue carpet, bats.
- **Treasury** - iron-bar vault bars with a single gap, chests and barrels
  in rows, gold, candles, and a captain guarding the middle.
- **Guard Post** - weapon racks (fence posts) with candles, a **phantom
  spawner** in the center, two vindicators on the door.
- **Sanctum** - end stone brick floor, purpur pillars, crying obsidian and a
  chorus plant on a pedestal, purple carpet, an **enderman spawner**, a live
  enderman pacing the corner.
- **Barracks** - four red beds with proper head/foot parts, smoker, furnace,
  crafting table, three guards on duty.
- **Scriptorium** - lecterns with candles, bookshelves and chiseled shelves,
  a cauldron of ink, a bat haunting the rafters.
- **Arboretum** - a glass-ceilinged garden: dirt floor, flowering azaleas,
  ferns, moss carpet, a lily pad pond.
- **Observatory** - concentric purpur ring inlays, an amethyst
  telescope-core, end-rod instruments along the walls.
- **Resonance Engine** - amethyst conduit pillars with end-rod emitters and a
  crying obsidian reactor - the end's power plant, studied safely from
  outside the portal.
- **Bastion** - deepslate pillars, an iron-bar partition, weapon racks, a
  captain and two guards. military.
- **Conservatory** - chorus plants and chorus flowers growing on end stone,
  flower pots and moss, an enderman in residence.
- **Reliquary** - concentric obsidian ritual rings, an ender chest at the
  heart, candles, a captain standing watch.
- **Portal Cathedral** - the pay-off: a 3x3 lava pool sunk into a polished
  dais, magma beneath, **all twelve end portal frames** in a perfect ring,
  **always with no eyes** - progression intact, the fight still yours to
  earn. four purpur pillars, purple carpet aisles, a silverfish spawner
  guarding the corner.
- **Crypt** - bone slabs and cobwebs under the hub, soul lantern light, an
  ender chest, reached by a real ladder shaft.
- **Catacombs** - bone pillar alcoves, cobwebs, soul lanterns, a skeleton
  spawner.
- **Starwell** - a vertical shaft with spiral purpur pillars climbing it,
  amethyst clusters and end rods for light columns, an enderman on the floor.
- corridors connect everything, each with one of five variants - red carpet
  runners, storage alcoves, enderman ambushes, ceremonial pillars of light
  toward the portal, or quiet candle niches - and they're **guaranteed
  connected**: the air punches clean doorways through whatever wall they
  meet, which took a full rewrite of the punch-through logic to get right.

## the hard rule: no endesium gear

this was the non-negotiable. the stronghold must preview the end, never spoil
it. so:

- every chest uses one vanilla-flavored loot table (`endesium:chests/stronghold`).
- **no void armor, no void tools, no relics, no endesium anything** can ever
  drop here. grinding the stronghold can't shortcut the mod's progression.
- the mobs are all vanilla guards: vindicator squads (with iron axes, and a
  named 40hp **Stronghold Captain** in chainmail on the hard rooms), phantoms,
  endermen, silverfish, skeletons, a witch, bats. nothing from the mod's
  roster, nothing that drops mod materials.

## the audit that came with it

this wasn't just a new structure. the deep pass over the whole mod surfaced
real bugs, and the biggest one was fatal: **the game wouldn't boot**. the
`LivingEntityMixin` aegis injection targeted `hurt(DamageSource, float)` -
which in 1.21.1 no longer lives on `LivingEntity` (its damage entry is
`actuallyHurt`). the mixin failed its injection scan at startup and the server
died. the aegis gate now targets `Entity.hurt` via a new `EntityMixin`, the
broken injection is stripped from `LivingEntityMixin`, and the server boots
clean - "Done" with zero errors, verified live.

also fixed along the way: the End Warden, Crown Sentinel, and End Golem all
had **duplicate attribute lines** where last-wins clobbered their real stats
down (warden's follow range 64→32, sentinel's 160hp/14dmg/48-range silently
reduced to 60/8/24), the golem's enrage phase summoned *fewer* minions than
phase 1, and an orphaned `ash_boots` model referenced an item that was never
registered.

## numbers

- 1 new structure type (`endesium:end_stronghold`) + piece serializer
- 1 datapack override: vanilla `stronghold.json` now points at the Endesium
  stronghold
- ~3x the vanilla footprint; 23 piece kinds; every room connected, no dead ends
- 12 end portal frames, always eyeless - the vanilla progression path intact
- 0 endesium items in loot - vanilla tables only
- mobs: vindicators (+ named captains), phantoms, endermen, silverfish,
  skeletons, witch, bats - all vanilla
- 1 boot-crash mixin fixed (`hurt` → `Entity.hurt`), 3 boss stat bugs fixed,
  1 orphaned item model removed
- `gradlew build` green; server boots and locates the stronghold cleanly

## what's next

the stronghold finally reads like the end's doorstep. next up is the rest of
the ten-hour pass - every file retyped, every line deliberate - and after
that, the bosses and dragon still want their second pass of polish. but the
gate is fixed. you'll never walk vanilla's cramped hallways to the end again.

go find the eye. the citadel is waiting.
