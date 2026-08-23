#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Dedupe the ashen blocks that the prior Jimbibo run inserted 3x in
EndesiumLanguageProvider.java and ModItemGroups.java."""
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def read(rel):
    with open(os.path.join(ROOT, rel), encoding="utf-8") as fh:
        return fh.read()


def write(rel, content):
    with open(os.path.join(ROOT, rel), "w", encoding="utf-8", newline="") as fh:
        fh.write(content)
    print(f"wrote {rel}")


# ── LanguageProvider: the ashen translation block appears 3x ──
p = "src/client/java/com/infernodude777/endesium/client/datagen/EndesiumLanguageProvider.java"
s = read(p)
block = (
    "\t\ttranslationBuilder.add(ModItems.ASHEN_EMBER, \"Ashen Ember\");\n"
    "\t\ttranslationBuilder.add(ModItems.MAGMA_CORE, \"Magma Core\");\n"
    "\t\ttranslationBuilder.add(ModItems.ASHWALKER_BOOTS, \"Ashwalker Boots\");\n"
    "\t\ttranslationBuilder.add(ModItems.EMBER_CHARM, \"Ember Charm\");\n"
    "\t\ttranslationBuilder.add(ModBlocks.ASHEN_CRUST, \"Ashen Crust\");\n"
    "\t\ttranslationBuilder.add(\"advancements.endesium.heart_of_the_volcano.title\", \"Heart of the Volcano\");\n"
    "\t\ttranslationBuilder.add(\"advancements.endesium.heart_of_the_volcano.description\", \"Find a dormant volcano in the Ashen Expanse\");\n"
    "\t\ttranslationBuilder.add(\"advancements.endesium.ember_walker.title\", \"Ember Walker\");\n"
    "\t\ttranslationBuilder.add(\"advancements.endesium.ember_walker.description\", \"Walk across lava in Ashwalker Boots\");\n"
)
n = s.count(block)
print(f"LanguageProvider ashen block count: {n}")
if n >= 2:
    s = s.replace(block * n, block, 1)
    write(p, s)

# ── ModItemGroups: the ashen accepts appear 3x ──
p = "src/main/java/com/infernodude777/endesium/registry/ModItemGroups.java"
s = read(p)
accepts = (
    "\t\t\t\t\t\toutput.accept(ModItems.ASHEN_EMBER);\n"
    "\t\t\t\t\t\toutput.accept(ModItems.MAGMA_CORE);\n"
    "\t\t\t\t\t\toutput.accept(ModItems.ASHWALKER_BOOTS);\n"
    "\t\t\t\t\t\toutput.accept(ModItems.EMBER_CHARM);\n"
    "\t\t\t\t\t\toutput.accept(ModItems.ASHEN_CRUST_ITEM);\n"
)
n = s.count(accepts)
print(f"ModItemGroups ashen accepts count: {n}")
if n >= 2:
    s = s.replace(accepts * n, accepts, 1)
    write(p, s)

print("provider dedupe done")
