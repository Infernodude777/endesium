#!/usr/bin/env python3
"""Composite preview sheet of all item/block textures on a checkerboard."""
import glob
import os
import struct
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def load(path):
    with open(path, "rb") as fh:
        d = fh.read()
    w, h = struct.unpack(">II", d[16:24])
    pos = 8
    idat = b""
    while pos < len(d):
        ln = struct.unpack(">I", d[pos:pos + 4])[0]
        typ = d[pos + 4:pos + 8]
        if typ == b"IDAT":
            idat += d[pos + 8:pos + 8 + ln]
        pos += 12 + ln
    raw = zlib.decompress(idat)
    stride = w * 4 + 1
    out = bytearray()
    prev = bytearray(w * 4)
    for y in range(h):
        f_ = raw[y * stride]
        line = bytearray(raw[y * stride + 1:(y + 1) * stride])
        for x in range(w):
            for c in range(4):
                v = line[x * 4 + c]
                if f_ == 1:
                    v = (v + prev[x * 4 + c]) & 255
                elif f_ == 2:
                    v = (v + prev[x * 4 + c]) & 255
                elif f_ == 3:
                    v = (v + prev[x * 4 + c]) & 255
                line[x * 4 + c] = v
                out.append(v)
        prev = line
    return w, h, out


def save_png(path, w, h, pixels):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        raw += bytes(pixels[y * w * 4:(y + 1) * w * 4])

    def chunk(typ, data):
        c = struct.pack(">I", len(data)) + typ + data
        c += struct.pack(">I", zlib.crc32(typ + data) & 0xFFFFFFFF)
        return c

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as fh:
        fh.write(png)


def main():
    tex = os.path.join(ROOT, "src", "main", "resources", "assets", "endesium", "textures")
    files = sorted(glob.glob(os.path.join(tex, "item", "*.png")))
    files += sorted(glob.glob(os.path.join(tex, "block", "*.png")))

    cell = 48
    cols = 10
    rows = (len(files) + cols - 1) // cols
    w, h = cols * cell, rows * cell
    sheet = bytearray()
    for y in range(h):
        for x in range(w):
            if (x // cell + y // cell) % 2 == 0:
                sheet += bytes((40, 40, 48, 255))
            else:
                sheet += bytes((58, 58, 68, 255))

    for i, p in enumerate(files):
        tw, th, px = load(p)
        cx = (i % cols) * cell + (cell - tw) // 2
        cy = (i // cols) * cell + (cell - th) // 2
        for yy in range(th):
            for xx in range(tw):
                idx = (yy * tw + xx) * 4
                r, g, b, a = px[idx], px[idx + 1], px[idx + 2], px[idx + 3]
                if a == 0:
                    continue
                bx = cx + xx
                by = cy + yy
                si = (by * w + bx) * 4
                br = sheet[si]
                bg = sheet[si + 1]
                bb = sheet[si + 2]
                na = a / 255.0
                sheet[si] = int(r * na + br * (1 - na))
                sheet[si + 1] = int(g * na + bg * (1 - na))
                sheet[si + 2] = int(b * na + bb * (1 - na))
                sheet[si + 3] = 255

    out = os.path.join(ROOT, "asset_preview.png")
    save_png(out, w, h, sheet)
    print(f"sheet {w}x{h} with {len(files)} textures -> {out}")


if __name__ == "__main__":
    main()
