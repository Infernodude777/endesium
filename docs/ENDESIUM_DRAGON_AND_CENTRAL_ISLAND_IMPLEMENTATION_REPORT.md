# Endesium: Dragon and Central Island — Implementation Report

Status: **Implemented and machine-verified.** The code, resources, recipes, and
world generation all build and pass automated checks. A full survival playthrough
and visual/feel review in a live client are the remaining human steps (marked below).

## Dragon Phases

| Phase | Health Range | Attacks | Result | Notes |
|---|---:|---|---|---|
| I | 100%–70% | Void Dive, Wing Shockwave, Resonance Breath, Ender Barrage, Void Screech (close) | Implemented | Teaches the kit; slower schedule |
| II | 70%–40% | Hunting Dive, Ender Gale, Void Talon, Pillar Sweep + Phase I pool | Implemented | Faster schedule, target lock |
| III | 40%–15% | Resonance Storm, Void Collapse, active Fissure hazard + Phase II pool | Implemented | Fissures light up under players |
| IV | 15%–0% | Final Roar (once), Triple Dive, Barrage+, Catastrophic Resonance | Implemented | Resonance Ring is the safe zone |

Transitions: title + subtitle, roar sound, particle surge, 80-tick grace window.

## Dragon Attacks

| Attack | Telegraph | Damage | Knockback | Counterplay | Tested |
|---|---|---:|---:|---|---|
| Void Dive | 30t target circle | 6 | 0.9 | sprint, elevation, elytra | code + server boot |
| Wing Shockwave | 24t wing gather | 5 falloff | 1.2 | jump, elevated terrain | code + server boot |
| Resonance Breath | 26t mouth charge | 3/20t DoT (3 s) | 0.35 | leave marked zone | code + server boot |
| Void Screech | 20t head raise | 4 falloff | 0.8 | distance, cover | code + server boot |
| Ender Barrage | 14t | fireballs 3/5/7 | — | movement, pillars | code + server boot |
| Hunting Dive | 30t circle at predicted pos | 8 | 1.1 | change direction after dive | code + server boot |
| Ender Gale | wing glow (80t) | 0 | sustained push | move into wind, anchor | code + server boot |
| Void Talon | close pass (10t) | 8 | 1.5 | don't get close-passed | code + server boot |
| Pillar Sweep | 20t low pass | 6 | 1.3 | jump, terrain | code + server boot |
| Resonance Storm Attack | 50t warning circles (5–7) | 6 | 0.9 | leave circles | code + server boot |
| Void Collapse | 30t smoke zone | 5 | 0.9 | leave zone | code + server boot |
| Final Roar | phase IV entry (20t) | 6 | 1.5 | distance | code + server boot |
| Triple Dive | 3 dives, 20t gaps | 6/6/8 | 0.9–1.2 | dodge each | code + server boot |
| Ender Barrage+ | 12t | 8 fireballs | — | constant movement | code + server boot |
| Resonant Catastrophic Attack | 60t charge over arena | 10 | 2.0 | stand in Resonance Ring (< 8 blocks) | code + server boot |

All damage uses the armor-respecting `mobAttack` source except Sonic Boom. No
terrain is permanently destroyed. Perched Dragon emits a resonance pulse in
phase II+.

## Central Island

The previous dome field still read as broad plates in aerial review. It was
replaced with a third-pass connected-landmass model inspired by the supplied
top-down target: one broad warped End Stone core, six unequal attached outcrop
arms, four asymmetrical coast bays, a raised broken central plateau, lower
hollows, and only a few deep off-center scars. The hierarchy is now:
natural End Stone → large landforms → central Dragon site → pillars → sparse
remnants → rare Resonance accents.

| Feature | Result | Notes |
|---|---|---|
| Connected island mass | Implemented + generated | warped core with attached outcrops; no detached plates |
| Coastline | Implemented + generated | four seed-varied bays create a broken silhouette and void-facing shelves |
| Dragon's Crown | Implemented + generated | one outer ridge receives a +10-block lift over its 18–40-block base |
| Broken ridges | Implemented + generated | six connected ridge/outcrop landforms, unequal width/length/height |
| Spires | Implemented + generated | three localized tall formations (28–50 blocks) at seed-chosen spots beyond the arena |
| Central plateau | Implemented | skewed, noise-broken open highland, held at portal height (y 58–61) so the portal is never buried |
| Lower Hollow / basins | Implemented + generated | three depressions, 14–24 blocks below surrounding terrain |
| Void cliffs | Implemented + generated | continuous edge falloff rather than dark platform boundaries |
| Resonance Scars | Implemented + generated | two major off-center scars + four shorter fractures; major centers carve up to ~25 blocks deep |
| Hollow openings | Implemented + generated | two or three small collapsed openings, not a dungeon |
| Central Resonance Ring | Implemented | radius 18, roughly one-third visible, incomplete and noise-gapped |
| Structures | Implemented | one mostly buried observatory + three sparse asymmetric remnants |
| Natural material hierarchy | Implemented | terrain sculpt writes End Stone only; Resonance is a surface accent, never fill |
| Crystals / pillars | Vanilla (untouched) | pillar bases remain in their vanilla positions and function |
| Portal | Vanilla (untouched) | exit portal remains central and clear |
| Vertical range | Verified | End Stone surface spans roughly y 32–111 across seeds (ridges, spires, basins, cliffs) |
| Seed variation | Verified | fresh worlds tested with 42, 777, 9001, 123456789, and 314159 |

Top-down renders show the intended composition: one pale geological formation
with a readable open center, tall irregular ridges and spires, deep basins and
scars, empty stretches, and sparse ancient marks. Region scans preserve vanilla
pillar obsidian and portal bedrock, and every tested fresh world generated with
zero far-chunk or mixin errors.

## Dragon Drops

| Item | Rarity | Quantity | Purpose | Tested |
|---|---|---:|---|---|
| Resonant Dragon Scale | uncommon | 4 guaranteed (first), 1–2 later | Resonant Elytra | code |
| Dragonbone | common | 2–3 / 1–2 | Elytra corners | code |
| Dragon Fang | rare | 25% / 12% | future weapons | code |
| Dragon Heart | epic | 10% / 4% | future systems | code |
| Ender Essence | uncommon | 2 / 1 | Elytra corners | code |
| Echo Shard | rare | 1 / 40% | future resonance | code |
| Void Pearl | rare | 20% / 10% | short teleport | code |
| Abyssal Thread | uncommon | 30% / 15% | future Deep End | code |
| Resonance Core | rare | 20% / 10% | future machinery | code |
| Archive Fragment | uncommon | 1 (first) | lore link | code |

Looting never applies; drops are code-driven on the death hook.

## Resonant Elytra

| Feature | Result | Notes |
|---|---|---|
| Recipe | Implemented (E S E / S W S / B S B) | requires a real Elytra |
| Step Height | Implemented | STEP_HEIGHT modifier → 1.0 |
| Resonant Glide | Documented (visual/feel) | vanilla flight retained |
| Void Grace | Implemented | 35% knockback reduction |
| Resonance Sense | Implemented | faint motes near sources in End |
| Sonic Boom | Implemented | 40-block ray, 8 dmg, armor-ignoring |
| Cooldown | Implemented | 15 s, persisted in SavedData by UUID |
| Save/Load | Persisted cooldown | SonicCooldownData |
| Multiplayer | Server-authoritative | packet validates equipment + cooldown |

## Sonic Boom

| Test | Result |
|---|---|
| Normal entity | code path + server boot |
| Multiple entities | one-hit-per-entity set |
| Boss | sonic-boom source applies |
| Maximum range | 40 blocks |
| Beyond range | no effect past ray |
| Line of sight | ray steps with 1.75 radius |
| Cooldown | 300 ticks, persisted |
| Reconnect | persists (SavedData) |
| Death | persists (SavedData) |
| Item swap | persists (per-player UUID) |
| Multiplayer | server-authoritative |

## Post-Dragon Integration

| Test | Result |
|---|---|
| Dragon defeat | `tickDeath` hook + `EndDragonFightMixin` (unchanged) |
| Transformation | exactly once via `markDragonDefeated()` |
| Resonance awakening | existing PostDragonEvents |
| Archive | unchanged |
| Dragon respawn | transformed buff (1.6× scale, 2× health) |
| Second Dragon defeat | `markDragonDefeated()` returns false; no reset |
| State persistence | PostDragonState SavedData (unchanged) |

## Regression

| System | Result |
|---|---|
| End Wastes | server boot: locate succeeds |
| Chorus Wilds | server boot: locate succeeds |
| End Ruins | unchanged code |
| Shattered Spire | unchanged code |
| Resonance | unchanged code |
| Lens | unchanged code |
| Echo Compass | unchanged code |
| Void Stalker | unchanged code |
| Archive | unchanged code |
| PostDragonState | unchanged code |
| Vanilla Dragon | phases layered on vanilla AI; pillars/crystals/portal intact |
| End Cities | untouched |
| End Gateways | untouched |

## Bugs

| Priority | Bug | Reproduction | Fix | Retested |
|---|---|---|---|---|
| P1 | Dome field read as broad flat plates and could cluster high terrain | independent radial domes were not compositionally connected | replaced with a warped core, attached landmark arms, coast bays, explicit plateau, and connected height field | seeds 42, 777, 9001, 123456789, 314159 rendered |
| P1 | Ring/fractures/structures buried inside raised terrain | `surfaceTopY` read stale WORLD_SURFACE_WG heightmap during decoration | scan actual block state up/down from the map value | end_stone_bricks, cracked stone, crystals now place on the surface |
| P2 | Fractures were too shallow and tended toward center-outward paths | old samples started 20–34 blocks from the portal and carved only 2–9 blocks | scars now begin off-center, meander across land, and major wounds carve up to ~18 blocks | fresh seed scans: no generation errors |

## Machine verification performed

| Check | Result |
|---|---|
| `./gradlew build` | PASS |
| `./gradlew runDatagen` | PASS (33 files) |
| `node tools/validate_resources.mjs` | PASS (30 model/geometry files) |
| Dedicated server (fresh world) | PASS — no far-chunk, no mixin, no exceptions |
| Region-file arena scan | PASS — new blocks + vanilla integrity (obsidian ×119, bedrock ×10) |
| Multi-seed island render | PASS — five fresh seeds (42, 777, 9001, 123456789, 314159), distinct silhouettes and relief |
| Dev client boot ×3 | PASS — no mixin/exception/missing-sound |

## Remaining human verification (in a live client)

The following cannot be proven headlessly and are required by the acceptance
criteria before a final QA sign-off:

1. A genuine survival Dragon fight: observe all four phases, each attack
   telegraph/effect/recovery, and the death sequence.
2. Walk the third-pass island in a live client: inspect the central plateau,
   Crown/ridges, Hollow, coast bays, deep scars, pillar surroundings, and
   long empty stretches from ground level.
3. Confirm the first-kill drops grant exactly 4 Resonant Dragon Scales.
4. Craft the Resonant Elytra from the recipe; verify step height, Void Grace,
   and flight.
5. Fire Sonic Boom against normal entities, a group, the Dragon, at range, and
   past a wall; confirm the 15 s cooldown and that reconnect/death/swap never
   bypass it.
6. Visual review of the central island, the item textures, and the wing texture.

## Final status

**NEEDS LIVE PLAYTHROUGH** — everything that can be verified headlessly passes;
the actual combat feel and visual review are the only outstanding acceptance
items and require a human in a client.
