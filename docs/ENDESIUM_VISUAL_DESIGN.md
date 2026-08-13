# Endesium Visual Design Specification

## Purpose

Endesium should feel like a restrained, ancient continuation of the End rather than a neon fantasy overhaul. Every visual element must communicate material, age, function, rarity, or resonance. Decoration without meaning is rejected.

## 1. Finalized palette

| Name | Hex | Meaning and use |
| --- | --- | --- |
| Void Black | `#111116` | Deepest recesses, true separation, and rare silhouette anchors. Never use as a default fill. |
| Charcoal | `#26232B` | Aged stone, dormant surfaces, and structural shadow planes. It should read as material, not empty black. |
| End Gray | `#77747D` | Desaturated weathering, exposed edges, and neutral transition planes. |
| End Stone Cream | `#D8D0B4` | The visual relationship to vanilla End stone: mineral, dry, and ancient. Use as a structural light, not a bright highlight. |
| Deep Violet | `#312A3D` | Void-facing planes, depth, and the cold base of resonant materials. |
| Muted Violet | `#5E526E` | The primary alien hue. It connects End materials without becoming saturated fantasy purple. |
| Pale Lavender | `#C4BBCD` | Quiet mineral highlights, inscriptions, and soft reflected light. |
| Desaturated Cyan | `#7EA7A6` | Dormant resonance and cold reflected energy. It is a secondary signal, not a dominant surface color. |
| Resonance Cyan | `#A9E6DF` | Rare active resonance. Use only for a focal pixel, seam, eye, or charged interaction. |
| Muted Magenta | `#94647C` | Aged organic or crystalline variation. It supports violet without competing with resonance. |
| Ancient Gold | `#C6A85A` | Rare history, relic metal, or a deliberate ancient focal mark. Use sparingly and never as decoration. |
| Pale White | `#F2F0E5` | The highest-value highlight, reserved for a single important glint or discovery cue. |

### Palette constraints

- A normal 16x16 item should use 4–7 colors plus transparency.
- A material surface should use one dark, one base, one light, and at most one signal accent.
- Resonance Cyan and Ancient Gold must never be placed together unless the asset explicitly communicates an ancient powered mechanism.
- Saturated purple, saturated blue, pure white, and pure black are not part of the working palette.
- Accent pixels are meaningful signals, not noise.

## 2. Material language

Endesium materials are dry, mineral, aged, and slightly wrong in a quiet way. Surfaces should suggest fractured End stone, compressed void glass, weathered charcoal, and dormant crystal. The base material is usually desaturated; alien color appears in seams, inclusions, or reflected planes rather than covering the entire object.

Every material needs:

1. A readable silhouette.
2. A dominant base plane.
3. One deliberate shadow direction.
4. One deliberate light plane or fracture.
5. A reason for every accent pixel.

## 3. Item texture language

Items must be readable at inventory, dropped-item, and first-person scales. Use a strong asymmetric silhouette with transparent space around it. The outer contour should be defined by shape and value contrast, not by a thick black outline.

- 16x16 RGBA PNG only.
- Hard pixel edges and nearest-neighbor intent.
- No anti-aliasing or semi-transparent edge pixels.
- Transparent background must be alpha 0, not a checkerboard or colored matte.
- Use stepped diagonals and clustered pixels rather than smooth curves.
- Highlights follow the material plane; they do not outline the whole object.
- A single bright pixel can be more effective than a bright border.

## 4. Block texture language

Blocks should read as Minecraft blocks first. Prefer broad, quiet planes with a controlled seam, grain, vein, or inset motif. Avoid black cubes with scattered colorful pixels. The block's base should communicate what it is made of before any resonance effect is visible.

- Use a restrained base value close to End stone, charcoal, or muted gray.
- Keep high-contrast motifs large enough to survive the 16x16 tile grid.
- Reserve animated or bright marks for functional blocks.
- Avoid noise, checkerboard dithering, and repeated random speckles.

## 5. Mob texture language

Endesium creatures should be recognizable through silhouette and posture before texture detail. Void Stalker surfaces should be dark mineral or desaturated organic forms with a small, controlled resonance signal. Eyes, joints, or a core may carry Resonance Cyan; the entire body must not glow cyan or purple.

Animation should communicate intent: slow suspended idle motion, purposeful movement, and readable attack preparation. Motion is part of the creature's material identity and should not be decorative jitter.

## 6. Structure material language

End Ruins should feel older than the player and related to the End without looking like generic purple fantasy architecture. Combine End stone cream, End gray, charcoal shadow, and occasional muted violet insets. Ancient Gold is reserved for a relic mechanism, inscription, or one focal artifact.

Structures need a clear material hierarchy:

- Foundation: End stone cream or End gray.
- Weathering: Charcoal and deep violet recesses.
- Cultural signal: pale lavender marks or muted magenta mineral growth.
- Active relic: one controlled cyan or gold focal point.

## 7. Particle language

Particles should be sparse and purposeful. Dormant material uses tiny desaturated gray or violet motes. Resonance uses a few pale cyan pixels with a slow, deliberate rhythm. Gold particles are reserved for ancient mechanisms and discovery moments. No particle system should become a constant neon cloud.

## 8. Lighting and atmosphere

The Endesium mood is low-contrast darkness punctuated by readable mineral planes. Ambient lighting should preserve silhouettes and let the vanilla End remain recognizable. Fog, particles, and emissive accents should create depth, not erase it.

- Keep most surfaces in the charcoal, gray, cream, and deep-violet range.
- Use bright values only at focal points.
- Prefer cool reflected light over saturated colored light.
- Preserve the vanilla End's emptiness as negative space.

## 9. Resonance visual language

Resonance is an information signal. Dormant resonance is desaturated cyan or pale lavender; active resonance is Resonance Cyan; ancient resonance is a controlled Ancient Gold mark. The signal should appear in seams, cores, lenses, and responsive particles rather than as a full-surface tint.

Resonance visual progression:

1. Dormant: muted violet or desaturated cyan inclusion.
2. Attuned: one pale cyan highlight or seam.
3. Active: a small Resonance Cyan focal point with a readable pulse.
4. Ancient: a controlled gold relic mark paired with a restrained cyan response.

## Proposed first real asset

**Name:** Endesium Material Fragment (temporary concept asset)

**Purpose:** A non-gameplay 16x16 material study for approving the palette, silhouette, shading, and resonance signal before real items or blocks are produced.

**Design:** An asymmetric fractured mineral sliver with transparent negative space, a charcoal/deep-violet body, an End Stone Cream facet, one desaturated-cyan internal seam, and at most one Ancient Gold inclusion. The dark pixels define material planes inside the silhouette; they do not create a black square or an outline around empty space.

**Why it represents Endesium:** It connects visually to vanilla End stone, introduces the alien violet/charcoal material language, and uses cyan and gold as meaningful signals instead of decoration. Its small silhouette remains legible at Minecraft scale and establishes a reusable standard for Void Shard, Resonance Lens, ruin insets, and future creature details.
