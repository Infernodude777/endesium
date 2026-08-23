# Testing Runbook

A copy-paste runbook for a full QA pass on a fresh checkout.

## 0. Prerequisites

- Java 21 (Temurin).
- Fabric Loader + Fabric API for 1.21.1.
- GeckoLib 4.9.2.
- Node.js (for the validation and inspection scripts).

## 0a. Structures migration gates

Flagships and landmarks are registered vanilla Structures now. After any
worldgen change:

1. `gradlew build` — registries and codecs are compile-checked.
2. Boot a dedicated server with RCON enabled
   (`enable-rcon=true`, `rcon.port`, `rcon.password` in `run/server.properties`).
3. Run the coverage sweep from another terminal:

   ```bash
   python tools/scan_structure_coverage.py --password <rcon password>
   ```

   All twenty structures must locate (`located 20/20`).
4. Forceload around one located flagship and probe for its region stone:

   ```
   execute in minecraft:the_end run forceload add <x-48> <z-48> <x+48> <z+48>
   execute in minecraft:the_end if block <x> <y> <z> endesium:<region_stone>
   ```

5. Grep `run/logs/latest.log` for `skipped:` lines — the generation
   diagnostics name the exact rejection reason (biome seam, slope spread,
   unsupportive footprint) for any candidate that did not build.

## 0b. Known-intentional log noise

- `No data fixer registered for endesium:<entity>`: intentional. Fabric offers
  no clean per-entity datafixer registration; entity NBT schemas have never
  changed since introduction, so no migration path is required yet. If an
  entity schema ever changes, add a datafixer or bump the entity id at that
  time — do not silence the log without providing migration behavior.


## 1. Build

```bash
./gradlew build
```

Expect the jar in `build/libs/endesium-*.jar`. Fix any compile errors before
continuing.

## 2. Datagen

```bash
./gradlew runDatagen
git status
```

Generated files under `src/main/generated` should be stable. Unexpected churn
means a provider changed output unintentionally.

## 3. Resource validation

```bash
node tools/validate_resources.mjs
```

Exit 0 means the asset tree is consistent. Exit 1 lists the first broken
reference.

## 4. Headless generation test

```bash
tools/qa_run.sh qa
```

Confirm the log shows locate results for `endesium:end_wastes` and
`endesium:chorus_wilds`, and no `Could not find`. Confirm feature blocks on disk:

```bash
node tools/scan_region_blocks.mjs
```

## 5. Seeds

Repeat step 4 with at least two distinct seeds and compare biome distribution,
ruin and spire placement, and transitions.

## 6. Manual client pass

- Verify item/block textures, the Void Stalker model/animations, particles, and
  sounds.
- Confirm the Lens gives a qualitative reading, not coordinates.
- Fight a Void Stalker in Wastes, Wilds, and near a ruin.

## 7. Persistence

- Activate a mechanism, save, quit, reload, and confirm state + reward persist
  without duplication.

## 8. Log review

```bash
grep -nE "The nearest|Could not find|ERROR|Exception|Invalid|missing" run/logs/latest.log
```

Treat every Endesium error as real, even if the server keeps running.

## N. Boss & Landmark QA (10x overhaul)

1. `/summon endesium:end_warden` in each region: verify regional texture +
   accessory bones (crest/tendrils/halo/horns), boss bar tint, guard stance
   (frontal immunity), signature attack, minion call at 66%, enrage at 50%,
   sigil drop with region tooltip, vault bars retract within 24 blocks on death.
2. Kill a dragon: confirm "Something Older Stirs" title + persistent End Golem
   spawn. Verify phase transitions (bar purple/yellow/red), stagger after 60+
   damage in 8s (double-damage window), beam sweep telegraph in phase 2+,
   arena tether beyond 48 blocks, core drops.
3. Use a Warden Sigil: +1 permanent heart (check with /attribute).
4. Absorb 10 Golem Cores: Golem's Resolve unlock; die to any mob -> survive
   once per day; second death same day should kill.
5. Attune 10 different-region sigils: Warden Ascendant advancement + aura
   particles + regen pulse in the End only.
6. Craft Golem Effigy (dragonbone x4 + void brick x4 + golem core): use in the
   End -> new golem; use again while one lives -> refused.
7. Landmarks: walk each region ~300 blocks; confirm landmark generates with
   loot using the biome table and an intact wakeable beacon mechanism (not
   buried/floating). Wake it for cache payout.
