#!/usr/bin/env python3
"""
verify_guidebook.py

Read-only verification of the guidebook fix: the item model must use an
integer GUI scale with no rotation, the screen must draw text with shadows,
and the content must include the new field-note and reference pages.

    python tools/verify_guidebook.py

Exits non-zero if any check fails.
"""
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
MODEL = ROOT / "src/main/resources/assets/endesium/models/item/endesium_gui_book.json"
SCREEN = ROOT / "src/client/java/com/infernodude777/endesium/client/screen/EndesiumGuidebookScreen.java"
CONTENT = ROOT / "src/client/java/com/infernodude777/endesium/client/screen/EndesiumGuidebookContent.java"

def read_text(path: pathlib.Path) -> str:
	return path.read_text(encoding="utf-8", errors="replace")

def check_model() -> tuple[bool, str]:
	data = json.loads(read_text(MODEL))
	display = data.get("display", {})
	gui = display.get("gui", {})
	fixed = display.get("fixed", {})
	gui_ok = gui.get("rotation") == [0, 0, 0] and gui.get("scale") == [1.0, 1.0, 1.0]
	fixed_ok = fixed.get("rotation") == [0, 0, 0] and fixed.get("scale") == [1.0, 1.0, 1.0]
	return (gui_ok and fixed_ok,
			f"gui={gui.get('rotation')}/{gui.get('scale')} fixed={fixed.get('rotation')}/{fixed.get('scale')}")

def check_screen() -> tuple[bool, str]:
	text = read_text(SCREEN)
	ok = "drawString(this.font, page.title()" in text and ", true)" in text
	return (ok, "screen draws title with shadow" if ok else "screen not shadowed")

def check_content() -> tuple[bool, str]:
	text = read_text(CONTENT)
	required = ["Field Notes: The Ruins", "Field Notes: The Spire",
				"Field Notes: The Archive", "Field Notes: The Monolith",
				"Field Notes: The Sanctum", "Crafting: Tools", "Crafting: Doors",
				"Crafting: Void Materials", "Crafting: The Armory",
				"The Resonance Current", "Resonance Sources",
				"After the Transformation", "The Respawned Dragon",
				"Builder's Notes: Texture", "Builder's Notes: Stability"]
	missing = [r for r in required if r not in text]
	return (not missing, "missing pages: " + (", ".join(missing) if missing else "none"))

def main() -> int:
	checks = [("model", check_model), ("screen", check_screen), ("content", check_content)]
	failures = 0
	for name, fn in checks:
		try:
			ok, detail = fn()
		except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError) as exc:
			ok, detail = False, f"raised {type(exc).__name__}: {exc}"
		print(f"[{'PASS' if ok else 'FAIL'}] guidebook {name}: {detail}")
		if not ok:
			failures += 1
	print(f"\n{len(checks) - failures}/{len(checks)} guidebook checks passed")
	return 1 if failures else 0

if __name__ == "__main__":
	sys.exit(main())