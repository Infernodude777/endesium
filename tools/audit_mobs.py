#!/usr/bin/env python3
"""Audit all Endesium mobs for broken references:
- animation names referenced in entity classes vs the .animation.json files
- geo JSON validity + texture references
- texture file presence
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "endesium")
ENTITY_DIR = os.path.join(ROOT, "src", "main", "java", "com", "infernodude777",
                          "endesium", "entity")

MOBS = ["ash_wraith", "chorus_stalker", "crystal_burrower", "dust_crawler",
        "lumen_moth", "marsh_crawler", "nullwalker", "void_ray", "void_stalker"]

issues = []

for mob in MOBS:
    entity_file = os.path.join(ENTITY_DIR, mob.title().replace("_", "") + "Entity.java")
    if not os.path.exists(entity_file):
        # try snake->Pascal
        pascal = "".join(w.capitalize() for w in mob.split("_"))
        entity_file = os.path.join(ENTITY_DIR, pascal + "Entity.java")
    if not os.path.exists(entity_file):
        issues.append(f"{mob}: entity class not found ({entity_file})")
        continue

    with open(entity_file, "r", encoding="utf-8") as fh:
        src = fh.read()

    # referenced animation names
    refs = set(re.findall(r'animation\.' + re.escape(mob) + r'\.[a-z_0-9]+', src))
    # also RawAnimation with full string
    refs |= set(re.findall(r'"animation\.' + re.escape(mob) + r'\.[a-z_0-9]+"', src))

    # animation file
    anim_file = os.path.join(ASSETS, "animations", "entity", mob + ".animation.json")
    if not os.path.exists(anim_file):
        issues.append(f"{mob}: animation file missing ({anim_file})")
        anim_names = set()
    else:
        try:
            with open(anim_file, "r", encoding="utf-8") as fh:
                anim_data = json.load(fh)
            anim_names = set(anim_data.get("animations", {}).keys())
        except Exception as e:
            issues.append(f"{mob}: animation file unparseable: {e}")
            anim_names = set()

    # geo file
    geo_file = os.path.join(ASSETS, "geo", "entity", mob + ".geo.json")
    geo_textures = []
    if not os.path.exists(geo_file):
        issues.append(f"{mob}: geo file missing ({geo_file})")
    else:
        try:
            with open(geo_file, "r", encoding="utf-8") as fh:
                geo = json.load(fh)
            geo_textures = geo.get("textures", [])
        except Exception as e:
            issues.append(f"{mob}: geo file unparseable: {e}")

    # check texture refs
    tex_file = os.path.join(ASSETS, "textures", "entity", mob + ".png")
    if not os.path.exists(tex_file):
        issues.append(f"{mob}: texture missing ({tex_file})")
    for t in geo_textures:
        # resolves to textures/entity/<t>.png (geo texture usually relative)
        candidate = os.path.join(ASSETS, "textures", "entity", os.path.basename(t))
        if not os.path.exists(candidate):
            issues.append(f"{mob}: geo references missing texture {t}")

    # cross-check animation names
    for ref in sorted(refs):
        clean = ref.strip('"')
        if clean not in anim_names:
            issues.append(f"{mob}: referenced animation '{clean}' NOT in animation file "
                          f"(has: {sorted(anim_names)})")

    print(f"{mob}: refs={sorted(refs)} anims={len(anim_names)} geo_tex={geo_textures}")

print("\n=== ISSUES ===")
if not issues:
    print("NONE - all mob references valid")
else:
    for i in issues:
        print(" -", i)
sys.exit(1 if issues else 0)
