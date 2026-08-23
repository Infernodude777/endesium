# Asset Guidelines

## The working palette

Endesium uses a restrained, desaturated palette. Saturated purple, saturated blue, pure white, and pure black are not part of it.

- Void Black #111116: deep recesses and silhouette separation only.
- Charcoal #26232B: aged stone and structural shadow.
- End Gray #77747D: weathering and neutral transitions.
- End Stone Cream #D8D0B4: the mineral light of End stone.
- Deep Violet #312A3D: void-facing planes.
- Muted Violet #5E526E: the primary alien hue.
- Pale Lavender #C4BBCD: quiet highlights and inscriptions.
- Desaturated Cyan #7EA7A6: dormant resonance.
- Resonance Cyan #A9E6DF: active resonance, used sparingly.
- Muted Magenta #94647C: aged organic variation.
- Ancient Gold #C6A85A: rare history, used sparingly.
- Pale White #F2F0E5: a single highest-value highlight.

Accent colors must mean something. Resonance Cyan means active energy. Ancient Gold means rare history. If an accent has no meaning, remove it.

## Item textures

- Exactly 16x16 pixels, RGBA PNG.
- Every pixel outside the silhouette is alpha 0. No checkerboard matte.
- No semi-transparent pixels; visible pixels are alpha 255.
- Nearest-neighbor scaling only.
- Hard pixel edges and stepped diagonals, no anti-aliasing.
- A readable asymmetric silhouette with transparent breathing room.
- A normal item uses 4 to 7 colors plus transparency.

## Block textures

- Blocks must read as Minecraft blocks first.
- Broad quiet planes with a controlled seam or grain.
- One dark, one base, one light, and at most one signal accent.
- Avoid noise, checkerboard dithering, and random speckles.
- Reserve bright or animated marks for functional blocks.
- Blocks with transparent pixels need a cutout render layer, never the solid layer.

## Mob textures

- The Void Stalker uses a 32x32 RGBA texture.
- The body stays charcoal, deep violet, and End gray with muted violet joints.
- Pale Lavender eyes at rest; Resonance Cyan only during attack commitment or repositioning.
- No full-body glow and no bright outline.

## Geometry and animation

- Geometry is authored in Blockbench and exported as GeckoLib JSON.
- Animations communicate intent: idle listen, observe, walk, run, attack anticipation, attack impact, attack recovery, hurt, reposition, and death.
- Motion is part of the creature's identity. No decorative jitter.

## File placement

- assets/endesium/models: item and block models.
- assets/endesium/blockstates: block states.
- assets/endesium/textures: textures.
- assets/endesium/geo: GeckoLib geometry.
- assets/endesium/animations: animation files.

File names must match registry names exactly, or the game will not find them.
