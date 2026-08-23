# Changelog - 10-Hour Audit-Fix Session

## Fixed

- **Guidebook blur**: the item's GUI display now uses integer scale 1.0 with no
  rotation; the `fixed` display is normalised too. The screen draws all text
  with shadows and a restyled page panel.
- **Guidebook content**: added Field Notes (Ruins, Spire, Archive, Monolith,
  Sanctum), a Crafting Reference (Tools, Doors, Void Materials, Armory),
  Resonance System pages, Post-Dragon pages, and Builder's Notes.
- **P2-2**: `tools/fix_power_of_two_textures.py` pads non-power-of-two textures
  (void_gravel.png 15x16 -> 16x16) by repeating edge pixels, with backups.
- **P2-4**: `EndRuinFeature`, `ShatteredSpireFeature`, and
  `ResonantArchiveFeature` now use per-column surface-height support checks.
- **P2-6**: `StructurePlacement` gates every landmark write to the generating
  3x3 chunk region, eliminating "setBlock in a far chunk" warnings.
- **P3**: `ResonanceSystem` wires the persistent Resonance attachment (grants
  on Endesium mob kills, tier notices); registered in `Endesium.onInitialize`.

## Added

- `tools/audit_verify.py` - read-only verification of every audit fix.
- `tools/verify_guidebook.py` - guidebook-specific verification.
- `docs/AUDIT_FIX_REPORT.md`, `docs/QA_REPORT_10H.md`,
  `docs/GUIDEBOOK_FIX_NOTES.md`, `docs/RESONANCE_SYSTEM_10H.md`,
  `docs/STRUCTURE_STABILITY_NOTES.md`.

## Verified already-fixed items

- P0-1 (features in biome JSONs), P1-1 (FRACTURED alignment), P1-2 (particles),
  P1-3 (sounds), P1-4 (dragon persistence), P2-1 (archive seeds), P3 (seed
  cache clear) - all confirmed present and covered by `audit_verify.py`.
