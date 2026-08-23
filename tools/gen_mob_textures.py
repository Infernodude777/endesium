#!/usr/bin/env python3
"""
gen_mob_textures.py — paint real 32x32 pixel-art textures for the 8 ecology mobs.

Approach:
  * Read each mob's geo.json (already verified to import in Blockbench).
  * For every cube, compute the six face rectangles using the Bedrock "box" UV
    convention: a cube at origin (x,y,z), size (w,h,d), uv (u,v) maps to a
    texture region of width 2d+2w and height d+h laid out as:

        +----+----+----+----+
        | W  | N  | E  | S  |   <- top row:  v .. v+d   (d tall)
        +----+----+----+----+
        | W  | N  | E  | S  |   <- side row: v+d .. v+d+h (h tall)
        +----+----+----+----+
        | W  | N  | E  | S  |   <- bottom row: v+d+h .. v+d+h+d (d tall)
        +----+----+----+----+

    Actually Bedrock box UV is: top face (u+d, v) size w×d; north (u+d, v+d)
    size w×h; east (u+d+w, v+d) size d×h; south (u+d+w+d, v+d) size w×h;
    west (u, v+d) size d×h; bottom (u+d, v+d+h) size w×d.
  * Paint with a per-mob art program: base color per bone, top-face lighten,
    bottom-face darken, plus seams / glows / eyes drawn as pixel stamps.
"""
import json
import os
import struct
import zlib

GEO_DIR = "src/main/resources/assets/endesium/geo/entity"
TEX_DIR = "src/main/resources/assets/endesium/textures/entity"

W = H = 32


def _chunk(tag, data):
    c = struct.pack(">I", len(data)) + tag + data
    return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)


def write_png(path, pixels):
    # pixels: list of 32 lists of 32 (r,g,b,a) tuples
    raw = bytearray()
    for y in range(H):
        raw.append(0)
        for x in range(W):
            r, g, b, a = pixels[y][x]
            raw += bytes((r, g, b, a))
    ihdr = struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0)
    png = (b"\x89PNG\r\n\x1a\n"
           + _chunk(b"IHDR", ihdr)
           + _chunk(b"IDAT", zlib.compress(bytes(raw)))
           + _chunk(b"IEND", b""))
    with open(path, "wb") as f:
        f.write(png)


# ---------------------------------------------------------------- color utils
def mix(c1, c2, t):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))


def shade(c, t):
    """t<0 darken, t>0 lighten."""
    if t >= 0:
        return mix(c, (255, 255, 255), t)
    return mix(c, (0, 0, 0), -t)


def clamp(v):
    return max(0, min(255, int(v)))


def with_alpha(c, a=255):
    return (c[0], c[1], c[2], a)


# ---------------------------------------------------------------- face rects
def face_rects(cube):
    """Return dict of face name -> (x0, y0, w, h) pixel rects in the texture.

    Bedrock box UV layout:
      top:    (u + d, v)            size w x d
      north:  (u + d, v + d)        size w x h
      east:   (u + d + w, v + d)    size d x h
      south:  (u + d + w + d, v+d)  size w x h
      west:   (u, v + d)            size d x h
      bottom: (u + d, v + d + h)    size w x d
    """
    ox, oy, oz = cube["origin"]
    sx, sy, sz = cube["size"]
    u, v = cube["uv"]
    w, h, d = sx, sy, sz
    return {
        "top":    (u + d, v, w, d),
        "north":  (u + d, v + d, w, h),
        "east":   (u + d + w, v + d, d, h),
        "south":  (u + d + w + d, v + d, w, h),
        "west":   (u, v + d, d, h),
        "bottom": (u + d, v + d + h, w, d),
    }


def paint_rect(pixels, rect, color, pattern=None, seed=0):
    """Fill rect with color; if pattern is a callable, it decides per-pixel."""
    x0, y0, w, h = rect
    import random
    rnd = random.Random(seed)
    for yy in range(h):
        for xx in range(w):
            px, py = x0 + xx, y0 + yy
            if px < 0 or py < 0 or px >= W or py >= H:
                continue
            if pattern is None:
                pixels[py][px] = with_alpha(color)
            else:
                c = pattern(xx, yy, w, h, rnd)
                if c is not None:
                    pixels[py][px] = with_alpha(c)


# ---------------------------------------------------------------- helpers
def paint_box(pixels, cube, base, shade_top=0.12, shade_bottom=-0.28,
              side_variation=0.0, seed=0):
    """Paint a whole cube using standard top-light/bottom-dark shading."""
    rects = face_rects(cube)
    import random
    rnd = random.Random(seed)
    paint_rect(pixels, rects["top"], shade(base, shade_top))
    paint_rect(pixels, rects["bottom"], shade(base, shade_bottom))
    for face in ("north", "south", "east", "west"):
        v = rnd.uniform(-side_variation, side_variation)
        paint_rect(pixels, rects[face], shade(base, v))


# ---------------------------------------------------------------- art program
# Each mob: dict of bone_name -> paint spec.
#   "base": base color
#   "faces": optional per-face override or pattern
#   "seams": list of (face, y_frac) horizontal seam lines
#   "eyes": list of (face, x_frac, y_frac, color)
#   "glow": list of (face, x_frac, y_frac, color, radius)
def paint_mob(geo, art):
    pixels = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]
    bones = {b["name"]: b for b in geo["minecraft:geometry"][0]["bones"]}
    for bone_name, spec in art.items():
        b = bones.get(bone_name)
        if not b:
            continue
        base = spec["base"]
        seed = spec.get("seed", 0)
        for cube in b.get("cubes", []):
            paint_box(pixels, cube, base,
                      shade_top=spec.get("shade_top", 0.12),
                      shade_bottom=spec.get("shade_bottom", -0.28),
                      side_variation=spec.get("side_variation", 0.03),
                      seed=seed)
            rects = face_rects(cube)
            # seams (horizontal lines across a face)
            for seam in spec.get("seams", []):
                face, yf, color = seam
                if face in rects:
                    x0, y0, w, h = rects[face]
                    py = y0 + int(h * yf)
                    for xx in range(w):
                        if 0 <= py < H and 0 <= x0 + xx < W:
                            pixels[py][x0 + xx] = with_alpha(color)
            # eyes / glow stamps
            for eye in spec.get("eyes", []):
                face, xf, yf, color = eye
                if face in rects:
                    x0, y0, w, h = rects[face]
                    px = x0 + int(w * xf)
                    py = y0 + int(h * yf)
                    for dy in (-1, 0, 1):
                        for dx in (-1, 0, 1):
                            if 0 <= py + dy < H and 0 <= px + dx < W:
                                pixels[py + dy][px + dx] = with_alpha(color)
            for glow in spec.get("glow", []):
                face, xf, yf, color, rad = glow
                if face in rects:
                    x0, y0, w, h = rects[face]
                    px = x0 + int(w * xf)
                    py = y0 + int(h * yf)
                    for dy in range(-rad, rad + 1):
                        for dx in range(-rad, rad + 1):
                            if dx * dx + dy * dy > rad * rad:
                                continue
                            if 0 <= py + dy < H and 0 <= px + dx < W:
                                pixels[py + dy][px + dx] = with_alpha(color)
    return pixels


# ---------------------------------------------------------------- per-mob art
# Colors chosen per design doc: each mob keeps its own restrained palette.

ASH = {
    "body": {"base": (92, 88, 96), "seed": 1, "side_variation": 0.05,
             "seams": [("north", 0.35, (70, 66, 74)), ("north", 0.65, (70, 66, 74))]},
    "lower_shard": {"base": (60, 56, 64), "seed": 2, "side_variation": 0.06},
    "head": {"base": (108, 102, 112), "seed": 3, "side_variation": 0.04,
             "eyes": [("north", 0.25, 0.5, (255, 214, 160)),
                      ("north", 0.75, 0.5, (255, 214, 160))]},
    "eye_left": {"base": (255, 214, 160), "seed": 4},
    "eye_right": {"base": (255, 214, 160), "seed": 5},
    "tail": {"base": (70, 66, 74), "seed": 6, "side_variation": 0.08},
}

CHORUS = {
    "body": {"base": (150, 118, 168), "seed": 1, "side_variation": 0.06,
             "seams": [("north", 0.25, (120, 92, 138)), ("north", 0.5, (120, 92, 138)),
                       ("north", 0.75, (120, 92, 138))]},
    "branch_left": {"base": (130, 100, 150), "seed": 2, "side_variation": 0.05},
    "branch_right": {"base": (130, 100, 150), "seed": 3, "side_variation": 0.05},
    "head": {"base": (168, 134, 186), "seed": 4, "side_variation": 0.04,
             "eyes": [("north", 0.25, 0.5, (255, 240, 200)),
                      ("north", 0.75, 0.5, (255, 240, 200))]},
    "eye_left": {"base": (255, 240, 200), "seed": 5},
    "eye_right": {"base": (255, 240, 200), "seed": 6},
    "left_arm": {"base": (120, 92, 140), "seed": 7, "side_variation": 0.06},
    "right_arm": {"base": (120, 92, 140), "seed": 8, "side_variation": 0.06},
    "left_leg": {"base": (96, 74, 112), "seed": 9, "side_variation": 0.06},
    "right_leg": {"base": (96, 74, 112), "seed": 10, "side_variation": 0.06},
}

CRYSTAL = {
    "body": {"base": (104, 116, 124), "seed": 1, "side_variation": 0.05,
             "seams": [("north", 0.3, (70, 82, 92)), ("north", 0.6, (70, 82, 92))],
             "glow": [("north", 0.5, 0.5, (120, 220, 230), 1)]},
    "crystal_a": {"base": (150, 120, 200), "seed": 2, "side_variation": 0.08,
                  "glow": [("north", 0.5, 0.5, (200, 180, 255), 1)]},
    "crystal_b": {"base": (120, 200, 210), "seed": 3, "side_variation": 0.08,
                  "glow": [("north", 0.5, 0.5, (180, 240, 250), 1)]},
    "head": {"base": (120, 132, 140), "seed": 4, "side_variation": 0.04,
             "eyes": [("north", 0.25, 0.5, (255, 220, 150)),
                      ("north", 0.75, 0.5, (255, 220, 150))]},
    "eye_left": {"base": (255, 220, 150), "seed": 5},
    "eye_right": {"base": (255, 220, 150), "seed": 6},
    "arm_left": {"base": (84, 96, 108), "seed": 7, "side_variation": 0.06},
    "arm_right": {"base": (84, 96, 108), "seed": 8, "side_variation": 0.06},
    "leg_left": {"base": (76, 88, 100), "seed": 9, "side_variation": 0.06},
    "leg_right": {"base": (76, 88, 100), "seed": 10, "side_variation": 0.06},
}

DUST = {
    "body": {"base": (214, 204, 184), "seed": 1, "side_variation": 0.04,
             "seams": [("north", 0.33, (184, 172, 150)), ("north", 0.66, (184, 172, 150))]},
    "head": {"base": (196, 186, 166), "seed": 2, "side_variation": 0.04,
             "eyes": [("north", 0.3, 0.5, (40, 60, 70)),
                      ("north", 0.7, 0.5, (40, 60, 70))]},
    "eye_left": {"base": (40, 60, 70), "seed": 3},
    "eye_right": {"base": (40, 60, 70), "seed": 4},
    "leg_fl": {"base": (150, 142, 126), "seed": 5, "shade_top": 0.05, "shade_bottom": -0.2},
    "leg_fr": {"base": (150, 142, 126), "seed": 6, "shade_top": 0.05, "shade_bottom": -0.2},
    "leg_ml": {"base": (150, 142, 126), "seed": 7, "shade_top": 0.05, "shade_bottom": -0.2},
    "leg_mr": {"base": (150, 142, 126), "seed": 8, "shade_top": 0.05, "shade_bottom": -0.2},
    "leg_bl": {"base": (150, 142, 126), "seed": 9, "shade_top": 0.05, "shade_bottom": -0.2},
    "leg_br": {"base": (150, 142, 126), "seed": 10, "shade_top": 0.05, "shade_bottom": -0.2},
}

LUMEN = {
    "body": {"base": (176, 196, 190), "seed": 1, "side_variation": 0.04,
             "glow": [("north", 0.5, 0.5, (200, 240, 235), 1)]},
    "head": {"base": (196, 210, 205), "seed": 2, "side_variation": 0.03,
             "eyes": [("north", 0.3, 0.5, (120, 220, 220)),
                      ("north", 0.7, 0.5, (120, 220, 220))]},
    "wing_left": {"base": (150, 190, 200), "seed": 3, "side_variation": 0.05,
                  "shade_top": 0.2, "shade_bottom": -0.1},
    "wing_right": {"base": (150, 190, 200), "seed": 4, "side_variation": 0.05,
                   "shade_top": 0.2, "shade_bottom": -0.1},
    "antenna_left": {"base": (120, 220, 220), "seed": 5},
    "antenna_right": {"base": (120, 220, 220), "seed": 6},
}

MARSH = {
    "body": {"base": (72, 96, 92), "seed": 1, "side_variation": 0.05,
             "seams": [("north", 0.4, (50, 72, 68)), ("north", 0.7, (50, 72, 68))]},
    "head": {"base": (80, 104, 100), "seed": 2, "side_variation": 0.04,
             "eyes": [("north", 0.25, 0.5, (180, 240, 230)),
                      ("north", 0.75, 0.5, (180, 240, 230))]},
    "eye_a": {"base": (180, 240, 230), "seed": 3},
    "eye_b": {"base": (180, 240, 230), "seed": 4},
    "tendril_left": {"base": (110, 150, 140), "seed": 5},
    "tendril_right": {"base": (110, 150, 140), "seed": 6},
    "leg_fl": {"base": (52, 72, 68), "seed": 7, "shade_top": 0.05, "shade_bottom": -0.25},
    "leg_fr": {"base": (52, 72, 68), "seed": 8, "shade_top": 0.05, "shade_bottom": -0.25},
    "leg_bl": {"base": (52, 72, 68), "seed": 9, "shade_top": 0.05, "shade_bottom": -0.25},
    "leg_br": {"base": (52, 72, 68), "seed": 10, "shade_top": 0.05, "shade_bottom": -0.25},
}

NULL = {
    "body": {"base": (20, 20, 26), "seed": 1, "side_variation": 0.06,
             "seams": [("north", 0.3, (34, 34, 44)), ("north", 0.6, (34, 34, 44))]},
    "head": {"base": (26, 26, 34), "seed": 2, "side_variation": 0.05,
             "eyes": [("north", 0.25, 0.5, (190, 200, 230)),
                      ("north", 0.75, 0.5, (190, 200, 230))]},
    "eye_left": {"base": (190, 200, 230), "seed": 3},
    "eye_right": {"base": (190, 200, 230), "seed": 4},
    "arm_ul": {"base": (16, 16, 22), "seed": 5, "side_variation": 0.08},
    "arm_ll": {"base": (16, 16, 22), "seed": 6, "side_variation": 0.08},
    "arm_ur": {"base": (16, 16, 22), "seed": 7, "side_variation": 0.08},
    "arm_lr": {"base": (16, 16, 22), "seed": 8, "side_variation": 0.08},
    "leg_left": {"base": (14, 14, 20), "seed": 9, "side_variation": 0.08},
    "leg_right": {"base": (14, 14, 20), "seed": 10, "side_variation": 0.08},
}

RAY = {
    "body": {"base": (70, 74, 84), "seed": 1, "side_variation": 0.05,
             "glow": [("north", 0.5, 0.5, (120, 200, 220), 1)]},
    "head": {"base": (60, 64, 74), "seed": 2, "side_variation": 0.04,
             "eyes": [("north", 0.3, 0.5, (140, 220, 235)),
                      ("north", 0.7, 0.5, (140, 220, 235))]},
    "wing_left": {"base": (56, 60, 70), "seed": 3, "side_variation": 0.06,
                  "shade_top": 0.15, "shade_bottom": -0.2},
    "wing_right": {"base": (56, 60, 70), "seed": 4, "side_variation": 0.06,
                   "shade_top": 0.15, "shade_bottom": -0.2},
    "tail": {"base": (48, 52, 62), "seed": 5, "side_variation": 0.08,
             "glow": [("north", 0.5, 0.2, (120, 200, 220), 1)]},
}

ARTS = {
    "ash_wraith": ASH,
    "chorus_stalker": CHORUS,
    "crystal_burrower": CRYSTAL,
    "dust_crawler": DUST,
    "lumen_moth": LUMEN,
    "marsh_crawler": MARSH,
    "nullwalker": NULL,
    "void_ray": RAY,
}


def main():
    for mob_id, art in ARTS.items():
        geo = json.load(open(os.path.join(GEO_DIR, f"{mob_id}.geo.json")))
        pixels = paint_mob(geo, art)
        write_png(os.path.join(TEX_DIR, f"{mob_id}.png"), pixels)
        n = len({px for row in pixels for px in row})
        print(f"wrote {mob_id}.png ({n} distinct colors)")


if __name__ == "__main__":
    main()
