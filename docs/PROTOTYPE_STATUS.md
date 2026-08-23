# Prototype Status

The following assets are retained as references and are NOT used by production
registrations. They are documented here so they are not mistaken for dead
assets and accidentally deleted.

## Retained geometry references

- `geo/entity/void_stalker_v1_reference.geo.json` — the original low quadruped
  direction, superseded by the tall biped.
- `geo/entity/void_stalker_v2.geo.json` — production model.
- `geo/entity/void_stalker_v2_absolute_reference.geo.json`,
  `..._neutral_reference.geo.json`, `..._parented_viewport_reference.geo.json`,
  `..._quadruped_reference.geo.json`, `..._textured_broken_reference.geo.json` —
  intermediate Blockbench exports kept for troubleshooting.

## Retained prototype textures

- `textures/item/foundation_test_item.png`
- `textures/block/foundation_test_block.png`

These support the retained prototype renderer reference only; they are not
reachable in normal gameplay.

## Removed in production

- `foundation_test_block` / `foundation_test_item` / `end_ruin_block`
  registrations and their generated data were removed in the production polish
  pass.

## Policy

Reference-only assets may be pruned in a future cleanup milestone, but only
after confirming no renderer, model, or documentation references them.
