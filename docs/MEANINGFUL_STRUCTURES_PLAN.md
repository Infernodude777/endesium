# Endesium — Meaningful Structures Plan (Implemented)
*10× improvement: rare, beautiful, lore-driven, dangerous, exclusive*

> **Rule:** No flat perimeters, no `random.nextInt(3)!=0` walls. Every flagship has **silhouette >12 high, interior 3×3+ hollow, ≥1 spawner, ≥2 chests/barrels, mechanism, danger, exclusive item, origin story.** Base frequency `rarity_filter 28` (≈3.6% chunks, half previous). Flagships further gated: **Lumen Castle 1/144**, **Volcano 1/96**, **Void Prison 1/96**, **Tower 1/144**.

---
## 1. Luminous Groves — **Lumen Castle** *(very rare, 1/144)*
**Lore:** Prior to the Shatter, the Groves were a cultivation-atollum where Lumen druids grew prism forests around a citadel that focused starlight into grafts. Abandoned when the End star dimmed.
**Visual:** 27×27 `lumen_stone` curtain walls with crenellations, 4× 4×4 towers 11h `prism_canopy_block` conical roofs + `lumen_bloom` lanterns, 7×7 keep 10h chandelier, colonnade `elder_chorus_wood`, courtyard water lightwell.
**Meaning:** The castle *is* the Groves — its light *makes* the biome glow. Finding it explains why the Groves are luminous.
**Exclusive:** `PRISM_SEED`, `LUMEN_GRAFT` + `LUMEN_GRAFT_BLOCK`, `PRISM_CANOPY_BLOCK` only vault `chests/bloom_conservatory` + `luminous_lightwell`. Powers: crafts prism canopy, grafts permanent night-vision gardens.
**Mobs/Danger:** `LUMEN_MOTH` queen spawner in vault, shroud spore `COBWEB` at entry, light-sensitive puzzle (water fountain).
**Rarity:** `slot0` + `random.nextInt(3)==0` → very rare.

## 2. Ashen Expanse — **Volcano Forge** + **Ashen Bastion**
**Lore:** The Expanse is the caldera of the world-engine. Forge at summit smelted `magma_core` into `ashen_crust`; Bastion was its guard, now lava-moated ruin.
**Visual: Volcano** 26-32h, 32d base terraced `ash_stone→resonant_basalt`, 4 `magma_block` lavafalls, 9-wide caldera with 5-wide 2-deep lava lake, basalt pillars, 22-step `void_brick` spiral + `crying_obsidian` vault 5×5.
**Bastion** 17×17 curtain walls 7h `ash_stone`, lava moat + bridge, 4 hell-towers `resonant_basalt` + `SOUL_LANTERN`, obsidian keep `MAGMA_BLOCK` heart, vault `ashen_citadel`+`ashen_volcano`.
**Meaning:** Explains why Expanse is fire-y — you *see* the volcano.
**Exclusive:** `MAGMA_CORE`, `ASHEN_EMBER`, `EMBER_CHARM`, `ASH_CORE` (only `ashen_volcano`/`ashen_citadel`).
**Mobs/Danger:** `ASH_WRAITH` queen in vault + patrol, magma/crying obsidian, lava moat fall.
**Power:** Ember Charm fire-resistance, bastion crafts Ashwalk.

## 3. Void Skirts — **Void Prison** (Citadel) + Tower
**Lore:** The Skirts are the prison-yard of the Void threshold. The Citadel jailed `null_fragment`-touched Nullwalkers; Void Stalkers were wardens. Central spire was execution void crystal.
**Visual: Citadel** 21×21 `void_brick` bastion walls slit `void_glass`, 4 bastions `umbral_stone→void_spire` + `void_crystal` tips, 16h spire (hollow ladder), weave courtyard `void_lamp`, vault `umbral_stone` `VOID_CRYSTAL` heart.
**Tower** kept: 10-16h tapering `void_brick→void_slate→void_spire` column. Image-type boxy tower **not removed** — now **1/3 of slot1 (≈1/144)** with **epic loot**.
**Meaning:** Prison explains Skirts' emptiness — it *contained* the void.
**Exclusive:** `VOID_CORE`, `NULL_FRAGMENT`, `THRESHOLD_KEY`, `THRESHOLD_CORE_BLOCK` (only `void_monolith` prison vault + tower now `void_monolith`+`end_spire_treasure`).
**Mobs/Danger:** `NULLWALKER` vault + `VOID_STALKER` courtyard, `COBWEB` cells, void static.
**Power:** Unlocks Void armor set, Threshold teleport.

## 4. End Wastes — **Wastes Cathedral** (+ Fossil Spire)
**Lore:** Wastes are burial dust. Cathedral was wind-scoured mortuary for resonant-slate rites; fallen spire is fossilized leviathan spine.
**Visual:** 18×28 Gothic `wastes_stone` 6-8h walls, flying buttresses, 4 nave pillars 7h with arches, `dormant_crystal` rose window, crypt 5×5 under altar.
**Meaning:** Makes Wastes feel dead-sacred, not just cracked stone.
**Exclusive:** `WASTES_STONE`, `DORMANT_RESONANT_CRYSTAL`, `RESONANT_SLATE` (only cathedral crypt).
**Mobs/Danger:** `DUST_CRAWLER` crypt brood, `COBWEB` burial dust, fall from nave.
**Power:** Resonant crafting.

## 5. Chorus Wilds — **Mother Tree** (+ Elder Shrine)
**Lore:** Wilds are alive — Mother Tree *is* the Wilds, elder `chorus_wood` that birthed `chorus_root` network. Archive inside hollow roots was first Eldertide library.
**Visual:** 14-18h trunk 2r→1r, 4 root buttresses 6b `chorus_root→moss`, canopy cross 11b `hollow_chorus_wood`, hollow 3×3 archive room.
**Meaning:** Living origin; finding it explains why Wilds spread.
**Exclusive:** `CHORUS_EYE`, `STALKER_TENDRIL`, `ELDER_CHORUS_WOOD` (only `wilds_archive`).
**Mobs/Danger:** `CHORUS_STALKER` ambush from canopy, `RESONANT_BLOOM` lantern fall.
**Power:** Wilds tendril growth.

## 6. Shattered Highlands — **Highland Lighthouse** (wind)
**Lore:** Highlands were sky-port. Lighthouse guided `void_ray` migrations; Windscar Lift was counterweight elevator.
**Visual:** 7×7 disc `highland_slate`, 5×5 shaft 12h `highland_stone/windscar_bracket` every 4th, lantern `highland_lensstone→glass→BEACON` 15h, spiral `LADDER`.
**Meaning:** Wind/height fantasy, not flat shrine.
**Exclusive:** `HIGHLAND_FEATHER`, `WINDSCAR_WINCH` (only `highlands_summit`/`highland_observatory`).
**Mobs/Danger:** `VOID_RAY` sky ambush, fall 15b.
**Power:** Grappler (`highland_grappler`) + wind lift.

## 7. Void Marshes — **Sunken Mire Cathedral** + Bell
**Lore:** Marshes drowned a tide-cathedral; Bell tolled to hold `tide_iron` water. Mire reliquary hides under mud.
**Visual:** 21×21 flooded nave `tide_iron/mireglass`, `MARSH_MOSS` cross, 7×7 bell-tower 12h `tide_iron` + `mireglass` dome + `BEACON`, vault `void_marsh_soil`.
**Meaning:** Swamp not random pond — cathedral explains drowned architecture.
**Exclusive:** `MIRE_BELL_CLAPPER`, `VOID_SAP`, `TIDE_IRON`.
**Mobs/Danger:** `MARSH_CRAWLER` brood vault + drowning, `LILY_PAD` false floor.
**Power:** Water breathing bell.

## 8. Crystal Barrens — **Crystal Sanctum Palace**
**Lore:** Barrens are shattered geode. Palace is geode's heart-faceting.
**Visual:** 19×19 faceted `pale/crystal_shard` walls, 4×14h `dark→pale` spires `crystal_cluster` tip, dome ribs `crystal_shard`, geode `AMETHYST_BLOCK` floor, vault `dark_crystal`.
**Meaning:** Crystal made meaningful — palace *is* the crystal.
**Exclusive:** `CRYSTAL_CORE`, `BURROWER_PLATE`, `CRYSTAL_FANG`, `PALE/DARK_CRYSTAL_BLOCK`.
**Mobs/Danger:** `CRYSTAL_BURROWER` queen, `POINTED_DRIPSTONE` shard traps.
**Power:** Resonant crystal crafting.

## 9. Void Crown — **Crown Spire**
**Lore:** Crown is void nobility platform; needle mast was coronation beacon that slanted to next void region.
**Visual:** 13×13 `void_slate` disc ring `void_brick`, 18h slanted `crown_needle_block` with `LANTERN` every 4b, `crown_seal_block` chapel.
**Meaning:** Royal void, explains `VOID_CROWN` name.
**Exclusive:** `CROWN_SEAL`, `CROWN_NEEDLE`, `THRESHOLD_KEY`.
**Mobs/Danger:** `NULLWALKER` temple guard.

## 10. Umbral Reach — **Null Citadel**
**Lore:** Reach is ending — where void frays. Archive frames contain `threshold_core_block`; central `void_weave` dais is last doorway.
**Visual:** 15×15 hollow `null_archive_frame` 6h + 4×10h towers `void_lamp` tip, gate, `void_weave` dais `THRESHOLD_CORE`, vault.
**Meaning:** Umbral dread, final prison.
**Exclusive:** `NULL_FRAGMENT`, `THRESHOLD_CORE_BLOCK`, `VOID_WEAVE`.
**Mobs/Danger:** `NULLWALKER` wardens, hollow threshold collapse.
**Power:** End-game threshold teleport.

## Implementation Notes
- All flagships hollow, loot-bearing, mechanism-bearing, spawner-bearing, danger-bearing, exclusive-item-bearing.
- Tower (image) preserved: `buildVoidSpire` now double-chest (`void_monolith` + `end_spire_treasure`) + `VOID_STALKER` spawner, gated `random.nextInt(3)==0`.
- Overall `biome_structure` `chance 12→28` → half as frequent, each encounter memorable.
