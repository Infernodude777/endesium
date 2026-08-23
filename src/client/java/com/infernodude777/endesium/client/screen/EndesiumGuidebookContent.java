package com.infernodude777.endesium.client.screen;

import java.util.List;

/**
 * All of the Endesium Guidebook's page content, kept separate from the screen
 * so the writing can be edited without touching any rendering code. Each page
 * is a title plus a body; the screen wraps the body to fit the page.
 *
 * <p>Pages are grouped into 10 categories with short tab labels. The screen
 * renders tabs along the top edge of the book panel so the player can jump
 * between sections.
 */
public final class EndesiumGuidebookContent {
	private EndesiumGuidebookContent() {
	}

	public record Page(String title, String body) {
	}

	public record Category(String tabLabel, String displayName, int firstPage, int lastPage) {
	}

	public static final List<Page> PAGES = List.of(
			// === START (0-2) ===
			new Page("Welcome to Endesium",
					"You slew the Ender Dragon, and the End answered. What you knew is gone. In its place is Endesium \u2014 a deeper, stranger realm layered onto the End's outer islands. Ten biomes, nine creatures, ten landmark structures, a hidden resonance system, and post-Dragon transformation. This book is your field guide."),
			new Page("How to Use This Book",
					"Click the < > buttons or press left/right arrows to turn pages. Click a tab at the top to jump sections. Number keys 1-0 switch tabs directly. Left-click the page to advance; right-click to go back. The counter at the bottom shows your position in the current section."),
			new Page("Quick Start",
					"1. Explore the outer End islands. 2. Find an End Ruin and hold the Resonance Lens near the mechanism. 3. Wake the mechanism to earn a Resonance Token. 4. Craft the Echo Compass from a Token + Void Shards. 5. Follow compass headings to stronger signals. 6. When ready, face the Dragon."),

			// === WORLD (3-12) ===
			new Page("End Wastes \u2014 The Fractured Shelf",
					"The safest starting biome. Fractured end stone shelves, dark seams in the ground, dead chorus remnants. Blocks: Wastes Stone, Wastes Gravel, Dust Reeds, Void Grass. The Dust Crawler scavenges here; the Void Stalker hunts here. Landmark: the Wastes Cathedral (rare) with its crypt, and the Fallen Spire fossil."),
			new Page("Chorus Wilds \u2014 The Living Forest",
					"A dense forest of Elder Chorus with pale bark, moss, tangled roots. Blocks: Elder Chorus Wood/Bark, Hollow Chorus Wood, Chorus Root, Chorus Moss. The Chorus Stalker ambushes among trees. Landmark: the Mother Tree (rare) \u2014 a 16-block colossus whose hollow roots hold the first archive."),
			new Page("Shattered Highlands \u2014 Wind & Stone",
					"Jagged Highland Stone/Slate peaks with updrafts. Highland Lensstone glows faintly; Windscar Bracket marks old lift anchors. Void Rays glide between peaks. Landmark: the Highland Lighthouse (rare) \u2014 15 blocks tall, beacon-lit, spiral ladder inside, Void Ray ambush at the top."),
			new Page("Void Marshes \u2014 Drowned Ground",
					"Waterlogged Void Marsh Soil, Void Reeds, Marsh Moss, Tide Iron ribs of drowned ships. Marsh Crawlers lurk in shallows. Landmark: the Sunken Mire Cathedral (rare) \u2014 flooded nave, tide-iron columns, bell-tower lighthouse with beacon, sacristy vault below."),
			new Page("Luminous Groves \u2014 Starlit Atoll",
					"Lumen Stone/Moss glow soft cyan; Lumen Blooms pulse slowly. Lumen Moths drift harmlessly. Prism Canopy Block refracts light. Landmark: the Lumen Castle (very rare) \u2014 27\u00d727 citadel with four prism-roofed towers, central keep, water lightwell, and a vault holding exclusive Prism Seeds."),
			new Page("Ashen Expanse \u2014 Caldera of the Engine",
					"Ash Stone, Ashen Soil, Ashen Crust (walk on lava with Ashwalker Boots). Ash Wraiths attack from range. Resonant Basalt marks old forges. Landmarks: the Volcano Forge (rare) \u2014 28-block stratovolcano with lava lake, lavafalls, obsidian vault \u2014 and the Ashen Bastion fortress with lava moat."),
			new Page("Crystal Barrens \u2014 Shattered Geode",
					"Crystal Shard Blocks, Crystal Clusters, Dark and Pale Crystal. Crystal Burrowers tunnel beneath. Landmark: the Crystal Sanctum Palace (rare) \u2014 19\u00d719 faceted dome, four 14-block spires, geode floor, vault guarded by a Burrower queen."),
			new Page("Void Skirts \u2014 The Prison Yard",
					"Void Slate/Brick/Glass, Umbral Stone, Void Ore veins (smelt into Void Ingots). Void Stalkers patrol as wardens. Landmark: the Void Prison Citadel (rare) \u2014 21\u00d721 bastion walls, 16-block spire, subterranean vault holding Null Fragments and Threshold Keys."),
			new Page("Void Crown & Umbral Reach",
					"Crown: Void Slate disc, Crown Needle Block mast, Crown Seal chapel. Landmark: the Crown Spire (rare). Umbral Reach: the deepest region. Nullwalker appears rarely. Landmark: the Null Citadel (rare) \u2014 hollow archive frames around a Threshold Core dais."),

			// === EXPLORE (13-17) ===
			new Page("Resonance Lens",
					"Craft: 4 Void Shards around 1 Ender Eye. Also drops from Void Stalkers. Right-click to scan: shows resonance band (Weak/Medium/Strong/Overwhelming) and a broad direction. Works only in the End. 1-second cooldown between scans. The Lens never shows coordinates."),
			new Page("Resonance Token",
					"Earned by waking a dormant mechanism in an End Ruin or landmark. Hold the Lens near the mechanism until it activates. One Token per mechanism \u2014 the block stores your claim. Tokens craft the Echo Compass and other progression items."),
			new Page("Echo Compass",
					"Craft: 1 Resonance Token + 3 Void Shards + 1 Ender Eye. Shows a heading and approximate distance to the nearest recognized resonance source. Only tracks loaded sources in the End. The needle points; it does not give coordinates."),
			new Page("End Ruins & Mechanisms",
					"Every biome holds one grand flagship, and at its heart sleeps a resonant mechanism. Hold the Resonance Lens and use it to wake the mechanism: earn a Token, a clue fragment, and Void Shards. Waking your first mechanism is the true start of Endesium."),
			new Page("Landmark Index",
					"Two tiers now. FLAGSHIPS: Wastes DUST CATHEDRAL, Wilds ELDERWOOD SANCTUM, Highlands SKYREND KEEP, Marshes DROWNED CATHEDRAL, Groves LUMEN CATHEDRAL, Ashen GREAT CALDERA, Barrens GEODE OF THE SUNKEN HEART, Skirts VOID SPIRE, Crown CROWN OBSERVATORY, Umbral NULL ARCHIVE \u2014 each warded by an End Warden. LANDMARKS (common): fossil arches, hollow stumps, windvane towers, bell cairns, lightwell gazebos, ember shrines, shard spires, anchor ruins, needle circles, null obelisks \u2014 each with loot and a wakeable mini-beacon."),

			// === LIFE (18-22) ===
			new Page("Void Stalker",
					"Tall, slender biped. Watches before striking. Spawns in End Wastes and Void Skirts. Drops: 1 Resonance Lens (always) + 0-1 Void Shard. Melee attack with side-reposition. Keep distance and use a shield. The Lens drop makes this the earliest source of resonance detection."),
			new Page("Dust Crawler & Chorus Stalker",
					"Dust Crawler: low armored scavenger in Wastes Gravel. Drops Dust Chitin + Wastes Seed Pods. Non-threatening. Chorus Stalker: blends with pale trees in Chorus Wilds. Ambush predator. Drops Chorus Eyes. Wait for it to move before attacking."),
			new Page("Void Ray & Marsh Crawler",
					"Void Ray: passive glider in Shattered Highlands. Drops Void Membranes + Highland Feathers (needed for grappler). Marsh Crawler: amphibious hunter in Void Marshes. Slow on land, fast in water. Drops Marsh Tendrils + Crawler Eyes."),
			new Page("Lumen Moth & Ash Wraith",
					"Lumen Moth: passive, luminous. Drops Lumen Dust + rare Lumen Wings. Found in Luminous Groves. Ash Wraith: ranged attacker in Ashen Expanse. Hurls ash gouts. Drops Wraith Ash + Ash Cores. Watch for the heat shimmer before it attacks."),
			new Page("Crystal Burrower & Nullwalker",
					"Crystal Burrower: armored tunneler in Crystal Barrens. Drops Burrower Plates + Crystal Cores + Crystal Fangs. Stubborn but predictable. Nullwalker: extremely rare, Umbral Reach only. Made of void itself. Drops the Null Fragment \u2014 an item of immense power."),

			// === FIGHT (23-27) ===
			new Page("Void Sword",
					"Netherite-tier stats: 9 damage, 1.6 attack speed. Applies Slowness I on hit. Special: hold right-click for 3 seconds while wearing full Void Armor to charge a black hole singularity that spawns ahead of your gaze, pulls entities within 14 blocks for 8 seconds, then implodes. A charge bar fills above your hotbar; releasing near-full still fires. 5-minute cooldown."),
			new Page("Void Armor Set",
					"Netherite-tier protection (20 armor, 3 toughness). Helmet: Night Vision + Water Breathing. Chestplate: Resistance I + emergency Absorption below half health. Leggings: Haste II. Boots: Knockback Resistance + axe dash. Full set required for the sword's black hole and immunity to it."),
			new Page("Void Pickaxe / Axe / Shovel / Hoe",
					"All netherite-tier. Pickaxe: right-click resonance blast (14 magic damage AoE, needs Leggings, 5s cooldown). Axe: right-click forward dash (needs Boots, 1s cooldown). Shovel: Speed I while held. Hoe: Jump Boost I while held. Repair with Void Ingots."),
			new Page("Resonant Elytra & Sonic Boom",
					"Craft: Elytra + Resonant Dragon Scales + Abyssal Thread. An elytra with a built-in kit. Press R (configurable) to fire a 40-block Sonic Boom: 8 armor-ignoring damage, strong knockback, 1 durability, 15-second cooldown. While worn: glide about twice as fast, +4 armor, 3 extra hearts, halved fall damage, higher step, and 35% less knockback."),
			new Page("Ashwalker Boots & Ember Charm",
					"Ashwalker Boots: Fire Resistance while worn; walk on lava without converting sources. Essential for the Ashen Expanse. Ember Charm: right-click for brief Fire Resistance burst. Both crafted from Ashen Embers and Magma Cores found only in volcano/bastion vaults."),

			// === CRAFT (28-32) ===
			new Page("Void Ingots & Materials",
					"Void Ore smelts into Void Ingots. Voidstone and Umbral Stone also smelt into ingots. Void Gems cut from Void Shards. Void Nuggets: 9 per ingot. Umbral Shards found in Umbral Reach. Void Core: rare drop from Void Ore and prison vaults. Abyssal Thread + Dragonbone from Dragon kills."),
			new Page("Region Tools",
					"Wastes Compass: points to Wastes landmarks. Highland Grappler: pull yourself upward along cliffs. Lumen Lantern: lights dark areas. Void Filter: breathe in Void Marshes. Crystal Resonator: tune Crystal Clusters. Ash Sifter: recover embers from Ash Stone. Chorus Pruner: harvest Chorus cleanly."),
			new Page("Biome Relics",
					"Windscar Winch: slow-fall lift. Mire Bell Clapper: water breathing pulse. Lumen Graft: night vision flash. Crown Needle: points to next Crown signal. Null Quill: brief recall vision. Each relic is found in its biome's landmark vault and has limited durability."),
			new Page("Progression Items",
					"Resonance Lens: scan for resonance. Resonance Token: earned from mechanisms. Echo Compass: track resonance sources. Archive Key: direct access to Archive core. Archive Sigil: proof of Archive awakening. Void Pearl: a GOLEM DROP — safe 12-block teleport on durability; takes Unbreaking and Mending. Void Anchor: set a recall point in the End."),
			new Page("Exclusive Landmark Loot",
					"Great Caldera vault: Magma Cores, Dragon Fangs, Resonance Cores, diamonds, netherite, enchanted diamond armor, Ashwalker Boots. Skyrend Keep: Highland relics and Windscar gear. Dust Cathedral crypt: Spire treasures. Void Spire summit: the richest end_spire cache. Every flagship also hides barrels and a rare treasure chest."),

			// === DRAGON (33-36) ===
			new Page("The Dragon's Return",
					"The first Dragon fight stays vanilla. Kill the Dragon and the End transforms permanently: new biomes activate, dormant mechanisms reach farther, and the Resonant Archive unseals. The transformation survives server restarts and is not reset by respawning the Dragon."),
			new Page("Dragon Fight (Respawned)",
					"After the transformation, a respawned Dragon fights in phases. Phase 1-3: familiar breath, claws, dive. Phase 4+: resonance attacks \u2014 diving ground-shatter, shockwave knockback, void-fire breath, and a call that summons nearby creatures. Watch for the tell before each new attack."),
			new Page("Dragon Drops",
					"First kill: Guidebook + Resonant Dragon Scales + Dragonbone + Ender Essence + Resonance Echo + Archive Fragment. Subsequent kills: smaller material bundles. Rare rolls: Dragon Fangs, Abyssal Thread, Resonance Cores, Dragon Heart. The Heart is the rarest drop. The VOID PEARL now comes only from the End Golem."),
			new Page("The Null Archive",
					"A windowless monolith in the Umbral Reach, sealed until the Dragon dies. After the transformation, wake its core with the Lens: it yields the Archive Sigil and the knowledge that the End was never finished. The Resonant Wings recipe waits in vaults across the wastes: Elytra + Dragon Scales + Abyssal Thread."),
			new Page("The End Wardens",
					"Every flagship's vault is guarded: a hooded three-eyed construct attuned to its region. Each wears its biome's colors and fights with that biome's temper \u2014 dust bolts in the Wastes, flank-blinks in the Wilds, gale slams in the Highlands, ember novas at the Caldera. At half health it enrages: faster attacks, half the cooldowns. Kill one for its Warden Sigil."),
			new Page("Warden Sigils",
					"A carried sigil grants slow regeneration anywhere in the End. Use it to ATTUNE: consume it permanently for +1 heart (up to +10). Attunement is forever and stacks across every region's warden. Ten wardens stand between you and a permanently greater self."),
			new Page("The End Golem",
					"When the Dragon dies, something older wakes where it fell: a colossal engine of voidstone with a burning core. Phase 1: patient slams. Phase 2: homing resonance barrages and summoned minions. Phase 3: burning ground, faster shockwaves, sentinels. Bring potions, blocks, and patience \u2014 blasts barely dent it."),
			new Page("Golem Cores",
					"The Golem shatters into Golem Cores. Carry one for Resistance in the End, or absorb it: permanently +1 heart AND +0.25 attack damage per core (max +10 hearts, +4 damage). Nothing else in the End makes you stronger forever. Craft a GOLEM EFFIGY from dragonbone, void bricks, and a core to summon another whenever one is ready to fall."),
			new Page("Reading a Warden",
					"Each region's warden fights with its own temper: Wastes/Barrens fire homing shard fans \u2014 strafe behind cover. Wilds blinks BEHIND you \u2014 pre-swing your turn. Highlands/Crown gale-slams \u2014 keep six blocks out or shield the knockback. Marshes/Skirts drag you in \u2014 never stand near edges. Groves blinds and heals \u2014 burst through the flash. Ashen ignites the ring \u2014 fire resistance or range. Umbral weakens and slows your hands \u2014 end it fast. When it RAISES GUARD (arms up, eye dilates), frontal hits are wasted: flank or wait out the two seconds."),
			new Page("The Stagger Window",
					"The Golem's weakness is sustained pressure: deal 60+ damage inside 8 seconds and it STAGGERS \u2014 kneeling for five seconds, taking double damage, core exposed. Bank your biggest hits for that window: charged sword strikes, Sonic Boom, pickaxe blast. In phase 2+ watch for the beam sweep (a sparking line on the ground = sidestep sideways). Blast damage barely scratches it; bombs are dead weight here."),
			new Page("Sigil Mathematics",
					"Ten wardens x +1 heart each = +10 permanent hearts. Ten golem cores = another +10 hearts and +4 attack damage. A fully ascended slayer stands at +20 hearts, +4 damage, a regeneration aura, and a daily cheat against death \u2014 before counting gear. The End does not give this up kindly; every point of it is guarded."),

			// === GUIDE (37-41) ===
			new Page("Progression Path",
					"1. Explore outer End, find a ruin. 2. Wake mechanism with Lens, earn Token. 3. Craft Echo Compass. 4. Follow compass to stronger signals. 5. Visit each biome's landmark. 6. Gather Void Ingots from the Skirts. 7. Craft Void gear. 8. Kill the Dragon. 9. The End transforms. 10. Find the Archive. 11. Craft Resonant Wings."),
			new Page("Crafting Recipes",
					"Resonance Lens: 4 Void Shards + 1 Ender Eye (shapeless). Echo Compass: 1 Token + 3 Void Shards + 1 Ender Eye. Void Tools/Armor: Void Ingots in standard patterns. Archive Key: 1 Archive Fragment + 1 Resonance Core + 1 Ender Eye. Resonant Wings: 1 Elytra + 2 Dragon Scales + 2 Abyssal Thread."),
			new Page("Field Tips",
					"Bring Ender Pearls for escapes. The Void Stalker's reposition punishes backpedaling \u2014 strafe instead. Ruins have hidden compartments behind slate panels \u2014 mine every wall. Monoliths mark territory boundaries. The Lens bands are: Weak (>64 blocks), Medium (32-64), Strong (16-32), Overwhelming (<16)."),
			new Page("Advancements",
					"Track your progress through Endesium's advancements: First Resonance (wake a mechanism), Echo Sight (craft the compass), Into the Skirts (enter the void), Void Armorer (full set), Heart of the Volcano (volcano vault), What Remains (reach the Spire), The End Answers (Dragon kill), Archive Awakened (wake the Archive), Warden's Bane (fell a warden), Sigil Attuned, Heavier Than Stone (absorb a core), Effigy Ignited \u2014 plus hidden challenges for Warden Ascendant and Golem's Resolve."),
			new Page("Commands & Final Words",
					"/locate biome endesium:<biome> finds any region. /locate structure endesium:end_ruin finds ruins. The realm is vast and full of secrets meant to be found, not shown. Build, explore, fight, discover. The End is not an ending. It is a doorway. Walk through it."));

	public static final List<Category> CATEGORIES = List.of(
			new Category("Start", "Getting Started", 0, 2),
			new Category("World", "The Ten Regions", 3, 12),
			new Category("Explore", "Resonance", 13, 17),
			new Category("Life", "Creatures", 18, 22),
			new Category("Fight", "Combat", 23, 27),
			new Category("Craft", "Crafting", 28, 32),
			new Category("Dragon", "Dragon & Bosses", 33, 43),
			new Category("Guide", "Tips & Guide", 44, 48)
	);
}
