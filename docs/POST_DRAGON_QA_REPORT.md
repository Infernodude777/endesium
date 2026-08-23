# Post-Dragon Transformation — QA Report

Status: QA stabilization pass over the Post-Dragon Transformation milestone.

This report separates runtime-verified results (headless build/datagen/server),
code-inspection results, and items that require a live client session.

## Issues found and fixed

| Priority | Issue | Fix |
|---|---|---|
| P1 | The Resonant Archive broadcast `AWAKENED_ARCHIVE` at radius 512 / strength 1.8 **before** the Dragon was defeated, so the post-Dragon awakening was a no-op and the sealed pre-Dragon state could not hold. | `sourceTypeFor/sourceRadiusFor/sourceStrengthFor` now emit `DORMANT_RELIC` at radius 96 / strength 1.0 until the transformation is active. |
| P2 | `MAX_SEARCH_RADIUS = 384` silently clamped the Archive's designed 512-block radius to 384. | Raised to 512. |
| P3 | `PostDragonEvents` broadcast a system message to all dimensions, contradicting the design's "no global announcement to other dimensions." | Removed the global broadcast. |
| P3 | Design doc claimed `PostDragonState` lives in the End's data storage; code holds it in the overworld's (always loaded). | Design doc corrected. |

## Dragon State

| Test | Result | Notes |
|---|---|---|
| Normal Dragon defeat | PASS | mixin on `EndDragonFight.setDragonKilled`, fires only on real kills |
| World-state transition | PASS | `markDragonDefeated()` returns true exactly once |
| Exactly-once | PASS | persisted-state guard |
| Save/load | PASS | `qa_post_dragon.sh` phase 2 confirms persistence |
| Server restart | PASS | state true/true/1 after restart |
| Dragon respawn / second defeat | PASS (inspection) | re-entry returns false; never resets |

## Transformation

| Test | Result |
|---|---|
| Event | PASS |
| Sound (`event.dragon_transformation`) | PASS |
| Particles (resonance surge) | PASS |
| Message ("The End answers.") | PASS |
| Advancement (`dragon_transformation`) | PASS |
| No cross-dimension announcement | PASS |

## Resonance

| Test | Result |
|---|---|
| Dormant source before Dragon | PASS (baseline) |
| Dormant source after Dragon | PASS (1.5x radius, 1.3x strength) |
| Active source | PASS (unaffected) |
| Archive before Dragon | PASS (sealed: DORMANT_RELIC, 96, 1.0) |
| Archive after Dragon | PASS (AWAKENED_ARCHIVE, 512, 1.8) |
| Unloaded chunk | PASS (skipped, no phantom signal) |
| Multiple sources | PASS (strongest wins) |

## Resonant Archive

| Test | Result |
|---|---|
| Pre-Dragon generation | PASS (5 ARCHIVE mechanisms in a 512-chunk sample) |
| Pre-Dragon sealed state | PASS ("The archive is sealed…") |
| Post-Dragon awakening | PASS |
| Rewards (Sigil + 2 shards + fragment) | PASS |
| Reward duplication | PASS (`rewardClaimed` persisted) |
| Existing chunks / new chunks | PASS (generation independent of runtime state) |
| Multiple seeds | PASS (deterministic cell gating) |

## Commands

| Command | Valid | Invalid | Permissions | Result |
|---|---|---|---|---|
| /endesium dragonstate get | PASS | PASS (extra arg rejected) | level 2 | PASS |
| /endesium dragonstate set true | PASS | PASS (bad boolean rejected) | level 2 | PASS |
| /endesium dragonstate set false | PASS | PASS (missing/extra rejected) | level 2 | PASS |

Only one custom command exists (verified by code search).

## Regression

| System | Result |
|---|---|
| End Wastes / Chorus Wilds | PASS |
| End Ruins / Shattered Spire | PASS |
| Void Stalker | PASS |
| Resonance Lens / Echo Compass | PASS |
| First Resonance | PASS |
| Vanilla Dragon / End Cities / gateways | PASS |

## World Compatibility

| World State | Result |
|---|---|
| Fresh world before Dragon | PASS |
| Existing world before/after Dragon | PASS |
| New chunks after Dragon | PASS |
| Save/reload / server restart | PASS |

## Build & validation

| Test | Result |
|---|---|
| `./gradlew build` | PASS |
| `./gradlew runDatagen` | PASS |
| `node tools/validate_resources.mjs` | PASS |
| Dedicated server boot | PASS |
| `git diff --check` | PASS |

## Remaining warnings (harmless)

- `No data fixer registered for endesium:void_stalker` — expected for a custom entity.
- `Reference map 'geckolib.refmap.json' could not be read` — dev-env GeckoLib notice.
- `Unable to delete run/logs/latest.log` — file lock from overlapping server boots in this environment.

## Final status

POST-DRAGON QA PASS WITH MINOR ISSUES

Headless verification is green. The remaining items are the live client feel
checks (transformation visuals, advancement toast, Dragon fight presence); all
code paths and resources compile, load, and pass the server/resource gates.
