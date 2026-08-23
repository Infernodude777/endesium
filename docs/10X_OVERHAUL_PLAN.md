# Endesium 10x Overhaul — Detailed Plan (Iterating to Perfect)

## Philosophy: No AI Slop
- Every structure hand-designed block-by-block in code (not noise scatter, not `random.nextInt(3)!=0` perimeter).
- Interiors: always hollow, loot-bearing, mechanism-bearing, traversable.
- Silhouette: recognizable from 80+ blocks (tower / dome / volcano cone).
- Biome fidelity: material palette + form = name.

## Current Audit (What Was Still Slop)
- **Art:** void tools/armor were simple hue-shift of netherite → flat, no depth, no gold trim logic, holding transform custom.
- **Biomes:** 6/10 flagships already castle-grade (Ashen Volcano Forge, Lumen Citadel, Void Citadel, Wastes Cathedral, Mother Tree, Highland Lighthouse). Remaining 4 (Crystal Barrens, Void Marshes, Void Crown, Umbral Reach) still used tiny `5×5` perimeters or 6-block spires.
- **Fallback:** `buildShrine` generic 9×9 outline appeared in 4 biomes — breaks biome identity.
- **Black hole:** functional but sound used placeholder, no client lerp.

## Phase 1 — Art Overhaul (Handcrafted Void Set)
Goal: netherite as *shape* reference, but Void as *material* — deep abyssal metal with heat-tint.
- **Palette:** Base `#0D111E` (hollow black), plate `#1A2340`→`#2A3A5A`, highlight `#6E85B2`→`#9FB8E0`, cyan seam `#4ECDC4`→`#A7FFF8` (2px inner), gold trim `#C9A227`→`#F5D67A` (only on chest helm shoulders + chest band).
- **Technique:** For each netherite pixel, compute luma + edge (is top-edge of plate?). Darken 25%, shift hue 220° (brown→blue), add selective gold on helmet brow / chest band / boot toe, cyan on brightest crack.
- **Models:** All armor → `minecraft:item/generated`, all tools → `minecraft:item/handheld` (no custom display). Black outside-texture bbox clamp to 0,0,16,16.

## Phase 2 — Biome Flagships (Castles / Volcanoes / Lighthouses)
### Ashen Expanse (fire) — DONE + Polish
- **Volcano Forge** 28h×32d already. Add: `magma_block` vent + `soul_lantern` interior, obsidian vault with `magma_core` loot.
- **Burnt Citadel** → upgrade to **Ashen Bastion** (15×15 curtain walls, 4 lava-moated bastions, keep with `crying_obsidian` heart + `ash_wraith` spawner hint)

### Luminous Groves (light) — DONE
- **Lumen Citadel** (keep), **Prism Weave** (keep) — add night-glow: `sea_lantern` + `lumen_bloom` ambient.

### Void Skirts / Crown / Umbral (void) — DONE + New
- **Void Citadel** done.
- **Void Crown: Crown Spire** — 18-high `void_slate` spire on 13×13 `void_slate` disc, slanted `crown_needle_block` mast, sealed shrine `crown_seal_block` with procession stairs (remove small procession).
- **Umbral Reach: Null Citadel** — 13×13 hollow `null_archive_frame` walls 10-high, central `threshold_core_block` on `void_weave` dais, silent courtyard, hollow corner towers with `void_lamp`.

### NEW — Crystal Barrens (crystal)
- **Crystal Sanctum Palace** (replaces 6-block `buildCrystalHeart`): 19×19 faceted dome `pale_crystal_block` + `dark_crystal_block` ribs, 4 prism spires 14-high, central `crystal_cluster` chandelier, geode floor `amethyst`, vault under `crystal_core` loot. Second slot **Crystal Spire Grove** (5-spire forest, not random scatter).

### NEW — Void Marshes (bog)
- **Sunken Mire Cathedral** (replaces `buildSunkenTemple`): 21×21 flooded nave `void_marsh_soil` + `tide_iron` columns, lily + `mireglass` floor, 7-high bell tower `tide_iron` with `mire_bell_clapper` loot, side sacristy with `mireglass` vault.
- Keep **Tide Sunk Bell** as second flagship, enhance to proper lighthouse-bell hybrid.

### Shattered Highlands (wind) — DONE (Lighthouse + Observatory + Lift + Skybridge keep)

### End Wastes / Chorus Wilds — DONE (Cathedral, Mother Tree)

### Fallback Replacement
- Delete generic `buildShrine` perimeters. Replace with 4 biome-specific micro-ruins:
  - Wastes: **Dune Fossil** (spine of `wastes_stone`)
  - Wilds: **Chorus Shrine** (3×3 `elder_chorus_wood` with moss)
  - Barrens: **Crystal Shard** (2-high `crystal_shard_block`)
  - Marsh: **Mire Cairn** (3-high `void_marsh_soil` + `mireglass`)

## Phase 3 — Black Hole & Polish
- Sword `COOLDOWN 6000t` (5m) + forward spawn 6.5b + accretion disc particles already. Add: client HUD vignette (already), attenuation, sound falloff. Verify with `/give` + full armor test.
- Particles: `VOID_SKIRT_MOTE` tint cyan, `REVERSE_PORTAL` core.

## Iteration Protocol
1. Implement art → `/gradlew build` → in-game `/give @p endesium:void_*` + equip → screenshot.
2. Implement one flagship at a time → `BiomeStructureFeature` → `gradlew build` → `/locate structure endesium:biome_structure` + teleport → verify silhouette 80b / interior traversable / loot.
3. Remove AI-slop metric: if structure <5 min hand-design or <30 blocks distinct volume → redo.

## Success Criteria
- Armor on dummy = no pink, matches void palette, seam visible.
- Tools in hand = centered, 16×16, not clipped.
- Each of 10 biomes has 2 flagships, each >150 blocks volume, interior >3×3 hollow, ≥1 chest/barrel + mechanism, silhouette >12h.
- Build passes.

