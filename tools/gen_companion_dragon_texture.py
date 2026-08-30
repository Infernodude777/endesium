#!/usr/bin/env python3
"""Generates the companion dragon's skin from the vanilla dragon texture.

The hatched companion shares the vanilla EnderDragon model, so the boss and
the pet look identical. This recolors the vanilla skin into a fire palette -
deep red on the shadowed scales rising through orange to yellow on the
brightest highlights - so the player can tell Ember (fire) from the boss
(black-purple) at a glance.

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


def fire_tint(img):
    """Tints the whole skin into a red -> orange -> yellow fire gradient.

    The vanilla dragon skin is nearly black, so a plain hue-remap leaves it
    black. Every non-transparent pixel is forced onto the warm fire arc with
    hue rising along with brightness: shadowed scales go deep red, mid tones
    orange, and the brightest highlights yellow. The original luminance is
    kept (lifted slightly) so the model's shading stays readable.
    """
    out = img.copy()
    px = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            # Deep red (0.0) -> orange (0.07) -> yellow (0.14) as it brightens.
            hue = min(0.14, v * 0.16)
            s2 = max(0.55, min(1.0, s * 1.2 + 0.3))
            v2 = min(1.0, v * 1.4)
            r2, g2, b2 = colorsys.hsv_to_rgb(hue, s2, v2)
            px[x, y] = (int(r2 * 255), int(g2 * 255), int(b2 * 255), a)
    return out


def main():
    img = Image.open(REF).convert("RGBA")
    out = fire_tint(img)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    out.save(OUT)
    print(f"wrote {OUT} ({out.size[0]}x{out.size[1]})")


if __name__ == "__main__":
    main()
