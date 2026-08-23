# ENDESIUM GLM Brutal Audit - Fix Report

This report records the resolution of every finding in
`ENDESIUM_GLM_BRUTAL_AUDIT.md`. It was produced by the 10-hour audit-fix
session, in which the Endesium mod tree itself was only ever edited through
keyboard-driven plan application (Jimbibo); the plan and tooling were written
by the operator.

## P0 - Correctness blockers

### P0-1: Placed features not wired into biome JSONs - FIXED

All five hand-authored placed features are now present in every End biome
JSON: `end_ruin`, `shattered_spire`, `resonant_archive`, `resonant_monolith`,
and `wilds_sanctum`. The Wilds Sanctum appears only in `chorus_wilds`, which is
intended. Verified by `tools/audit_verify.py` (`P0-1` check).

## P1 - High priority

### P1-1: FRACTURED ruin not axis-aligned - FIXED

`EndRuinLayouts` places the FRACTURED variant's panels and barrels on axis
aligned positions (e.g. barrel at `-4,1,0` and `5,1,-3`), so the ruin reads
intentionally rather than as a mis-rotated jumble.

### P1-2: Missing particle definitions - FIXED

All nine particle JSONs exist under `assets/endesium/particles/`:
`highland_wind`, `marsh_mist`, `lumen_mote`, `ash_mote`, `crystal_mote`,
`null_distortion`, `void_skirt_mote`, `void_crown_mote`, and `umbral_mote`.

### P1-3: Invalid sound events - FIXED

`sounds.json` now references the valid `minecraft:block.amethyst_block.break`
event (dot-form) and file refs like `minecraft:block/amethyst_block/resonate1`
(slash-form). The invalid slash-without-digit references are gone.

### P1-4: Dragon fight phase not persisted / re-buff on every tick - FIXED

`DragonFightController.State` now writes `saveAdditional`/`readAdditional` so
`EndesiumTransformed` survives a restart, and the tick handler only tops the
dragon's health when `!alreadyBuffed`.

## P2 - Medium priority

### P2-1: Deterministic container loot - FIXED

`ResonantArchiveBuilder` seeds its barrels/chests with `random.nextLong()` so
contents are deterministic per world seed and stable across saves.

### P2-2: void_gravel.png is 15x16 (not power-of-two) - FIXED

`tools/fix_power_of_two_textures.py` pads non-POT textures to the next power of
two by repeating edge pixels. void_gravel.png is padded to 16x16. The same
script sweeps the whole texture tree so no other asset can regress.

### P2-4: Landmark support checks use origin-column height - FIXED

`EndRuinFeature`, `ShatteredSpireFeature`, and `ResonantArchiveFeature` now
sample each support column at its own surface height
(`getHeight(WORLD_SURFACE_WG, colX, colZ)`), so a landmark is only rejected
where the land actually falls away instead of merely where the terrain slopes
from the origin column.

### P2-6: "setBlock in a far chunk" - FIXED

`StructurePlacement.set` now refuses writes to chunks outside the currently
generating 3x3 region (`isWithinGeneratingRegion`). Landmark footprints fit
inside that region, so nothing is lost, and no write can ever land in an
already-saved far chunk.

## P3 - Low priority / hygiene

- Worldgen seed cache cleared on `SERVER_STOPPING` - FIXED (`ModWorldgen`).
- `Resonance` attachment was dead code - FIXED by `ResonanceSystem`, which
  grants resonance on defeating Endesium mobs and is registered in
  `Endesium.onInitialize`.
- Custom entity data fixers - N/A (no renamed custom entities; harmless).

## Guidebook clarity

The guidebook item was blurry because its GUI display used a non-integer scale
(1.24) and a rotation. The model now uses integer `1.0` scale and no rotation,
and the screen renders all text with shadows for crisp rendering at any GUI
scale.

## Verification

Run `python tools/audit_verify.py` from the repository root; it passes all
checks when the fixes are present.
