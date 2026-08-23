#!/usr/bin/env python3
"""Regenerate the Void armor layer textures, trim overlay, and tool/item
sprites used by Endesium's void gear.

The armors are plain 2-pixel-per-block vanilla armor paint (64x32 RGBA,
same layout as netherite_layer_1 / layer_2). The tools are 16x16 RGBA
sprites with a charcoal void gradient, ancient-gold trim, and a faint
cyan resonance seam, so they read as the cooled Void sibling of the
netherite set.

Run:  python tools/regenerate_void_textures.py
"""

import os
import struct
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ITEM_DIR = os.path.join(ROOT, "src/main/resources/assets/endesium/textures/item")
ARMOR_DIR = os.path.join(ROOT, "src/main/resources/assets/endesium/textures/models/armor")
TRIMS_DIR = os.path.join(ROOT, "src/main/resources/assets/endesium/textures/trims/models/armor")

# ---------------------------------------------------------------- png writer
def write_png(path, width, height, pixels):
    """pixels: list of rows, each row a list of (r, g, b, a) tuples."""
    raw = b""
    for row in pixels:
        raw += b"\x00"  # filter: none
        for px in row:
            if len(px) == 3:
                px = (px[0], px[1], px[2], 255)
            r, g, b, a = px
            raw += bytes((r, g, b, a))
    def chunk(kind, data):
        c = kind + data
        return struct.pack(">I", len(data)) + c + struct.pack(">I", zlib.crc32(c) & 0xFFFFFFFF)
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


def save_png(path, width, height, paint):
    """paint: dict mapping (x, y) -> (r, g, b, a). Missing pixels are clear."""
    rows = []
    for y in range(height):
        row = []
        for x in range(width):
            row.append(paint.get((x, y), (0, 0, 0, 0)))
        rows.append(row)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    write_png(path, width, height, rows)

# ------------------------------------------------------------- palette
CHARCOAL = (38, 40, 46)
CHARCOAL_DARK = (24, 26, 31)
CHARCOAL_LIGHT = (58, 62, 72)
VOID_MID = (42, 48, 62)
VOID_DARK = (28, 33, 45)
CYAN = (103, 233, 226)
CYAN_DIM = (74, 168, 168)
CYAN_DEEP = (50, 110, 118)
GOLD = (201, 162, 39)
GOLD_LIGHT = (232, 201, 106)
GOLD_DARK = (140, 112, 27)


def shade(rgb, factor):
    return (min(255, int(rgb[0] * factor)), min(255, int(rgb[1] * factor)), min(255, int(rgb[2] * factor)))


def rect(paint, x0, y0, x1, y1, color):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            paint[(x, y)] = color


# ============================================================ armor: base plate
def armor_base():
    # The 64x32 sheet lays out body+legs in layer_1 and helmet+boots in
    # layer_2, and HumanoidArmorLayer paints each piece from those regions.
    paint = {}

    # --- torso body (layer_1) ----------------------------------------------
    rect(paint, 0, 0, 31, 7, CHARCOAL)             # body plate
    rect(paint, 0, 0, 31, 0, CHARCOAL_LIGHT)       # collar highlight
    rect(paint, 0, 1, 2, 7, VOID_MID)              # left edge shade
    rect(paint, 29, 1, 31, 7, VOID_DARK)           # right edge shade
    rect(paint, 4, 2, 27, 2, VOID_MID)             # chest band
    rect(paint, 6, 4, 9, 6, CHARCOAL_DARK)         # heart plate
    rect(paint, 7, 4, 8, 6, CYAN_DEEP)             # heart seam
    paint[(7, 4)] = CYAN
    paint[(8, 4)] = CYAN
    paint[(7, 6)] = CYAN
    paint[(8, 6)] = CYAN
    rect(paint, 14, 4, 16, 6, CYAN_DEEP)           # waist seam
    paint[(14, 4)] = CYAN
    paint[(16, 6)] = CYAN
    rect(paint, 0, 6, 31, 7, CHARCOAL_DARK)        # belly shade
    rect(paint, 0, 6, 31, 6, GOLD)                 # belt trim
    paint[(0, 6)] = GOLD_DARK
    paint[(31, 6)] = GOLD_DARK

    # --- legs (layer_1) ----------------------------------------------------
    rect(paint, 0, 16, 7, 23, CHARCOAL)            # left leg
    rect(paint, 0, 16, 0, 23, VOID_DARK)           # outer shade
    rect(paint, 1, 17, 6, 19, VOID_MID)            # hip plate
    rect(paint, 1, 20, 6, 22, CHARCOAL_DARK)       # shin shade
    rect(paint, 1, 22, 6, 23, GOLD)                # boot cuff
    paint[(1, 22)] = CYAN
    paint[(6, 22)] = CYAN
    rect(paint, 8, 16, 15, 23, CHARCOAL)           # right leg
    rect(paint, 8, 16, 8, 23, VOID_DARK)
    rect(paint, 9, 17, 14, 19, VOID_MID)
    rect(paint, 9, 20, 14, 22, CHARCOAL_DARK)
    rect(paint, 9, 22, 14, 23, GOLD)
    paint[(9, 22)] = CYAN
    paint[(14, 22)] = CYAN

    # --- helmet (layer_2) --------------------------------------------------
    rect(paint, 32, 0, 63, 7, CHARCOAL)            # head dome
    rect(paint, 33, 0, 62, 0, CHARCOAL_LIGHT)
    rect(paint, 35, 1, 39, 2, VOID_DARK)           # brow shade
    rect(paint, 56, 1, 60, 2, VOID_DARK)
    rect(paint, 44, 1, 51, 2, CYAN_DEEP)           # forehead seam
    paint[(45, 1)] = CYAN
    paint[(46, 2)] = CYAN
    paint[(49, 1)] = CYAN
    paint[(50, 2)] = CYAN
    rect(paint, 33, 3, 62, 4, VOID_MID)            # brow band
    rect(paint, 36, 3, 39, 3, GOLD_DARK)           # gold rivets
    rect(paint, 56, 3, 59, 3, GOLD_DARK)
    rect(paint, 35, 5, 60, 6, CHARCOAL_DARK)       # visor
    rect(paint, 38, 5, 44, 5, CYAN_DIM)            # left eye slit
    rect(paint, 52, 5, 58, 5, CYAN_DIM)            # right eye slit
    rect(paint, 40, 6, 55, 7, GOLD)                # jaw trim
    paint[(46, 6)] = CYAN
    paint[(49, 6)] = CYAN

    # --- boots (layer_2) ---------------------------------------------------
    rect(paint, 32, 16, 39, 23, CHARCOAL)          # left boot
    rect(paint, 32, 16, 32, 19, VOID_DARK)
    rect(paint, 33, 17, 38, 20, CHARCOAL_DARK)
    rect(paint, 33, 21, 38, 23, GOLD)              # foot cap
    rect(paint, 36, 21, 37, 23, CYAN)              # cleft
    rect(paint, 34, 17, 35, 20, VOID_MID)
    rect(paint, 40, 16, 47, 23, CHARCOAL)          # right boot
    rect(paint, 40, 16, 40, 19, VOID_DARK)
    rect(paint, 41, 17, 46, 20, CHARCOAL_DARK)
    rect(paint, 41, 21, 46, 23, GOLD)
    rect(paint, 42, 21, 43, 23, CYAN)
    rect(paint, 44, 17, 45, 20, VOID_MID)
    return paint


# ============================================================ trim overlay
def trim_overlay():
    paint = {}
    # Only the gold trim edges; the overlay is tinted by the trim material.
    rect(paint, 33, 4, 38, 5, (255, 255, 255))
    rect(paint, 57, 4, 62, 5, (255, 255, 255))
    rect(paint, 47, 13, 48, 13, (255, 255, 255))
    return paint


# ============================================================ item sprites
def sprite_sword():
    paint = {}
    # Blade spine
    for y in range(0, 9):
        paint[(7, y)] = (230, 250, 248)
        paint[(8, y)] = CYAN
    # Guard
    for x in range(4, 12):
        paint[(x, 8)] = GOLD
        paint[(x, 9)] = GOLD_DARK if x in (4, 11) else GOLD
    # Grip
    for y in range(10, 16):
        paint[(7, y)] = (52, 57, 68)
        paint[(8, y)] = (38, 42, 52)
    paint[(6, 10)] = GOLD_DARK
    paint[(9, 10)] = GOLD_DARK
    paint[(6, 14)] = CYAN_DIM
    paint[(9, 14)] = CYAN_DIM
    return paint


def sprite_pickaxe():
    paint = {}
    # Curved netherite-style head
    paint[(4, 3)] = CYAN
    paint[(5, 3)] = CYAN
    paint[(6, 3)] = GOLD
    paint[(7, 4)] = GOLD_LIGHT
    paint[(8, 5)] = GOLD_LIGHT
    paint[(9, 6)] = GOLD
    paint[(10, 7)] = GOLD_DARK
    paint[(3, 4)] = CYAN_DIM
    paint[(4, 5)] = CYAN
    paint[(5, 6)] = CYAN_DEEP
    paint[(6, 7)] = CYAN_DIM
    # Handle
    for y in range(8, 16):
        paint[(6, y)] = (62, 67, 80)
        paint[(7, y)] = (48, 53, 64)
    paint[(6, 8)] = GOLD_DARK
    paint[(7, 8)] = GOLD_DARK
    paint[(6, 15)] = CYAN_DIM
    paint[(7, 15)] = CYAN_DIM
    return paint


def sprite_axe():
    paint = {}
    # Blade wing
    paint[(3, 3)] = CYAN
    paint[(4, 3)] = CYAN
    paint[(5, 4)] = GOLD
    paint[(6, 5)] = GOLD_LIGHT
    paint[(7, 6)] = GOLD_LIGHT
    paint[(8, 7)] = GOLD
    paint[(2, 4)] = CYAN_DIM
    paint[(3, 5)] = CYAN
    paint[(4, 6)] = CYAN_DIM
    paint[(5, 7)] = CYAN_DEEP
    # Back edge
    paint[(9, 3)] = CYAN_DIM
    paint[(10, 4)] = CYAN_DIM
    paint[(11, 5)] = CYAN_DEEP
    # Handle
    for y in range(8, 16):
        paint[(6, y)] = (62, 67, 80)
        paint[(7, y)] = (48, 53, 64)
    paint[(6, 8)] = GOLD_DARK
    paint[(7, 8)] = GOLD_DARK
    paint[(6, 15)] = CYAN_DIM
    paint[(7, 15)] = CYAN_DIM
    return paint


def sprite_shovel():
    paint = {}
    # Head
    for x in range(5, 11):
        paint[(x, 2)] = (230, 250, 248) if x in (6, 7, 8, 9) else CYAN
    paint[(5, 3)] = CYAN
    paint[(10, 3)] = CYAN
    paint[(6, 3)] = CYAN_DEEP
    paint[(9, 3)] = CYAN_DEEP
    for x in range(5, 11):
        paint[(x, 4)] = GOLD
    paint[(5, 4)] = GOLD_DARK
    paint[(10, 4)] = GOLD_DARK
    # Handle
    for y in range(5, 16):
        paint[(7, y)] = (62, 67, 80)
        paint[(8, y)] = (48, 53, 64)
    paint[(7, 5)] = GOLD_DARK
    paint[(8, 5)] = GOLD_DARK
    paint[(7, 15)] = CYAN_DIM
    paint[(8, 15)] = CYAN_DIM
    return paint


def sprite_hoe():
    paint = {}
    # Blade
    for x in range(6, 12):
        paint[(x, 3)] = GOLD
        paint[(x, 4)] = GOLD_LIGHT
    paint[(11, 2)] = CYAN_DIM
    paint[(12, 3)] = CYAN
    paint[(12, 4)] = CYAN
    paint[(5, 4)] = CYAN_DIM
    paint[(10, 3)] = CYAN
    # Handle
    for y in range(6, 16):
        paint[(7, y)] = (62, 67, 80)
        paint[(8, y)] = (48, 53, 64)
    paint[(7, 6)] = GOLD_DARK
    paint[(8, 6)] = GOLD_DARK
    paint[(7, 15)] = CYAN_DIM
    paint[(8, 15)] = CYAN_DIM
    return paint


def sprite_helmet():
    paint = {}
    # Dome
    for x in range(4, 12):
        paint[(x, 6)] = (70, 76, 90)
        paint[(x, 7)] = (58, 63, 74)
        paint[(x, 8)] = (48, 53, 64)
    paint[(5, 7)] = (50, 55, 66)
    paint[(10, 7)] = (50, 55, 66)
    # Crest
    paint[(7, 3)] = (44, 48, 58)
    paint[(8, 3)] = (44, 48, 58)
    paint[(7, 4)] = (52, 57, 68)
    paint[(8, 4)] = (52, 57, 68)
    paint[(7, 5)] = (44, 48, 58)
    paint[(8, 5)] = (44, 48, 58)
    # Brim
    for x in range(4, 12):
        paint[(x, 9)] = GOLD
    # Eye slit
    for x in range(5, 11):
        paint[(x, 11)] = CYAN
    paint[(5, 11)] = (200, 255, 252)
    # Jaw
    for x in range(6, 10):
        paint[(x, 13)] = CYAN_DEEP
        paint[(x, 14)] = (38, 42, 52)
    return paint


def sprite_chest():
    paint = {}
    # Core plate
    for x in range(4, 12):
        paint[(x, 4)] = (58, 63, 74)
        paint[(x, 5)] = (52, 57, 68)
        paint[(x, 6)] = (48, 53, 64)
        paint[(x, 7)] = (44, 48, 58)
        paint[(x, 8)] = (52, 57, 68)
        paint[(x, 9)] = (58, 63, 74)
    # Gold straps
    paint[(4, 4)] = GOLD
    paint[(4, 5)] = GOLD
    paint[(4, 8)] = GOLD
    paint[(4, 9)] = GOLD
    paint[(11, 4)] = GOLD
    paint[(11, 5)] = GOLD
    paint[(11, 8)] = GOLD
    paint[(11, 9)] = GOLD
    # Cyan heart gem
    paint[(7, 6)] = (200, 255, 252)
    paint[(8, 6)] = CYAN
    paint[(7, 7)] = CYAN
    paint[(8, 7)] = CYAN_DEEP
    return paint


def sprite_legs():
    paint = {}
    # Waist
    for x in range(4, 12):
        paint[(x, 3)] = (62, 67, 80)
        paint[(x, 4)] = (52, 57, 68)
    # Gold belt
    for x in range(5, 11):
        paint[(x, 5)] = GOLD
    # Legs
    for y in range(7, 13):
        paint[(6, y)] = (52, 57, 68)
        paint[(7, y)] = (62, 67, 80)
        paint[(8, y)] = (44, 48, 58)
        paint[(9, y)] = (52, 57, 68)
    # Gold knee trim
    paint[(6, 10)] = GOLD_DARK
    paint[(7, 10)] = GOLD
    paint[(8, 10)] = GOLD
    paint[(9, 10)] = GOLD_DARK
    # Boot cuffs with cyan
    paint[(6, 13)] = CYAN_DIM
    paint[(7, 13)] = CYAN
    paint[(8, 13)] = CYAN
    paint[(9, 13)] = CYAN_DIM
    return paint


def sprite_boots():
    paint = {}
    for x in range(4, 12):
        paint[(x, 6)] = (52, 57, 68)
        paint[(x, 7)] = (58, 63, 74)
    # Gold caps
    paint[(4, 8)] = GOLD
    paint[(5, 8)] = GOLD_LIGHT
    paint[(6, 8)] = GOLD
    paint[(9, 8)] = GOLD
    paint[(10, 8)] = GOLD_LIGHT
    paint[(11, 8)] = GOLD
    # Soles
    paint[(5, 9)] = CYAN_DIM
    paint[(6, 9)] = CYAN
    paint[(7, 9)] = (38, 42, 52)
    paint[(8, 9)] = (38, 42, 52)
    paint[(9, 9)] = CYAN
    paint[(10, 9)] = CYAN_DIM
    return paint


def sprite_ingot():
    paint = {}
    # Ingot body
    for x in range(4, 12):
        paint[(x, 5)] = (160, 168, 182)
        paint[(x, 6)] = (196, 204, 216)
        paint[(x, 7)] = (212, 219, 230)
        paint[(x, 8)] = (196, 204, 216)
        paint[(x, 9)] = (150, 158, 172)
    # Notches
    paint[(5, 6)] = (180, 188, 202)
    paint[(10, 6)] = (128, 136, 150)
    paint[(5, 8)] = (176, 184, 198)
    paint[(10, 8)] = (124, 132, 146)
    # Cyan vent
    paint[(7, 6)] = CYAN
    paint[(8, 6)] = CYAN
    paint[(7, 7)] = (200, 255, 252)
    paint[(8, 7)] = CYAN
    # Gold corner flecks
    paint[(4, 5)] = GOLD
    paint[(11, 9)] = GOLD
    return paint


# ------------------------------------------------------------------- main
def main():
    save_png(os.path.join(ARMOR_DIR, "void_layer_1.png"), 64, 32, armor_base())
    save_png(os.path.join(ARMOR_DIR, "void_layer_2.png"), 64, 32, armor_base())
    save_png(os.path.join(TRIMS_DIR, "void_trim.png"), 64, 32, trim_overlay())

    for name, fn in [
        ("void_sword", sprite_sword),
        ("void_pickaxe", sprite_pickaxe),
        ("void_axe", sprite_axe),
        ("void_shovel", sprite_shovel),
        ("void_hoe", sprite_hoe),
        ("void_helmet", sprite_helmet),
        ("void_chestplate", sprite_chest),
        ("void_leggings", sprite_legs),
        ("void_boots", sprite_boots),
        ("void_ingot", sprite_ingot),
    ]:
        save_png(os.path.join(ITEM_DIR, name + ".png"), 16, 16, fn())
        print("wrote", name)

    print("Void textures regenerated.")


if __name__ == "__main__":
    main()