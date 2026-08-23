#!/usr/bin/env python3
"""Repair chest loot tables that were written as single-line with literal
backslash-n escape sequences (a Python repr string) instead of real JSON."""
import json
import os
import sys

BACKSLASH_N = "\\n"  # the 2-char sequence: backslash + n

FILES = [
    "ashen_citadel", "crystal_heart", "highlands_summit", "luminous_lightwell",
    "marsh_temple", "void_monolith", "wastes_cathedral", "wilds_archive",
]

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CHESTS = os.path.join(ROOT, "src", "main", "resources", "data", "endesium",
                      "loot_table", "chests")

fixed = 0
for name in FILES:
    p = os.path.join(CHESTS, name + ".json")
    with open(p, "r", encoding="utf-8", newline="") as fh:
        raw = fh.read()
    if BACKSLASH_N not in raw:
        print(f"SKIP {name}.json (no literal \\n)")
        continue
    # Unescape the literal escape sequences into real characters.
    unescaped = raw.replace("\\n", "\n").replace("\\t", "    ").replace('\\"', '"')
    # Validate fully.
    parsed = json.loads(unescaped)
    # Rewrite as clean pretty-printed JSON with LF endings.
    out = json.dumps(parsed, indent=2) + "\n"
    with open(p, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(out)
    print(f"fixed {name}.json ({len(raw)} -> {len(out)} bytes)")
    fixed += 1

print(f"Total fixed: {fixed}")
sys.exit(0 if fixed > 0 else 1)
