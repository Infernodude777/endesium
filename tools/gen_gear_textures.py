#!/usr/bin/env python3
"""Generates recolored gear textures from the Void set. Every Luminous, Ash,
Null, and Dragon Wings texture (item icons + worn armor layers) is derived
from the Void set's pixels with a palette remap, so silhouettes stay identical
while each line reads as its own material. Also fixes the Ashwalker Boots
item icon (recolored from void_boots to the ashen palette).

Run from the repo root: python tools/gen_gear_textures.py
Requires Pillow."""
import colorsys
import pathlib
import sys

try:
    from PIL import Image
except ImportError:
    sys.exit("Pillow is required: pip install Pillow")

TEX = pathlib.Path("src/main/resources/assets/endesium/textures")


def remap_img(img, hue_target, sat_mult, val_mult, sat_floor=0.0):
    """Shifts every non-transparent pixel's hue toward hue_target (0-1),
    scales saturation and value, keeping the original alpha and shading."""
    out = img.copy()
    px = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if s < 0.08:
                s2 = s
            else:
                s2 = max(sat_floor, s * sat_mult)
            v2 = min(1.0, v * val_mult)
            r2, g2, b2 = colorsys.hsv_to_rgb(hue_target, s2, v2)
            px[x, y] = (int(r2 * 255), int(g2 * 255), int(b2 * 255), a)
    return out


# Palette remaps: (hue, saturation multiplier, value multiplier)
LUMINOUS = (0.13, 0.9, 1.35)   # gold-cyan, brighter
NULL = (0.75, 1.1, 0.65)       # deep violet, darker
ASH = (0.05, 0.5, 0.85)        # warm gray-ember
DRAGON = (0.98, 1.0, 0.9)      # crimson

JOBS = [
    # (source, destination, remap)
    ("item/void_helmet.png", "item/luminous_helmet.png", LUMINOUS),
    ("item/void_chestplate.png", "item/luminous_chestplate.png", LUMINOUS),
    ("item/void_leggings.png", "item/luminous_leggings.png", LUMINOUS),
    ("item/void_boots.png", "item/luminous_boots.png", LUMINOUS),
    ("item/void_sword.png", "item/luminous_sword.png", LUMINOUS),
    ("item/void_pickaxe.png", "item/luminous_pickaxe.png", LUMINOUS),
    ("item/void_axe.png", "item/luminous_axe.png", LUMINOUS),
    ("item/void_shovel.png", "item/luminous_shovel.png", LUMINOUS),
    ("item/void_hoe.png", "item/luminous_hoe.png", LUMINOUS),
    ("models/armor/void_layer_1.png", "models/armor/luminous_layer_1.png", LUMINOUS),
    ("models/armor/void_layer_2.png", "models/armor/luminous_layer_2.png", LUMINOUS),
    ("item/void_helmet.png", "item/null_helmet.png", NULL),
    ("item/void_chestplate.png", "item/null_chestplate.png", NULL),
    ("item/void_leggings.png", "item/null_leggings.png", NULL),
    ("item/void_boots.png", "item/null_boots.png", NULL),
    ("item/void_sword.png", "item/null_sword.png", NULL),
    ("item/void_pickaxe.png", "item/null_pickaxe.png", NULL),
    ("item/void_axe.png", "item/null_axe.png", NULL),
    ("item/void_shovel.png", "item/null_shovel.png", NULL),
    ("item/void_hoe.png", "item/null_hoe.png", NULL),
    ("models/armor/void_layer_1.png", "models/armor/null_layer_1.png", NULL),
    ("models/armor/void_layer_2.png", "models/armor/null_layer_2.png", NULL),
    # Ashwalker Boots icon fix: recolor from void_boots to the ashen palette.
    ("item/void_boots.png", "item/ashwalker_boots.png", ASH),
    ("item/void_sword.png", "item/ash_sword.png", ASH),
    ("item/void_pickaxe.png", "item/ash_pickaxe.png", ASH),
    ("item/void_axe.png", "item/ash_axe.png", ASH),
    ("item/void_shovel.png", "item/ash_shovel.png", ASH),
    ("item/void_hoe.png", "item/ash_hoe.png", ASH),
    ("item/void_helmet.png", "item/ash_helmet.png", ASH),
    ("item/void_chestplate.png", "item/ash_chestplate.png", ASH),
    ("item/void_leggings.png", "item/ash_leggings.png", ASH),
    # Dragon Wings: recolored from the resonant wings.
    ("item/resonant_wings.png", "item/dragon_wings.png", DRAGON),
    ("entity/resonant_wings.png", "entity/dragon_wings.png", DRAGON),
]


def main():
    count = 0
    for src_rel, dst_rel, remap in JOBS:
        src = TEX / src_rel
        dst = TEX / dst_rel
        if not src.exists():
            print(f"[skip] {src_rel} not found")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        img = Image.open(src).convert("RGBA")
        result = remap_img(img, *remap)
        result.save(dst)
        count += 1
        print(f"[ok] {dst_rel}")
    print(f"Generated {count} gear textures.")


if __name__ == "__main__":
    main()
