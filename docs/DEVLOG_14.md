# Devlog 14
hey guys.

another one. smaller than 13 but a lot of the actual gameplay got wired up this time.

## void gear is done

netherite-tier stats: 2031 durability, 9.0 attack damage, 4.0 speed. sword, pickaxe, axe, shovel, hoe — full tool set, all void ingot + stick.

the armor follows the void set: helmet, chestplate, leggings, boots. two-layer P-mode armor model with gold shoulder plates. helmet has the retexture layers. the full set bonus is coming later but the base is solid.

void ingot chain works: void gem -> smelt -> void ingot. ingot <-> 9 nuggets. void brick from void slate (2x2). slab, stairs, wall. all the recipes are wired up in datagen.

## black holes

hold void sword for 5 seconds with the full void set -> singularity fires 6 blocks ahead, pulls everything in a 14 block radius for 8 seconds, 5 minute cooldown. the HUD has a charge bar now: resonance cyan rising to pale glow, ancient gold pulse when full. the label "VOID SINGULARITY" sits above it.

this is client-side display only. the actual singularity logic lives on the server through the use item tick.

## sonic boom

elbyra + resonant wings -> press R -> sonic boom fires. client sends a packet, server validates wings + cooldown + alive-ness before executing. keybind registered on client tick.

## the client is finally wired

all 13 entities now have renderers. the crash where any entity without a renderer NPE'd the level renderer is gone. particles registered (void skirt mote), block render layers set for void glass and mireglass (translucent), guidebook screen opens on right-click.

dragon armor model layer registered for the resonant wings passives.

## what was deleted

the old void stalker entity, old resonance lens item, old void shard item, old foundation test block, and the old generated loot/recipe/advancement JSONs for them — all removed. everything has been replaced by the production versions.

## what's next

more ruin variants for the landmark tier, ambient life for the empty skies, maybe the deep end. see the roadmap.

go find a black hole.
