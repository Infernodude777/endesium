#!/usr/bin/env python3
"""Generate pixel-art armor textures for the Void and Ashen armor sets.

Item sprites are 16x16 with a transparent background (Minecraft item style).
Armor layer textures are the standard 64x32 armor UV layout:
  layer_1 = head + body + arms + boots (helmet / chestplate / boots)
  layer_2 = waist + legs (leggings)

Palettes:
  Void  : dark slate (27,27,34) with pale cyan (126,167,166) seams
  Ashen : ash gray (62,61,68) with ember orange (232,120,44) glow
"""
import os
import struct
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ITEM_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "endesium", "textures", "item")
ARMOR_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "endesium", "textures", "models", "armor")

os.makedirs(ITEM_DIR, exist_ok=True)
os.makedirs(ARMOR_DIR, exist_ok=True)

# ── palettes ──────────────────────────────────────────────────────────────
VOID_BASE = (27, 27, 34)      # dark slate
VOID_BASE_L = (38, 38, 48)    # lit slate
VOID_BASE_D = (17, 17, 23)    # shadow slate
VOID_CYAN = (126, 167, 166)   # pale cyan seam
VOID_CYAN_D = (82, 122, 121)  # darker cyan
ASH_BASE = (62, 61, 68)       # ash gray
ASH_BASE_L = (87, 85, 94)
ASH_BASE_D = (40, 39, 46)
ASH_EMBER = (232, 120, 44)    # ember orange
ASH_EMBER_D = (176, 82, 28)


# ── tiny PNG writer ───────────────────────────────────────────────────────
def write_png(path, w, h, pixels):
    """pixels: list of (r,g,b,a) rows of length w*h."""
    raw = bytearray()
    for y in range(h):
        raw.append(0)  # filter: none
        for x in range(w):
            r, g, b, a = pixels[y * w + x]
            raw += bytes((r, g, b, a))
    def chunk(typ, data):
        c = struct.pack(">I", len(data)) + typ + data
        c += struct.pack(">I", zlib.crc32(typ + data) & 0xFFFFFFFF)
        return c
    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", ihdr)
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as fh:
        fh.write(png)


def blank(w, h, bg=(0, 0, 0, 0)):
    return [bg] * (w * h)


def setp(px, w, h, x, y, c):
    if 0 <= x < w and 0 <= y < h:
        if len(c) == 3:
            c = (c[0], c[1], c[2], 255)
        px[y * w + x] = c


def rect(px, w, h, x0, y0, x1, y1, c, fill=True):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            setp(px, w, h, x, y, c)


def outline(px, w, h, x0, y0, x1, y1, c):
    rect(px, w, h, x0, y0, x1, y0, c)
    rect(px, w, h, x0, y1, x1, y1, c)
    rect(px, w, h, x0, y0, x0, y1, c)
    rect(px, w, h, x1, y0, x1, y1, c)


# ── 16x16 item sprites ────────────────────────────────────────────────────
def item_void_helmet():
    px = blank(16, 16)
    # helmet dome (pixels 3..12 x, 2..9 y)
    rect(px, 16, 16, 4, 2, 11, 8, VOID_BASE)
    rect(px, 16, 16, 4, 2, 11, 3, VOID_BASE_L)       # top highlight
    rect(px, 16, 16, 3, 7, 3, 8, VOID_BASE_D)        # left shade
    rect(px, 16, 16, 12, 7, 12, 8, VOID_BASE_D)
    # brim
    rect(px, 16, 16, 3, 9, 12, 9, VOID_BASE_D)
    # cyan visor
    rect(px, 16, 16, 5, 6, 10, 7, VOID_CYAN)
    rect(px, 16, 16, 6, 6, 9, 6, VOID_CYAN_D)
    # side vents
    setp(px, 16, 16, 4, 5, VOID_CYAN_D)
    setp(px, 16, 16, 11, 5, VOID_CYAN_D)
    return px


def item_void_chestplate():
    px = blank(16, 16)
    # torso
    rect(px, 16, 16, 5, 3, 10, 12, VOID_BASE)
    rect(px, 16, 16, 5, 3, 10, 4, VOID_BASE_L)
    # shoulder pads
    rect(px, 16, 16, 3, 3, 4, 6, VOID_BASE_D)
    rect(px, 16, 16, 11, 3, 12, 6, VOID_BASE_D)
    # cyan center seam
    rect(px, 16, 16, 7, 4, 8, 11, VOID_CYAN_D)
    setp(px, 16, 16, 7, 5, VOID_CYAN)
    setp(px, 16, 16, 8, 5, VOID_CYAN)
    # lower trim
    rect(px, 16, 16, 5, 11, 10, 12, VOID_BASE_D)
    return px


def item_void_leggings():
    px = blank(16, 16)
    # waist
    rect(px, 16, 16, 5, 2, 10, 5, VOID_BASE)
    # two legs
    rect(px, 16, 16, 5, 6, 7, 12, VOID_BASE)
    rect(px, 16, 16, 8, 6, 10, 12, VOID_BASE)
    rect(px, 16, 16, 5, 6, 7, 7, VOID_BASE_L)
    rect(px, 16, 16, 8, 6, 10, 7, VOID_BASE_L)
    # cyan knee seams
    rect(px, 16, 16, 5, 9, 7, 10, VOID_CYAN_D)
    rect(px, 16, 16, 8, 9, 10, 10, VOID_CYAN_D)
    setp(px, 16, 16, 6, 9, VOID_CYAN)
    setp(px, 16, 16, 9, 9, VOID_CYAN)
    return px


def item_void_boots():
    px = blank(16, 16)
    # boots
    rect(px, 16, 16, 4, 4, 7, 11, VOID_BASE)
    rect(px, 16, 16, 8, 4, 11, 11, VOID_BASE)
    # soles
    rect(px, 16, 16, 4, 11, 7, 12, VOID_BASE_D)
    rect(px, 16, 16, 8, 11, 11, 12, VOID_BASE_D)
    # cyan ankle seams
    rect(px, 16, 16, 4, 8, 7, 9, VOID_CYAN_D)
    rect(px, 16, 16, 8, 8, 11, 9, VOID_CYAN_D)
    setp(px, 16, 16, 5, 8, VOID_CYAN)
    setp(px, 16, 16, 10, 8, VOID_CYAN)
    return px


def item_ashwalker_boots():
    px = blank(16, 16)
    rect(px, 16, 16, 4, 4, 7, 11, ASH_BASE)
    rect(px, 16, 16, 8, 4, 11, 11, ASH_BASE)
    rect(px, 16, 16, 4, 4, 7, 5, ASH_BASE_L)
    rect(px, 16, 16, 8, 4, 11, 5, ASH_BASE_L)
    rect(px, 16, 16, 4, 11, 7, 12, ASH_BASE_D)
    rect(px, 16, 16, 8, 11, 11, 12, ASH_BASE_D)
    # ember glow seams
    rect(px, 16, 16, 4, 8, 7, 9, ASH_EMBER_D)
    rect(px, 16, 16, 8, 8, 11, 9, ASH_EMBER_D)
    setp(px, 16, 16, 5, 8, ASH_EMBER)
    setp(px, 16, 16, 10, 8, ASH_EMBER)
    return px


# ── 64x32 armor layer textures ───────────────────────────────────────────
# Standard UV regions (layer_1: body/arms/legs; layer_2: head/feet).
#   head:      x 0..8,  y 0..8
#   body:      x 16..32, y 16..32
#   right arm: x 40..48, y 16..32
#   left arm:  x 48..56, y 16..32
#   right leg: x 0..8,   y 16..32
#   left leg:  x 8..16,  y 16..32

def layer_body(px, base, base_l, base_d, seam, seam_d):
    """Paint the body + arms + legs overlay for layer_1."""
    # body (torso)
    rect(px, 64, 32, 20, 16, 27, 30, base)
    rect(px, 64, 32, 20, 16, 27, 18, base_l)
    # cyan chest seam
    rect(px, 64, 32, 23, 18, 24, 28, seam_d)
    setp(px, 64, 32, 23, 19, seam)
    setp(px, 64, 32, 24, 19, seam)
    # shoulder straps
    setp(px, 64, 32, 19, 16, seam_d)
    setp(px, 64, 32, 28, 16, seam_d)
    # arms
    for ax in (40, 48):
        rect(px, 64, 32, ax + 1, 17, ax + 6, 29, base)
        rect(px, 64, 32, ax + 1, 17, ax + 6, 19, base_l)
        rect(px, 64, 32, ax + 5, 17, ax + 6, 29, base_d)
        # elbow seam
        rect(px, 64, 32, ax + 2, 22, ax + 5, 23, seam_d)
        setp(px, 64, 32, ax + 3, 22, seam)
    # legs
    for lx in (0, 8):
        rect(px, 64, 32, lx + 1, 16, lx + 6, 30, base)
        rect(px, 64, 32, lx + 1, 16, lx + 6, 18, base_l)
        rect(px, 64, 32, lx + 6, 16, lx + 7, 30, base_d)
        # knee seam
        rect(px, 64, 32, lx + 2, 22, lx + 5, 23, seam_d)
        setp(px, 64, 32, lx + 3, 22, seam)


def layer_head_feet(px, base, base_l, base_d, seam, seam_d):
    """Paint the helmet + boots overlay for layer_2."""
    # helmet (head region, x 0..8, y 0..8)
    rect(px, 64, 32, 1, 1, 7, 7, base)
    rect(px, 64, 32, 1, 1, 7, 2, base_l)
    rect(px, 64, 32, 1, 6, 7, 7, base_d)
    # cyan visor
    rect(px, 64, 32, 2, 4, 6, 5, seam_d)
    setp(px, 64, 32, 2, 4, seam)
    setp(px, 64, 32, 6, 4, seam)
    # boots (bottom of leg regions, y 27..31)
    for lx in (0, 8):
        rect(px, 64, 32, lx + 1, 27, lx + 6, 31, base)
        rect(px, 64, 32, lx + 6, 27, lx + 7, 31, base_d)
        # ankle seam
        rect(px, 64, 32, lx + 2, 28, lx + 5, 29, seam_d)
        setp(px, 64, 32, lx + 3, 28, seam)


def layer_void_1():
    # Vanilla uses layer 1 for the helmet, chestplate, and boots. Keep the
    # head and feet regions together with the body so every outer armor model
    # has a visible standard-Minecraft silhouette.
    px = blank(64, 32)
    layer_body(px, VOID_BASE, VOID_BASE_L, VOID_BASE_D, VOID_CYAN, VOID_CYAN_D)
    layer_head_feet(px, VOID_BASE, VOID_BASE_L, VOID_BASE_D, VOID_CYAN, VOID_CYAN_D)
    return px


def layer_void_2():
    # Vanilla's inner model is the leggings layer.
    px = blank(64, 32)
    layer_body(px, VOID_BASE, VOID_BASE_L, VOID_BASE_D, VOID_CYAN, VOID_CYAN_D)
    return px


def layer_ashen_1():
    px = blank(64, 32)
    layer_body(px, ASH_BASE, ASH_BASE_L, ASH_BASE_D, ASH_EMBER, ASH_EMBER_D)
    layer_head_feet(px, ASH_BASE, ASH_BASE_L, ASH_BASE_D, ASH_EMBER, ASH_EMBER_D)
    return px


def layer_ashen_2():
    px = blank(64, 32)
    layer_body(px, ASH_BASE, ASH_BASE_L, ASH_BASE_D, ASH_EMBER, ASH_EMBER_D)
    return px


# ── emit ──────────────────────────────────────────────────────────────────
items = {
    "void_helmet": item_void_helmet,
    "void_chestplate": item_void_chestplate,
    "void_leggings": item_void_leggings,
    "void_boots": item_void_boots,
    "ashwalker_boots": item_ashwalker_boots,
}
for name, fn in items.items():
    write_png(os.path.join(ITEM_DIR, name + ".png"), 16, 16, fn())
    print(f"item {name}.png written")

layers = {
    "void_layer_1": layer_void_1,
    "void_layer_2": layer_void_2,
    "ashen_layer_1": layer_ashen_1,
    "ashen_layer_2": layer_ashen_2,
}
for name, fn in layers.items():
    write_png(os.path.join(ARMOR_DIR, name + ".png"), 64, 32, fn())
    print(f"layer {name}.png written")
