package com.infernodude777.endesium.client.screen;

import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * The written archives behind the lore book. Every entry answers the three
 * questions the book is asked: what is it, how do you get it, and what is it
 * for. Items without an entry fall back to a plain identification.
 */
public final class EndesiumLore {
	private static final Map<Item, String[]> LORE = new HashMap<>();

	private EndesiumLore() {
	}

	private static void add(Item item, String... lines) {
		LORE.put(item, lines);
	}

	static {
		add(ModItems.VOID_SHARD,
				"A splinter of void glass that hums against your palm.",
				"Obtain: mine Void Ore/voidstone, kill Void Stalkers, loot ruins or wake mechanisms.",
				"Used in: Resonance Lens, Echo Compass, Void Gem, and Void Ingot recipes.");
		add(ModItems.VOID_INGOT,
				"Void glass, forged and reforged until it holds an edge.",
				"Obtain: smelt Void Ore, Voidstone, Umbral Shards, or Void Shards in a furnace.",
				"Used in: all Void tools and armor, plus Void utility recipes; repairs Void gear.");
		add(ModItems.VOID_GEM,
				"A cut stone that drinks light and gives it back colder.",
				"Obtain: craft 9 Void Shards together, or mine crystal deposits in the Void Skirts.",
				"Used in: Void Sword, Void Dash, Void Flare, Void Filter, Void Compass, and Void Core recipes.");
		add(ModItems.VOID_CORE,
				"The beating heart of voidstone machinery.",
				"Obtain: craft from a Void Gem, Ender Essence, and Void Ingots; rare deep-ruin loot.",
				"Used in: Resonant Wings, Void tools, Void armor, anchors, and Golem Effigy recipes.");
		add(ModItems.VOID_ANCHOR,
				"A thrown anchor that drags you across the void to it.",
				"Forged from void ingots and abyssal thread.",
				"Throw it, hold on, and fly.");
		add(ModItems.VOID_DASH,
				"A burst of condensed void in the palm of your hand.",
				"Cut from void gem and ender pearl.",
				"Use to blink forward through the air.");
		add(ModItems.VOID_FLARE,
				"A star in a bottle, angry and bright.",
				"Bound from a void gem and a fire charge.",
				"Releases a blinding flare that burns back the dark.");
		add(ModItems.VOID_FILTER,
				"A mask lens that decides what air you breathe.",
				"Cut from void gem over a glass pane.",
				"Wear it to breathe easy where the void leaks.");
		add(ModItems.VOID_PEARL,
				"A pearl that remembers where the void ends.",
				"Traded up from ender pearls and void shards.",
				"Throw it to pull yourself along its arc.");
		add(ModItems.VOID_COMPASS,
				"Points not north, but down - toward the deep void.",
				"Void gem over four void ingots.",
				"Needles toward the nearest deep-void fissure.");
		add(ModItems.WASTES_COMPASS,
				"A dust-caked compass that remembers the wastes.",
				"Wastes stone ring around an echo shard.",
				"Needles toward the End Wastes heartland.");
		add(ModItems.ECHO_COMPASS,
				"Listens for structures instead of places.",
				"Obtain: craft with a Resonance Token and 4 Void Shards; alternate recipe uses an Echo Shard.",
				"Used for: locating the strongest loaded resonance source and guiding exploration.");
		add(ModItems.ECHO_SHARD,
				"A shard that repeats the last sound it heard, quietly.",
				"Dug from the deepest end stone, or bartered from nullwalkers.",
				"Crafts the Echo Compass and archive keys.");
		add(ModItems.WARDEN_SIGIL,
				"A seal torn from a warden's chest, still warm.",
				"Claimed from the End Warden's hoard.",
				"Marks attunement and opens the warden's vaults.");
		add(ModItems.ARCHIVE_KEY,
				"Cold brass. The Null Archive has been waiting for it.",
				"Assembled from echo shards and an archive fragment.",
				"Opens the Null Archive's sealed rotunda.");
		add(ModItems.ARCHIVE_SIGIL,
				"A mark of ownership signed by the archive itself.",
				"Wake the archive's mechanism with its own key.",
				"Proof you read what was written for you.");
		add(ModItems.ARCHIVE_FRAGMENT,
				"A page that resists being read twice the same way.",
				"Fallen among the ruins around the Null Archive.",
				"Binds archive keys together.");
		add(ModItems.GOLEM_CORE,
				"The patient heart of an End Golem.",
				"Dropped by golems guarding the highlands.",
				"Ignites the Golem Effigy.");
		add(ModItems.GOLEM_EFFIGY,
				"A little stone idol, warm to the touch.",
				"Carved from highland stone around a golem core.",
				"Ignite it to summon a golem to your side.");
		add(ModItems.HIGHLAND_GRAPPLER,
				"A hook, a chain, and a lot of confidence.",
				"Forged from highland slate and a windscar winch.",
				"Fire it at cliffs to pull yourself up.");
		add(ModItems.HIGHLAND_FEATHER,
				"A wind-worn feather from the shattered peaks.",
				"Dropped in the Shattered Highlands.",
				"Lightens the Grappler's pull.");
		add(ModItems.LUMEN_LANTERN,
				"A lantern that burns cold grove-light.",
				"Lumen graft over a lantern frame.",
				"Places a light that pushes back the umbral dark.");
		add(ModItems.LUMEN_DUST,
				"Grove-light ground into powder.",
				"Crushed from lumen blooms and moths' leavings.",
				"Crafts lanterns and grove glass.");
		add(ModItems.CRYSTAL_RESONATOR,
				"Hums when a crystal heart is nearby.",
				"Crystal fang over shard glass.",
				"Points to the Sunken Geode's heart.");
		add(ModItems.CRYSTAL_FANG,
				"A shard of the geode's shell, sharp enough to cut stone.",
				"Mined from the Crystal Barrens' spires.",
				"Crafts the resonator and crystal tools.");
		add(ModItems.CRYSTAL_CORE,
				"The geode's pulse, made solid.",
				"Dropped by crystal burrowers guarding the heart.",
				"Wakes the geode's mechanism.");
		add(ModItems.CHORUS_PRUNER,
				"A tool that convinces chorus growth to behave.",
				"Chorus eye over elder bark.",
				"Harvests wilds growth cleanly.");
		add(ModItems.CHORUS_EYE,
				"Sees the wilds the way the wilds see itself.",
				"Dropped by chorus stalkers.",
				"Crafts the pruner and wilds gear.");
		add(ModItems.ASH_SIFTER,
				"Pan for embers the way you pan for gold.",
				"Ash stone sieve over a stick.",
				"Sift ash piles for buried embers.");
		add(ModItems.ASH_CORE,
				"Still hot. Always still hot.",
				"Dropped by ash wraiths.",
				"Crafts ember charms and caldera keys.");
		add(ModItems.ASHEN_EMBER,
				"An ember that refused to go out.",
				"Shifted from ashen crust with the sifter.",
				"Feeds ember charms.");
		add(ModItems.EMBER_CHARM,
				"Wearable stubbornness against the heat.",
				"Ember and ash core in a basalt setting.",
				"Wear it to shrug off volcanic heat.");
		add(ModItems.DUST_CHITIN,
				"A plate of living dust, hardened.",
				"Dropped by dust crawlers.",
				"Crafts crawler-scale plating.");
		add(ModItems.CRAWLER_EYE,
				"It watches back.",
				"Dropped by dust crawlers.",
				"Crafts crawler gear and lenses.");
		add(ModItems.BURROWER_PLATE,
				"Armor that grew instead of being forged.",
				"Dropped by crystal burrowers.",
				"Crafts crystal-plated equipment.");
		add(ModItems.STALKER_TENDRIL,
				"Still twitching. They always are.",
				"Dropped by void stalkers.",
				"Crafts stalker-thread gear.");
		add(ModItems.NULL_FRAGMENT,
				"A piece of something the archive deleted.",
				"Left behind by nullwalkers.",
				"Crafts null-frame mechanisms.");
		add(ModItems.NULL_QUILL,
				"Writes by itself. You just hold it.",
				"Dropped by nullwalkers in the Umbral Reach.",
				"Crafts archive records.");
		add(ModItems.THRESHOLD_KEY,
				"Opens the door that isn't a door.",
				"Bound from void weave and threshold cores.",
				"Opens the Umbral thresholds.");
		add(ModItems.PRISM_SEED,
				"Plant it and watch the light bend.",
				"Dropped in the Luminous Groves.",
				"Grows prismatic canopy.");
		add(ModItems.MARSH_TENDRIL,
				"Pulls water out of the air and lets it go again.",
				"Grows in the Void Marshes' shallows.",
				"Crafts marsh gear and reed lamps.");
		add(ModItems.MIRE_BELL_CLAPPER,
				"The tongue of the drowned bell.",
				"Sunken beside the mire cairns.",
				"Rings the Mire Bell properly.");
		add(ModItems.SKYGLASS_SHARD,
				"A window that fell out of the sky.",
				"Found among the shattered highlands.",
				"Crafts windscar tools.");
		add(ModItems.WINDSCAR_WINCH,
				"Cranked by the gale itself.",
				"Highland slate around a windscar bracket.",
				"Powers the grappler.");
		add(ModItems.DRAGONBONE,
				"The only part of the dragon that forgives.",
				"Dropped when the dragon falls.",
				"Crafts dragonbone equipment.");
		add(ModItems.DRAGON_FANG,
				"Keep it away from your thumb.",
				"Dropped when the dragon falls.",
				"Crafts dragon-fang weapons.");
		add(ModItems.DRAGON_HEART,
				"It beats when you aren't looking.",
				"The dragon's own heart, claimed at the kill.",
				"The rarest crafting core in the End.");
		add(ModItems.RESONANT_DRAGON_SCALE,
				"A scale that hums the dragon's last note.",
				"Dropped when the dragon falls.",
				"Upgrades the Resonant Wings.");
		add(ModItems.RESONANT_WINGS,
				"The dragon's flight, given back.",
				"Assembled from resonant scales and a void core.",
				"Wear them and glide on command; elytra-compatible.");
		add(ModItems.ASHWALKER_BOOTS,
				"The lava is a floor, if you refuse to believe otherwise.",
				"Forged for the Ashen Expanse's caldera walkers.",
				"Wear them to stand and walk on lava, unburned.");
		add(ModItems.VOID_SWORD,
				"An edge that cuts the argument short.",
				"Void ingots over a void gem.",
				"A heavy void-forged blade.");
		add(ModItems.VOID_AXE,
				"Fells trees and everything else.",
				"Void ingots over a void core.",
				"A heavy void-forged axe.");
		add(ModItems.VOID_PICKAXE,
				"End stone argues; the pickaxe wins.",
				"Void ingots over a void core.",
				"Mines voidstone and deep geology.");
		add(ModItems.VOID_SHOVEL,
				"Digs the void like it owes you money.",
				"Void ingots over a void core.",
				"Moves void gravel and soils fast.");
		add(ModItems.VOID_HOE,
				"Even the void has farmland.",
				"Void ingots over a void core.",
				"Tills end soils for Endesium crops.");
		add(ModItems.VOID_HELMET,
				"Sealed against the dark.",
				"Void ingots, helmet pattern.",
				"Void armor, head slot.");
		add(ModItems.VOID_CHESTPLATE,
				"Forged to be the last thing that breaks.",
				"Void ingots, chest pattern.",
				"Void armor, body slot.");
		add(ModItems.VOID_LEGGINGS,
				"Weightless and load-bearing.",
				"Void ingots, leggings pattern.",
				"Void armor, legs slot.");
		add(ModItems.VOID_BOOTS,
				"Steps that leave no echo.",
				"Void ingots, boots pattern.",
				"Void armor, feet slot.");
		add(ModItems.ABYSSAL_THREAD,
				"Spun in the dark by something patient.",
				"Drawn from void stalkers and deep ruins.",
				"Binds anchors and deep gear.");
		add(ModItems.ENDER_ESSENCE,
				"The End, distilled to a single drop.",
				"Pressed from ender pearls and chorus fruit.",
				"Binds cores, keys, and sigils.");
		add(ModItems.RESONANCE_CORE,
				"A machine that agrees with itself very loudly.",
				"Resonant slate around a dormant crystal.",
				"Powers resonance mechanisms.");
		add(ModItems.RESONANCE_LENS,
				"Look through it and the End shows you its seams.",
				"Ground from resonance cores and skyglass.",
				"Reads resonance and finds mechanisms.");
		add(ModItems.RESONANCE_TOKEN,
				"A promise the mechanism keeps.",
				"Obtain: wake a Resonant Mechanism with the Resonance Lens; craft the alternate token recipe.",
				"Used in: Echo Compass recipes and deep progression rewards.");
		add(ModItems.PROGRESSION_GUIDE,
				"You are holding the answer key.",
				"Obtain: craft with a book, 2 paper, and an Ender Pearl; the first Dragon kill also grants one.",
				"Used for: item lookup, every known acquisition route, every known recipe, and item pairings.");
		add(Items.ENDER_PEARL,
				"Vanilla, but the End remembers what you did with it.",
				"Obtain: kill Endermen, barter, or find it in vanilla loot.",
				"Used in: Ender Essence, Progression Guide, Eyes of Ender, Void Pearl, and many Endesium recipes.");
		add(Items.END_CRYSTAL,
				"You know what these are for. So does the dragon.",
				"Ghast tears and eyes of ender over glass.",
				"Respawns the dragon - and its fight.");
	}

	/** The lore lines for an item, or a fallback identification. */
	public static String[] forItem(Item item) {
		String[] lines = LORE.get(item);
		if (lines != null) {
			return lines;
		}
		return new String[]{
				"The archives hold no dedicated record of this item.",
				"Check the field guide pages for the broader path."
		};
	}

	/** Whether the archives have a dedicated entry. */
	public static boolean hasEntry(Item item) {
		return LORE.containsKey(item);
	}
}
