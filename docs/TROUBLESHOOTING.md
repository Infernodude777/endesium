# Troubleshooting

## The modded biomes do not generate on a fresh world

- Confirm `./gradlew build` succeeded after the `getNoiseBiome` overwrite fix.
- Run `tools/qa_run.sh qa` and check that `/locate biome endesium:end_wastes`
  returns coordinates rather than "Could not find".
- If the world predates the fix, generate a new world (the biome source is
  resolved at world construction, not per-chunk).

## `runDatagen` fails on unreferenced biome keys

- Ensure `EndesiumBiomes.END_WASTES_KEY` and `CHORUS_WILDS_KEY` are registered
  in the datagen dynamic-registry builder.

## `validate_resources.mjs` reports missing `minecraft:` assets

- This was a bug; the validator now skips the `minecraft:` namespace. If you
  still see it, update the script from `main`.

## The Void Stalker does not render

- Check that `geo/entity/void_stalker_v2.geo.json` and
  `textures/entity/void_stalker.png` exist, and that
  `ProductionVoidStalkerRenderer` / `ProductionVoidStalkerModel` reference them.

## Resonance reports nothing near a ruin

- The Lens only reports loaded, active sources. A dormant mechanism reads faint;
  a destroyed or unloaded source reads nothing.

## Structures float over void

- Ruin and spire placement run a terrain-support check. If you see floating
  structures, the platform check in `EndRuinFeature` / `ShatteredSpireFeature`
  may need its support threshold adjusted.
