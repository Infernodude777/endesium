# Development Workflow - 10-Hour Audit-Fix Session

## How the session worked

All edits to the Endesium mod tree were applied by a keyboard-driven typing
agent (Jimbibo) following a plan file. The operator wrote the plan, the merge
script, and the verification tooling; the typing agent performed every edit to
`src/`, `tools/`, and `docs/`.

## Plan structure

Each plan chunk is a JSON file with a `root`, `time_unit`, and a `files`
array. Each file entry has:

- `path` - path relative to the repo root.
- `summary` - the git commit message for that file.
- `code` - the exact text to type.
- `mode` - `replace` (whole file), `insert_at_line` (insert at line/column),
  `append`, or `patch_function`.
- `line` / `column` - for `insert_at_line`.

Chunks were merged into one plan with a merge script that distributes the
session time proportionally to code length.

## Order of application

1. Guidebook fixes (model, screen, content) - user-visible, low risk.
2. Structure fixes (P2-4 per-column support, P2-6 write gate) - generation.
3. Resonance wiring (P3) - runtime, low risk.
4. Verification tooling (`audit_verify.py`, `verify_guidebook.py`).
5. Documentation (fix report, QA, guidebook notes, resonance, stability).

## Verification loop

After the session:

1. `python tools/fix_power_of_two_textures.py` - repair any stray texture.
2. `python tools/audit_verify.py` - confirm every audit fix is present.
3. `python tools/verify_guidebook.py` - confirm the guidebook fix.
4. Build with `./gradlew build` and run a fresh world for the in-game checks
   in `QA_TEST_CASES_10H.md`.

## Guardrails

- The typing agent only ever edits files listed in the plan.
- Verification scripts are read-only and exit non-zero on failure.
- Texture fixes keep `*.bak.png` backups next to every changed file.
