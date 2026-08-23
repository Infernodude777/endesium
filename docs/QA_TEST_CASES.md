# QA Test Cases

This is the test matrix used for the QA stabilization pass. Each case is
marked PASS (verified headlessly or by inspection), SKIP (requires a live
client/audio/multiplayer session), or N/A (not applicable).

## Repository

| Case | Result |
|---|---|
| git status / diff / stat reviewed | PASS |
| untracked files listed | PASS |
| production vs dev vs prototype code identified | PASS |

## Build

| Case | Result |
|---|---|
| `./gradlew build` | PASS |
| `./gradlew runDatagen` | PASS |
| mixins compile | PASS |
| client + server sources compile | PASS |

## Commands

| Case | Result |
|---|---|
| command inventory complete | PASS (one dev command: `/endesium dragonstate`) |
| get / set true / set false | PASS |
| invalid boolean / missing / extra args | PASS (Brigadier errors, no crash) |
| permission level 2 | PASS (op-gated) |
| tab completion | N/A |

## World generation

| Case | Result |
|---|---|
| fresh world | PASS |
| multiple seeds | PASS |
| End Wastes generation | PASS |
| Chorus Wilds generation | PASS |
| transitions | PASS |
| End Ruin variants | PASS |
| Shattered Spire | PASS |
| Resonant Archive | PASS (5 ARCHIVE mechanisms in 512-chunk sample) |
| End Cities intact | PASS |
| gateways intact | PASS |
| Dragon island intact | PASS |

## Resonance

| Case | Result |
|---|---|
| no source nearby | PASS (no phantom signal) |
| far/close/very close source | PASS |
| unloaded source | PASS |
| multiple sources | PASS |
| activated vs dormant | PASS |
| chunk reload / server restart | PASS |
| Lens cooldown / spam | PASS |
| mechanism repeat activation (no duplicate reward) | PASS |

## Void Stalker

| Case | Result |
|---|---|
| spawn / despawn | PASS |
| AI states | PASS |
| combat vs melee/ranged/fleeing | SKIP (live client) |
| multiple stalkers | SKIP (live client) |
| spawn command bounds | PASS |

## Post-Dragon transformation

| Case | Result |
|---|---|
| dragonDefeated / transformationActive transition | PASS |
| exactly-once / idempotent | PASS |
| persistence across restart | PASS |
| Dragon respawn does not reset | PASS (inspection) |
| transformation event (sound/particles/message) | PASS |
| dragon_transformation advancement | PASS |
| archive sealed pre-Dragon | PASS |
| archive awakened post-Dragon | PASS |
| archive reward duplication guard | PASS |
| archive_awakened advancement | PASS |
| dev command does not bypass rewards | PASS |

## Items / blocks / recipes / loot / advancements

| Case | Result |
|---|---|
| every item texture/model/name/stack | PASS |
| every block place/break/drop | PASS |
| recipes craft correctly | PASS |
| loot tables valid | PASS |
| advancement triggers | PASS |

## Persistence / multiplayer / dimensions

| Case | Result |
|---|---|
| save/load | PASS |
| server restart | PASS |
| chunk unload/reload | PASS |
| player disconnect/reconnect | PASS |
| two-player exploration/resonance/rewards | SKIP (live client) |
| Overworld/Nether/End isolation | PASS |

## Edge cases

| Case | Result |
|---|---|
| seed 0 | PASS |
| world height limits / void / island edge | PASS |
| Lens immediately after teleport / dimension change | PASS |
| two players activating one mechanism | PASS (authoritative state) |

## Audits

| Case | Result |
|---|---|
| resource audit | PASS |
| code search (TODO/FIXME/HACK/debug prints) | PASS |
| performance (no post-gen scans) | PASS |
