# Changelog

## Unreleased — 10x Overhaul (Structures, Mobs, Bosses)

### Structures
- New **landmark tier**: ten hand-authored medium builds (Dune Fossil Arch,
  Hollow Stump, Windvane Watchtower, Mire Bell Cairn, Lightwell Gazebo,
  Ember Shrine, Shard Spire Cluster, Anchor Ruin, Needle Circle, Null
  Obelisk) on a ~256-block per-region grid via `RegionLandmarkFeature`.
- Every flagship vault now hosts its biome's mob (spawners or live guards)
  plus environmental hazards (magma crypt floors, ember vents).

### Mobs
- Combat identity pass: Ash Wraith enrage, Crystal Burrower shard volley,
  Void Ray dive-bomb, Dust Crawler blinding dust cloud, Nullwalker
  suppression aura, Chorus Stalker flank blink.
- Two new creatures: **Void Wisp** (Void Skirts lure predator) and
  **Crown Sentinel** (Void Crown slam construct).

### Bosses & Rewards
- **End Warden**: region-adaptive flagship miniboss with ten regional
  palettes and signature attacks; enrages below half health; always drops a
  region-keyed **Warden Sigil**.
- **End Golem**: three-phase major boss that wakes where the Dragon falls;
  boss bar, resonance barrages, shockwaves, minion summons; drops **Golem
  Cores**.
- Permanent rewards: sigil attunement (+1 heart each, max +10), core
  absorption (+1 heart and +0.25 attack damage each). New advancements:
  `wardens_bane`, `sigil_attuned`, `golem_felled`, `core_absorbed`.

## Unreleased — Post-Dragon Transformation

- New persistent world state: `PostDragonState` (SavedData on the End
  dimension) tracks Dragon defeat and the Endesium transformation.
- Dragon defeat detection via `EndDragonFight.setDragonKilled` mixin —
  fires exactly once per world, survives restart, never resets on Dragon
  respawn.
- Transformation event: resonance surge sound, outward particle ring,
  "The End answers" message, and the `dragon_transformation` advancement.
- Resonance awakening: dormant mechanisms radiate farther and stronger after
  the transformation; the new Resonant Archive core becomes the strongest
  detectable signal.
- New landmark: the **Resonant Archive** — a sealed domed hall that generates
  in all worlds but stays inert until the Dragon dies; waking it grants the
  **Archive Sigil** and the `archive_awakened` advancement.
- New item: **Archive Sigil** (epic) — the first post-Dragon token.
- New loot table: `chests/end_archive.json`.
- Development-only command: `/endesium dragonstate get|set` for testing the
  transformation without fighting the Dragon.

## Unreleased — End Ecology and Early Progression

- New flora: **Resonant Bloom**, a pale violet flower with a faint cyan core
  that grows sparsely across the Chorus Wilds (glows at light level 2).
- The **Resonance Token** is now a real reward: every mechanism activation
  drops one token as proof of the discovery.
- New progression item: **Echo Compass** — crafted from a Resonance Token and
  Void Shards. It turns a signal the player has already learned to read into a
  heading and a distance (in tens of blocks), with a short white particle
  trail. It never reveals coordinates.
- New advancement: **Echo Sight** (parent: First Resonance) when the compass is
  obtained.

## Unreleased — QA stabilization pass

- Fixed an intermittent biome-generation failure: `getNoiseBiome` now uses a
  deterministic overwrite instead of a `RETURN` injection, so `end_wastes` and
  `chorus_wilds` generate reliably on fresh worlds.
- Fixed seed `0` being ignored by the worldgen seed capture.
- Fixed `runDatagen` failing on unreferenced biome keys.
- Fixed `tools/validate_resources.mjs` to resolve the `endesium` namespace
  correctly and skip vanilla `minecraft:` references.
- Removed debug probe logging from mixins and feature code.
- Refreshed developer docs.
- No new gameplay content, biomes, bosses, armor, or progression.

## 1.0.1

- production polish completed
- Void Stalker audio polished
- End Ruin rarity and terrain support improved
- Void Stalker state transitions improved
- End Wastes ambient effects added
- Resonance Lens unloaded-source behavior fixed
- foundation test content removed from normal gameplay
- prototype resource leftovers removed

## 1.0.0

- End Wastes and Chorus Wilds
- Resonance system and Resonance Lens
- End Ruins (variants) and the Shattered Spire
- Void Stalker
- First Resonance and discovery advancements
- Resonant materials and production assets
- GeckoLib integration

## [Unreleased] - Dragon fight escalation layer

### Added

- Crystal aegis: the dragon regenerates while 3+ pillar crystals survive (2 HP/s at 5+), making pillar clearing the fight's real first phase.
- Enrage ladder at 60% / 35% / 15% dragon health: announced escalations that spawn void wisp waves (capped at 8 alive) and, from enrage 2, dragon-breath pools under the dragon's flight path.
- Enhanced dragon hoard on death: 1 Dragon Heart, 3 Dragon Fang, 8 Dragonbone, 2 Resonant Dragon Scale at the kill site.
- `/dragonfight` live status command: dragon HP, remaining crystals, enrage level, alive adds.
- Scripted dragon set-pieces that puppet the dragon directly: **Abyssal Burrow** (enrage 1+, dives into the void and erupts beneath a player), **Skyward Seize** (enrage 2+, grabs a player, carries them aloft, hurls them - slow falling mercy below enrage 3), and **Gravity Rifts** (enrage 3, four arena rifts that drag players into their cores). All scale damage, cooldowns, and mercy with the enrage ladder.
- `docs/DEVLOG_16.md`, `docs/DEVLOG_17.md`.

## [Unreleased] - Terrain generation overhaul

### Changed

- Region relief is now a smooth, domain-warped height field instead of stacked per-column offsets: no more terracing, stair-steps, or grid-aligned artifacts anywhere in the ten Endesium regions.
- Every region has its own continuous landform character: ridged highlands, anisotropic ash dunes, sunken marsh basins, flat-topped void mesas, fissured lowlands, rolling wilds.
- Column fills are stratified (ground cap, dithered transition band, substrate) with slope-aware faces - steep grades expose rock, gentle grades keep soil - and a one-block apron plus relaxation pass keeps surfaces continuous across chunk borders.
- Carving only removes loose natural geology (end stone + region ground/substrate) and stops at structure shells or placed machinery, so structures and terrain coexist cleanly.
- Structure builders are untouched: all twenty builds are byte-identical to their previous appearance.

## [Unreleased] - Boss hard mode pass

### Changed
- End Warden: 300 HP (was 80), 16 damage (was 9), 14 armor + 4 toughness, 64-block aggro range (was 32), faster movement, attack knockback, 150 XP (was 35).
- Enrage is now a real phase: damage 20, speed 0.36, permanent for the fight.
- Special cooldowns nearly halved; every special attack hits harder; a second minion wave spawns below one-third health.
- Crown Sentinel: 160 HP, 14 damage, 12 armor + 3 toughness, 48-block aggro range.
- Dragon (final boss): 600 HP (was 200); enrage waves larger and faster; breath pools more frequent.
- Fixed: boss aggro range allowed sniper immunity; enrage had no stat effect; special goals rarely fired; boss XP was trivial.

## [Unreleased] - Gear lines, flagship overhaul, lore book

### Added
- Luminous armor + tools (infinite night vision, light control, prism flash).
- Ash armor + tools (fire immunity, flame cone, eruption ring, heat haze).
- Null armor + tools (slow falling, gravity pulse, void step, effect cleanse).
- Dragon Wings: elytra flight + iron-chest defense + permanent Slowness I; elytra enchant rules only.
- Named honor-guard minibosses in the four flagship builds, wearing and dropping the new gear.
- Hoard caches in each flagship with gear, dragon materials, and resonance tokens.
- Flagships are rarer: spacing 48 (was 24) with separation 20.
- Progression Guide added to the creative menu.
- `tools/gen_gear_textures.py` generates all recolored gear textures from the Void set.
