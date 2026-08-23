# Structure Stability Notes

This document explains the two structural-generation fixes applied in the
10-hour audit session: per-column support checks (P2-4) and the generating
region write gate (P2-6).

## Per-column support checks (P2-4)

### The problem

`EndRuinFeature`, `ShatteredSpireFeature`, and `ResonantArchiveFeature` all
sampled support at the origin column's height:

```java
level.getBlockState(base.offset(dx, -1, dz))
```

`base` is the surface position at the origin column. On sloped terrain this
means the check looked at the block one below the *origin* height for every
column, so a column that was actually several blocks lower (or higher) was
misjudged. The result: landmarks could float over dips or clip into rises even
when the land was perfectly solid at each column's own surface.

### The fix

Each feature now samples every column at its own surface height:

```java
int colX = base.getX() + dx;
int colZ = base.getZ() + dz;
int colY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, colX, colZ);
if (isSupported(level.getBlockState(new BlockPos(colX, colY - 1, colZ)))) {
    support++;
}
```

Thresholds are unchanged (68% for ruins, 360 for the spire, 320 for the
archive), so the fix only makes placement *more correct*, not more or less
common on flat ground.

## Generating-region write gate (P2-6)

### The problem

Hand-authored landmark builders write blocks directly. During feature
placement the game loads a 3x3 chunk region around the generating chunk, but a
write into a chunk outside that region triggers the "Detected setBlock in a far
chunk" warning and can corrupt already-saved chunks.

### The fix

`StructurePlacement.set` now refuses writes whose chunk is outside the 3x3
region centered on the generating chunk:

```java
private static boolean isWithinGeneratingRegion(WorldGenLevel level, BlockPos pos) {
    if (level instanceof WorldGenRegion region) {
        ChunkPos center = region.getCenter();
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        return Math.abs(cx - center.x) <= 1 && Math.abs(cz - center.z) <= 1;
    }
    return true;
}
```

All current landmark footprints (ruin 13x9, spire 23x23, archive 23x23) fit
inside the 3x3 region, so no blocks are lost. The gate is a hard guarantee
that no landmark write can ever land in an already-saved far chunk.

## Future guidance

- Keep landmark footprints within roughly one chunk of their origin.
- If a future structure needs a wider footprint, prefer the vanilla Structure
  system (which has proper bounding-box arbitration) over hand-authored writes.
