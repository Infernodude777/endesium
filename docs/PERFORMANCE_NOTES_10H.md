# Performance Notes - 10-Hour Audit-Fix Session

## Summary

The audit-fix changes are deliberately cheap. None of them adds a per-tick
scan, a global search, or any allocation on the hot path.

## Per-column support checks (P2-4)

Each landmark feature now calls `getHeight(WORLD_SURFACE_WG, colX, colZ)` for
up to 169 columns (ruin 13x13, spire/archive 23x23) instead of reading a block
state at a fixed offset. Heightmap lookups are O(1) reads of a packed array, so
this is a negligible cost that runs once per candidate cell during chunk
generation - not per tick, not per chunk.

## Generating-region write gate (P2-6)

`StructurePlacement.set` adds one `instanceof WorldGenRegion` check and two
integer shifts per write. This is a handful of nanoseconds per block and runs
only during generation. It also *removes* work: writes that would have been
rejected by the server with a far-chunk warning are now skipped before the
block-state lookup.

## Resonance system (P3)

`ResonanceSystem` registers a single death event. The handler runs only when
an Endesium mob dies and a player is the killer - a rare event. It performs
one attachment read, one write, and (only on tier-up) one chat message. There
is no per-tick cost and no client traffic.

## Guidebook

The screen renders at most ~12 wrapped lines plus a title and indicator per
frame, all with the vanilla font. The model change (integer scale, no
rotation) does not affect runtime cost at all.

## Net effect

- Generation: +~170 heightmap reads per landmark candidate (negligible).
- Runtime: +1 death-event handler (rare).
- Memory: no new caches, no new fields on entities.
- Disk: no new data files; the persistent Resonance attachment is one int per
  player.
