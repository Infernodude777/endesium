# Devlog 20 - Three New Sets

hey guys.

this one adds the thing that's been missing since the void set went in — actual custom armor that isn't void.

 Luminous, Ash, and Null are new. 12 armor pieces, 15 tools, all with their own textures, their own powers, and their own place in the creative tab. void is still there exactly how it was — this is about the other three.

## three sets, three identities

the void set already had its silhouette and its trim workflow. the new sets follow the same P-mode two-layer model so trims work the same, but they're recolored palettes with their own read:

- **Luminous** gold-cyan, glassy, the grove's light. high enchantability, diamond-tier defense with a light-mobility kit.
- **Ash** gray-ember with ember-orange highlights, volcano-forged. slightly lighter than void but built for fire.
- **Null** violet-void, deleted matter. void-tier defense, hungrier enchant cost, gravity-deletion kit.

`luminous_layer_1/2.png`, `ashen_layer_1/2.png`, and `null_layer_1/2.png` all use the correct 64x32 humanoid layout. the ashen pair was regenerated from the void silhouette at the right luminance rank so the helmet actually sits on a head and the body actually sits on a body — no more horizontal bar helmet.

## you can read them now

none of the three sets had working names before — datagen was fine, but the generated lang had `Luminous_helmet` with a capital L, so the game looked for `item.endesium.luminous_helmet` and found nothing. 26 keys showed `item.endesium.___` in inventories.

`src/main/generated/assets/endesium/lang/en_us.json` is now lowercase for all luminous/ash/null armor and tools, the phantom `Ash_boots` is gone. hover a luminous sword and it actually says luminous sword.

ashwalker boots also finally sit with their set. they were registered in `ModItems` and listed 80 slots away from `ASH_HELMET/CHEST/LEGGINGS` in `ModItemGroups`. now it's `ASH_HELMET/CHEST/LEGGINGS/ASHWALKER_BOOTS/SWORD...` — contiguous.

## armor is per-piece now

new rule: every single piece does something on its own. the full set is an upgrade, not the only reason to wear it.

**Luminous — the grove's light** — `GearAbilities` per piece + tooltips on `GearArmorItem`:
- helmet `Gleamsight` — night vision while worn
- chest `Radiant Aegis` — attackers get glowing 8s; full set `Prism Ward` adds darkness 3s + 3 magic burn-back
- leggings `Lightspeed` — speed I (II with full set)
- boots `Lumen Leap` — jump II

**Ash — the volcano** — volcano kit, fire is fuel:
- helmet `Ember Crown` — fire resistance while worn
- chest `Searing Plate` — attackers ignite 4s; burning/in lava gives strength I
- leggings `Magma Blood` — regen I while burning/in lava
- boots `Ashwalker` — stand on lava, and now hold shift in lava to sink at -0.18/tick to -0.55 like scaffolding, release and you stay down with `LAVA` puffs every 4t

full ash `Volcanic Heart` is permanent strength I. the set wants you in lava.

**Null — deleted matter** — `Erased Mind` helmet purges levitation/darkness/nausea every tick, `Erased Wound` chest gives absorption 5s on hit, `Weightless` leggings no fall damage, `Null Step` boots +1 step height via attribute, full set `Void Body` deletes 25% of incoming projectiles with smoke + shield block feedback.

all ticks use a 40t refresh with 120t buffer so there's never a flicker, and the luminous retaliation has a reentrancy guard.

## tools are not potions anymore

old `EndgearTools` was 15 tools sharing 4 `MobEffectInstance`s. axes did almost nothing, pickaxe did the same cone as sword.

rewrote all 15, zero potions, every `use` and `hurtEnemy` is particles + physics + world:

- **Luminous** — sword 20b prism beam piercing 4 (6 dmg, flash at 10b) + shard splash, pick lamp + light pulse, axe 120° arc cleave to 2 + 8b dash trail, shovel sky-launch + shockwave, hoe 7x7 till + 40x growth and 3x3 reap.
- **Ash** — sword is the 3s toggle you asked for: right-click toggles `Firebending Stance` 60t (72t cooldown), swings shoot small fireball + 5s burn + 2 bonus dmg, with flame/lava particles. pick `Magma Quench` superheats 3x3x3 (stone->smooth, ores pop raw), axe 10b crescent + 6b ring slam, shovel hurls 3x3 falling blocks as flaming projectiles, hoe 5x5 scorch till.
- **Null** — sword shreds 4 armor durability + portal burst, pull 7b then implodes 5 dmg, pick 12b aimed void-step, axe shreds 3 + lifts and phases through wall 4b, shovel phase-slips and folds 5x5 ground down 1b lifting foes then unfolds after 80t, hoe phases 5x5 harvest direct to inventory.

each tool now has two gray tooltip lines, so hover tells you what it actually does.

## durability you can feel

only `ashwalker_boots` had a durability before (429). the other 11 armor pieces were `stacksTo(1)` infinite. tool tiers were also low.

now via `gearProps(durability)`:
- luminous 480/640/600/520
- ash 520/680/640/580 (boots 620)
- null 680/880/820/720
void left alone. tool `getUses()` buffed: luminous 1750->2800, ash 1400->2600, null 2031->3400. they actually wear now, and they last.

## numbers

- 3 new armor sets (12 pieces) + 15 new tools, 2 layer textures regenerated, 26 lang keys, 15 tools rewritten (~672 lines), 11 armor pieces given durability
- `gradlew build` green

## what's next

more landmark variants, ambient life for the empty skies, and the deep end is still sitting there being ominous.

go pick a set. they finally play differently.
