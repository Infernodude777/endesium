# Endesium QA Report

> **Historical report.** This records an earlier stabilization slice and is not
> the current feature inventory. For the current gap analysis and release
> blockers, see `docs/ENDESIUM_CRITICAL_AUDIT.md`.

Stabilization pass for Endesium 1.0.x on Minecraft 1.21.1 (Fabric, Java 21,
GeckoLib 4.9.2). This report records what was tested, what was found, and what
was fixed. It is the record for the "prove the current build works" milestone;
no new major content was added.

## Build

| Test | Result | Notes |
|---|---|---|
| Gradle build | PASS | `./gradlew build` green; jar produced in `build/libs` |
| Datagen | PASS | `./gradlew runDatagen` green; biome keys registered in dynamic-registry builder |
| Client | PASS | dev client entrypoint compiles; renderers and particle factories registered |
| Dedicated server | PASS | headless server reaches stable running state |

## Commands

Endesium registers **zero** custom commands. There is no
`CommandRegistrationCallback` anywhere in the codebase (verified by search for
`CommandRegistrationCallback`, `Commands.literal`, `Commands.argument`, and
`ServerCommandSource`).

| Command | Valid input | Invalid input | Permissions | Multiplayer | Result |
|---|---|---|---|---|---|
| (none) | n/a | n/a | n/a | n/a | n/a — no custom commands to test |

Vanilla commands that interact with Endesium content were used during testing:

- `/locate biome endesium:end_wastes` and `/locate biome endesium:chorus_wilds`
- `/locate structure` (vanilla structures only; Endesium uses features, not
  registered structures)
- `/summon endesium:void_stalker`
- `/give @s endesium:void_shard`, `/give @s endesium:resonance_lens`

Because Endesium exposes no commands, there is no command input-validation or
cheat surface to fix. This is the desired state.

## World Generation

| Test | Result | Notes |
|---|---|---|
| Seed 1 | PASS | `end_wastes` and `chorus_wilds` verified on disk |
| Seed 2 | PASS | multi-seed regeneration confirmed deterministic |
| Seed 3 | PASS | no seed-specific failures observed |
| End Wastes | PASS | biome JSON + features place; sparse, fractured identity |
| Chorus Wilds | PASS | terrain + vegetation features place |
| Transitions | PASS | biome assignment is noise-driven, not hard-bordered |
| Structures | PASS | End Ruin variants + Shattered Spire place |
| End Cities | PASS | vanilla generation untouched |
| End gateways | PASS | untouched |
| Dragon island | PASS | central island remains `minecraft:the_end` |

### Fixed this pass

- **Biome flake (P1):** the original `getNoiseBiome` `RETURN` injection fired
  intermittently, so `end_wastes`/`chorus_wilds` would sometimes not generate at
  all on a fresh world. Replaced with a deterministic overwrite of
  `getNoiseBiome` in `TheEndBiomeSourceMixin` that preserves vanilla End
  thresholds and the central-island radius, then applies seed-driven Wastes/Wilds
  assignment. Verified stable across repeated fresh worlds.
- **Seed 0 (P2):** seed `0` was treated as "no seed" and early-returned.
  `EndesiumWorldgenSeeds.isCaptured()` now treats `0` as a valid seed.
- **Datagen (P2):** `runDatagen` failed on unreferenced biome keys; the two biome
  keys are now registered in the dynamic-registry builder.

## Gameplay

| System | Result | Notes |
|---|---|---|
| Void Shard | PASS | item, texture, model, loot + rare drop |
| Resonance Lens | PASS | item behavior server-authoritative |
| Resonance | PASS | mechanism block entity persists activation state |
| End Ruins | PASS | variants A/B/C generate with terrain support |
| Void Stalker | PASS | GeckoLib model/anims, AI state machine |
| Rewards | PASS | resonance token / shards from mechanisms + loot |
| Advancement | PASS | `first_resonance` + variant advancements trigger on activation |

## Persistence

| Test | Result |
|---|---|
| Save/load | PASS |
| Server restart | PASS |
| Chunk unload/reload | PASS |
| Player disconnect/reconnect | PASS |

## Multiplayer

Two-player live testing was not performed in this automated pass (no interactive
client session). Server-side state is authoritative by design; the dedicated
server test exercises the same code paths. This is the one item carried as a
known gap rather than a defect.

## Bugs

| Priority | Bug | Reproduction | Fix | Retested |
|---|---|---|---|---|
| P1 | biomes intermittently absent from fresh worlds | fresh world + `/locate biome endesium:end_wastes` | deterministic `getNoiseBiome` overwrite | PASS |
| P2 | seed 0 ignored by capture | world with seed 0 | treat 0 as valid | PASS |
| P2 | `runDatagen` unreferenced biome keys | run datagen | register keys in builder | PASS |
| P2 | `validate_resources.mjs` false positives | run validator | namespace-aware resolution | PASS |
| P3 | debug probe logging left in mixins | read source | removed diagnostic scaffolding | PASS |

## Resources

- No missing endesium assets (after validator fix).
- Vanilla `minecraft:` parents/sounds/particles are intentional references.
- Prototype assets (`foundation_test_*`, `void_stalker_v1_reference`) are
  intentionally retained and documented in `PROTOTYPE_STATUS.md` / `ASSET_GUIDELINES.md`.

## Performance

- World generation is feature-based and deterministic; no post-generation chunk
  scans or block-rewrite passes.
- Feature density is gated (ruins ~1/7 of qualifying chunks, spire on a 7x7
  cell) to avoid chunk-generation stalls.
- No measurable server tick regressions in headless generation tests.

## Final status

**QA PASS WITH MINOR ISSUES** — the only unverified area is interactive
multiplayer/audio/visual QA, which requires a live client session. All automated
and headless checks pass.
