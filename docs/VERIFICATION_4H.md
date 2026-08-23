# Verification - 4-Hour Bug-Fix Session

## One-shot verification

Run these from the repository root. All must exit 0.

```
./gradlew compileJava
python tools/audit_verify.py
python tools/verify_guidebook.py
python tools/fix_power_of_two_textures.py --verify
```

## What each command proves

- `compileJava` - the mod's main source set compiles; the `WorldGenRegion`
  import fix is in place.
- `audit_verify.py` - every `ENDESIUM_GLM_BRUTAL_AUDIT` item is resolved,
  including the corrected P1-4 persistence check and the P2-2 texture check.
- `verify_guidebook.py` - the guidebook model, screen shadowing, and content
  pages are intact.
- `fix_power_of_two_textures.py --verify` - no texture is non-power-of-two;
  this is the CI-friendly mode added this session.

## Expected output

```
[PASS] P0-1: five placed features wired into every biome json
[PASS] P1-1: FRACTURED ruin is axis-aligned
[PASS] P1-2: all nine particle jsons exist
[PASS] P1-3: sounds.json uses valid event refs only
[PASS] P1-4: Dragon fight phase persists and only buffs once
[PASS] P2-1: archive containers use random seeds
[PASS] P2-2: every texture is power-of-two
[PASS] P2-4: landmark support checks are per-column
[PASS] P2-6: landmark writes gated to generating region
[PASS] P3: seed cache cleared on server stop
[PASS] P3: resonance attachment is wired, not dead code
[PASS] Guidebook: crisp model (integer scale, no rotation)
[PASS] Guidebook: screen renders with text shadow
[PASS] Guidebook: added field-note pages
[PASS] Tools: POT texture fixer exists
15/15 checks passed
```

## In-game smoke test (manual)

1. Open the guidebook - pages are crisp at GUI scales 1-4.
2. Defeat an Endesium mob - resonance notice appears; `/resonance` reflects it.
3. Generate a fresh world - ruins, spires, and archives sit on real ground.
4. Restart the server mid-Dragon-fight - the phase and buff are preserved.