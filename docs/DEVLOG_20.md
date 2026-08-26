# Devlog 20 - The Custom Pass

hey guys.

this one is the pass where the custom gear finally stops feeling like a recolor.

playtests kept circling the same three complaints: the textures were rotated, the names were `item.endesium.___`, and the powers were just slow falling with extra steps. which is fair. if the reward for wearing all four null armor is an effect everyone hates, it's not a reward.

so everything custom got touched. not just the numbers — the feel.

## the rotated ashen set

the ashen armor layers were straight up 90 degrees off. `ashen_layer_1.png` and `ashen_layer_2.png` had the helmet as a horizontal bar and the body as vertical strips. you wore it and the game tried to map that onto a human. it looked broken because it was.

regenerated both from the void silhouette with the correct 64x32 layout, recolored into warm gray-ember with the ember-orange highlight at the same luminance rank the luminous and null sets use. same silhouette, correct palette, actually maps to a body now.

## the missing names

26 lang keys were capitalized. `Luminous_helmet` instead of `luminous_helmet`. fabric's `translationBuilder.add(item, name)` generates lowercase `item.endesium.luminous_helmet`, so every luminous/ash/null armor and tool showed `item.endesium.___` in inventory.

fixed `src/main/generated/assets/endesium/lang/en_us.json` to lowercase, removed the phantom `Ash_boots` that never had an item. datagen already generates the right keys — the generated file had been hand-edited with the wrong case.

ashwalker boots also lived 80 slots away from its own set in the creative tab. `ModItemGroups` now lists `ASH_HELMET/CHEST/LEGGINGS/ASHWALKER_BOOTS/SWORD...` so the set is contiguous.

## armor stopped being one potion

old `GearAbilities`: full luminous = night vision + glowing on yourself (a downside), full ash = fire res you already had from boots, full null = forced slow falling. no reason to mix, no reason to care about a single piece.

now every piece is its own thing, and the full set is an upgrade:

**Luminous (grove light)** — helmet `Gleamsight` night vision, chest `Radiant Aegis` (attackers get glowing 8s; full set adds darkness 3s + 3 magic burn-back as `Prism Ward`), leggings `Lightspeed` speed I (II with full set), boots `Lumen Leap` jump II.

**Ash (volcano)** — helmet `Ember Crown` fire res, chest `Searing Plate` (attackers ignite 4s; burning/in lava gives strength I), leggings `Magma Blood` (regen I while burning/in lava), boots lava-walk stays, full set `Volcanic Heart` permanent strength I.

**Null (deletion)** — helmet `Erased Mind` purges levitation/darkness/nausea every tick, chest `Erased Wound` absorption 5s on hit, leggings `Weightless` no fall damage, boots `Null Step` +1 step height via attribute, full set `Void Body` 25% projectile delete with smoke + shield block feedback.

**Void** got the same treatment: per-piece tooltips now say what they do, durability added (620/850/800/700), tool tier buffed to 3040 uses / 9.2 speed.

all of it ticks with a 40t refresh and 120t buffer so there's never a flicker, and the damage hooks use a reentrancy guard so luminous retaliation doesn't ping-pong forever.

ashwalker boots also learned scaffolding: hold shift in lava and you sink at -0.18/tick to -0.55 like scaffolding, release and you stay down. no rebound. `LAVA` particles every 4t while sinking.

## tools stopped being potions

every `EndgearTools` class was `MobEffectInstance` on hit or on use. 15 tools shared like 4 effects. axes did basically nothing, pickaxe did the same cone as sword.

all 15 rewritten, no `MobEffects` at all. every use/hit is particles + physics + world:

- **Luminous Sword** 20b prism beam (6 dmg piercing 4, flash at 10b), hit splashes 2 dmg to nearby foes. Pick places void lamp + 3-block light pulse. Axe arc cleaves 2 extra in 120° for 4 dmg + 8b dash trail. Shovel sky-launches + 5-block shockwave. Hoe 7x7 till + 40x randomTick growth and 3x3 reap on hit.

- **Ash Sword** is the request: toggle firebending, 3s. right-click toggles `Firebending Stance` 60t (cooldown 72). while active every swing fires a small fireball, ignites 3s (5s + 2 bonus dmg in stance), flame/lava particles. Pick `Magma Quench` superheats 3x3x3 (stone->smooth, cobble->stone, ores pop raw), axe throws 10b crescent flame piercing 3 and 6b ring slam, shovel hurls 3x3 falling blocks as flaming projectiles, hoe scorches till 5x5 with flame edge.

- **Null Sword** shreds 4 armor durability + portal burst, use pulls 7b then implodes 5 dmg falloff and deletes projectiles. Pick aimed 12b void-step to solid top + afterimage invuln. Axe shreds 3 durability + lifts, use phases through wall 4b. Shovel hit phase-slips foe, use folds 5x5 ground down 1b, lifts foes 0.95b, unfolds after 80t. Hoe phases 5x5 crops/leaves direct to inventory + shreds 5 durability on hit.

each tool has two gray tooltip lines now, so hover tells you the actual ability.

## durability finally exists

only `ashwalker_boots` had `durability(429)`. everything else was `stacksTo(1)` with no durability — infinite armor and tools that used tier `getUses()` but felt disposable.

all non-void armor now has explicit durability via `gearProps(durability)`: luminous 480/640/600/520, ash 520/680/640/580 (boots 620), null 680/880/820/720. void 620/850/800/700. tool tiers buffed: luminous 1750->2800, ash 1400->2600, null 2031->3400, void 2031->3040 and +0.2 speed/damage where it made sense.

## numbers

- 6 files for the gear pass, ~672 lines added in the toolkit rewrite alone, plus 241 before that
- 2 armor layer textures re-saved, 26 lang keys fixed, 15 tools rewritten, 11 armor pieces given durability
- `gradlew build` still green — 8 tasks, no potion imports left in `EndgearTools`

## what's next

deeper void armor physical abilities (right now void still leans on potions for night vision/resistance — want to replace with attribute/outline versions), more landmark variants, and the deep end still being ominous on the roadmap.

go wear a set. it should finally feel like a set.
