#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Repair the broken Endesium registries caused by a duplicated insert run.

The insert_at_line landed 3x, so several files declare the same fields
multiple times. This script dedupes them and fixes the missing imports.
Only Java/JSON files are touched; textures are generated separately.
"""
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src")


def read(rel):
    with open(os.path.join(ROOT, rel), encoding="utf-8") as fh:
        return fh.read()


def write(rel, content):
    with open(os.path.join(ROOT, rel), "w", encoding="utf-8", newline="") as fh:
        fh.write(content)
    print(f"wrote {rel}")


def count(content, needle):
    return content.count(needle)


# ═══════════════════════════════════════════════════════════════════════════
# 1. ModBlocks.java — ASHEN_CRUST declared 3x; keep one.
# ═══════════════════════════════════════════════════════════════════════════
p = "src/main/java/com/infernodude777/endesium/registry/ModBlocks.java"
s = read(p)
crust = "\t// --- Ashen Expanse: lava-walk crust ---\n\tpublic static final Block ASHEN_CRUST = register(\"ashen_crust\",\n\t\t\tnew com.infernodude777.endesium.block.AshenCrustBlock(BlockBehaviour.Properties.of().strength(0.4F).sound(SoundType.STONE).noOcclusion()));\n\n"
n = s.count(crust)
print(f"ModBlocks ASHEN_CRUST blocks: {n}")
if n == 3:
    s = s.replace(crust * 2, "", 1)  # drop copies 2 and 3 (contiguous)
    write(p, s)

# ═══════════════════════════════════════════════════════════════════════════
# 2. ModItems.java — ashen material block declared 3x; keep one, add imports.
# ═══════════════════════════════════════════════════════════════════════════
p = "src/main/java/com/infernodude777/endesium/registry/ModItems.java"
s = read(p)
ashen_block = (
    "\t// --- Ashen Expanse materials ---\n"
    "\tpublic static final Item ASHEN_EMBER = register(\"ashen_ember\",\n"
    "\t\t\tnew Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));\n"
    "\tpublic static final Item MAGMA_CORE = register(\"magma_core\",\n"
    "\t\t\tnew Item(new Item.Properties().stacksTo(8).rarity(Rarity.RARE)));\n"
    "\tpublic static final Item ASHWALKER_BOOTS = register(\"ashwalker_boots\",\n"
    "\t\t\tnew AshwalkerBootsItem(AshenArmorMaterials.ASHEN, ArmorItem.Type.BOOTS,\n"
    "\t\t\t\t\tnew Item.Properties().durability(429).rarity(Rarity.RARE)));\n"
    "\tpublic static final Item EMBER_CHARM = register(\"ember_charm\",\n"
    "\t\t\tnew EmberCharmItem(new Item.Properties().rarity(Rarity.RARE)));\n"
    "\tpublic static final Item ASHEN_CRUST_ITEM = blockItem(\"ashen_crust\", ModBlocks.ASHEN_CRUST);\n\n"
)
n = s.count(ashen_block)
print(f"ModItems ashen blocks: {n}")
if n >= 2:
    s = s.replace(ashen_block * n, ashen_block, 1)
    # Add missing imports (alphabetical position).
    imports = [
        "import com.infernodude777.endesium.item.AshenArmorMaterials;\n",
        "import com.infernodude777.endesium.item.AshwalkerBootsItem;\n",
        "import com.infernodude777.endesium.item.EmberCharmItem;\n",
    ]
    for imp in imports:
        if imp not in s:
            # insert before the next import that sorts after it
            anchor = "import com.infernodude777.endesium.item."
            idx = s.index(anchor)
            head, tail = s[:idx], s[idx:]
            # find insertion point: first existing import alphabetically after this one
            lines = tail.splitlines(keepends=True)
            insert_at = len(lines)
            for i, line in enumerate(lines):
                if line.startswith("import ") and line > imp:
                    insert_at = i
                    break
            lines.insert(insert_at, imp)
            tail = "".join(lines)
            s = head + tail
    write(p, s)

# ═══════════════════════════════════════════════════════════════════════════
# 3. AshwalkerBootsItem.java — missing `import net.minecraft.world.item.Item;`
# ═══════════════════════════════════════════════════════════════════════════
p = "src/main/java/com/infernodude777/endesium/item/AshwalkerBootsItem.java"
s = read(p)
if "import net.minecraft.world.item.Item;" not in s:
    s = s.replace(
        "import net.minecraft.world.item.ItemStack;\n",
        "import net.minecraft.world.item.Item;\nimport net.minecraft.world.item.ItemStack;\n",
        1,
    )
    write(p, s)

print("registry repair done")
