#!/usr/bin/env python3
"""Generate the 16x16 Endesium Guidebook item texture as a PNG.

Textures are binary, so they are produced by this small script instead of
being edited by hand. It writes an RGBA PNG with no external dependencies.
"""
import struct
import zlib

# Palette
DARK_SLATE = (20, 22, 28, 255)
DARKER_SLATE = (14, 16, 21, 255)
GOLD = (201, 162, 39, 255)
GOLD_LIGHT = (232, 201, 106, 255)
CYAN = (126, 167, 166, 255)
TRANSPARENT = (0, 0, 0, 0)

SIZE = 16


def make_pixels():
    pixels = [[TRANSPARENT for _ in range(SIZE)] for _ in range(SIZE)]
    # Book cover body (dark slate)
    for y in range(2, 14):
        for x in range(2, 14):
            pixels[y][x] = DARK_SLATE
    # Bottom shading for depth
    for y in range(11, 14):
        for x in range(2, 14):
            pixels[y][x] = DARKER_SLATE
    # Gold border on all four edges
    for x in range(2, 14):
        pixels[2][x] = GOLD
        pixels[13][x] = GOLD
    for y in range(2, 14):
        pixels[y][2] = GOLD
        pixels[y][13] = GOLD
    # Cyan diamond emblem centered at (7, 7)
    for dy in range(-2, 3):
        half = 2 - abs(dy)
        for dx in range(-half, half + 1):
            pixels[7 + dy][7 + dx] = CYAN
    # Gold highlight at the heart of the emblem
    pixels[7][7] = GOLD_LIGHT
    return pixels


def write_png(path, pixels):
    raw = bytearray()
    for row in pixels:
        raw.append(0)  # filter type 0 (None)
        for r, g, b, a in row:
            raw.extend((r, g, b, a))

    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        c += struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
        return c

    ihdr = struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", ihdr)
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


def main():
    out = "src/main/resources/assets/endesium/textures/item/endesium_guidebook.png"
    write_png(out, make_pixels())
    print("Wrote", out)


if __name__ == "__main__":
    main()
