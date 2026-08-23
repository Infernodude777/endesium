# Endesium Brutal Audit (GLM review pass)

**Audit date:** 2026-08-18
**Reviewer:** GLM reviewer + repair engineer
**Minecraft:** 1.21.1, Fabric loader 0.19.3, Fabric API 0.116.15, GeckoLib 4.9.2
**Standard:** judge the actual player experience. The working tree was treated as
valuable; no reset or revert was performed. Every issue below was verified
against the current source tree, resources, and (where available) run logs.

## Executive verdict

Endesium is a large, ambitious, partially-consistent mod. The commit history
only contains the original vertical slice; the current tree is a much larger
untracked milestone (ten biomes, nine entities, dozens of items, a transformed
Dragon, post-Dragon state). It does not crash at startup **as of this audit's
build**, but it contains one release-blocking wiring failure and several
confirmed player-facing bugs that the documentation either denies or does not
mention.

**Verdict: not release-ready, but materially improved.** The highest-impact
bug (the core progression structures never generating) has been fixed and
**verified by generating a real world on a dedicated server**: ruins, the
Shattered Spire, the Resonant Archive, and the Resonant Monolith now appear
in the region files. Two new findings surfaced during that validation
(cross-chunk writes into far chunks, and noisy startup data-fixer errors) and
are documented below. A live client smoke test is still required before
release.

---

## Severity-ranked issues

### P0 — Release blocking

#### P0-1. End Ruins, Shattered Spire, Resonant Archive, Resonant Monolith, and Wilds Sanctum never generate (confirmed)

The placed features exist:
`src/main/resources/data/endesium/worldgen/placed_feature/{end_ruin,shattered_spire,resonant_archive,resonant_monolith,wilds_sanctum}.json`
and the features are registered (`world/ModWorldgen.java:14-21`), but **no
biome references them**. All ten biome JSONs
(`data/endesium/worldgen/biome/*.json`) list only `biome_terrain`,
`biome_vegetation`, `biome_structure`, and `minecraft:end_gateway_return`.
The `data/minecraft/worldgen/biome/the_end.json` override adds only
`endesium:dragon_arena`. A recursive grep over `src/main/resources/data` shows
these placed features are referenced only by their own configured/placed JSONs.

**Player impact:** the core progression loop is dead in a fresh world:
- No End Ruins → no `ResonantMechanismBlockEntity` variants → no Resonance
  Token → the Echo Compass cannot be crafted (recipe requires the Token).
- No Shattered Spire → `what_remains`, `the_long_resonance`, SPIRE_CORE signal
  unreachable.
- No Resonant Archive → the post-Dragon `archive_awakened` advancement and
  Archive Sigil unreachable.
- `first_resonance`, `fractured_station`, `sunken_archive` advancements
  unreachable.
- The biome `biome_structure` feature builds small shrines/cathedrals, but
  those are not the End Ruin variants and do not carry the mechanism variants.

**Reproduction:** create a world, fly to any Endesium biome, `/locate biome`
works (biomes generate) but no ruin/spire/archive structures exist; the
`end_ruin` feature never runs because its placed feature is not in any biome's
feature list.

**Why it matters:** the mod's own advancement tree, guidebook, and README all
describe a discovery loop that cannot exist in a fresh world.

---

### P1 — High impact

#### P1-1. FRACTURED ruin hidden compartment can never open (confirmed)

`EndRuinLayouts.buildFractured` places the compartment panel at layout offset
`(-5, 1, 2)` (END_GRAY) and barrels at `(5, 1, -3)` and `(-4, 1, 2)`
(`EndRuinLayouts.java:107, 117-118`). But
`ResonantMechanismBlockEntity.openHiddenCompartment`
(`ResonantMechanismBlockEntity.java:157-171`) only searches **axis-aligned**
panel@±5 / barrel@±4 in the same cardinal direction. Rotating `(-5, 2)` by any
multiple of 90° yields `(±2, ±5)` or `(±5, ±2)` — never `(±5, 0)`/`(0, ±5)`.
The INTACT and SUNKEN layouts happen to match; FRACTURED never opens.

**Player impact:** the FRACTURED variant's documented "hidden compartment
behind a slate panel" (its reason to exist over INTACT) is unreachable in every
world.

#### P1-2. Nine particle types have no particle definition → client crash / invisible ambience (confirmed)

`ModParticles.java:19-30` registers 16 particle types; only 7 have
`assets/endesium/particles/*.json`. Missing:
`highland_wind`, `marsh_mist`, `lumen_mote`, `ash_mote`, `crystal_mote`,
`null_distortion`, `void_skirt_mote`, `void_crown_mote`, `umbral_mote`.

These are used as biome ambient particles in 8 of 10 biome JSONs, and spawned
directly by `AshWraithEntity.java:168` (ASH_MOTE), `CrystalBurrowerEntity.java:157`
(CRYSTAL_MOTE), and `NullwalkerEntity.java:157` (NULL_DISTORTION). On the
client, a `SimpleParticleType` without a particle JSON has no sprite entry in
`ParticleEngine`; the registered factory (`ResonanceMoteParticle.Factory`) then
receives a null/empty `SpriteSet` and `setSpriteFromAge` throws (empty-list
index or NPE). Worst case the client crashes when the particle spawns; best
case the biome ambience is silently absent.

**Reproduction:** stand in the Ashen Expanse or near an Ash Wraith with the
client; biome ambient particles (probability 0.02-0.04 per block tick) fire
frequently.

#### P1-3. sounds.json references nonexistent sound files (confirmed by run log)

`sounds.json` uses slash-path entries for vanilla sounds. Most resolve to real
vanilla files, but `minecraft:block/amethyst_block/break` and
`minecraft:block/amethyst_block/step` have no digit suffix (vanilla ships
`break1-4.ogg`, `step1-5.ogg`). `run/logs/latest.log` confirms:

```
File minecraft:sounds/block/amethyst_block/break.ogg does not exist, cannot add it to event endesium:entity.marsh_crawler.hurt
```

Affected events (all silent + warning spam on every boot):
`entity.void_stalker.hurt/death`, `entity.void_ray.hurt/death`,
`entity.marsh_crawler.hurt/death`, `entity.lumen_moth.hurt/death`,
`entity.crystal_burrower.hurt/death`, `entity.nullwalker.hurt/death`.

#### P1-4. Transformed Dragon heals to full health on server restart (confirmed)

`DragonFightController.State` is a mixin `@Unique` field on `EnderDragon`
(`EnderDragonMixin.java:25-28`) and is never persisted. After a restart, the
respawned (transformed) Dragon loads with `state.transformed == false`, so
`DragonFightController.tick` re-applies the buff block
(`DragonFightController.java:151-158`), which includes
`dragon.setHealth((float) maxHealth)` — healing a half-dead Dragon to full.
Phase also resets to 1 and re-announces transitions.

**Player impact:** a server restart mid-fight silently undoes progress on the
hardest fight in the mod.

---

### P2 — Moderate

#### P2-1. Every Resonant Archive container rolls identical loot (confirmed)

`ResonantArchiveBuilder.furnishings` sets constant seeds `1L..5L`
(`ResonantArchiveBuilder.java:243-247`). Vanilla generates container loot from
the stored seed, so every archive barrel/chest in every world rolls the same
result.

#### P2-2. Non-power-of-two texture (confirmed by run log)

`assets/endesium/textures/block/void_gravel.png` is 15x16; the log warns
"limits mip level from 4 to 0". Causes shimmering at distance.

#### P2-3. Dead code and misleading comments

- `DragonArenaBuilder.carveHollows` contains a dead `if (angle % 120 == 0)`
  block (double modulo — never true) plus broken indentation
  (`DragonArenaBuilder.java:229-233`).
- `VoidCompassItem` javadoc contains a stray `private static final int
  MAX_RANGE = 1024;` line inside the comment (`VoidCompassItem.java:21-24`).
- `BiomeStructureFeature.buildVoidGate` is never called (dead archetype).
- `DragonFightController` non-transformed phase titles/branches are unreachable
  (the first fight returns before `updatePhase`); harmless but misleading.
- Placed features `end_wastes_surface`, `chorus_wilds_terrain`,
  `chorus_wilds_vegetation` are registered and shipped but referenced by no
  biome (superseded by `biome_terrain`/`biome_vegetation`).

#### P2-6. Structures write blocks into "far chunks" during generation (confirmed)

A fresh-world dedicated-server boot with a 13x13-chunk forceload around the
located `endesium:end_wastes` produced two errors in the run log:

```
Detected setBlock in a far chunk [-55, -37], pos: BlockPos{x=-879, y=59, z=-592}, status: minecraft:features, currently generating: ResourceKey[minecraft:worldgen/placed_feature / endesium:biome_structure]
```

`BiomeStructureFeature` (and the ruin/spire/archive builders) place
multi-chunk footprints from a single placed-feature invocation. Vanilla's
`ChunkAccess.setBlockState` permits writes within 1 chunk of the generating
chunk and logs this error beyond that. The blocks **do** persist (the region
scan confirms `resonant_mechanism`, `inscribed_slate`, `resonant_pillar`,
`resonant_slate`, `dormant_resonant_crystal`, `barrel` all saved), so content
is not lost in a fresh forceload, but the pattern is fragile: if a neighbor
chunk was already generated and saved in a prior session, a later structure
write into it can be silently lost or corrupt it. This is a systemic design
choice (structures as placed features rather than vanilla Structure system),
not something introduced by this repair pass. Long-term fix: move at least the
large-footprint structures (Shattered Spire, Resonant Archive) to the vanilla
Structure system, or gate each write to the generating chunk's 3x3 region.

#### P2-4. Worldgen structural risks (probable, not confirmed)

- Spire/Archive features write a 23x23 footprint centered at
  `chunk.minX+8`, i.e. `minX-3 .. minX+19` — up to 3 blocks into neighboring
  chunks. During feature placement the 3x3 `WorldGenRegion` is loaded, so
  writes land, but a neighbor chunk's `biome_terrain` pass runs on stale
  heightmaps and can raise/carve around the platform. The support checks
  (68%/68% of a 13x13 area measured at the origin column's y, not per-column
  heights) reduce but do not eliminate floating/clipped structures.
- `EndRuinFeature` support check compares every column against `base.y - 1`
  (the origin column's height), so ruins on rolling islands can float over
  dips or clip into rises despite the 68% gate.
- `structureSlot` in `BiomeStructureFeature` is deterministic per chunk-cell
  hash, so "slot 0/1 named landmarks" are guaranteed per region — but the
  slot value is shared by all 9x9 cells with the same hash; acceptable, but
  unverified visually.

#### P2-5. Documentation contradicts code

- README: "the Dragon receives Endesium's telegraphed stage choreography" —
  false. `DragonFightController.tick` returns before `updatePhase` when the
  post-Dragon transformation is inactive (`DragonFightController.java:103-108`);
  the first fight is deliberately vanilla (design doc agrees, README does not).
- `VoidOreBlock` javadoc says "Drops Void Gems" — the loot table drops the ore
  itself (`loot_table/blocks/void_ore.json`), which is then smelted
  (`recipe/void_ingot_from_void_ore.json`). Obtainable either way; comment is
  wrong.
- `ResonantArchiveFeature` javadoc says "5x5 chunk cell", code uses 7x7.

---

### P3 — Low

- `EndesiumWorldgenSeeds` static seed/captured is never cleared on
  `SERVER_STOPPING`; a second world in the same JVM could briefly use the old
  seed until its `RandomState` is created (always before sampling, so risk is
  theoretical).
- `Resonance` attachment (persistent player resonance level) is registered but
  nothing reads/writes it — dead code.
- No data fixers for custom entities ("No data fixer registered" warnings) —
  harmless today.
- `run/crash-reports/crash-2026-08-18_07.40.07-client.txt` shows the
  Guidebook screen crashing with "screen has not been correctly initialised".
  The current `EndesiumClient` defers `setScreen` to the render thread via
  `Minecraft.getInstance().execute(...)`; that fix postdates the crash report
  and needs a client smoke test to confirm.
- `run/crash-reports/crash-2026-08-18_16.35.49-server.txt` (Watchdog while
  force-loading chunks) and `18.15.01-server.txt` ("Failed to initialize
  server", no stack below `runServer`) are test-harness artifacts; the latter
  could not be attributed to mod code and is re-verified in Phase 4.

---

## Phase 4 validation results

Commands run (all from the repo root, Git Bash on Windows):

| Check | Command | Result |
|---|---|---|
| Compile | `./gradlew compileJava compileClientJava` | PASS (no errors) |
| Datagen | `./gradlew runDatagen` | PASS (0 files written — no drift; earlier failure was a broken mixin inject, fixed and re-run) |
| Build | `./gradlew build` | PASS |
| Resource validation | `node tools/validate_resources.mjs` | PASS (assets valid) |
| Dedicated server boot | `./gradlew runServer` (fresh world, seed 777) | PASS — clean boot `Done (4.4s)`, clean shutdown, no mixin/resource errors |
| Biome selection | `/locate biome endesium:end_wastes` → `[-800, 71, -640]`; `chorus_wilds` → `[800, 71, 800]` | PASS (biome mixin works on a dedicated server) |
| Structure generation | 13x13-chunk forceload at the located wastes, then `node tools/scan_region_blocks.mjs` | **PASS — structures now generate** (see below) |

### Structure generation scan (region-file evidence)

Scanning the generated `DIM1` regions for the forceloaded area
(chunks -58..-42 / -48..-32) found the progression structure blocks saved to
disk:

```
endesium:resonant_mechanism    3 cells   (-55,-38) (-54,-40) (-50,-48)
endesium:inscribed_slate       3 cells   (-55,-40) (-50,-40) (-49,-48)
endesium:resonant_pillar       4 cells   (spire)
endesium:cracked_spire_stone   5 cells   (spire)
minecraft:barrel               6 cells   (ruin loot)
endesium:resonant_slate        5 cells   (archive)
endesium:dormant_resonant_crystal 6 cells (archive)
endesium:end_gray             11 blocks  (monolith)
```

Biomes in the same area: `endesium:end_wastes` (1024 chunk-mentions),
`endesium:shattered_highlands` (16), plus vanilla `the_end`/`small_end_islands`/
`end_barrens`. Terrain/vegetation (`void_grass`, `dust_reed`, `wastes_stone`,
`wastes_gravel`) confirmed. The Wilds Sanctum is wired only into
`chorus_wilds` (not in the forceloaded area) — expected, wiring verified in
JSON.

**P0-1 is fixed and verified.** The remaining server-log errors are the two
far-chunk writes (P2-6) and nine "No data fixer registered" notices (P3),
neither fatal.

## Confirmed-fix plan — status

1. ✅ Wire the five placed features into the ten biome JSONs (structures slot),
   matching each feature's internal cell gating so generation cost stays low.
   Co-location handled deterministically: ruin (3x3 cells) is a strict
   superset of the spire/archive/monolith/sanctum grids, and each feature now
   skips cells that a coarser grid claims.
2. ✅ Move the FRACTURED panel/barrel to axis-aligned positions the compartment
   opener actually checks.
3. ✅ Add the nine missing `particles/*.json` definitions (same
   `minecraft:generic_0` texture the existing seven use).
4. ✅ Replace the invalid `amethyst_block/break` and `amethyst_block/step`
   sound references with the vanilla sound-event names (datagen log: 0
   "does not exist" warnings).
5. ✅ Give the Resonant Archive containers random seeds.
6. ✅ Persist the Dragon fight state on the entity and skip re-healing when the
   transformation buff was already applied.
7. ✅ Remove dead code, fix comments, clear the worldgen seed on server stop,
   correct the README claims.

### Files changed (this repair pass)

- `src/main/resources/data/endesium/worldgen/biome/*.json` (10) — wired
  `end_ruin`, `shattered_spire`, `resonant_archive`, `resonant_monolith`,
  (`wilds_sanctum` in chorus_wilds) into the structures slot.
- `src/main/java/com/infernodude777/endesium/world/{EndRuinFeature,ResonantArchiveFeature,ResonantMonolithFeature,WildsSanctumFeature}.java` — deterministic co-location cell exclusions.
- `src/main/java/com/infernodude777/endesium/world/EndRuinLayouts.java` — FRACTURED compartment fix.
- `src/main/resources/assets/endesium/particles/{highland_wind,marsh_mist,lumen_mote,ash_mote,crystal_mote,null_distortion,void_skirt_mote,void_crown_mote,umbral_mote}.json` — new.
- `src/main/resources/assets/endesium/sounds.json` — invalid sound refs fixed.
- `src/main/java/com/infernodude777/endesium/world/ResonantArchiveBuilder.java` — random container seeds.
- `src/main/java/com/infernodude777/endesium/dragon/DragonFightController.java` + `src/main/java/com/infernodude777/endesium/mixin/EnderDragonMixin.java` — fight-state persistence + no re-heal.
- `src/main/java/com/infernodude777/endesium/world/{DragonArenaBuilder,ModWorldgen,EndesiumWorldgenSeeds}.java`, `src/main/java/com/infernodude777/endesium/item/VoidCompassItem.java`, `src/main/java/com/infernodude777/endesium/block/VoidOreBlock.java`, `README.md` — dead code / comments / docs.

## Issues requiring live client/multiplayer testing

- Guidebook screen after the render-thread deferral fix.
- GeckoLib bone-name mismatches (geo JSON vs animation JSON) — only visible as
  render warnings; needs a client.
- The transformed Dragon fight end-to-end (phases, scale, drops exactly once).
- Dedicated-server behavior of the biome-source `@Overwrite` and the static
  seed capture under real chunk load.
- Ruin/spire/archive visual quality on real island terrain (floating/clipping).
- Multiplayer desync of the resonance source registry across chunk loads.
- Cross-chunk structure writes (P2-6): verify no lost blocks when neighbors are
  generated in a different order / across sessions.
