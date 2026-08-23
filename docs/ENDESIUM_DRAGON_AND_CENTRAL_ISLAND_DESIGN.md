# Endesium: Dragon and Central End Island Design

Status: **Design for the Dragon + central island milestone.** This document defines
the overhaul of the central End island and the Ender Dragon fight. It builds on the
completed Post-Dragon Transformation and is deliberately NOT the Deep End.

## 1. Design goals

1. The central island must read as an ancient, inhabited place — not a flat disc with
   randomly scattered blocks.
2. The Dragon fight must be a **signature Endesium encounter**: readable phases,
   telegraphed attacks, real counterplay, and a dramatic death.
3. Difficulty comes from movement, positioning, attack recognition, terrain, and crystal
   management — never from a giant health sponge.
4. The fight stays compatible with vanilla: portal, crystals, respawn, gateways, and the
   Dragon Egg all keep their vanilla behavior.
5. The island must contain the first visual hints that the void beneath the End is not
   empty (a controlled "Hollow" layer), without implementing the Deep End.
6. Everything stays server-authoritative; the client only ever sees effects.

## 2. Central island generation

The island keeps the vanilla terrain generator (no island reshaping, no retroactive
terrain edits). An additive **arena decoration feature** runs chunk-locally across the
9x9 chunk region around the world origin and only ever writes inside its own chunk
column, only on solid End stone, and never over vanilla blocks.

Layout (radii from the exit portal at 0,0):

| Radius | Feature |
|---|---:|
| 0–4 | Vanilla exit portal + platform (untouched) |
| 12–14 | **Resonance Ring** — inscribed slate ring, eye symbols at cardinals |
| 17–22 | **Inner Terrace** — End Gray ring with resonant slate steps |
| 26–33 | **Mid Terrace** — cracked stone / brick ring with fallen pillar stubs |
| 36–40 | Gap (vanilla obsidian pillars at radius 42 stay untouched) |
| 42 | Vanilla obsidian pillars + crystals (untouched) |
| 48–58 | **Outer Wall** — broken circular wall with eight deliberate gaps |
| 14→44 | **Six radial fissures** (60° apart), 2–3 deep trenches lined with slate |

The island is read as three layers:

- **The Crown** — the arena, terraces, resonance ring, wall.
- **The Fractures** — the radial fissure trenches; dangerous floor geometry and cover.
- **The Hollow** — four sealed pits around the outer wall: carved 3×3 shafts lined with
  End Gray, floored with inscribed slate, containing a dormant crystal. Nothing more is
  revealed yet; the player sees that *something* is below.

## 3. Central arena

The arena is the space inside the Resonance Ring. The ring is visual only before the
Dragon's death; after the transformation it is the safe zone for the signature final
attack (Catastrophic Resonance), which makes the ring's purpose discoverable during the
fight that unlocks it.

## 4. Dragon phases

Every fight (first and respawned) uses four phases based on health fraction:

| Phase | Health | Identity |
|---|---|---|
| I | 100%–70% | Familiar Dragon; teaches the new attacks (dive, shockwave, breath, barrage) |
| II | 70%–40% | Hunter: hunting dive, gale, talon, sweep; faster schedule |
| III | 40%–15% | Destabilization: fissures activate, resonance storm, void collapse |
| IV | 15%–0% | Desperation: final roar, triple dive, barrage+, catastrophic resonance |

Transitions are loud and brief: title, roar, particle surge, and a short grace window.
No transition is instantaneous and none reset fight progress.

## 5. Dragon attacks

All attacks are server-side, telegraphed, and damage via `mobAttack` (armor applies).

| Attack | Phase | Telegraph | Effect | Counterplay |
|---|---|---|---|---|
| Void Dive | I+ | Rise + target circle | 6 dmg, up-knock, radius 4 | Sprint sideways, elevation, elytra |
| Wing Shockwave | I+ | Wing gather | 5 dmg + knock, radius 16 | Jump, fly, elevated terrain |
| Resonance Breath | I+ | Mouth charge | 3 dmg/20t DoT zones, 3 s | Leave the marked zones |
| Void Screech | I+ | Head raise | 4 dmg + knock, radius 32, falloff | Distance, cover |
| Ender Barrage | I+ | Wing flick | 3–5 fireballs | Move, pillars as cover |
| Hunting Dive | II | Dive at predicted pos | 8 dmg | Change direction after the dive starts |
| Ender Gale | II | Wing glow | No damage; sustained push | Move into the gale, anchor terrain |
| Void Talon | II | Close pass | 8 dmg + heavy knock | Don't get close-passed; pillars |
| Pillar Sweep | II | Low pass | 6 dmg + knock, body radius | Jump, terrain, reposition |
| Resonance Storm | III | 50 t warning circles | 6 dmg, radius 3.5 per strike | Leave marked circles |
| Fissure Hazard | III+ | Passive | 2 dmg on active fissures | Don't stand on fissure lines |
| Void Collapse | III | Target zone | 5 dmg zone + collapse visuals | Leave the zone |
| Final Roar | IV (once) | Landing + roar | 6 dmg + knock, radius 40 | Distance; transition moment |
| Triple Dive | IV | 3 sequential dives | 6/6/8 dmg | Dodge each dive; gaps between |
| Barrage+ | IV | — | 12 mixed fireballs | Constant movement |
| Catastrophic Resonance | IV | 60 t charge over the arena | 10 dmg radius 8–32 | Stand inside the Resonance Ring (radius < 8) |

Perched Dragon: a resonance pulse (3 dmg + knock, radius 24) every ~80 ticks once the
Dragon is in phase II+; perching remains the best melee window but is never safe.

## 6. Targeting

Server-authoritative: nearest living player within 160 blocks, with a 120-tick target
lock. The Dragon does not swap targets every tick; players can read when they are hunted.

## 7. Crystal behavior

Vanilla crystals remain fully vanilla (heal the Dragon, destroyable). No special
crystal mechanics this milestone; the spec's optional resonance-crystal ideas are
documented for a future pass.

## 8. Terrain interaction

The Dragon never permanently edits terrain. Fissure hazards, storm zones, and collapse
zones are damage zones plus particles. The death sequence spawns a resonance wave and
particles but does not carve the island.

## 9. Multiplayer

Targeting and damage are server-side. The transformation remains a world-level event
(existing PostDragonState). Rewards are dropped at the death site, not granted to
specific players, so no duplication is possible.

## 10. Dragon drops

First kill (guaranteed): 4 Resonant Dragon Scales, 2–3 Dragonbone, 2 Ender Essence,
1 Echo Shard, 1 Archive Fragment. Random rolls: Dragon Fang 25%, Void Pearl 20%,
Abyssal Thread 30%, Resonance Core 20%, Dragon Heart 10%. Later kills: 1–2 scales and
reduced rolls. Looting never applies (drops are code-driven on the death hook).

| Item | Rarity | Purpose |
|---|---|---|
| Resonant Dragon Scale | uncommon | Resonant Elytra (4 required) |
| Dragonbone | common | Elytra corner, future crafting |
| Dragon Fang | rare | future weapons |
| Dragon Heart | epic | future major systems |
| Ender Essence | uncommon | Elytra corners |
| Echo Shard | rare | future resonance systems |
| Void Pearl | rare | short-range safe teleport, 10 s cooldown |
| Abyssal Thread | uncommon | future Deep End component |
| Resonance Core | rare | future machinery |
| Archive Fragment | uncommon | lore link to the Resonant Archive |

## 11. Resonant Elytra

Upgrade of the existing Resonant Wings (now named **Resonant Elytra**):

- Recipe (shaped): `E S E / S W S / B S B` — S = Resonant Dragon Scale, W = vanilla
  Elytra, E = Ender Essence, B = Dragonbone. Requires a real Elytra.
- Passives: step height raised to 1.0 (STEP_HEIGHT attribute modifier), knockback
  reduced 35% while worn (Void Grace), and a subtle Resonance Sense — faint particles
  when a meaningful resonance source is near. Flight and the custom wing texture are
  already implemented.
- **Sonic Boom** (signature active): keybind (B) sends a C2S packet; the server fires a
  40-block ray, 8 damage via the sonic boom damage source, strong knockback, particles
  and a distinct sound. Cooldown is **15 s, server-authoritative, persisted in SavedData
  keyed by player UUID** — item swaps, reconnects, and death cannot bypass it.

## 12. Post-Dragon connection

The fight uses the existing PostDragonState + PostDragonEvents exactly once per world.
Respawned Dragons are larger (1.6x scale, 2x health) and carry the crimson tint and the
full four-phase kit. Killing a respawned Dragon never resets progression.

## 13. Future Deep End connection

The Hollow pits are the only hint: sealed shafts with dormant crystals. The death
sequence's resonance wave visually connects the fight to the already-implemented
Archive awakening. Nothing about the Deep End is implemented here.

## 14. Balance targets

- First fight: vanilla health; difficulty from the new attacks only.
- Attack damage 3–10, all telegraphed, all avoidable.
- Sonic Boom: 8 damage / 15 s cooldown; strong but situational.
- No infinite flight, no invulnerability, no teleport spam (Void Pearl: 10 s cooldown,
  destination must be safe).

## 15. Performance requirements

- No per-tick global scans: the fight controller is per-Dragon and player iteration is
  bounded by online players.
- Attack zones are ticked only while active and capped (≤ 6 breath zones, ≤ 7 storm
  circles, one gale).
- Particles are burst-based; no continuous emitters except the Dragon's existing motes.

## 16. Technical architecture

- `DragonArenaFeature`/`DragonArenaBuilder` — chunk-local arena decoration (feature runs
  in `|cx| ≤ 4`), shared geometry constants in `ArenaGeometry`.
- `DragonFightController` + `EnderDragonMixin` — the four-phase state machine and
  scheduler; the mixin stays a thin delegate.
- `DragonLoot` — code-driven death drops on the existing `tickDeath` hook.
- `EndesiumPackets` + `SonicBoomHandler` + `SonicCooldownData` — the Sonic Boom
  networking and persisted cooldown.
- `ResonantWingsPassives` — step height, resonance sense; `LivingEntityMixin` adds the
  knockback grace.
- `ModItems`/`ModItemGroups`/datagen — the ten new materials, textures, and language.

## 17. Testing requirements

- Fresh world, existing world, post-Dragon world, respawned Dragon world.
- Every attack: telegraph, damage, knockback, duration, recovery, counterplay.
- Multiplayer: two players at different elevations and distances.
- Sonic Boom: entity, multiple entities, boss, range, line of sight, cooldown,
  reconnect, death, item swap.
- Multiple seeds; region-file scan verifying the arena and vanilla integrity.

## 18. Explicitly out of scope

Deep End implementation, final boss, Endesium armor, Voidsteel armor, Momentum combat,
Resonance Storms as a world system, new dimensions, and complete End replacement.
