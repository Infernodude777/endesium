# Endesium Testing Runbook

This runbook is a step-by-step checklist for validating Endesium after a
content pass. It is intended to be run in order.

## 0. Build verification

- [ ] `./gradlew compileJava compileClientJava` succeeds.
- [ ] `./gradlew build` produces a jar with no duplicate-resource errors.

## 1. Data load

- [ ] Launch a dev client with a fresh world.
- [ ] Confirm no recipe errors appear in the log.
- [ ] Confirm no advancement errors appear in the log.
- [ ] Open the recipe book and confirm the new recipes are listed.

## 2. Crafting pass

- [ ] Craft a Wastes Compass and confirm it points toward the set target.
- [ ] Craft a Highland Grappler and confirm the pull works.
- [ ] Craft a Lumen Lantern and confirm it emits light.
- [ ] Craft a Void Filter and confirm underwater breathing works in marshes.
- [ ] Craft a Crystal Resonator and confirm it tunes clusters.
- [ ] Craft an Ash Sifter and confirm it recovers embers.
- [ ] Craft a Chorus Pruner and confirm clean harvests.
- [ ] Craft an Archive Key and confirm it opens the Archive.
- [ ] Craft a Void Flare and confirm the signal.
- [ ] Craft an End Cartographer and confirm biome tracking.
- [ ] Smelt Voidstone and Umbral Shards into Void Ingots.
- [ ] Craft the alternate dragonbone tools and weave-lined armor.

## 3. Advancement pass

- [ ] Use `/give @s endesium:void_shard` and confirm the toast fires.
- [ ] Grant each new item and confirm the matching advancement completes.
- [ ] Confirm advancement titles and descriptions are localized (no raw keys).

## 4. Worldgen pass

- [ ] Fly to each of the ten biomes and confirm terrain and structures.
- [ ] Confirm Endesium stations activate with the Resonance Lens.
- [ ] Confirm the Shattered Spire, Resonant Archive, and Dragon Arena generate.

## 5. Combat pass

- [ ] Confirm all nine mobs spawn, animate, and drop their loot tables.
- [ ] Fight the post-dragon fight and confirm dragon materials drop.

## 6. Performance pass

- [ ] Fly across all biomes at render distance 12; watch frame time.
- [ ] Confirm no memory leaks or runaway entity counts.

## 7. Sign-off

- [ ] Record any issues with screenshots and log excerpts.
- [ ] Update `docs/QA_REPORT_6H.md` with results.
