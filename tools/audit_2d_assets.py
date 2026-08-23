#!/usr/bin/env python3
"""Audit all blocks/items for complete 2D asset coverage across BOTH
src/main/resources (hand-written) and src/main/generated (datagen output).
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASES = [os.path.join(ROOT, "src", "main", "resources"),
         os.path.join(ROOT, "src", "main", "generated")]


def exists_in(*rel):
    return any(os.path.exists(os.path.join(base, *rel)) for base in BASES)


def read_first(*rel):
    for base in BASES:
        p = os.path.join(base, *rel)
        if os.path.exists(p):
            with open(p, "r", encoding="utf-8") as fh:
                return fh.read()
    return None


blocks_src = open(os.path.join(ROOT, "src", "main", "java", "com", "infernodude777",
                               "endesium", "registry", "ModBlocks.java"),
                  encoding="utf-8").read()
block_ids = set(re.findall(r'register\("([a-z0-9_]+)"', blocks_src))
block_ids |= set(re.findall(r'block\("([a-z0-9_]+)"', blocks_src))

items_src = open(os.path.join(ROOT, "src", "main", "java", "com", "infernodude777",
                              "endesium", "registry", "ModItems.java"),
                 encoding="utf-8").read()
item_ids = set(re.findall(r'register\("([a-z0-9_]+)"', items_src))
item_ids |= set(re.findall(r'blockItem\("([a-z0-9_]+)"', items_src))

issues = []

for b in sorted(block_ids):
    for kind, rel in [
        ("blockstate", ("assets", "endesium", "blockstates", b + ".json")),
        ("block model", ("assets", "endesium", "models", "block", b + ".json")),
        ("item model", ("assets", "endesium", "models", "item", b + ".json")),
        ("loot table", ("data", "endesium", "loot_table", "blocks", b + ".json")),
    ]:
        if not exists_in(*rel):
            issues.append(f"block {b}: MISSING {kind}")
    content = read_first("assets", "endesium", "models", "block", b + ".json")
    if content:
        try:
            for tex in json.loads(content).get("textures", {}).values():
                if tex.startswith("endesium:") and not exists_in(
                        "assets", "endesium", "textures", tex.split(":", 1)[1] + ".png"):
                    issues.append(f"block {b}: model refs MISSING texture {tex}")
        except Exception as e:
            issues.append(f"block {b}: block model unparseable {e}")

for it in sorted(item_ids):
    content = read_first("assets", "endesium", "models", "item", it + ".json")
    if not content:
        issues.append(f"item {it}: MISSING item model")
        continue
    try:
        for tex in json.loads(content).get("textures", {}).values():
            if tex.startswith("endesium:") and not exists_in(
                    "assets", "endesium", "textures", tex.split(":", 1)[1] + ".png"):
                issues.append(f"item {it}: model refs MISSING texture {tex}")
    except Exception as e:
        issues.append(f"item {it}: item model unparseable {e}")

lang = {}
for base in BASES:
    p = os.path.join(base, "assets", "endesium", "lang", "en_us.json")
    if os.path.exists(p):
        lang = json.load(open(p, "r", encoding="utf-8"))
        break
for b in sorted(block_ids):
    if f"block.endesium.{b}" not in lang:
        issues.append(f"block {b}: MISSING lang key")
for it in sorted(item_ids):
    if f"item.endesium.{it}" not in lang:
        issues.append(f"item {it}: MISSING lang key")

print(f"blocks={len(block_ids)} items={len(item_ids)} issues={len(issues)}")
for i in issues:
    print(" -", i)
sys.exit(1 if issues else 0)
