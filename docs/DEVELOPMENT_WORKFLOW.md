# Development Workflow

## Day-to-day

```bash
./gradlew build
./gradlew runDatagen
node tools/validate_resources.mjs
tools/qa_run.sh qa
```

## Building the jar

```bash
./gradlew build
# output: build/libs/endesium-*.jar
```

## Running the client / server

```bash
./gradlew runClient
./gradlew runServer
```

## Inspecting generated chunks

```bash
node tools/parse_mca.mjs <region.mca>
node tools/scan_region_blocks.mjs
```

## Texture generation

Pixel-art textures are generated with `tools/generate_textures.mjs` following
`docs/ASSET_GUIDELINES.md`: hard pixel edges, true alpha, Endesium palette.

## Jimbibo (AI Typer)

Keyboard-mode sessions type authored files character-by-character into VS Code
with realistic timing and git commits. Use for documentation and Blockbench work;
use direct mode for unattended headless writes.
