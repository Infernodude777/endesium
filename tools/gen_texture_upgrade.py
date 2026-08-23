#!/usr/bin/env python3
"""Upgrade all flat placeholder textures to real pixel art.

Covers the 60 flat textures found by the audit:
- 16 block textures (noise-based stone/soil/plant art)
- 43 item textures (shaded sprites on transparent backgrounds)
- entity/resonant_wings.png (worn elytra layer, 64x32)
"""
import os
import struct
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "endesium", "textures")


# ── tiny PNG writer ───────────────────────────────────────────────────────
def write_png(path, w, h, pixels):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
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


def blank(w, h):
    return [(0, 0, 0, 0)] * (w * h)


def setp(px, w, h, x, y, c):
    if 0 <= x < w and 0 <= y < h:
        if len(c) == 3:
            c = (c[0], c[1], c[2], 255)
        px[y * w + x] = c


def rect(px, w, h, x0, y0, x1, y1, c, fill=True):
    for y in range(max(0, y0), min(h, y1 + 1)):
        for x in range(max(0, x0), min(w, x1 + 1)):
            setp(px, w, h, x, y, c)


def hline(px, w, h, x0, x1, y, c):
    rect(px, w, h, x0, y, x1, y, c)


def vline(px, w, h, x, y0, y1, c):
    rect(px, w, h, x, y0, x, y1, c)


def outline(px, w, h, x0, y0, x1, y1, c):
    hline(px, w, h, x0, x1, y0, c)
    hline(px, w, h, x0, x1, y1, c)
    vline(px, w, h, x0, y0, y1, c)
    vline(px, w, h, x1, y0, y1, c)


def diamond(px, w, h, cx, cy, r, c):
    for dy in range(-r, r + 1):
        rem = r - abs(dy)
        for dx in range(-rem, rem + 1):
            setp(px, w, h, cx + dx, cy + dy, c)


def circle(px, w, h, cx, cy, r, c):
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            if dx * dx + dy * dy <= r * r + r:
                setp(px, w, h, cx + dx, cy + dy, c)


def tri(px, w, h, cx, y_top, width, height, c):
    """Isosceles triangle pointing down from (cx, y_top)."""
    for dy in range(height):
        half = max(0, (width * (dy + 1)) // (2 * height))
        for dx in range(-half, half + 1):
            setp(px, w, h, cx + dx, y_top + dy, c)


def noise_fill(px, w, h, seed, palette, weight):
    """Speckle texture with palette colors; weight = prob of light/dark speckle."""
    import random
    rng = random.Random(seed)
    for y in range(h):
        for x in range(w):
            r = rng.random()
            if r < weight[0]:
                setp(px, w, h, x, y, palette[0])   # light speckle
            elif r < weight[0] + weight[1]:
                setp(px, w, h, x, y, palette[1])   # dark speckle
            else:
                setp(px, w, h, x, y, palette[2])   # base


# ── shared item painters ──────────────────────────────────────────────────
def paint_ingot(px, base, light, dark, accent=None):
    # blocky ingot shape, 10x8
    rect(px, 16, 16, 3, 4, 12, 11, base)
    rect(px, 16, 16, 3, 4, 12, 5, light)
    rect(px, 16, 16, 3, 10, 12, 11, dark)
    vline(px, 16, 16, 3, 4, 11, dark)
    vline(px, 16, 16, 12, 4, 11, dark)
    # beveled top
    rect(px, 16, 16, 4, 5, 11, 5, light)
    setp(px, 16, 16, 4, 4, light)
    setp(px, 16, 16, 11, 4, light)
    if accent:
        rect(px, 16, 16, 6, 7, 9, 9, accent)


def paint_shard(px, base, light, dark):
    # angular splinter pointing down-right
    diamond(px, 16, 16, 7, 6, 3, base)
    setp(px, 16, 16, 9, 3, light)
    setp(px, 16, 16, 10, 4, light)
    setp(px, 16, 16, 6, 9, light)
    setp(px, 16, 16, 5, 7, light)
    setp(px, 16, 16, 8, 8, dark)
    setp(px, 16, 16, 9, 9, dark)
    setp(px, 16, 16, 7, 10, dark)
    setp(px, 16, 16, 4, 5, dark)


def paint_dust(px, base, light, dark):
    # loose pile
    diamond(px, 16, 16, 8, 9, 4, base)
    rect(px, 16, 16, 5, 10, 11, 10, base)
    setp(px, 16, 16, 6, 7, light)
    setp(px, 16, 16, 8, 6, light)
    setp(px, 16, 16, 10, 7, light)
    setp(px, 16, 16, 5, 9, dark)
    setp(px, 16, 16, 11, 9, dark)
    setp(px, 16, 16, 7, 11, dark)
    setp(px, 16, 16, 9, 11, dark)


def paint_eye(px, outer, inner, glow=None):
    circle(px, 16, 16, 8, 8, 4, outer)
    circle(px, 16, 16, 8, 8, 2, inner)
    setp(px, 16, 16, 7, 7, (255, 255, 255, 255))
    if glow:
        setp(px, 16, 16, 5, 6, glow)
        setp(px, 16, 16, 11, 6, glow)
        setp(px, 16, 16, 8, 12, glow)


def paint_core(px, base, glow, dark):
    circle(px, 16, 16, 8, 8, 4, dark)
    circle(px, 16, 16, 8, 8, 3, base)
    circle(px, 16, 16, 8, 8, 1, glow)
    setp(px, 16, 16, 7, 7, (255, 255, 255, 255))
    setp(px, 16, 16, 4, 5, glow)
    setp(px, 16, 16, 12, 5, glow)
    setp(px, 16, 16, 4, 11, glow)
    setp(px, 16, 16, 12, 11, glow)


def paint_thread(px, base, light, dark):
    # wavy strand
    for i, (x, y) in enumerate([(3, 12), (4, 11), (5, 10), (6, 9), (7, 8),
                                (8, 7), (9, 7), (10, 6), (11, 5), (12, 4), (13, 3)]):
        setp(px, 16, 16, x, y, base)
        setp(px, 16, 16, x, y - 1, light if i % 2 == 0 else dark)


def paint_tendril(px, base, dark, tip):
    # curling tendril
    for (x, y) in [(4, 12), (5, 11), (6, 11), (7, 10), (8, 9), (9, 8),
                   (10, 7), (11, 6), (11, 5), (10, 4), (9, 4), (8, 5)]:
        setp(px, 16, 16, x, y, base)
    setp(px, 16, 16, 4, 12, dark)
    setp(px, 16, 16, 8, 5, tip)


# ── block painters ────────────────────────────────────────────────────────
def paint_stone(px, seed, palette, weight=(0.14, 0.18, 0.68)):
    noise_fill(px, 16, 16, seed, palette, weight)


def paint_brick(px, base, light, dark, mortar):
    noise_fill(px, 16, 16, 7, (light, dark, base), (0.12, 0.15, 0.73))
    # mortar lines
    for y in [0, 4, 8, 12]:
        hline(px, 16, 16, 0, 15, y, mortar)
    hline(px, 16, 16, 0, 15, 15, mortar)
    vline(px, 16, 16, 7, 1, 3, mortar)
    vline(px, 16, 16, 3, 5, 7, mortar)
    vline(px, 16, 16, 11, 5, 7, mortar)
    vline(px, 16, 16, 7, 9, 11, mortar)
    vline(px, 16, 16, 3, 13, 14, mortar)
    vline(px, 16, 16, 11, 13, 14, mortar)


def paint_slate(px, seed, palette):
    noise_fill(px, 16, 16, seed, palette, (0.10, 0.12, 0.78))
    # smooth horizontal bands
    hline(px, 16, 16, 0, 15, 3, palette[0])
    hline(px, 16, 16, 0, 15, 7, palette[0])
    hline(px, 16, 16, 0, 15, 11, palette[0])


def paint_glass(px, base, highlight, dark):
    # mostly transparent with a tinted pane and streaks
    rect(px, 16, 16, 2, 1, 13, 14, base)
    rect(px, 16, 16, 2, 1, 13, 2, highlight)
    rect(px, 16, 16, 2, 13, 13, 14, dark)
    vline(px, 16, 16, 2, 1, 14, dark)
    vline(px, 16, 16, 13, 1, 14, dark)
    hline(px, 16, 16, 4, 6, 5, highlight)
    hline(px, 16, 16, 8, 11, 8, highlight)
    setp(px, 16, 16, 5, 10, highlight)


def paint_bark(px, base, light, dark):
    noise_fill(px, 16, 16, 3, (light, dark, base), (0.12, 0.15, 0.73))
    # vertical bark ridges
    for x in [1, 4, 7, 10, 13]:
        vline(px, 16, 16, x, 0, 15, dark)
        vline(px, 16, 16, x + 1, 2, 13, light)


def paint_plant_cross(px, palette_fn):
    """Paint a plant on transparent bg for cross models."""
    pass  # specialized per plant below


# ── per-texture definitions ───────────────────────────────────────────────
# palettes keyed by dominant visual identity
SLATE = ((44, 46, 62), (70, 74, 96), (22, 23, 34))        # light, dark, base
VOID_STONE = ((52, 54, 74), (24, 25, 38), (34, 36, 52))
VOID_BRICK_P = ((66, 70, 92), (30, 31, 46), (44, 47, 66))
VOID_SOIL_P = ((56, 52, 66), (24, 22, 32), (38, 35, 50))
VOID_GLASS_P = ((86, 130, 150, 90), (180, 225, 235, 200), (40, 70, 90, 120))
ASH_P = ((96, 92, 100), (48, 46, 54), (70, 67, 76))
BARK_P = ((104, 76, 110), (52, 36, 60), (74, 52, 82))
MOSS_P = ((110, 140, 130), (52, 76, 66), (78, 106, 94))
CYAN = (126, 167, 166)
CYAN_D = (82, 122, 121)


def main():
    os.makedirs(TEX, exist_ok=True)
    written = []

    def save(rel, w, h, px):
        p = os.path.join(TEX, rel.replace("/", os.sep))
        os.makedirs(os.path.dirname(p), exist_ok=True)
        write_png(p, w, h, px)
        written.append(rel)

    # ── blocks ──
    px = blank(16, 16); paint_stone(px, 11, VOID_STONE); save("block/umbral_stone.png", 16, 16, px)
    px = blank(16, 16); paint_stone(px, 21, ((54, 58, 80), (26, 28, 44), (38, 41, 60))); save("block/voidstone.png", 16, 16, px)
    px = blank(16, 16); paint_brick(px, VOID_BRICK_P[2], VOID_BRICK_P[0], VOID_BRICK_P[1], (20, 21, 32)); save("block/void_brick.png", 16, 16, px)
    px = blank(16, 16); paint_slate(px, 31, SLATE); save("block/void_slate.png", 16, 16, px)
    px = blank(16, 16); paint_stone(px, 41, VOID_SOIL_P); save("block/void_soil.png", 16, 16, px)
    # void weave - woven threads
    px = blank(16, 16)
    for y in range(0, 16, 2):
        hline(px, 16, 16, 0, 15, y, (52, 50, 70, 255))
        hline(px, 16, 16, 1, 14, y + 1, (34, 33, 48, 255))
    for x in range(0, 16, 4):
        vline(px, 16, 16, x, 0, 15, (24, 23, 36, 255))
    save("block/void_weave.png", 16, 16, px)
    # void glass - translucent pane
    px = blank(16, 16); paint_glass(px, VOID_GLASS_P[0], VOID_GLASS_P[1], VOID_GLASS_P[2]); save("block/void_glass.png", 16, 16, px)
    # ashen crust - cracked ash
    px = blank(16, 16); paint_stone(px, 51, ASH_P)
    for (x0, y0, x1, y1) in [(2, 0, 4, 5), (9, 1, 11, 7), (0, 8, 3, 12), (6, 10, 8, 15), (12, 9, 15, 13), (4, 4, 7, 6)]:
        vline(px, 16, 16, x0, y0, y1, (30, 28, 34, 255))
    save("block/ashen_crust.png", 16, 16, px)
    # elder chorus bark
    px = blank(16, 16); paint_bark(px, BARK_P[2], BARK_P[0], BARK_P[1]); save("block/elder_chorus_bark.png", 16, 16, px)
    # lumen moss
    px = blank(16, 16); paint_stone(px, 61, MOSS_P); save("block/lumen_moss.png", 16, 16, px)
    # umbral grass - dark grass with blade tops
    px = blank(16, 16); paint_stone(px, 71, ((48, 50, 70), (20, 21, 32), (32, 34, 50)))
    for x in [1, 3, 6, 8, 11, 13, 15]:
        if (x * 7) % 3:
            setp(px, 16, 16, x, 0, (70, 84, 96, 255))
            setp(px, 16, 16, x + 1 if x % 2 else x - 1, 1, (58, 70, 82, 255))
    save("block/umbral_grass.png", 16, 16, px)
    # crystal cluster (cross plant) - transparent bg, cyan crystals
    px = blank(16, 16)
    tri(px, 16, 16, 5, 2, 6, 8, CYAN)
    tri(px, 16, 16, 11, 3, 5, 7, CYAN_D)
    tri(px, 16, 16, 8, 1, 4, 6, (170, 205, 204, 255))
    setp(px, 16, 16, 8, 1, (220, 240, 240, 255))
    setp(px, 16, 16, 5, 2, (220, 240, 240, 255))
    save("block/crystal_cluster.png", 16, 16, px)
    # dust reed - pale dry stalk on transparent
    px = blank(16, 16)
    vline(px, 16, 16, 8, 3, 14, (168, 158, 132, 255))
    vline(px, 16, 16, 7, 4, 14, (132, 122, 98, 255))
    setp(px, 16, 16, 8, 2, (196, 186, 160, 255))
    setp(px, 16, 16, 6, 6, (150, 140, 114, 255))
    setp(px, 16, 16, 10, 8, (150, 140, 114, 255))
    setp(px, 16, 16, 5, 11, (150, 140, 114, 255))
    save("block/dust_reed.png", 16, 16, px)
    # void fern - dark fronds
    px = blank(16, 16)
    vline(px, 16, 16, 8, 4, 14, (36, 40, 54, 255))
    for (x0, y0, x1, y1) in [(8, 5, 4, 4), (8, 6, 12, 5), (8, 8, 3, 8), (8, 9, 13, 9), (8, 11, 5, 12), (8, 12, 11, 13)]:
        vline(px, 16, 16, min(x0, x1), min(y0, y1), max(y0, y1), (54, 60, 78, 255))
    save("block/void_fern.png", 16, 16, px)
    # void grass - small blades
    px = blank(16, 16)
    for (bx, hgt) in [(3, 6), (7, 8), (11, 5), (13, 7), (5, 4)]:
        for i in range(hgt):
            setp(px, 16, 16, bx + (i // 3), 14 - i, (40, 44, 58, 255) if i % 3 else (58, 64, 82, 255))
    save("block/void_grass.png", 16, 16, px)
    # void reed - tall hollow reed
    px = blank(16, 16)
    vline(px, 16, 16, 7, 1, 14, (44, 48, 64, 255))
    vline(px, 16, 16, 8, 1, 14, (60, 66, 86, 255))
    setp(px, 16, 16, 8, 0, (80, 88, 110, 255))
    setp(px, 16, 16, 5, 4, (52, 58, 76, 255))
    setp(px, 16, 16, 10, 7, (52, 58, 76, 255))
    setp(px, 16, 16, 6, 10, (52, 58, 76, 255))
    setp(px, 16, 16, 9, 12, (52, 58, 76, 255))
    save("block/void_reed.png", 16, 16, px)
    # lumen bloom - glowing blossom
    px = blank(16, 16)
    for dx in range(-3, 4):
        for dy in range(-3, 4):
            if abs(dx) + abs(dy) <= 3:
                setp(px, 16, 16, 8 + dx, 8 + dy, (150, 220, 210, 255))
    circle(px, 16, 16, 8, 8, 2, (200, 245, 235, 255))
    setp(px, 16, 16, 8, 7, (255, 255, 255, 255))
    setp(px, 16, 16, 5, 5, (110, 190, 180, 255))
    setp(px, 16, 16, 11, 5, (110, 190, 180, 255))
    setp(px, 16, 16, 5, 11, (110, 190, 180, 255))
    setp(px, 16, 16, 11, 11, (110, 190, 180, 255))
    save("block/lumen_bloom.png", 16, 16, px)

    # ── items ──
    # materials
    px = blank(16, 16); paint_ingot(px, (46, 49, 68), (74, 80, 106), (26, 27, 40), CYAN); save("item/void_ingot.png", 16, 16, px)
    px = blank(16, 16); paint_dust(px, (46, 49, 68), (74, 80, 106), (26, 27, 40)); save("item/void_nugget.png", 16, 16, px)
    px = blank(16, 16); paint_core(px, (46, 49, 68), CYAN, (24, 25, 36)); save("item/void_gem.png", 16, 16, px)
    px = blank(16, 16); paint_shard(px, (36, 38, 54), (66, 70, 92), (20, 21, 32)); save("item/umbral_shard.png", 16, 16, px)
    px = blank(16, 16); paint_core(px, (60, 64, 86), CYAN, (28, 30, 44)); save("item/void_core.png", 16, 16, px)
    px = blank(16, 16); paint_core(px, (150, 100, 120), (255, 190, 220), (80, 46, 62)); save("item/dragon_heart.png", 16, 16, px)
    px = blank(16, 16); paint_ingot(px, (156, 148, 138), (200, 194, 184), (110, 104, 96)); save("item/dragonbone.png", 16, 16, px)
    px = blank(16, 16); paint_shard(px, (140, 180, 200), (200, 230, 240), (90, 130, 150)); save("item/echo_shard.png", 16, 16, px)
    px = blank(16, 16); paint_shard(px, (36, 36, 44), (70, 70, 84), (18, 18, 24)); save("item/null_fragment.png", 16, 16, px)
    px = blank(16, 16); paint_core(px, (120, 90, 160), (220, 180, 255), (60, 44, 84)); save("item/resonance_core.png", 16, 16, px)
    px = blank(16, 16); paint_core(px, (110, 170, 200), (220, 245, 255), (60, 100, 130)); save("item/crystal_core.png", 16, 16, px)
    px = blank(16, 16); paint_core(px, (120, 70, 40), (255, 180, 90), (70, 38, 20)); save("item/ash_core.png", 16, 16, px)
    px = blank(16, 16); paint_core(px, (70, 76, 96), CYAN, (36, 38, 54)); save("item/void_pearl.png", 16, 16, px)
    px = blank(16, 16); paint_eye(px, (110, 90, 150), (60, 44, 90), (220, 180, 255)); save("item/chorus_eye.png", 16, 16, px)
    px = blank(16, 16); paint_eye(px, (50, 100, 110), (24, 60, 70), (140, 220, 230)); save("item/crawler_eye.png", 16, 16, px)
    px = blank(16, 16); paint_thread(px, (120, 90, 150), (170, 140, 200), (70, 50, 90)); save("item/abyssal_thread.png", 16, 16, px)
    px = blank(16, 16); paint_tendril(px, (110, 90, 130), (60, 48, 78), (170, 140, 200)); save("item/stalker_tendril.png", 16, 16, px)
    px = blank(16, 16); paint_tendril(px, (46, 96, 100), (24, 56, 60), (130, 210, 215)); save("item/marsh_tendril.png", 16, 16, px)
    px = blank(16, 16); paint_thread(px, (60, 70, 90), (110, 130, 160), (30, 36, 50)); save("item/void_membrane.png", 16, 16, px)
    px = blank(16, 16); paint_dust(px, (60, 110, 100), (120, 190, 170), (32, 70, 62)); save("item/void_sap.png", 16, 16, px)
    px = blank(16, 16); paint_dust(px, (150, 150, 160), (210, 210, 220), (96, 96, 108)); save("item/wraith_ash.png", 16, 16, px)
    px = blank(16, 16); paint_dust(px, (140, 200, 190), (210, 245, 235), (84, 130, 120)); save("item/lumen_dust.png", 16, 16, px)
    px = blank(16, 16); paint_dust(px, (140, 120, 90), (200, 180, 140), (90, 74, 54)); save("item/dust_chitin.png", 16, 16, px)
    px = blank(16, 16); paint_shard(px, (150, 150, 160), (210, 210, 220), (96, 96, 108)); save("item/crystal_fang.png", 16, 16, px)
    px = blank(16, 16); paint_shard(px, (160, 170, 190), (220, 230, 245), (100, 110, 130)); save("item/burrower_plate.png", 16, 16, px)
    px = blank(16, 16); paint_shard(px, (150, 130, 110), (210, 190, 160), (96, 80, 62)); save("item/dragon_fang.png", 16, 16, px)
    px = blank(16, 16); paint_shard(px, (110, 90, 150), (170, 150, 210), (60, 48, 90)); save("item/archive_fragment.png", 16, 16, px)
    px = blank(16, 16); paint_ingot(px, (120, 110, 150), (170, 160, 200), (70, 62, 96)); save("item/resonant_dragon_scale.png", 16, 16, px)
    px = blank(16, 16); paint_thread(px, (60, 130, 150), (120, 210, 220), (32, 80, 100)); save("item/ender_essence.png", 16, 16, px)
    px = blank(16, 16); paint_dust(px, (200, 190, 160), (240, 235, 210), (140, 130, 100)); save("item/wastes_seed_pod.png", 16, 16, px)
    # ember charm - glowing charm
    px = blank(16, 16); paint_core(px, (200, 110, 50), (255, 200, 110), (120, 60, 24)); save("item/ember_charm.png", 16, 16, px)
    # lumen wing - translucent wing
    px = blank(16, 16)
    for dy in range(8):
        for dx in range(9 - dy):
            setp(px, 16, 16, 4 + dx, 4 + dy, (150, 220, 210, 180))
    vline(px, 16, 16, 4, 4, 11, (90, 160, 150, 255))
    setp(px, 16, 16, 12, 4, (220, 250, 245, 255))
    save("item/lumen_wing.png", 16, 16, px)
    # archive key - ornate key
    px = blank(16, 16)
    circle(px, 16, 16, 5, 5, 2, (170, 150, 110, 255))
    setp(px, 16, 16, 5, 5, (230, 215, 170, 255))
    vline(px, 16, 16, 7, 6, 12, (150, 130, 92, 255))
    hline(px, 16, 16, 7, 9, 10, (150, 130, 92, 255))
    setp(px, 16, 16, 10, 11, (150, 130, 92, 255))
    setp(px, 16, 16, 7, 6, (200, 180, 140, 255))
    save("item/archive_key.png", 16, 16, px)
    # chorus pruner - shears-like tool
    px = blank(16, 16)
    vline(px, 16, 16, 5, 3, 11, (120, 130, 150, 255))
    vline(px, 16, 16, 7, 3, 11, (160, 170, 190, 255))
    hline(px, 16, 16, 4, 8, 3, (90, 100, 120, 255))
    setp(px, 16, 16, 6, 8, (80, 90, 110, 255))
    setp(px, 16, 16, 6, 12, (140, 70, 90, 255))
    save("item/chorus_pruner.png", 16, 16, px)
    # void compass
    px = blank(16, 16)
    circle(px, 16, 16, 8, 8, 4, (44, 47, 66, 255))
    outline(px, 16, 16, 4, 4, 12, 12, (70, 74, 96, 255))
    circle(px, 16, 16, 8, 8, 2, (24, 25, 38, 255))
    setp(px, 16, 16, 8, 5, CYAN)
    vline(px, 16, 16, 8, 5, 11, (160, 190, 190, 255))
    setp(px, 16, 16, 8, 6, (230, 245, 245, 255))
    save("item/void_compass.png", 16, 16, px)
    # void anchor
    px = blank(16, 16)
    circle(px, 16, 16, 8, 4, 2, (60, 64, 86, 255))
    vline(px, 16, 16, 8, 6, 12, (46, 49, 68, 255))
    vline(px, 16, 16, 9, 6, 12, (74, 80, 106, 255))
    hline(px, 16, 16, 5, 11, 7, (46, 49, 68, 255))
    hline(px, 16, 16, 5, 11, 12, (60, 64, 86, 255))
    setp(px, 16, 16, 6, 12, (74, 80, 106, 255))
    setp(px, 16, 16, 10, 12, (74, 80, 106, 255))
    setp(px, 16, 16, 8, 6, CYAN)
    save("item/void_anchor.png", 16, 16, px)
    # void lantern
    px = blank(16, 16)
    rect(px, 16, 16, 5, 2, 10, 3, (70, 74, 96, 255))
    rect(px, 16, 16, 6, 4, 9, 11, (90, 130, 150, 255))
    rect(px, 16, 16, 6, 4, 9, 5, (180, 225, 235, 255))
    rect(px, 16, 16, 5, 12, 10, 13, (46, 49, 68, 255))
    setp(px, 16, 16, 7, 8, (220, 250, 250, 255))
    setp(px, 16, 16, 6, 9, (140, 200, 210, 255))
    save("item/void_lantern.png", 16, 16, px)
    # void dash - burst emblem
    px = blank(16, 16)
    tri(px, 16, 16, 8, 3, 6, 8, CYAN)
    vline(px, 16, 16, 8, 4, 11, (46, 49, 68, 255))
    setp(px, 16, 16, 4, 5, CYAN_D)
    setp(px, 16, 16, 12, 5, CYAN_D)
    setp(px, 16, 16, 6, 12, CYAN_D)
    save("item/void_dash.png", 16, 16, px)
    # void tools (slate + cyan accent)
    # sword
    px = blank(16, 16)
    vline(px, 16, 16, 8, 1, 8, (200, 210, 215, 255))
    vline(px, 16, 16, 9, 1, 8, (150, 160, 170, 255))
    setp(px, 16, 16, 8, 1, (240, 248, 250, 255))
    vline(px, 16, 16, 8, 9, 10, (70, 74, 96, 255))
    hline(px, 16, 16, 6, 10, 9, (90, 95, 120, 255))
    hline(px, 16, 16, 7, 9, 11, (60, 64, 86, 255))
    hline(px, 16, 16, 7, 9, 12, (46, 49, 68, 255))
    vline(px, 16, 16, 8, 13, 14, (70, 74, 96, 255))
    setp(px, 16, 16, 8, 13, CYAN)
    save("item/void_sword.png", 16, 16, px)
    # pickaxe
    px = blank(16, 16)
    hline(px, 16, 16, 3, 12, 4, (150, 160, 170, 255))
    hline(px, 16, 16, 4, 11, 3, (200, 210, 215, 255))
    setp(px, 16, 16, 7, 5, (240, 248, 250, 255))
    vline(px, 16, 16, 8, 5, 10, (70, 74, 96, 255))
    vline(px, 16, 16, 9, 5, 10, (90, 95, 120, 255))
    hline(px, 16, 16, 8, 9, 11, (60, 64, 86, 255))
    hline(px, 16, 16, 8, 9, 12, (46, 49, 68, 255))
    vline(px, 16, 16, 8, 13, 14, (70, 74, 96, 255))
    setp(px, 16, 16, 8, 13, CYAN)
    save("item/void_pickaxe.png", 16, 16, px)
    # axe
    px = blank(16, 16)
    rect(px, 16, 16, 4, 2, 9, 6, (150, 160, 170, 255))
    rect(px, 16, 16, 5, 3, 8, 5, (200, 210, 215, 255))
    setp(px, 16, 16, 5, 3, (240, 248, 250, 255))
    vline(px, 16, 16, 8, 7, 10, (70, 74, 96, 255))
    vline(px, 16, 16, 9, 7, 10, (90, 95, 120, 255))
    hline(px, 16, 16, 8, 9, 11, (60, 64, 86, 255))
    hline(px, 16, 16, 8, 9, 12, (46, 49, 68, 255))
    vline(px, 16, 16, 8, 13, 14, (70, 74, 96, 255))
    setp(px, 16, 16, 8, 13, CYAN)
    save("item/void_axe.png", 16, 16, px)
    # shovel
    px = blank(16, 16)
    rect(px, 16, 16, 6, 2, 10, 5, (150, 160, 170, 255))
    rect(px, 16, 16, 7, 3, 9, 4, (200, 210, 215, 255))
    setp(px, 16, 16, 7, 3, (240, 248, 250, 255))
    vline(px, 16, 16, 8, 6, 10, (70, 74, 96, 255))
    vline(px, 16, 16, 9, 6, 10, (90, 95, 120, 255))
    hline(px, 16, 16, 8, 9, 11, (60, 64, 86, 255))
    hline(px, 16, 16, 8, 9, 12, (46, 49, 68, 255))
    vline(px, 16, 16, 8, 13, 14, (70, 74, 96, 255))
    setp(px, 16, 16, 8, 13, CYAN)
    save("item/void_shovel.png", 16, 16, px)
    # hoe
    px = blank(16, 16)
    rect(px, 16, 16, 3, 4, 8, 5, (150, 160, 170, 255))
    rect(px, 16, 16, 4, 3, 9, 4, (200, 210, 215, 255))
    setp(px, 16, 16, 4, 3, (240, 248, 250, 255))
    vline(px, 16, 16, 8, 6, 10, (70, 74, 96, 255))
    vline(px, 16, 16, 9, 6, 10, (90, 95, 120, 255))
    hline(px, 16, 16, 8, 9, 11, (60, 64, 86, 255))
    hline(px, 16, 16, 8, 9, 12, (46, 49, 68, 255))
    vline(px, 16, 16, 8, 13, 14, (70, 74, 96, 255))
    setp(px, 16, 16, 8, 13, CYAN)
    save("item/void_hoe.png", 16, 16, px)
    # resonant wings (elytra item sprite)
    px = blank(16, 16)
    for dy in range(7):
        for dx in range(10 - dy):
            setp(px, 16, 16, 3 + dx, 3 + dy, (60, 66, 96, 255))
    for dy in range(5):
        for dx in range(8 - dy):
            setp(px, 16, 16, 3 + dx, 10 + dy, (44, 49, 72, 255))
    vline(px, 16, 16, 3, 3, 14, (90, 100, 130, 255))
    vline(px, 16, 16, 12, 3, 9, (90, 100, 130, 255))
    setp(px, 16, 16, 5, 5, CYAN)
    setp(px, 16, 16, 8, 7, CYAN)
    setp(px, 16, 16, 10, 9, CYAN_D)
    save("item/resonant_wings.png", 16, 16, px)

    # ── entity/resonant_wings.png (worn elytra layer, 64x32) ──
    px = blank(64, 32)
    # left wing
    for dy in range(24):
        for dx in range(26 - dy // 2):
            setp(px, 64, 32, 6 + dx, 4 + dy, (52, 57, 84, 255))
    # right wing
    for dy in range(24):
        for dx in range(26 - dy // 2):
            setp(px, 64, 32, 32 + dx, 4 + dy, (52, 57, 84, 255))
    # wing ribs
    for y in [6, 12, 18, 24]:
        hline(px, 64, 32, 6, 28, y, (74, 80, 110, 255))
        hline(px, 64, 32, 35, 57, y, (74, 80, 110, 255))
    # cyan seams
    for y in [9, 15, 21]:
        setp(px, 64, 32, 8, y, CYAN)
        setp(px, 64, 32, 30, y, CYAN_D)
        setp(px, 64, 32, 34, y, CYAN)
        setp(px, 64, 32, 56, y, CYAN_D)
    save("entity/resonant_wings.png", 64, 32, px)

    print(f"wrote {len(written)} textures")
    for w in written:
        print(" -", w)


if __name__ == "__main__":
    main()
