# Contributing

## Environment

- Java 21 (Temurin).
- Fabric Loader + Fabric API for Minecraft 1.21.1.
- GeckoLib 4.9.2.
- Official Mojang mappings.

## Workflow

1. `./gradlew build` to compile.
2. `./gradlew runDatagen` to regenerate committed data.
3. `node tools/validate_resources.mjs` to gate resource integrity.
4. `tools/qa_run.sh qa` to run the headless generation test.

## Style

- Follow existing package layout and naming.
- Keep mixins minimal; prefer registry/feature systems.
- No client-only code in common/server packages.
- Server-authoritative gameplay state.
- No hard-coded world coordinates; generation must be seed-dependent.

## Scope

Do not add bosses, armor, the Deep End, or post-Dragon progression without a
separate design milestone. Keep the End sparse; mystery is intentional.
