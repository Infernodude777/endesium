# Resonance System - Wiring Notes

## Background

The `Resonance` attachment (`src/main/.../resonance/Resonance.java`) has existed
since the vertical slice: a persistent per-player integer attachment clamped to
0-100. The brutal audit flagged it as dead code - `get` and `add` existed but
nothing ever called them.

## The fix

A new `ResonanceSystem` class wires the attachment into gameplay:

- It registers `ServerLivingEntityEvents.AFTER_DEATH`.
- When an Endesium mob dies and the killer is a `ServerPlayer`, the player
  gains resonance via `Resonance.add`.
- When the player crosses a tier boundary (0/20/40/60/80/100), they receive a
  short chat notice naming their new tier.

`Endesium.onInitialize` now calls `ResonanceSystem.register()`.

## Grants

| Mob | Resonance granted |
| --- | ----------------- |
| Void Stalker, Ash Wraith | 4 |
| Chorus Stalker, Nullwalker | 3 |
| All other Endesium mobs | 2 |

Grants are intentionally small so progression is steady. Reaching 100 requires
roughly 25-50 kills depending on the mix.

## Tiers

| Tier | Threshold | Name |
| ---- | --------- | ---- |
| 0 | 0 | Still |
| 1 | 20 | Attuned |
| 2 | 40 | Awakened |
| 3 | 60 | Resonant |
| 4 | 80 | Harmonized |
| 5 | 100 | Ascendant |

## Design notes

- The attachment is persistent, so resonance survives restarts and is stored
  with the player's data.
- The system is server-side only; the client never needs the value.
- Future recipes can gate on `ResonanceSystem.tierFor(Resonance.get(player))`.
- Tuning lives entirely in `ResonanceSystem`; no data files are involved.

## Verification

`tools/audit_verify.py` checks that `ResonanceSystem` exists, registers
`AFTER_DEATH`, and is called from `Endesium.onInitialize`.
