# Production Asset Status

## Textures (16x16, hard pixel edges, true alpha)

| Asset | Status |
|---|---|
| `block/resonant_slate.png` | production |
| `block/end_gray.png` | production |
| `block/dormant_resonant_crystal.png` | production |
| `block/end_ruin_mechanism.png` | production |
| `block/inscribed_slate_0..3.png` | production (4 variants) |
| `block/cracked_spire_stone.png` | production |
| `block/resonant_pillar.png` | production |
| `block/chorus_sprout.png` | production |
| `block/wild_tendril.png` | production |
| `item/resonance_lens.png` | production |
| `item/void_shard.png` | production |
| `item/resonance_token.png` | production |
| `entity/void_stalker.png` | production |

## Models / geometry / animations

- Production GeckoLib geometry: `geo/entity/void_stalker_v2.geo.json`.
- Production animation set: `animations/entity/void_stalker_production.animation.json`.
- `void_stalker_v2.animation.json` retained as an earlier export.

## Sounds

Registered in `sounds.json`; most reuse restrained vanilla amethyst sounds. No
missing `.ogg` files after the validator fix (vanilla `minecraft:` refs are
intentional).

## Policy

All production assets must match `ENDESIUM_VISUAL_DESIGN.md`: restrained palette,
readable in Minecraft, no full-body glow. Placeholder or broken assets fail the
resource validator.
