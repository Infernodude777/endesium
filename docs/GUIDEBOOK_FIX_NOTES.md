# Guidebook Fix Notes

## Why it was blurry

The Endesium guidebook is an item whose inventory icon is drawn from a 16x16
texture (layer0 `minecraft:item/book`, layer1 `endesium:item/archive_sigil`).
Its GUI display transform had:

```json
"gui": {
  "rotation": [0, 0, -8],
  "scale": [1.24, 1.24, 1.24]
}
```

A non-integer scale (1.24) plus a rotation causes the renderer to sample the
16x16 texture at fractional texels, producing a soft, blurry icon in the
inventory, hotbar, and item frame preview.

## The fix

The GUI display now uses an integer scale and no rotation:

```json
"gui": {
  "rotation": [0, 0, 0],
  "translation": [0, 0, 0],
  "scale": [1.0, 1.0, 1.0]
}
```

The `fixed` (item frame) display was also normalised to integer scale with no
rotation. In-world `ground` and `thirdperson` displays keep their small
rotations because they are rendered with mipmaps in a 3D context where the
rotations read naturally and do not look blurry.

## Screen improvements

Beyond the icon, the `EndesiumGuidebookScreen` was updated for crispness:

- All title and body text is drawn with shadows enabled (`drawString(..., true)`),
  which both sharpens the glyphs and improves contrast against the dark page.
- The page area is a slightly lighter panel so text sits on a readable field.
- A gold border and cyan accent line frame the panel at integer coordinates.
- A page indicator ("n / total") was added at the bottom of the panel.

## New guidebook content

The session also added pages so the book is a more useful reference:

- **Field Notes**: The Ruins, The Spire, The Archive, The Monolith, The Sanctum.
- **Crafting Reference**: Tools, Doors, Void Materials, The Armory.
- **Resonance System**: The Resonance Current, Resonance Sources.
- **Post-Dragon**: After the Transformation, The Respawned Dragon.
- **Builder's Notes**: Texture, Stability.

## Verification

- `tools/audit_verify.py` checks the model (`guidebook_model`) and screen
  (`guidebook_shadow`, `guidebook_pages`) checks.
- Visually: the icon should be pixel-crisp in the inventory, and the open book
  should be readable at GUI scales 1 through 4.
