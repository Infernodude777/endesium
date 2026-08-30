#!/usr/bin/env python3
"""Generates the companion dragon's skin from the vanilla dragon texture.

The hatched companion shares the vanilla EnderDragon model, so the boss and
the pet look identical. This recolors the vanilla skin to the mod's resonance
cyan (the same palette family as the Resonant Bloom), preserving the original
shading so the model keeps its depth - the player can tell Ember from the
boss at a glance.

Reference: tools/ref/dragon.png (extracted verbatim from vanilla 1.21.1).
Output: src/main/resources/assets/endesium/textures/entity/enderdragon/dragon.png

Run from the repo root: python tools/gen_companion_dragon_texture.py
Requires Pillow.
"""
import colorsys
import pathlib
import sys

try:
    from PIL import Image
except ImportError:
    sys.exit("Pillow is required: pip install Pillow")

REF = pathlib.Path("tools/ref/dragon.png")
OUT = pathlib.Path("src/main/resources/assets/endesium/textures/entity/enderdragon/dragon.png")


def tint_img(img, hue_target, sat_mult, val_mult, sat_floor):
    """Recolors the whole skin toward hue_target (0-1).

    The vanilla dragon skin is nearly black, so a plain hue-remap leaves it
    black. This forces every non-transparent pixel to the target hue and
    lifts the darkest grays up to a saturation floor, keeping the original
    luminance (and therefore the model's shading) intact.
    """
    out = img.copy()
    px = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            s2 = max(sat_floor, min(1.0, s * sat_mult))
            v2 = min(1.0, v * val_mult)
            r2, g2, b2 = colorsys.hsv_to_rgb(hue_target, s2, v2)
            px[x, y] = (int(r2 * 255), int(g2 * 255), int(b2 * 255), a)
    return out


def main():
    img = Image.open(REF).convert("RGBA")
    # Resonance cyan: hue 0.50 (180 deg), pushed saturation and brightness so
    # the pet reads clearly against the boss's dark purple-black.
    out = tint_img(img, 0.50, 1.2, 1.35, sat_floor=0.45)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    out.save(OUT)
    print(f"wrote {OUT} ({out.size[0]}x{out.size[1]})")


if __name__ == "__main__":
    main()
