# Endesium biome structures, content, and Dragon 2.0 plan

## Scope and baseline

The current working tree already defines ten Endesium biomes and a shared, biome-dispatching worldgen feature. This plan does not replace that work. It turns the existing collection into a deliberate content pass with a measurable minimum: **every custom biome has at least two named, recognizable structures**, and every structure has a visual language, a gameplay purpose, and a bounded reward.

The implementation remains vanilla-compatible:

- the End biome source continues to select Endesium biomes without replacing the End dimension;
- structures are placed only in their matching biome and outside the central Dragon arena;
- structure writes are protected from bedrock, obsidian pillars, gateways, the portal, and existing block entities;
- rewards are server-side and one-time where progression requires discovery;
- the Dragon controller layers readable attacks on top of vanilla flight, phase, perch, death, gateway, and respawn behavior.

## Structure coverage matrix

| Biome | Structure 1 | Structure 2 | Meaning and reward direction |
|---|---|---|---|
| End Wastes | **Dust Cathedral** | **Fallen Spire** | A collapsed listening station and the remains of a navigation tower. Lens clues, Wastes materials, and an introductory resonance reward. |
| Chorus Wilds | **Rootbound Archive** | **Elder Shrine** | The builders attempted to cultivate life here. Chorus-root materials, a living landmark, and a clue toward the Wilds progression. |
| Shattered Highlands | **Rift Observatory** | **Windscar Lift** | The highlands were used to watch and traverse the broken terrain. Lensstone and controlled vertical-movement rewards. |
| Void Marshes | **Tide-Sunk Bell** | **Mire Reliquary** | The marsh remembers sound and preserves objects below its waterline. Water traversal, mud/glass materials, and a buried clue. |
| Luminous Groves | **Bloom Conservatory** | **Prism Canopy** | The light is cultivated rather than natural. Lumen grafts, prism materials, and a readable route through the canopy. |
| Ashen Expanse | **Dormant Volcano** | **Burnt Citadel** | The biome is a cooled catastrophe around a surviving fortress. Ashwalker/ember progression and a high-risk cache. |
| Crystal Barrens | **Crystal Heart** | **Crystal Landmark Field** | Resonance has mineralized into a living geological signal. Crystal blocks, tuning materials, and a stronger source. |
| Void Skirts | **Void Monolith** | **Void Spire** | The first dark-edge markers of the deeper End. Void building materials and a safe, directional signal. |
| Void Crown | **Crown Observatory** | **Crownstep Procession** | The Crown is an approach, not a destination: its observatory points inward and its steps mark a missing path. Crown relics and route clues. |
| Umbral Reach | **Null Archive** | **Hollow Threshold** | The Reach contains deliberate absences and sealed transitions. Null/threshold relics and the late-game direction toward the Archive. |

The existing Shattered Spire, Resonant Archive, Wilds Sanctum, and central Dragon arena remain **landmarks**, not substitutes for the two-biome-structure minimum. Their placement rules and progression gates are independently tested.

## Per-structure construction rules

Each structure follows the same six-part recipe:

1. **Silhouette:** one primary shape visible from a distance; no random block scatter.
2. **Material hierarchy:** local biome stone for mass, one contrasting structural material, and one restrained resonance/light accent.
3. **Damage story:** missing arches, fallen beams, buried floors, waterlines, collapsed corners, or incomplete bridges explain the ruin.
4. **Interaction:** one mechanism, cache, inscription, or traversal affordance gives the structure a reason to exist.
5. **Footprint safety:** flatten only the intended footprint, never erase vanilla arena infrastructure, and refuse to overwrite block entities.
6. **Determinism:** use the feature's seeded random source; do not use global mutable randomness or cross-chunk writes.

## Blocks and items

Content is added in biome families rather than as disconnected collectibles.

- **Wastes:** Wastes Stone, Wastes Gravel, Dust Reed, Void Grass, and Wastes Compass.
- **Wilds:** Elder Chorus Wood/Bark, Chorus Root/Moss, Prism Canopy, and Chorus Pruner.
- **Highlands:** Highland Stone/Slate, Lensstone, Windscar Bracket, and Highland Grappler/Windscar Winch.
- **Marshes:** Marsh Soil, Tide Iron, Mireglass, Marsh Moss, and Mire Bell Clapper.
- **Groves:** Lumen Stone/Moss/Bloom, Lumen Graft, and Lumen Lantern.
- **Ash:** Ash Stone/Soil/Crust, Ashen Ember, Magma Core, Ash Sifter, Ember Charm, and Ashwalker Boots.
- **Crystal:** Pale/Dark Crystal blocks, Crystal Cluster, Crystal Resonator, Crystal Core, and Crystal Fang.
- **Void families:** Void Slate/Brick/Glass, Umbral Stone/Grass, Void Spire/Lamp/Crystal, Void materials, and the late-game utility items.

Every registered block item and item has:

- a translation;
- a generated or hand-authored model with a clear 16x16 inventory silhouette;
- a texture that follows the Endesium palette and does not rely on full-body neon glow;
- a recipe or intentional structure/loot source;
- a loot table where breaking the block should drop itself or its intended resource.

The asset pass uses simple parent models for cube blocks, cutout parents for plants, and hand-authored transforms for tools, relics, compasses, and medallions. Item models are validated for missing textures, invalid parents, and dangling model references.

## Dragon 2.0 stages

### Stage A — readability and safety

- Keep vanilla DragonFight authoritative for spawning, crystals, perch, death animation, exit portal, gateways, and respawn.
- Add four health bands at 75%, 45%, and 20%, with a short transition grace window.
- Telegraph every custom attack before damage; use server-side damage sources and one impact tick.
- Cancel attacks and clear hazard zones when the target dies, disconnects, or the Dragon enters death animation.
- Ensure first-kill loot is granted exactly once per death and transformation state is changed only by `EndDragonFight.setDragonKilled`.

### Stage B — encounter depth

- Phase 1: distant dives, breath zones, and a readable wing shockwave.
- Phase 2: hunting dives, talon passes, gale pressure, and perch pulses.
- Phase 3: storm markers, collapsing ground markers, and arena fissure hazards.
- Phase 4: final roar, limited catastrophe, and a recovery window after heavy attacks.
- Avoid unbounded particle loops and avoid applying the same effect every tick to every player.

### Stage C — post-Dragon awakening

- Persist the first Dragon defeat in `PostDragonState`.
- On later Dragon respawns, apply a bounded visual scale and a stronger but still telegraphed attack pool.
- Add Void Rift, Resonance Howl, and Meteor Shower only to the awakened fight.
- Keep the transformation permanent across restart and repeated Dragon kills; never duplicate the announcement or first-kill bundle.

### Stage D — visual model and asset polish

The Dragon's apparent model evolves through renderer state rather than replacing vanilla geometry:

- **Dormant/first fight:** vanilla silhouette with restrained Resonance Cyan pulses.
- **Phase 2:** brighter eye/core accents and a subtle wing-edge tint.
- **Phase 3:** fractured cyan seam overlay and stronger particle wake.
- **Phase 4:** controlled resonance crown and short-lived attack telegraphs.
- **Awakened respawn:** a larger, darker overlay/scale treatment with unique phase tinting.

The renderer must use client-only classes, must not load client types from common code, and must reset transient shader/pose state after each render. No stage may make the Dragon permanently emissive or obscure hit readability.

## Bug-fix and acceptance gates

### Compile and registration

- `./gradlew build` succeeds with Java 21.
- Common source never imports a client-only class.
- Every referenced feature, particle, sound, item, block, loot table, recipe, model, texture, and mixin exists.
- Each mixin target resolves in the mapped Minecraft 1.21.1 jar.

### Worldgen

- A fresh End selects all ten custom biomes without central-island replacement.
- Each custom biome can generate both named structures over many seeds.
- No structure writes bedrock, obsidian pillars, gateways, the portal, or an existing block entity.
- Structures remain coherent at chunk boundaries and do not repeatedly regenerate on reload.
- The Archive remains sealed until the Dragon defeat state is active.

### Dragon fight

- First fight retains vanilla death, portal, gateway, and respawn behavior.
- Custom attacks have telegraph, impact, recovery, and cooldown windows.
- Dead/disconnected targets cannot leave armed zones behind.
- The Dragon is not double-sized on the first tick, and its health is not repeatedly reset.
- First-kill loot and post-Dragon transformation fire once; later kills do not duplicate first-kill rewards.
- Renderer stage transitions agree with server phase thresholds.

### Asset quality

- Item models render in inventory, hand, ground, and GUI contexts.
- Cutout plants do not render as opaque cubes.
- No model references a missing texture or a deleted prototype asset.
- The final resource audit reports zero missing required assets.

## Implementation order

1. Run the baseline compile/resource audit and repair blockers first.
2. Add/verify this structure matrix and feature dispatch coverage.
3. Complete registrations, loot, recipes, translations, and item/block models.
4. Harden Dragon lifecycle hooks and staged controller behavior.
5. Run build, datagen/resource validation, and headless smoke tests.
6. Only then perform visual playtesting for structure silhouettes, model readability, and Dragon telegraph timing.
