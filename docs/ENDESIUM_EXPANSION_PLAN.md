# Endesium Expansion Plan

**Milestone:** biome landmark expansion + Dragon encounter 2.0  
**Date:** 2026-08-18  
**Goal:** make every Endesium region feel like a place with a readable history, a reason to explore it, and a mechanically distinct discovery loop.

## 1. Non-negotiable design rules

1. **A structure must tell a story from its silhouette.** No isolated random pillars, square boxes, or decorative noise. Every landmark has a primary shape, a secondary detail, a damaged section, and an intentional focal point.
2. **A structure must have a reason to exist.** It either teaches a biome mechanic, contains a named reward, provides a meaningful resource, or points toward the next region. Pure scenery is explicitly labeled as scenery.
3. **Every landmark is biome-native.** Its palette, vertical profile, damage pattern, and reward belong to the region rather than being a reskinned generic shrine.
4. **Generation must be deterministic and safe.** Candidates are seed-stable, support-tested, rotation-aware, and limited to an owned chunk footprint or a documented cross-chunk contract. Failed support checks must leave no partial structure.
5. **New items are not inventory filler.** Each new item has one clear use, a tooltip, a recipe or loot source, and a reason not to be replaced immediately by vanilla equipment.
6. **The first Dragon remains readable.** The encounter is harder through telegraphs, phase identity, and counterplay—not by silently stacking unavoidable damage.

## 2. Current biome inventory and deficit

The current code has ten custom biomes. End Wastes and Chorus Wilds already have multiple named landmark families. Ashen Expanse, Crystal Barrens, and Void Skirts already have at least two named archetypes in the shared biome feature. The following five regions do not yet have two strong, named, biome-specific structures and receive two new structures each:

| Biome | Existing named content | New structures required | New identity |
|---|---|---|---|
| Shattered Highlands | Skybridge, Summit Shrine | **Rift Observatory**, **Windscar Lift** | altitude, navigation, broken sky machinery |
| Void Marshes | Sunken Temple, small shrine | **Tide-Sunk Bell**, **Mire Reliquary** | submerged memory, unstable ground, slow resonance |
| Luminous Groves | Lightwell, small shrine | **Bloom Conservatory**, **Prism Canopy** | cultivated light, living architecture, vertical paths |
| Void Crown | Void Well | **Crown Observatory**, **Crownstep Procession** | high-altitude void pressure, distant signal tracking |
| Umbral Reach | Void Gate | **Null Archive**, **Hollow Threshold** | erased history, silence, controlled absence |

The two existing structures in a row are counted only when they are recognizable
landmarks with a distinct layout and reward. The generic dispatch feature may
select variants, but it does not excuse a biome from having named identities.

### Structure contracts

Each new structure has:

- a deterministic candidate salt and rarity;
- a support radius and maximum height;
- one entrance or approach direction;
- one focal block or mechanism;
- one loot table or meaningful resource output;
- one biome item/block showcase;
- one advancement criterion or guidebook reference;
- a failure-safe placement path that performs no writes until support passes.

## 3. Structure briefs

### Shattered Highlands

#### Rift Observatory

A 17-block-wide broken circular platform suspended over a natural notch. Four
slate piers support an offset lens chamber; one pier has fallen outward and leaves
a readable gap. The center contains a **Highland Lensstone** and a mechanism
that briefly reveals the direction of the nearest loaded signal. The upper rim is
open on one side so the silhouette reads as an observatory rather than a fort.

- Palette: Highland Stone, Highland Slate, Resonant Pillar, Highland Lensstone.
- Reward: **Skyglass Shard**, Highland Feather, Echo Shard chance.
- Player lesson: the Highlands reward looking outward and upward.

#### Windscar Lift

A tall, broken elevator tower built into a cliff face. Two parallel shafts, a
collapsed counterweight, hanging bridge segments, and a sheltered lower cache
create a strong vertical landmark. The **Windscar Winch** item is found here and
later lets the player pull small entities/items upward a short distance.

- Palette: Highland Slate, End Gray, Windscar Bracket, Void Glass accents.
- Reward: Windscar Winch, grappler parts, directional clue toward the Void Crown.
- Player lesson: vertical traversal is deliberate, not random fall damage.

### Void Marshes

#### Tide-Sunk Bell

A half-submerged bell tower whose lower chamber is flooded. The bell hangs inside
a three-sided frame, with reed-covered buttresses and a visible waterline. Using
the **Mire Bell Clapper** on the mechanism produces a short pulse that marks safe
solid ground around the player without revealing coordinates.

- Palette: Void Marsh Soil, End Clay, Marsh Moss, Tide Iron.
- Reward: Mire Bell Clapper and Marsh Tendril; controlled water-route clue.
- Player lesson: the Marsh is navigated by sound and safe footing.

#### Mire Reliquary

A low, oval reliquary sunk into the mud, approached by three stepping-stone
islands. Its roof is broken open around a suspended core, and the chest is below
the waterline behind a side passage. The focal **Mire Reliquary** block stores a
single charge of the region's safest route pulse.

- Palette: End Clay, Void Marsh Soil, Resonant Basalt, Marsh Moss.
- Reward: **Mireglass**, Crawler Eye, Marsh Tendril, water-breathing utility.
- Player lesson: the best rewards are below the obvious floor.

### Luminous Groves

#### Bloom Conservatory

A three-tier greenhouse grown from Lumen Stone and elder wood. Each tier has a
missing wall, a different light level, and a central living bloom. The player can
harvest a **Lumen Graft** from the upper bloom only after activating the lower
mechanism, making the vertical route meaningful.

- Palette: Lumen Stone, Lumen Moss, Lumen Bloom, Elder Chorus Wood.
- Reward: Lumen Graft and Lumen Wing; temporary light-source utility.
- Player lesson: light in the Groves is cultivated and layered.

#### Prism Canopy

A tall ring of living chorus trunks connected by three translucent bridges. The
bridges are incomplete but visually point toward the center. A **Prism Canopy**
block at the top refracts the Lens pulse and gives a broad elevation hint.

- Palette: Elder Chorus Wood, Hollow Chorus Wood, Void Glass, Pale Crystal.
- Reward: **Prism Seed**, Lumen Dust, a rare Chorus Eye.
- Player lesson: the Groves are vertical and navigable by silhouettes.

### Void Crown

#### Crown Observatory

A wide, wind-scoured crown platform with a slanted telescope-like frame aimed at
the outer End. It has a low shell, two broken support arcs, and one tall mast so
it is visible from a distance without becoming a tower spam problem. The mast
holds a **Crown Needle** that tunes the Void Compass to the nearest Crown signal.

- Palette: Umbral Stone, Void Slate, Void Glass, Void Lamp.
- Reward: Crown Needle and Void Gem; long-range signal clue.
- Player lesson: the Crown is an observation frontier, not a flat biome.

#### Crownstep Procession

A broad staircase that climbs a natural island shoulder and ends at a sealed
three-sided shrine. Half the steps are missing, but the route is readable from
below. The shrine's **Crown Seal** is a one-time activation reward and points the
player toward the Umbral Reach.

- Palette: Void Brick, Void Slate, Umbral Stone, Void Spire.
- Reward: Crown Seal and Void Core chance.
- Player lesson: elevation and procession communicate importance.

### Umbral Reach

#### Null Archive

A low, silent archive made of offset black frames around an empty center. The
interior deliberately contains less than expected: one sealed pedestal, one
broken shelf, and a narrow lightless corridor. The **Null Archive** mechanism
restores one erased inscription and awards the player a Null Fragment.

- Palette: Voidstone, Void Weave, Umbral Stone, Void Glass.
- Reward: Null Fragment, Archive Fragment, **Null Quill**.
- Player lesson: absence is a deliberate form of Endesium history.

#### Hollow Threshold

A massive incomplete doorway sunk into a ravine wall. Its upper lintel is split
and its interior is empty, but a narrow side stair reaches a threshold core. The
core can be activated only after the player has visited another Umbral landmark,
turning the pair into a coherent micro-arc rather than two unrelated chests.

- Palette: Void Brick, Void Weave, Void Lamp, Void Crystal.
- Reward: **Threshold Key**, Void Pearl, final Umbral clue.
- Player lesson: the Umbral Reach is a chain of omissions and locked memories.

## 4. New block set

Add two showcase blocks per new biome. They are intentionally reusable in
structures and available for building after discovery:

| ID | Biome | Purpose |
|---|---|---|
| `highland_lensstone` | Highlands | focal observatory crystal with low light |
| `windscar_bracket` | Highlands | structural metal/stone support block |
| `tide_iron` | Marshes | weathered submerged frame block |
| `mireglass` | Marshes | translucent green-gray waterline glass |
| `lumen_graft_block` | Groves | living bright graft used in conservatories |
| `prism_canopy_block` | Groves | pale refracting canopy accent |
| `crown_needle_block` | Void Crown | tall signal mast block |
| `crown_seal_block` | Void Crown | sealed shrine face / activation focal point |
| `null_archive_frame` | Umbral Reach | dark structural frame with a quiet texture |
| `threshold_core_block` | Umbral Reach | low-light threshold focal block |

All blocks receive blockstates, block models, item models, textures, language
entries, loot tables, and recipes only where a recipe makes progression sense.
The focal blocks are not ores and do not create a new mining tier.

## 5. New item set

| ID | Biome | Function | Source |
|---|---|---|---|
| `skyglass_shard` | Highlands | craft component for the Windscar Winch | Rift Observatory loot |
| `windscar_winch` | Highlands | short, server-authoritative upward pull utility | Rift Observatory recipe |
| `mire_bell_clapper` | Marshes | emits safe-ground pulse around the player | Tide-Sunk Bell loot |
| `mireglass` | Marshes | crafting component and waterline clue item | Mire Reliquary loot |
| `lumen_graft` | Groves | temporary placed light sprout / conservatory key | Bloom Conservatory loot |
| `prism_seed` | Groves | grows one controlled Prism Canopy plant | Prism Canopy loot |
| `crown_needle` | Void Crown | tunes Void Compass signal band | Crown Observatory loot |
| `crown_seal` | Void Crown | one-time progression token for Umbral access | Crownstep activation |
| `null_quill` | Umbral Reach | restores one Inscribed Slate clue | Null Archive activation |
| `threshold_key` | Umbral Reach | opens the paired Hollow Threshold only | Hollow Threshold loot |

Every utility item must define its cooldown, failure feedback, durability or
charge behavior, and multiplayer authority before implementation. Items that
only exist as loot curiosities remain simple materials rather than pretending to
be tools.

## 6. Item model standard

- Materials use the shared GUI parent with a consistent 1.15–1.22 GUI scale.
- Utility tools use the handheld parent with a deliberate silhouette and a
  stable first-person transform.
- Tall items (Crown Needle, Winch, Clapper) use a restrained 3D element model
  only when the silhouette cannot read as a sprite.
- No item receives a random neon outline. Cyan is reserved for active resonance;
  gold is reserved for historical/progression significance.
- Every new model must pass inventory, ground, first-person, and third-person
  review before being called finished.

## 7. Ender Dragon 2.0

### Encounter model

The first Dragon kill remains the permanent transformation trigger. The fight is
improved through a staged state machine that is deterministic, telegraphed, and
safe for multiplayer. The awakened respawned Dragon receives the full version;
the first Dragon receives the readable phase choreography without surprise
Endesium-only attacks.

### Stages

| Stage | Health band | Identity | New behavior | Visual model treatment |
|---|---:|---|---|---|
| I — Waking | 100–75% | familiar, watchful | longer readable approach arcs, single target dive, restrained breath | vanilla silhouette, faint cyan eye/core overlay |
| II — Fracture | 75–45% | damaged and hunting | one wing loses visual brightness, staggered dive, telegraphed ring shockwave | cracked resonance seams on wings and neck |
| III — Resonance | 45–20% | arena-connected | ground fissures, perch pulse, meteor markers, limited safe zones | cyan/gold pulse through body; particles concentrated at joints |
| IV — Answer | 20–0% | desperate, unstable | one signature arena-wide event with a visible safe ring, fewer but stronger attacks | broken crown/wing highlights, darkened body, controlled crimson accents |

For the transformed Dragon, the model treatment is additive and reversible: the
client reads a synchronized stage value, applies emissive overlays and controlled
scale changes, and never replaces the vanilla Dragon geometry with an unrelated
silhouette. The server remains authoritative for health, phase, attacks, and
stage transitions.

### Dragon bug fixes required

- Store fight state per Dragon and reset it when a new Dragon entity is created.
- Never permanently mutate shared attribute instances more than once.
- Clamp transformed health changes so repeated mixin calls cannot multiply max
  health every tick.
- Do not schedule custom attacks before transformation state is loaded.
- Cancel or clear all active zones when the Dragon dies, despawns, or changes
  dimension.
- Make meteor and storm positions use the End arena origin, not accidental
  world-relative `(0, 0)` assumptions.
- Ensure every delayed attack has exactly one impact, even if the target dies or
  disconnects during its telegraph.
- Bound particles and entity scans by a player/arena radius.
- Use a damage source appropriate to each attack and avoid duplicate vanilla
  damage on the same tick.
- Synchronize stage/model state to clients rather than inferring it from scale
  alone.
- Never apply post-Dragon attacks during the first Dragon fight.
- Preserve vanilla death, gateway, XP, and respawn semantics.

### Counterplay requirements

Every major attack has:

1. a visible marker;
2. a minimum reaction window;
3. a safe movement choice;
4. a recovery window or positional advantage for the player;
5. a multiplayer-safe target rule.

No attack may kill a full-health unarmored player from outside the visible arena
without a clearly communicated escape route.

## 8. Implementation order

1. Land this plan and biome inventory.
2. Add blocks/items and registry/data contracts.
3. Add support-safe structure builders and dispatch entries.
4. Add models, textures/reused palette assets, loot, recipes, and translations.
5. Refactor Dragon state into explicit stage transitions and clear attack zones.
6. Add client stage synchronization and renderer overlays.
7. Run build/datagen/resource checks.
8. Run multi-seed structure generation and Dragon headless tests.
9. Perform a live client visual pass and multiplayer fight pass.
10. Update the critical audit with remaining honest blockers.

## 9. Acceptance gates

- Every custom biome has at least two named structures in the current feature
  inventory.
- New structures pass support checks on at least five seeds and four rotations.
- No partial writes occur when support fails.
- Every new registered block/item has complete resources and a clear tooltip.
- Every new item model reads cleanly at GUI and hand scale.
- First Dragon: vanilla gateway/death semantics preserved.
- Awakened Dragon: four stages are visible, bounded, telegraphed, and reversible.
- Two-player test: no duplicated rewards, zones, or cooldown bypasses.
- `./gradlew build`, `./gradlew runDatagen`, resource validation, and worldgen
  QA all pass before the milestone is marked complete.

## 10. Implementation status

The first implementation slice is now landed:

- [x] Added this detailed plan and biome deficit inventory.
- [x] Added ten showcase blocks for the five underrepresented biomes.
- [x] Added nine relic/material items with bounded server-side behavior where appropriate.
- [x] Added clean GUI/handheld item models and block inventory models using the shared Endesium presentation parents.
- [x] Added ten named structure builders and wired them into the biome dispatcher.
- [x] Added structure-specific loot and datagen recipes/translations.
- [x] Added readable four-stage Dragon choreography to the first fight and exclusive resonance attacks to the awakened fight.
- [x] Added stage-aware client Dragon scaling/posture treatment while preserving vanilla geometry.
- [x] Fixed Dragon zone lifetime cleanup, one-shot meteor impacts, storm expiry timing, talon attack timing, and death cleanup.

The art is deliberately palette-compatible in this first slice: the new block
models reuse existing Endesium biome textures while the structure language and
item silhouettes are new. This keeps the palette coherent and the resource tree
small, but a final art pass should replace the most visible aliases with bespoke
textures rather than treating this pass as finished art.

Remaining release gates:

- [ ] Multi-seed inspection of all ten new structure archetypes in a live client.
- [ ] Multiplayer Dragon fight with two or more players.
- [ ] Verify the client Dragon renderer mixin against a packaged, non-development
  client rather than compile-only validation.
- [ ] Add bespoke textures for the hero block/item set.
- [ ] Measure structure density and chunk-generation time after expansion.
- [ ] Add contract tests for rewards, mechanism activation, and paired Umbral
  progression.
- [x] Harden Dragon attack lifecycle, cooldown liveness, stage thresholds, and
  death-hook ordering.
- [x] Cache Dragon fracture geometry and protect expanded Feature writes from
  vanilla portal/arena blocks and existing block entities.
