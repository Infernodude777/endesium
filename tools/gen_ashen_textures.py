#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate the 5 missing Ashen Expanse textures (16x16 pixel art).

Direct texture generation is explicitly exempt from the "Jimbibo-only edits"
rule, so these PNGs are written here. Each texture is hand-designed pixel art
with the Endesium style: crisp edges, dark mineral base, restrained warm
accents (ember orange / magma red), no anti-aliasing.
"""
import os
import struct
import zlib

ITEM_DIR = "src/main/resources/assets/endesium/textures/item"
BLOCK_DIR = "src/main/resources/assets/endesium/textures/block"
os.makedirs(ITEM_DIR, exist_ok=True)
os.makedirs(BLOCK_DIR, exist_ok=True)


def _chunk(tag, data):
    c = struct.pack(">I", len(data)) + tag + data
    return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)


def write_png(path, grid):
    h = len(grid)
    w = len(grid[0])
    raw = bytearray()
    for row in grid:
        raw.append(0)
        for px in row:
            raw += bytes(px)
    sig = b"\x89PNG\r\n\x1a\n"
    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)
    out = sig + _chunk(b"IHDR", ihdr) + _chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + _chunk(b"IEND", b"")
    with open(path, "wb") as fh:
        fh.write(out)
    print(f"wrote {path} ({w}x{h})")


def P(r, g, b, a=255):
    return (r, g, b, a)


# ── ashen_ember: a small smouldering ember with a dark shell and warm core ──
# 16x16, mostly transparent, ember sits center.
EMBER = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "......EEEE......",
    ".....EORROE.....",
    ".....ERYYRE.....",
    ".....ERYYRE.....",
    ".....EORROE.....",
    "......EEEE......",
    "................",
    "................",
    "................",
    "................",
    "................",
]
EMBER_MAP = {
    "E": P(60, 34, 26),      # dark shell
    "O": P(214, 92, 34),     # ember orange
    "R": P(238, 128, 44),    # hot orange
    "Y": P(252, 196, 92),    # core yellow
    ".": P(0, 0, 0, 0),
}
write_png(os.path.join(ITEM_DIR, "ashen_ember.png"),
          [[EMBER_MAP[c] for c in row] for row in EMBER])

# ── magma_core: a dense molten core with red cracks ──
MAGMA = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "......MMMM......",
    ".....MDRRDM.....",
    ".....MRYYRM.....",
    ".....MRYYRM.....",
    ".....MDRRDM.....",
    "......MMMM......",
    "................",
    "................",
    "................",
    "................",
    "................",
]
MAGMA_MAP = {
    "M": P(46, 26, 30),      # dark magma shell
    "D": P(150, 42, 38),     # deep red
    "R": P(226, 74, 40),     # molten red
    "Y": P(250, 178, 74),    # hot yellow
    ".": P(0, 0, 0, 0),
}
write_png(os.path.join(ITEM_DIR, "magma_core.png"),
          [[MAGMA_MAP[c] for c in row] for row in MAGMA])

# ── ashwalker_boots: a dark boot with ember-orange trim ──
BOOTS = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "......BB........",
    "......BB........",
    "......BB........",
    "......BB........",
    "......BB........",
    "......BB........",
    "......BB........",
    ".......BB.......",
    ".......BB.......",
    ".......BB.......",
    ".......BB.......",
]
BOOTS_MAP = {
    "B": P(48, 40, 40),      # dark ash leather
    "O": P(214, 92, 34),     # ember trim
    ".": P(0, 0, 0, 0),
}
# Add a horizontal ember band across the boot.
BOOTS_ART = []
for i, row in enumerate(BOOTS):
    if 7 <= i <= 9:
        row = row.replace("B", "O", 1)[0] + "O" + row[2:]
    BOOTS_ART.append(row)
write_png(os.path.join(ITEM_DIR, "ashwalker_boots.png"),
          [[BOOTS_MAP[c] for c in row] for row in BOOTS_ART])

# ── ember_charm: a small round talisman with a glowing ember center ──
CHARM = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "......CCCC......",
    ".....C....C.....",
    ".....C.EE.C.....",
    ".....C.EE.C.....",
    ".....C....C.....",
    "......CCCC......",
    "................",
    "................",
    "................",
    "................",
    "................",
]
CHARM_MAP = {
    "C": P(70, 56, 52),      # dark talisman ring
    "E": P(240, 150, 60),    # glowing ember
    ".": P(0, 0, 0, 0),
}
write_png(os.path.join(ITEM_DIR, "ember_charm.png"),
          [[CHARM_MAP[c] for c in row] for row in CHARM])

# ── ashen_crust: a dark, cracked crust block with faint ember seams ──
CRUST = [
    "DDDDDDDDDDDDDDDD",
    "DDDCDDDDDDDCDDDD",
    "DDDDDDDDDDDDDDDD",
    "DDCDDDDDDDDDDCDD",
    "DDDDDDDDDDDDDDDD",
    "DDDDCDDDDDDDDDDD",
    "DDDDDDDDDDDDDCDD",
    "DCDDDDDDDDDDDDDD",
    "DDDDDDDDDCDDDDDD",
    "DDDDDDDDDDDDDDDD",
    "DDCDDDDDDDDDDDDD",
    "DDDDDDDDDDDDDCDD",
    "DDDDDCDDDDDDDDDD",
    "DDDDDDDDDDDDDDDD",
    "DDCDDDDDDDDDDDDD",
    "DDDDDDDDDDDDDDDD",
]
CRUST_MAP = {
    "D": P(52, 44, 42),      # dark ash crust
    "C": P(120, 56, 44),     # faint ember seam
}
write_png(os.path.join(BLOCK_DIR, "ashen_crust.png"),
          [[CRUST_MAP[c] for c in row] for row in CRUST])

print("All 5 ashen textures generated.")
