#!/usr/bin/env python3
"""
Comprehensive resource completeness audit for Endesium.

Checks, for every registered block/item/entity:
  - block: blockstate + block model + block texture (+ item model where needed)
  - item: item model + layer0/block texture
  - entity: geo.json + animation.json + texture png + sounds
across BOTH resource roots (src/main/resources and src/main/generated).
"""
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources", "assets", "endesium")
GEN = os.path.join(ROOT, "src", "main", "generated", "assets", "endesium")
JAVA = os.path.join(ROOT, "src", "main", "java", "com", "infernodude777", "endesium")

problems = []


def has_file(*rel):
    """Return True if any of the resource roots contains the relative asset path."""
    for base in (RES, GEN):
        if os.path.exists(os.path.join(base, *rel)):
            return True
    return False


def find_file(*rel):
    for base in (RES, GEN):
        p = os.path.join(base, *rel)
        if os.path.exists(p):
            return p
    return None


# ---------------------------------------------------------------- blocks
blocks_src = os.path.join(JAVA, "registry", "ModBlocks.java")
block_ids = re.findall(r'register\("([a-z_]+)"', open(blocks_src).read())

# plants registered via plant() helper also use the same id pattern
for bid in block_ids:
    bs = has_file("blockstates", f"{bid}.json")
    bm = has_file("models", "block", f"{bid}.json")
    if bid == "inscribed_slate":
        bm = all(has_file("models", "block", f"{bid}_{symbol}.json") for symbol in range(4))
    if bid == "void_brick_wall":
        bm = has_file("models", "block", "void_brick_wall_inventory.json")
    texture_id = {
        "resonant_mechanism": "end_ruin_mechanism",
        "void_brick_slab": "void_brick",
        "void_brick_stairs": "void_brick",
        "void_brick_wall": "void_brick",
        "highland_lensstone": "highland_stone",
        "windscar_bracket": "highland_slate",
        "tide_iron": "end_clay",
        "mireglass": "void_glass",
        "lumen_graft_block": "lumen_stone",
        "prism_canopy_block": "pale_crystal_block",
        "crown_needle_block": "void_spire",
        "crown_seal_block": "void_lamp",
        "null_archive_frame": "voidstone",
        "threshold_core_block": "void_crystal",
    }.get(bid, bid)
    bt = has_file("textures", "block", f"{texture_id}.png")
    if bid == "inscribed_slate":
        bt = all(has_file("textures", "block", f"{bid}_{symbol}.png") for symbol in range(4))
    im = has_file("models", "item", f"{bid}.json")
    if not bs:
        problems.append(f"block {bid}: MISSING blockstate")
    if not bm:
        problems.append(f"block {bid}: MISSING block model")
    if not bt:
        problems.append(f"block {bid}: MISSING block texture")
    if not im and not bm:
        problems.append(f"block {bid}: MISSING item model")

# ---------------------------------------------------------------- items
items_src = os.path.join(JAVA, "registry", "ModItems.java")
item_ids = re.findall(r'register\("([a-z_]+)"', open(items_src).read())
for iid in item_ids:
    im = has_file("models", "item", f"{iid}.json")
    if not im:
        problems.append(f"item {iid}: MISSING item model")
        continue
    # find the layer0 texture or parent block model
    model_path = find_file("models", "item", f"{iid}.json")
    if model_path:
        try:
            m = json.load(open(model_path))
        except Exception as e:
            problems.append(f"item {iid}: invalid item model JSON ({e})")
            continue
        # walk parent chain
        for _ in range(5):
            parent = m.get("parent")
            if not parent:
                break
            if parent.startswith("endesium:block/"):
                break
            if parent.startswith("endesium:item/"):
                # resolve parent model
                pp = find_file("models", "item", parent.split(":", 1)[1] + ".json")
                if pp and os.path.exists(pp):
                    try:
                        m = json.load(open(pp))
                    except Exception:
                        break
                    continue
            break
        tex = None
        if "textures" in m:
            tex = m["textures"].get("layer0")
        if tex and tex.startswith("endesium:"):
            texrel = tex.split(":", 1)[1] + ".png"
            if not has_file("textures", *texrel.split("/")):
                problems.append(f"item {iid}: layer0 texture missing ({tex})")

# ---------------------------------------------------------------- entities (geo/anim/texture)
client = os.path.join(ROOT, "src", "client", "java", "com", "infernodude777", "endesium", "client", "entity")
for fn in os.listdir(client):
    if not fn.endswith("Model.java"):
        continue
    src = open(os.path.join(client, fn)).read()
    geo = re.search(r'Endesium\.id\("(geo/[^"]+)"\)', src)
    tex = re.search(r'Endesium\.id\("(textures/[^"]+)"\)', src)
    anim = re.search(r'Endesium\.id\("(animations/[^"]+)"\)', src)
    if geo and not has_file(*geo.group(1).split("/")):
        problems.append(f"{fn}: MISSING geo {geo.group(1)}")
    if tex and not has_file(*tex.group(1).split("/")):
        problems.append(f"{fn}: MISSING texture {tex.group(1)}")
    if anim and not has_file(*anim.group(1).split("/")):
        problems.append(f"{fn}: MISSING animation {anim.group(1)}")

# ---------------------------------------------------------------- sounds.json
sounds_path = find_file("sounds.json")
if sounds_path:
    snds = json.load(open(sounds_path))
    for k, v in snds.items():
        entries = v.get("sounds", []) if isinstance(v, dict) else v
        for e in entries:
            name = e if isinstance(e, str) else e.get("name")
            if name and name.startswith("endesium:"):
                rel = name.split(":", 1)[1] + ".ogg"
                if not has_file("sounds", *rel.split("/")):
                    problems.append(f"sound {k}: missing ogg {name}")

# ---------------------------------------------------------------- content contracts
# These checks deliberately inspect the authoritative source/data files rather
# than claiming that a Java class existing is enough. They catch accidental
# removal of a biome dispatch, a landmark archetype, or a Dragon safety guard.
world_src = os.path.join(JAVA, "world", "BiomeStructureFeature.java")
world_text = open(world_src, encoding="utf-8").read()
flagship_contracts = {
    "END_WASTES": "dustCathedral",
    "CHORUS_WILDS": "elderwoodSanctum",
    "SHATTERED_HIGHLANDS": "skyrendKeep",
    "VOID_MARSHES": "drownedCathedral",
    "LUMINOUS_GROVES": "lumenCathedral",
    "ASHEN_EXPANSE": "greatCaldera",
    "CRYSTAL_BARRENS": "sunkenGeode",
    "VOID_SKIRTS": "voidSpire",
    "VOID_CROWN": "crownObservatory",
    "UMBRAL_REACH": "nullArchive",
}
for region, builder in flagship_contracts.items():
    if not re.search(r"case EndesiumRegions\." + region + r" -> " + builder + r"\(", world_text):
        problems.append(f"worldgen contract {region}: missing flagship dispatch ({builder})")

landmark_src = os.path.join(JAVA, "world", "RegionLandmarkFeature.java")
landmark_text = open(landmark_src, encoding="utf-8").read()
landmark_contracts = {
    "END_WASTES": "duneFossilArch",
    "CHORUS_WILDS": "hollowStump",
    "SHATTERED_HIGHLANDS": "windvaneWatchtower",
    "VOID_MARSHES": "mireBellCairn",
    "LUMINOUS_GROVES": "lightwellGazebo",
    "ASHEN_EXPANSE": "emberShrine",
    "CRYSTAL_BARRENS": "shardSpireCluster",
    "VOID_SKIRTS": "anchorRuin",
    "VOID_CROWN": "needleCircle",
    "UMBRAL_REACH": "nullObelisk",
}
for region, builder in landmark_contracts.items():
    if not re.search(r"case EndesiumRegions\." + region + r" -> " + builder + r"\(", landmark_text):
        problems.append(f"landmark contract {region}: missing dispatch ({builder})")

biome_root = os.path.join(ROOT, "src", "main", "resources", "data", "endesium", "worldgen", "biome")
structure_root = os.path.join(ROOT, "src", "main", "resources", "data", "endesium", "worldgen", "structure")
for biome in ("end_wastes", "chorus_wilds", "void_marshes", "ashen_expanse", "crystal_barrens",
              "void_skirts", "void_crown", "shattered_highlands", "luminous_groves", "umbral_reach"):
    path = os.path.join(biome_root, biome + ".json")
    try:
        data = json.load(open(path, encoding="utf-8"))
        features = json.dumps(data.get("features", []))
        if "biome_structure" in features or "biome_landmark" in features:
            problems.append(f"worldgen contract {biome}: retired feature placement still wired")
    except Exception as exc:
        problems.append(f"worldgen contract {biome}: invalid or missing biome JSON ({exc})")

# Structures migration contract: twenty registered structures + two sets.
for sid in ("dust_cathedral", "elderwood_sanctum", "skyrend_keep", "drowned_cathedral",
            "lumen_cathedral", "great_caldera", "sunken_geode", "void_spire",
            "crown_observatory", "null_archive", "dune_fossil_arch", "hollow_stump",
            "windvane_watchtower", "mire_bell_cairn", "lightwell_gazebo", "ember_shrine",
            "shard_spire_cluster", "anchor_ruin", "needle_circle", "null_obelisk"):
    if not os.path.exists(os.path.join(structure_root, sid + ".json")):
        problems.append(f"structures migration: missing worldgen/structure/{sid}.json")
for sset in ("endesium_flagships", "endesium_landmarks"):
    if not os.path.exists(os.path.join(structure_root, "..", "structure_set", sset + ".json")):
        problems.append(f"structures migration: missing structure_set/{sset}.json")

dragon_mixin = open(os.path.join(JAVA, "mixin", "EnderDragonMixin.java"), encoding="utf-8").read()
dragon_fight = open(os.path.join(JAVA, "mixin", "EndDragonFightMixin.java"), encoding="utf-8").read()
controller = open(os.path.join(JAVA, "dragon", "DragonFightController.java"), encoding="utf-8").read()
for label, text, required in (
    ("Dragon death hook", dragon_mixin, ("!state.isDragonDefeated()", "clearZones")),
    ("Dragon fight transition", dragon_fight, ("setDragonKilled", "markDragonDefeated", "fireTransformation")),
    ("Dragon controller safety", controller, ("tickCooldowns", "dragonDeathTime", "fracturePoints")),
):
    missing = [token for token in required if token not in text]
    if missing:
        problems.append(f"{label}: missing safety contract {missing}")

renderer_path = os.path.join(ROOT, "src", "client", "java", "com", "infernodude777", "endesium", "client", "mixin", "EnderDragonRendererMixin.java")
renderer = open(renderer_path, encoding="utf-8").read()
for token in ("pushPose()V", "At.Shift.AFTER", "stageScale"):
    if token not in renderer:
        problems.append(f"Dragon renderer contract: missing pose-safe stage token {token}")
# Structure builders must route writes through the guarded StructurePlacement
# helper; terrain-scale features (DragonArenaBuilder, ChorusWildsTerrainFeature)
# intentionally write directly and are exempt.
for builder in ("BiomeStructureFeature.java", "RegionLandmarkFeature.java"):
    builder_path = os.path.join(JAVA, "world", builder)
    if not os.path.exists(builder_path):
        problems.append(f"structure safety contract: missing current builder {builder}")
        continue
    builder_text = open(builder_path, encoding="utf-8").read()
    if "level.setBlock(" in builder_text:
        problems.append(f"structure safety contract {builder}: direct level.setBlock remains")

# ---------------------------------------------------------------- particles
part_src = os.path.join(JAVA, "particle", "ModParticles.java")
if os.path.exists(part_src):
    # particle textures are optional (some use vanilla/no texture)
    pass

print(f"checked {len(block_ids)} blocks, {len(item_ids)} items")
if problems:
    print(f"\n{len(problems)} problem(s):")
    for p in problems:
        print("  - " + p)
else:
    print("ALL RESOURCES PRESENT")
