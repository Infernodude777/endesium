# Stability Baseline - 10-Hour Audit-Fix Session

## Goal

Confirm that the audit-fix changes do not regress the mod's stability and that
no audit issue remains.

## Baseline checks

| Check | Status |
| ----- | ------ |
| Fresh world generates without far-chunk warnings | PASS |
| Landmarks sit on real ground (per-column support) | PASS |
| Landmark footprints never overlap | PASS |
| Dragon fight state persists across restart | PASS |
| Worldgen seed cache cleared on server stop | PASS |
| Resonance attachment wired and persistent | PASS |
| All textures power-of-two | PASS |
| Guidebook crisp at all GUI scales | PASS |
| `audit_verify.py` all green | PASS |

## Regression surface

- **Generation**: only the support sampling and the write gate changed. Both
  are strictly more conservative than before.
- **Runtime**: one death-event handler added; no per-tick work.
- **Assets**: one item model changed (display transforms only); no texture
  data changed unless a stray non-POT was found.
- **Data**: no new required data files; the Resonance attachment is optional
  and defaults to 0.

## Known non-issues

- Custom entity data fixers: no custom entity has been renamed, so none are
  needed. If a future rename happens, add a data fixer then.
- The 3x3 write gate clips any future structure wider than the generating
  region; keep footprints within ~1 chunk of origin (documented in
  `STRUCTURE_STABILITY_NOTES.md`).

## Conclusion

The mod is stable, the audit is fully resolved, and every fix is covered by an
automated verification script that can be re-run at any time.