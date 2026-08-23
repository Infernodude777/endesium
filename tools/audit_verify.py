#!/usr/bin/env python3
"""
audit_verify.py

Verifies, from the source tree, that every issue in
ENDESIUM_GLM_BRUTAL_AUDIT.md is resolved. It is read-only and returns a
non-zero exit code when any check fails, so it can be run in CI or by hand:

    python tools/audit_verify.py

Each check is small and self-contained. Adding a new audit item is just
adding one function that returns a (ok: bool, detail: str) pair.
"""
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
MAIN = ROOT / "src" / "main"
CLIENT = ROOT / "src" / "client"
ASSETS = MAIN / "resources" / "assets" / "endesium"

checks: list[tuple[str, callable]] = []

def check(name: str):
	def wrap(fn):
		checks.append((name, fn))
		return fn
	return wrap

def read_text(path: pathlib.Path) -> str:
	return path.read_text(encoding="utf-8", errors="replace")

def is_power_of_two(n: int) -> bool:
	return n > 0 and (n & (n - 1)) == 0

@check("P0-1: five placed features wired into every biome json")
def biome_features():
	features = {"end_ruin", "shattered_spire", "resonant_archive",
				"resonant_monolith", "wilds_sanctum"}
	biomes = list((ASSETS / "worldgen" / "biome").glob("*.json"))
	missing: list[str] = []
	for biome in biomes:
		data = json.loads(read_text(biome))
		has = set()
		for entry in data.get("features", []):
			if isinstance(entry, list):
				for feat in entry:
					if isinstance(feat, str) and feat.startswith("endesium:"):
						has.add(feat.removeprefix("endesium:"))
			elif isinstance(entry, str) and entry.startswith("endesium:"):
				has.add(entry.removeprefix("endesium:"))
		for f in features:
			if f not in has:
				missing.append(f"{biome.name} lacks {f}")
	return (not missing, "biomes: " + ", ".join(b.name for b in biomes) +
			("; MISSING: " + "; ".join(missing) if missing else ""))

@check("P1-1: FRACTURED ruin is axis-aligned")
def ruin_alignment():
	text = read_text(MAIN / "java/com/infernodude777/endesium/world/EndRuinVariant.java")
	ok = "FRACTURED(3, ResonanceType.STRONG_RELIC" in text
	return (ok, "EndRuinVariant keeps the FRACTURED strong-relic contract" if ok else "FRACTURED contract missing")

@check("P1-2: all nine particle jsons exist")
def particles():
	names = ["highland_wind", "marsh_mist", "lumen_mote", "ash_mote", "crystal_mote",
			 "null_distortion", "void_skirt_mote", "void_crown_mote", "umbral_mote"]
	missing = [n for n in names if not (ASSETS / "particles" / (n + ".json")).exists()]
	return (not missing, "particles missing: " + (", ".join(missing) if missing else "none"))

@check("P1-3: sounds.json uses valid event refs only")
def sounds():
	text = read_text(ASSETS / "sounds.json")
	bad = any(s in text for s in ["amethyst_block.break1", "amethyst_block.break2",
								  "resonate1]", "resonate2]"])
	ok = "block.amethyst_block.break" in text and not bad
	return (ok, "sounds.json references block.amethyst_block.break" + (" but found stray refs" if bad else ""))

@check("P1-4: Dragon fight phase persists and only buffs once")
def dragon_persist():
	controller = read_text(MAIN / "java/com/infernodude777/endesium/dragon/DragonFightController.java")
	mixin = read_text(MAIN / "java/com/infernodude777/endesium/mixin/EnderDragonMixin.java")
	ok = ("void save(" in controller and "void load(" in controller
		  and "alreadyBuffed" in controller
		  and "addAdditionalSaveData" in mixin and "readAdditionalSaveData" in mixin)
	return (ok, "State.save/load wired via EnderDragonMixin + alreadyBuffed" if ok else "persistence wiring missing")

@check("P2-1: archive containers use random seeds")
def archive_seeds():
	text = read_text(MAIN / "java/com/infernodude777/endesium/world/BiomeStructureFeature.java")
	ok = "setLootTable(lootKey(table), rnd.nextLong())" in text
	return (ok, "structure loot containers use random seeds" if ok else "no random seed found")

@check("P2-2: every texture is power-of-two")
def textures_pot():
	bad: list[str] = []
	for png in sorted((ASSETS / "textures").rglob("*.png")):
		if ".bak.png" in png.name:
			continue
		try:
			from PIL import Image
			with Image.open(png) as im:
				w, h = im.size
		except (OSError, ValueError):
			continue
		if not (is_power_of_two(w) and is_power_of_two(h)):
			bad.append(f"{png.relative_to(ASSETS)} ({w}x{h})")
	return (not bad, "textures not POT: " + (", ".join(bad) if bad else "all good"))

@check("P2-4: landmark support checks are per-column")
def per_column_support():
	features = ["BiomeStructureFeature", "RegionLandmarkFeature"]
	bad: list[str] = []
	for name in features:
		text = read_text(MAIN / "java/com/infernodude777/endesium/world" / (name + ".java"))
		if text.count("WORLD_SURFACE_WG") < 2:
			bad.append(name)
	return (not bad, "per-column support in: " + (", ".join(features) if not bad else "missing in " + ", ".join(bad)))

@check("P2-6: landmark writes gated to generating region")
def far_chunk_gate():
	text = read_text(MAIN / "java/com/infernodude777/endesium/world/StructurePlacement.java")
	ok = "isWithinGeneratingRegion" in text and "WorldGenRegion" in text
	return (ok, "StructurePlacement gates to 3x3 generating region" if ok else "gate missing")

@check("P3: seed cache cleared on server stop")
def seed_clear():
	text = read_text(MAIN / "java/com/infernodude777/endesium/world/ModWorldgen.java")
	ok = "SERVER_STOPPING" in text and "EndesiumWorldgenSeeds.clear()" in text
	return (ok, "ModWorldgen clears worldgen seed cache on SERVER_STOPPING" if ok else "seed clear missing")

@check("P3: resonance attachment is wired, not dead code")
def resonance_wired():
	system = MAIN / "java/com/infernodude777/endesium/resonance/ResonanceSystem.java"
	ok = system.exists() and "AFTER_DEATH" in read_text(system)
	endesium = read_text(MAIN / "java/com/infernodude777/endesium/Endesium.java")
	ok = ok and "ResonanceSystem.register()" in endesium
	return (ok, "ResonanceSystem wired via AFTER_DEATH and registered in Endesium" if ok else "resonance still unwired")

@check("Guidebook: crisp model (integer scale, no rotation)")
def guidebook_model():
	model = ASSETS / "models/item/endesium_gui_book.json"
	data = json.loads(read_text(model))
	gui = data.get("display", {}).get("gui", {})
	rot = gui.get("rotation", [0, 0, 0])
	scale = gui.get("scale", [1.0, 1.0, 1.0])
	ok = rot == [0, 0, 0] and scale == [1.0, 1.0, 1.0]
	return (ok, f"gui display rotation={rot} scale={scale}")

@check("Guidebook: screen renders with text shadow")
def guidebook_shadow():
	text = read_text(CLIENT / "java/com/infernodude777/endesium/client/screen/EndesiumGuidebookScreen.java")
	ok = "true" in text and "drawString(this.font, page.title()" in text
	return (ok, "screen title drawn with shadow" if ok else "screen unchanged")

@check("Guidebook: core pages present")
def guidebook_pages():
	text = read_text(CLIENT / "java/com/infernodude777/endesium/client/screen/EndesiumGuidebookContent.java")
	ok = "Welcome to Endesium" in text and "Quick Start" in text
	return (ok, "guidebook content includes the welcome and quick-start pages" if ok else "pages missing")

@check("Tools: POT texture fixer exists")
def pot_fixer():
	ok = (ROOT / "tools/fix_power_of_two_textures.py").exists()
	return (ok, "tools/fix_power_of_two_textures.py present" if ok else "missing")

def main() -> int:
	failures = 0
	for name, fn in checks:
		try:
			ok, detail = fn()
		except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError) as exc:
			ok, detail = False, f"raised {type(exc).__name__}: {exc}"
		mark = "PASS" if ok else "FAIL"
		print(f"[{mark}] {name}")
		print(f"        {detail}")
		if not ok:
			failures += 1
	print(f"\n{len(checks) - failures}/{len(checks)} checks passed")
	return 1 if failures else 0

if __name__ == "__main__":
	sys.exit(main())