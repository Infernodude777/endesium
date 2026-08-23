# QA Report - 10-Hour Audit-Fix Session

## Scope

This session applied the remaining `ENDESIUM_GLM_BRUTAL_AUDIT` fixes through
keyboard-driven plan application (Jimbibo) and added verification tooling so
every fix can be re-checked automatically.

## What changed

| Area | Change |
| ---- | ------ |
| Guidebook model | GUI display: integer scale 1.0, no rotation (blur fix) |
| Guidebook screen | All text drawn with shadow; page panel restyled |
| Guidebook content | Field notes, crafting reference, resonance, post-Dragon pages |
| P2-2 | `tools/fix_power_of_two_textures.py` pads non-POT textures |
| P2-4 | Per-column support checks in the three landmark features |
| P2-6 | `StructurePlacement` gates writes to the generating 3x3 region |
| P3 | `ResonanceSystem` wires the persistent Resonance attachment |
| Verification | `tools/audit_verify.py` (read-only, exit code on failure) |
| Docs | `AUDIT_FIX_REPORT.md`, `GUIDEBOOK_FIX_NOTES.md` |

## How to verify

1. Run `python tools/fix_power_of_two_textures.py` (fixes any stray texture).
2. Run `python tools/audit_verify.py` - expect all checks to PASS.
3. In game: open the guidebook; the page should be crisp at GUI scales 1-4.
4. In game: defeat any Endesium mob; watch for the resonance chat notice and
   `/resonance` state (or inspect the attachment).
5. Generate a fresh world and look for ruins, spires, and archives - they must
   sit on real ground with no floating sections, and the log must not contain
   "setBlock in a far chunk".

## Risks and mitigations

- Per-column support slightly changes where landmarks spawn (more tolerant of
  gentle slopes). If a build feels off, the support thresholds can be tuned in
  each feature (68% ruin, 360 spire, 320 archive).
- The 3x3 write gate is conservative: any future structure with a footprint
  wider than the generating region would be clipped. Keep landmark footprints
  within ~1 chunk of their origin.
- Resonance grants are intentionally small (2-4 per kill); thresholds in
  `ResonanceSystem` can be retuned without touching data.

## Result

All audit issues from the brutal audit are resolved and covered by an automated
verification script. No runtime data migration is required.
