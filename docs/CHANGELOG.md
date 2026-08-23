# Changelog

## Unreleased — Structures Migration & Polish Pass

### Worldgen
- **Flagships and landmarks migrated to registered vanilla Structures.**
  New `endesium:flagship` and `endesium:landmark` structure types (region
  codec field), twenty data-driven structure JSONs, and two `random_spread`
  structure sets (`endesium_flagships` 24-chunk spacing, `endesium_landmarks`
  16-chunk). Generation runs from a single anchor-chunk piece with bounding-box
  write clipping; every candidate logs seam/support rejection reasons.
- Retired the `biome_structure`/`biome_landmark` Feature placements from all
  ten biome JSONs and removed their configured/placed feature files.
- `/endesium locate structure` now resolves through the vanilla registry;
  vanilla `/locate structure` also finds every Endesium anchor.
- Landmarks gained per-column terrain support checks (rejects cliff edges).
- `StructurePlacement` accepts full-strength levels so `/place structure` works.

### Mobs & bosses
- Flying physics fixed: Void Ray, Lumen Moth, and Ash Wraith use
  `FlyingMoveControl`; Void Ray's conflicting ground melee goal removed.
- End Warden special/guard cooldowns actually tick down (they previously froze
  after first use), spectator boss-bar leak fixed.
- End Golem: beam sweep cooldown, self-inflicted upkeep damage no longer feeds
  its own stagger meter, stagger animation reachable, barrage fan real
  (per-axis shard starts), direct entity-type summons.
- Reversible enrage for Void Stalker; hurt animations play on all eight
  creatures that define them; goal cooldowns are poll-frequency independent.
- Marsh Crawler embraces its marshes (no water avoidance, +40% surge speed).

### Visual & UX
- All sixteen particle types render client-side with region-keyed tints.
- Dragon regalia assembles per combat phase (horns, neck bands, plates,
  mantle, braces, tail crown) plus an emissive chest core model.
- Mechanical tooltips on all 24 previously-silent utility items.
- Creative inventory: dedicated tab is canonical; vanilla-tab flooding removed.
- `/endesium dragonstate set` requires `-Dendesium.devcommands=true`.

### Engineering
- Pure JUnit suite: resonance direction buckets, anchor expiry, dragon phase
  thresholds, fracture-distance math (`gradlew test`).
- Shared `AirWanderGoal`, shared fracture-distance helper, recipe-book unlocks
  derived from the registry instead of a hardcoded list.
- Resonance cache prunes unloaded sources; entity data-fixer noise documented.
- Thirteen orphaned prototype assets removed; dust/marsh crawler textures made
  POT (48→64) with matching geo declarations.
- Validators repaired and extended: resource checker asserts the structures
  migration contract; audit script back to 15/15.

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
- Design document: `docs/ENDESIUM_POST_DRAGON_DESIGN.md`.

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
- Design document: `docs/ENDESIUM_ECOLOGY_AND_PROGRESSION_DESIGN.md`.

## Unreleased — QA stabilization pass

- Fixed an intermittent biome-generation failure: `getNoiseBiome` now uses a
  deterministic overwrite instead of a `RETURN` injection, so `end_wastes` and
  `chorus_wilds` generate reliably on fresh worlds.
- Fixed seed `0` being ignored by the worldgen seed capture.
- Fixed `runDatagen` failing on unreferenced biome keys.
- Fixed `tools/validate_resources.mjs` to resolve the `endesium` namespace
  correctly and skip vanilla `minecraft:` references.
- Removed debug probe logging from mixins and feature code.
- Added `docs/QA_REPORT.md`, `docs/COMMANDS.md`, and refreshed developer docs.
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
