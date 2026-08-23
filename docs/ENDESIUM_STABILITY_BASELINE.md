# Endesium Stability Baseline

This document records the verified state of the Endesium project after the
full audit and stabilization pass. It is the reference for future development:
any change should be checked against the versions, systems, and test
procedures recorded here before it is considered done.

> Status: **STABLE BASELINE** — build green, datagen green, resources valid,
> fresh-world generation verified across seeds, post-Dragon state verified,
> ecology mobs summonable. Remaining known gaps are listed in
> [Known limitations](#known-limitations) and are intentional, not silently hidden.

---

## Versions

| Component | Version |
|---|---|
| Minecraft | 1.21.1 |
| Java | Temurin 21.0.12 (JDK 21) |
| Fabric Loader | >= 0.19.3 (via `fabric.mod.json`) |
| Fabric API | `*` (latest for 1.21.1) |
| GeckoLib | >= 4.9.2 |
| Endesium | unreleased (dev) |

`JAVA_HOME` used for all builds: `C:\Users\Nikhil\.jdks\temurin-21.0.12`.

---

## Major systems (in this baseline)

- **Seven-biome outer End** (`end_wastes`, `chorus_wilds`, `shattered_highlands`,
  `void_marshes`, `luminous_groves`, `ashen_expanse`, `crystal_barrens`) via
  `EndesiumRegions` (jittered-lattice Voronoi, ~1500-block cells) and
  `TheEndBiomeSourceMixin`. Wastes↔Wilds adjacency is a hard guarantee.
- **Per-biome terrain / vegetation / structure features** (`BiomeTerrainFeature`,
  `BiomeVegetationFeature`, `BiomeStructureFeature`) — chunk-local, seed-deterministic.
- **Central End island** (`ArenaGeometry`, `DragonArenaBuilder`, `DragonArenaFeature`)
  — natural End Stone landmass with dragon arena, pillars, portal.
- **Post-Dragon transformation** (`PostDragonState` SavedData + `PostDragonEvents`,
  `EndDragonFightMixin`, `EnderDragonMixin`, `DragonFightController`, `DragonLoot`).
- **Resonance system** (`ResonanceManager`, `ResonanceSource`, `ResonanceType`,
  `ResonanceLensItem`, `EchoCompassItem`, `ResonantMechanismBlockEntity`).
- **Ecology mobs** (8): `dust_crawler`, `chorus_stalker`, `void_ray`,
  `marsh_crawler`, `lumen_moth`, `ash_wraith`, `crystal_burrower`, `nullwalker` —
  entity classes, GeckoLib models/renderers, loot tables, spawn rules, sounds.
- **Resonant Elytra + Sonic Boom** (`ResonantWingsItem`, `SonicBoomHandler`,
  `SonicCooldownData` SavedData, `ResonantWingsPassives`, `ElytraLayerMixin`,
  `LivingEntityMixin`, `EndesiumPackets.SonicBoomPayload`, `B` keybind).
- **26+ blocks, 10 utility items, 17 ecology drops, 9 spawn eggs**, recipes,
  advancements, sounds, particles, creative tabs.
- **Commands**: `/endesium dragonstate get|set <true|false>` (op level 2, dev-only).

---

## Known intentional warnings

| Warning | Classification |
|---|---|
| `No data fixer registered for endesium:<entity>` (startup) | Expected / harmless — normal for new entity types without a data fixer; cosmetic only. |
| `uses or overrides a deprecated API` (ModBlockEntities) | Dependency-generated — `FabricBlockEntityTypeBuilder` deprecation; no functional impact. |
| `unchecked or unsafe operations` (ModWorldgen) | Development-only — generic array casts; no runtime impact. |

---

## Known limitations

1. **Ecology mob visual assets not yet authored.** The 8 ecology mobs have
   entity classes, GeckoLib model/renderer classes, loot tables, spawn rules,
   and sounds, and **all renderers + attributes are registered** — but their
   `geo/entity/<mob>.geo.json`, `animations/entity/<mob>.animation.json`, and
   `textures/entity/<mob>.png` are **not yet generated**. Until those assets
   exist, the mobs spawn and move but render with a missing GeckoLib model.
   This is the user-owned "textures + models via tooling" track.
2. **Ambient biome sounds reuse vanilla `.ogg` files** (no authored audio yet).
   The sound *events* exist and are wired; the audio content is vanilla.
3. **Functional crafting blocks** (End Workbench, Crystal Resonator, Lumen
   Infuser) are designed but not yet implemented — deferred to a later tranche.
4. **Ground-level visual feel** cannot be judged headlessly; an in-client
   flyover is still recommended for color/lighting/density review.
5. **Retained reference assets**: `geo/entity/void_stalker_v2_*_reference*.json`
   and `void_stalker_v1_reference.geo.json` are intentionally retained
   Blockbench reference exports (documented in the design docs), not shipped
   models. The production model uses `void_stalker_v2.geo.json`.

---

## Test procedures

### Build + datagen
```bash
export JAVA_HOME="C:\\Users\\Nikhil\\.jdks\\temurin-21.0.12"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew clean build          # expect: BUILD SUCCESSFUL
./gradlew runDatagen           # expect: all providers finish, 0 errors
node tools/validate_resources.mjs   # expect: "asset tree is valid"
```

### Fresh-world generation (per seed)
```bash
bash tools/server_test_ecology.sh <seed>   # locates all 7 biomes, force-generates outer End
```
Expected: all 7 biomes locate, no far-chunk/mixin/exception errors (only the
known data-fixer lines).

### Command-driven content test
```bash
# summon every mob
/summon endesium:void_stalker
/summon endesium:dust_crawler
/summon endesium:chorus_stalker
/summon endesium:void_ray
/summon endesium:marsh_crawler
/summon endesium:lumen_moth
/summon endesium:ash_wraith
/summon endesium:crystal_burrower
/summon endesium:nullwalker
# give key items
/give @s endesium:resonant_wings
/give @s endesium:echo_compass
/give @s endesium:null_fragment
/give @s endesium:dust_chitin
# post-Dragon state
/endesium dragonstate get
/endesium dragonstate set true
/endesium dragonstate get
/endesium dragonstate set false
```

### Wastes↔Wilds adjacency check
A compiled Java harness replicating `EndesiumRegions` reported
**0 violations over 192,096,012 block samples across 12 seeds** (see
`tools/` history / this doc). Re-run after any change to `EndesiumRegions`.

---

## Successful test categories (this baseline)

| Category | Result |
|---|---|
| `./gradlew clean build` | PASS |
| `./gradlew runDatagen` | PASS (59 generated files) |
| `tools/validate_resources.mjs` | PASS (117 files) |
| Fresh server, seed 123456789 | 7/7 biomes locate, 0 far-chunk/mixin/exception errors |
| Fresh server, seed 777 | 9/9 mobs summon, item gives OK, dragonstate get/set works |
| Entity attribute registration | PASS — 9/9 mobs spawn without attribute errors |
| Entity renderer registration | PASS — 9/9 renderers registered (client) |
| Mob sounds in `sounds.json` | PASS — 50/50 registered sounds have entries |
| Language coverage | PASS — 0 registered items/blocks without lang |
| Item model coverage | PASS — 0 registered items without item model/texture |
| Block model/texture/blockstate | PASS — 0 missing |
| Loot table references | PASS — all entity loot tables reference valid items |
| Advancement references + lang | PASS — all advancement icons/keys valid, lang present |
| Post-Dragon SavedData | PASS — idempotent, persists, command round-trip verified |
| Sonic Boom | PASS — server-authoritative, persisted cooldown, keybind wired |
| Wastes↔Wilds adjacency | PASS — 0 violations / 192M samples / 12 seeds |

---

## Commands used for testing

See [docs/COMMANDS.md](COMMANDS.md) for the full set. Core ones are in
[Test procedures](#test-procedures) above.
