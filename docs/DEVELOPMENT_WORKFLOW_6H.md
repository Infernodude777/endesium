# Endesium Development Workflow

This document describes the recommended day-to-day workflow for contributing
to Endesium, from editing code to verifying the build.

## Prerequisites

- Java 21+ (the Gradle toolchain targets 21).
- A recent Node.js (for the `tools/*.mjs` asset scripts).
- Python 3 (for the `tools/*.py` scripts and the Fabric datagen tooling).

## Editing Java

Endesium uses two source sets. Common code lives in `src/main/java` and
client-only code in `src/client/java`. When you add an item or block:

1. Register it in `registry/ModItems.java` or `registry/ModBlocks.java`.
2. Add a translation in `client/datagen/EndesiumLanguageProvider.java`.
3. Add a recipe (either in `client/datagen/EndesiumRecipeProvider.java` or as a
   raw JSON file under `src/main/resources/data/endesium/recipe/`).
4. Add an item model or blockstate (raw JSON or the model provider).
5. Add a loot table entry in `EndesiumLootTableProvider.java`.

## Compiling

Always verify the build before committing:

```
./gradlew compileJava compileClientJava
```

This compiles both source sets without running datagen. A full build is:

```
./gradlew build
```

## Running datagen

```
./gradlew runDatagen
```

This regenerates recipes, loot tables, models, and language from the
`client/datagen` providers. After making provider changes, re-run datagen and
check the diff before committing.

## Adding data JSON by hand

Raw JSON data files (recipes, advancements) load at runtime alongside datagen
output. Follow these rules:

- Use unique file names; never collide with a datagen-generated ID.
- Use the 1.21.1 result format: `"result": { "id": "..." }`.
- Reference only registered items and blocks (cross-check `ModItems` and
  `ModBlocks`).

## Asset scripts

Run `node tools/gen_*.mjs` or `python tools/gen_*.py` to regenerate textures.
Always preview with `tools/preview_textures_embedded.html` after regenerating.

## Testing

Run `./gradlew runClient` and follow `docs/TESTING_RUNBOOK.md` for the
structured test pass.

## Committing

Commit in small, descriptive units. Use the mod's existing convention: a
concise summary of what changed and why.
