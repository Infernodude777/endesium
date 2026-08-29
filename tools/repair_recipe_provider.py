#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Repair EndesiumRecipeProvider.java.

The ashen recipe block was inserted 3x and a line was corrupted
(`.\t\t// Ember Charm:` and stray `save(exporter);`). This keeps exactly one
clean ashen recipe block and removes the corruption.
"""
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
p = os.path.join(ROOT, "src/client/java/com/infernodude777/endesium/client/datagen/EndesiumRecipeProvider.java")
with open(p, encoding="utf-8") as fh:
    s = fh.read()

# The clean ashen block we want to keep exactly once.
clean_block = (
    "\t\t// Ashen Expanse: Ashwalker Boots - leather boots wrapped in ember and magma.\n"
    "\t\tShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ASHWALKER_BOOTS)\n"
    "\t\t\t\t.define('E', ModItems.ASHEN_EMBER)\n"
    "\t\t\t\t.define('M', ModItems.MAGMA_CORE)\n"
    "\t\t\t\t.define('B', Items.LEATHER_BOOTS)\n"
    "\t\t\t\t.pattern(\"E E\")\n"
    "\t\t\t\t.pattern(\"MBM\")\n"
    "\t\t\t\t.unlockedBy(\"has_ember\", has(ModItems.ASHEN_EMBER))\n"
    "\t\t\t\t.unlockedBy(\"has_magma\", has(ModItems.MAGMA_CORE))\n"
    "\t\t\t\t.save(exporter);\n"
    "\t\t// Ashen Crust is a transient world block, but give it a decorative recipe\n"
    "\t\t// so it can be built with ashen embers and ash stone.\n"
    "\t\tShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ASHEN_CRUST, 4)\n"
    "\t\t\t\t.define('E', ModItems.ASHEN_EMBER)\n"
    "\t\t\t\t.define('A', ModBlocks.ASH_STONE)\n"
    "\t\t\t\t.pattern(\" A \")\n"
    "\t\t\t\t.pattern(\"AEA\")\n"
    "\t\t\t\t.pattern(\" A \")\n"
    "\t\t\t\t.unlockedBy(\"has_ember\", has(ModItems.ASHEN_EMBER))\n"
    "\t\t\t\t.save(exporter);\n"
    "\t\t// Ember Charm: a talisman of ember and string, reusable fire resistance.\n"
    "\t\tShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EMBER_CHARM)\n"
    "\t\t\t\t.define('E', ModItems.ASHEN_EMBER)\n"
    "\t\t\t\t.define('S', Items.STRING)\n"
    "\t\t\t\t.pattern(\" E \")\n"
    "\t\t\t\t.pattern(\"ESE\")\n"
    "\t\t\t\t.pattern(\" E \")\n"
    "\t\t\t\t.unlockedBy(\"has_ember\", has(ModItems.ASHEN_EMBER))\n"
    "\t\t\t\t.save(exporter);\n"
)

# The corrupted text that the broken run wrote (uses real em-dashes in comments
# and a mangled `.\t\t// Ember Charm:` line plus stray `save(exporter);`).
# We locate the region between the "// -- Void armor --" marker and
# "armorRecipes(exporter);" and replace it wholesale with one clean block.
start_marker = "\t\t// \u2500\u2500 Void armor \u2500\u2500\n"
end_marker = "\t\tarmorRecipes(exporter);\n"

si = s.find(start_marker)
ei = s.find(end_marker)
print("start idx:", si, "end idx:", ei)
if si >= 0 and ei > si:
    head = s[:si]
    tail = s[ei:]
    s = head + clean_block + "\n\t\t" + tail
    with open(p, "w", encoding="utf-8", newline="") as fh:
        fh.write(s)
    print("repaired recipe provider")
else:
    print("markers not found; not modifying")

# sanity: count occurrences of the boots recipe
print("ASHWALKER_BOOTS recipe count:", s.count("ModItems.ASHWALKER_BOOTS"))
print("stray save(exporter); count:", s.count("\nsave(exporter);"))
