# Everything Added After `a40e0ed`

Baseline: **`a40e0ed` — "Add devlog 14: void gear, black holes, sonic boom, and client wiring" (08/22 22:25)**
Current head: **`4e734f1`** (08/23 21:16)

**Scale of what would be removed by a hard reset to `a40e0ed`: 1,216 files changed, ~53,200 lines added across 11 commits.**

> Note: `a40e0ed` itself contains **no custom structures** — the only world feature at that
> point was a 7x7 tick-placed ruin in `EndesiumWorld.java`. Every structure in the mod was
> added *after* that commit. A reset to `a40e0ed` removes all structures, all ten biomes,
> all bosses, the dragon endgame, and the guidebook.

---

## 1. Commit-by-commit timeline

| Commit | When | Summary | Size |
|---|---|---|---|
| `7e55da5` | 08/23 10:13 | Fix boss placement and structure spacing — **creates the ten flagship + ten landmark builders**, `BossPlacement`, `EndWardenEntity`, `GolemEffigyItem`, `PostDragonEvents` | 6 files, +2,759 |
| `4e947b3` | 08/23 10:13 | Improve locate command and unlock recipe book entries | 3 files, +416 |
| `6941224` | 08/23 10:13 | Enhance boss models, expand progression guide, add devlog 15 | 6 files, +3,325 |
| `5285ee1` | 08/23 11:59 | Polish pass: mob AI + boss combat fixes, particle rendering, item tooltips, hardened worldgen checks, **the entire asset/data payload** (textures, models, advancements, loot tables, recipes, tools) | 1,171 files, +45,406 |
| `21295cd` | 08/23 14:51 | Migrate structures to the vanilla Structure system (structure JSONs + structure sets), stage-assembled dragon regalia, pure-logic test suite | 66 files, +1,259 |
| `f16f87e` | 08/23 15:24 | Fix stray brace breaking CI; ModelPart scale fields for dragon core | 3 files |
| `a6cee91` | 08/23 15:39 | Translate boss titles and reward feedback, arena center constants, coverage scanner + migration QA gates | 12 files, +235 |
| `1d8fa62` | 08/23 18:45 | *(agent)* Fix /locate pointing at unbuilt sites; first structure redesign | 4 files, +852 |
| `e357a3f` | 08/23 19:20 | *(agent)* Restore the original builder designs; keep the locate fix | 2 files |
| `b530e6f` | 08/23 20:25 | *(agent)* Rebuild structures around the grand canopy silhouette | 2 files, +825 |
| `4e734f1` | 08/23 21:16 | *(agent)* Titan anatomy: roots, boulder body, spoke canopy | 1 file, +310 |

The last four commits are the structure-look iterations from today's sessions.

---

## 2. World generation & structures

### The ten-biome End (regions)
- `world/EndesiumRegions.java` — deterministic continent-scale region lattice (10 regions)
- `world/EndesiumBiomeHolders.java`, `world/EndBiomeProfiles.java` — biome→region mapping and per-region geology profiles
- `world/EndesiumNoise.java`, `world/EndesiumWorldgenSeeds.java` — worldgen noise + seed capture
- `mixin/RandomStateMixin.java` — captures the world seed for deterministic biome selection
- Biome JSONs: `ashen_expanse`, `chorus_wilds`, `crystal_barrens`, `end_wastes` (modified), `luminous_groves`, `shattered_highlands`, `umbral_reach`, `void_crown`, `void_marshes`, `void_skirts`
- Terrain/vegetation features: `BiomeTerrain(+Feature)`, `BiomeVegetationFeature`, `ChorusWildsTerrain(+Vegetation)Feature`, `EndWastesFeature`, plus their configured/placed feature JSONs
- `data/minecraft/worldgen/biome/the_end.json` override

### The 20 custom structures (added in `7e55da5`, migrated in `21295cd`, re-shaped in `1d8fa62`..`4e734f1`)
- **Flagships (10):** `dustCathedral`, `elderwoodSanctum`, `skyrendKeep`, `drownedCathedral`, `lumenCathedral`, `greatCaldera`, `sunkenGeode`, `voidSpire`, `crownObservatory`, `nullArchive` — all in `world/BiomeStructureFeature.java`
- **Landmarks (10):** `duneFossilArch`, `hollowStump`, `windvaneWatchtower`, `mireBellCairn`, `lightwellGazebo`, `emberShrine`, `shardSpireCluster`, `anchorRuin`, `needleCircle`, `nullObelisk` — all in `world/RegionLandmarkFeature.java`
- Vanilla Structure integration: `world/structure/EndesiumFlagshipStructure.java`, `EndesiumLandmarkStructure.java`, `EndesiumStructureTypes.java` (2 structure types + 2 piece serializers)
- 20 `worldgen/structure/*.json` entries + 2 `worldgen/structure_set/*.json` (random_spread placement)
- `world/StructurePlacement.java` — safe write boundary (piece-box clipping, 3x3 region gate, protected blocks)
- `world/EndRuinVariant.java` — mechanism variants tying structures to progression
- Loot tables for every structure chest/barrel (38 chest tables)
- `/endesium locate structure` + `/endesium locate biome` (`command/EndesiumCommands.java`)
- **Locate-truthfulness fix (today):** noise-based `siteValid` in both structure classes so `/locate` only reports sites that actually generate

### Dragon arena
- `world/DragonArenaBuilder.java`, `world/DragonArenaFeature.java`, `world/ArenaGeometry.java` + arena configured/placed feature JSONs

---

## 3. Bosses & mobs

- **End Warden boss** (`entity/EndWardenEntity.java`, 626 lines) + `entity/BossPlacement.java` (footing-validated boss spawns)
- 11 new creatures: `AshWraith`, `ChorusStalker`, `CrownSentinel`, `CrystalBurrower`, `DustCrawler`, `EndGolem`, `LumenMoth`, `MarshCrawler`, `Nullwalker`, `VoidRay`, `VoidWisp` (+ `ProductionVoidStalkerEntity` replacing the old `VoidStalkerEntity`)
- `entity/goal/AirWanderGoal.java`
- Client: 13 model/renderer pairs (Bedrock-format `.geo.json` models for all of them), `EndWardenModel/Renderer`
- Spawn eggs, entity loot tables, entity textures for every mob

---

## 4. Dragon endgame

- `dragon/DragonFightController.java`, `dragon/DragonLoot.java`
- **Resonant Wings / dragon regalia:** `item/ResonantWingsItem.java`, `dragon/ResonantWingsPassives.java`, dragon armor + core models (`EndesiumDragonArmorModel`, `EndesiumDragonCoreModel`, `EnderDragonRendererMixin`, `ElytraLayerMixin`), `dragon_core.png`
- **Sonic boom:** `dragon/SonicBoomHandler.java`, `dragon/SonicCooldownData.java`, `item/VoidBlackHoleManager.java`
- Dragon mixins: `EndDragonFightMixin`, `EnderDragonMixin`
- Post-dragon transformation: `state/PostDragonState.java`, `state/PostDragonEvents.java`

---

## 5. Items, equipment, progression

- ~40 new items: void tool/armor sets (`VoidToolMaterial`, `VoidArmorMaterials`, `VoidArmorItem`, `VoidAxeItem`, `VoidSwordItem`, `VoidPickaxeItem`…), ashen armor (`AshenArmorMaterials`, `AshwalkerBootsItem`), and ability items: `VoidAnchor`, `VoidDash`, `VoidFlare`, `VoidFilter`, `VoidPearl`, `VoidCompass`, `WastesCompass`, `EchoCompass`, `WardenSigil`, `ArchiveKey`, `ArchiveSigil`, `GolemCore`, `GolemEffigy`, `HighlandGrappler`, `LumenLantern`, `CrystalResonator`, `ChorusPruner`, `AshSifter`, `EmberCharm`, `EndCartographer`, `BiomeRelic`, `ResonanceToken`, `ProgressionGuide`
- **Guidebook UI:** `client/screen/EndesiumGuidebookScreen(+Content)`, `ProgressionGuideScreen(+Content)`
- Production replacements for the old slice items (`ProductionResonanceLensItem`, `ProductionVoidShardItem` replace the deleted `ResonanceLensItem`/`VoidShardItem`)
- `state/RecipeUnlockEvents.java`, `state/AttunementState.java`, `state/BossRewardEvents.java`
- 46 advancement JSONs, ~100 recipe JSONs

---

## 6. Blocks & resonance system

- New blocks: `AshenCrustBlock`, `EndPlantBlock`, `InscribedSlateBlock`, `ResonantMechanismBlock(+BlockEntity)`, `VoidGlassBlock`, `VoidOreBlock` + `registry/ModBlockEntities.java`, `registry/ModItemGroups.java`
- **Resonance system:** `resonance/ResonanceManager.java`, `ResonanceSystem.java`, `ResonanceSource.java`, `ResonanceType.java`, `particle/ModParticles.java`, `net/EndesiumPackets.java`

---

## 7. Assets & data (bulk of the 1,171-file polish commit)

- ~55 block textures, ~120 item textures, ~30 entity textures (incl. 10-frame warden set), 5 armor layer textures
- Bedrock geometry models (16 `.geo.json`), blockstates, block/item models for every block and item
- 17 particle JSONs, `sounds.json`
- All chest/entity loot tables, all recipes, all advancements
- `endesium.client.mixins.json` (new client mixin config)

---

## 8. Tests, tooling, docs

- **Tests (4):** `ArenaFractureDistanceTest`, `DragonPhaseThresholdTest`, `ResonanceDirectionTest`, `VoidAnchorExpiryTest` (all passing, 17 cases)
- ~90 `tools/` scripts: texture generators, asset audits, resource validators, server smoke tests (`boot_smoke_server.ps1`, `rcon_smoke.py`, `server_test_*.sh`), structure coverage scanner, noise sims, region parsers
- CI fix (`f16f87e`), coverage scanner + QA gates (`a6cee91`)
- Docs: README rewrite, `docs/CHANGELOG.md`, `docs/ROADMAP.md`, `docs/README.md`, devlog 15, design doc updates, `AGENTS.md` updates
- Build changes: `build.gradle` (test/CI wiring), `gradle.properties`, `fabric.mod.json`, mixin config updates

---

## 9. Removed since `a40e0ed`

- `world/EndesiumWorld.java` (the old tick-placed 7x7 ruin) — replaced by the structure system
- `VoidStalkerEntity` + old model/renderer (replaced by `ProductionVoidStalkerEntity`)
- `ResonanceLensItem`, `VoidShardItem` (replaced by `Production*` variants)
- Foundation test block/item and their datagen outputs
- Old placed features `biome_landmark` / `biome_structure` (superseded by structure sets)

---

## 10. What a reset to `a40e0ed` would delete

Everything in sections 2–9: **all 20 structures, all 10 custom biomes, the End Warden boss and 12 mobs, the dragon arena and regalia, the resonance system, ~40 items, the guidebook, 46 advancements, ~100 recipes, all loot tables, all custom assets, the tests, and the tooling** — roughly 53,000 lines across 1,216 files. The mod would return to: one custom biome (End Wastes), the void stalker, void gear/black holes/sonic boom items, and a 7x7 tick-placed ruin.
