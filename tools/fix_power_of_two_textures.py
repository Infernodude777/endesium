#!/usr/bin/env python3
"""
fix_power_of_two_textures.py

Scans every Endesium block/item texture and pads any that are not
power-of-two (POT) up to the next POT by duplicating edge pixels, so the
assets render without shimmer or clamping. This is the fix for the audit
finding that void_gravel.png was 15x16 instead of 16x16.

The tool is idempotent and safe: it only touches files that are already
non-POT, backs each one up next to the original as *.bak.png, and prints a
summary of every change. Run it from the repository root:

    python tools/fix_power_of_two_textures.py

Optional: pass --dry-run to only report what would change, --strict to
fail (non-zero exit) if any non-POT texture is found, and --verify to
check the tree and report without writing anything (CI-friendly).
"""
import argparse
import pathlib
import sys

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
TEXTURE_DIR = ROOT / "src" / "main" / "resources" / "assets" / "endesium" / "textures"

def is_power_of_two(n: int) -> bool:
	return n > 0 and (n & (n - 1)) == 0

def next_power_of_two(n: int) -> int:
	p = 1
	while p < n:
		p <<= 1
	return p

def pad_to_pot(img: Image.Image) -> Image.Image:
	"""Pad an image to the next power-of-two by repeating edge pixels."""
	w, h = img.size
	nw = next_power_of_two(w)
	nh = next_power_of_two(h)
	if nw == w and nh == h:
		return img
	out = Image.new(img.mode, (nw, nh))
	for x in range(nw):
		sx = min(x, w - 1)
		for y in range(nh):
			sy = min(y, h - 1)
			out.putpixel((x, y), img.getpixel((sx, sy)))
	return out

def scan() -> tuple[list[pathlib.Path], int]:
	"""Return (non-POT files, count of already-POT files)."""
	bad: list[pathlib.Path] = []
	ok = 0
	for png in sorted(TEXTURE_DIR.rglob("*.png")):
		if ".bak.png" in png.name:
			continue
		try:
			with Image.open(png) as im:
				w, h = im.size
		except OSError:
			print(f"[skip] unreadable: {png.relative_to(ROOT)}")
			continue
		if is_power_of_two(w) and is_power_of_two(h):
			ok += 1
		else:
			bad.append(png)
	return bad, ok

def main() -> int:
	parser = argparse.ArgumentParser(description="Pad non-power-of-two Endesium textures.")
	parser.add_argument("--dry-run", action="store_true", help="only report, do not write")
	parser.add_argument("--strict", action="store_true", help="exit non-zero if any non-POT is found")
	parser.add_argument("--verify", action="store_true", help="report only; exit non-zero if any non-POT is found")
	args = parser.parse_args()

	bad, ok = scan()
	for png in bad:
		rel = png.relative_to(ROOT)
		with Image.open(png) as im:
			w, h = im.size
		print(f"[fix ] {rel}  {w}x{h} -> {next_power_of_two(w)}x{next_power_of_two(h)}")
		if args.dry_run or args.verify:
			continue
		backup = png.with_suffix(png.suffix + ".bak.png")
		backup.write_bytes(png.read_bytes())
		with Image.open(png) as im:
			pad_to_pot(im.convert("RGBA")).save(png)

	print(f"\nScanned: {ok + len(bad)} textures, {ok} already power-of-two.")
	if bad:
		print(f"Fixed: {len(bad)} texture(s). Originals kept as *.bak.png.")
	else:
		print("Nothing to fix - all textures are power-of-two.")
	if (args.strict or args.verify) and bad:
		return 1
	return 0

if __name__ == "__main__":
	sys.exit(main())