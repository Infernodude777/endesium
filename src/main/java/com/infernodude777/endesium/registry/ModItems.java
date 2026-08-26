package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.item.ArchiveKeyItem;
import com.infernodude777.endesium.item.ArchiveSigilItem;
import com.infernodude777.endesium.item.AshSifterItem;
import com.infernodude777.endesium.item.BiomeRelicItem;
import com.infernodude777.endesium.item.AshenArmorMaterials;
import com.infernodude777.endesium.item.AshwalkerBootsItem;
import com.infernodude777.endesium.item.ChorusPrunerItem;
import com.infernodude777.endesium.item.CrystalResonatorItem;
import com.infernodude777.endesium.item.EchoCompassItem;
import com.infernodude777.endesium.item.EmberCharmItem;
import com.infernodude777.endesium.item.EndCartographerItem;
import com.infernodude777.endesium.item.HighlandGrapplerItem;
import com.infernodude777.endesium.item.LumenLanternItem;
import com.infernodude777.endesium.item.ProductionResonanceLensItem;
import com.infernodude777.endesium.item.ProductionVoidShardItem;
import com.infernodude777.endesium.item.ResonanceTokenItem;
import com.infernodude777.endesium.item.ResonantWingsItem;
import com.infernodude777.endesium.item.VoidAnchorItem;
import com.infernodude777.endesium.item.VoidArmorMaterials;
import com.infernodude777.endesium.item.VoidArmorItem;
import com.infernodude777.endesium.item.VoidSwordItem;
import com.infernodude777.endesium.item.VoidCompassItem;
import com.infernodude777.endesium.item.VoidDashItem;
import com.infernodude777.endesium.item.VoidFilterItem;
import com.infernodude777.endesium.item.VoidFlareItem;
import com.infernodude777.endesium.item.VoidLanternItem;
import com.infernodude777.endesium.item.VoidPearlItem;
import com.infernodude777.endesium.item.VoidToolMaterial;
import com.infernodude777.endesium.item.WastesCompassItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;

public final class ModItems {
	// --- Core progression ---
	public static final Item VOID_SHARD = register("void_shard", new ProductionVoidShardItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
	public static final Item RESONANCE_LENS = register("resonance_lens", new ProductionResonanceLensItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
	public static final Item RESONANCE_TOKEN = register("resonance_token", new ResonanceTokenItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
	public static final Item ECHO_COMPASS = register("echo_compass", new EchoCompassItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
	public static final Item ARCHIVE_SIGIL = register("archive_sigil", new ArchiveSigilItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
	public static final Item RESONANT_WINGS = register("resonant_wings",
			new ResonantWingsItem(new Item.Properties().stacksTo(1).durability(864).rarity(Rarity.RARE)));
	public static final Item ENDESIUM_GUIDEBOOK = register("endesium_guidebook",
			new com.infernodude777.endesium.item.EndesiumGuidebookItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
	public static final Item PROGRESSION_GUIDE = register("progression_guide",
			new com.infernodude777.endesium.item.ProgressionGuideItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

	// --- Dragon materials ---
	public static final Item RESONANT_DRAGON_SCALE = register("resonant_dragon_scale",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
	public static final Item DRAGONBONE = register("dragonbone",
			new Item(new Item.Properties().stacksTo(16)));
	public static final Item DRAGON_FANG = register("dragon_fang",
			new Item(new Item.Properties().stacksTo(8).rarity(Rarity.RARE)));
	public static final Item DRAGON_HEART = register("dragon_heart",
			new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
	public static final Item ENDER_ESSENCE = register("ender_essence",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
	public static final Item ECHO_SHARD = register("echo_shard",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
	public static final Item VOID_PEARL = register("void_pearl",
			new VoidPearlItem(new Item.Properties().stacksTo(1).durability(238).rarity(Rarity.EPIC)));
	public static final Item ABYSSAL_THREAD = register("abyssal_thread",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
	public static final Item RESONANCE_CORE = register("resonance_core",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
	public static final Item ARCHIVE_FRAGMENT = register("archive_fragment",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

	// --- The ten new biome utility items ---
	public static final Item WASTES_COMPASS = register("wastes_compass",
			new WastesCompassItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
	public static final Item HIGHLAND_GRAPPLER = register("highland_grappler",
			new HighlandGrapplerItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
	public static final Item LUMEN_LANTERN = register("lumen_lantern",
			new LumenLanternItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
	public static final Item VOID_FILTER = register("void_filter",
			new VoidFilterItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
	public static final Item CRYSTAL_RESONATOR = register("crystal_resonator",
			new CrystalResonatorItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
	public static final Item ASH_SIFTER = register("ash_sifter",
			new AshSifterItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
	public static final Item CHORUS_PRUNER = register("chorus_pruner",
			new ChorusPrunerItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
	public static final Item ARCHIVE_KEY = register("archive_key",
			new ArchiveKeyItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
	public static final Item VOID_FLARE = register("void_flare",
			new VoidFlareItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
	public static final Item END_CARTOGRAPHER = register("end_cartographer",
			new EndCartographerItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

	// --- Expanded biome landmark relics ---
	public static final Item SKYGLASS_SHARD = register("skyglass_shard",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
	public static final Item WINDSCAR_WINCH = register("windscar_winch",
			new BiomeRelicItem(new Item.Properties().stacksTo(1).durability(64).rarity(Rarity.RARE),
					"The wind catches you.", MobEffects.SLOW_FALLING, 200, 100, BiomeRelicItem.RelicAction.WIND_LIFT));
	public static final Item MIRE_BELL_CLAPPER = register("mire_bell_clapper",
			new BiomeRelicItem(new Item.Properties().stacksTo(1).durability(32).rarity(Rarity.RARE),
					"The marsh answers with a low, safe note.", MobEffects.WATER_BREATHING, 240, 200, BiomeRelicItem.RelicAction.MARSH_PULSE));
	public static final Item LUMEN_GRAFT = register("lumen_graft",
			new BiomeRelicItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON),
					"A cultivated glow opens the dark.", MobEffects.NIGHT_VISION, 300, 300, BiomeRelicItem.RelicAction.LUMEN_FLASH));
	public static final Item PRISM_SEED = register("prism_seed",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
	public static final Item CROWN_NEEDLE = register("crown_needle",
			new BiomeRelicItem(new Item.Properties().stacksTo(1).durability(48).rarity(Rarity.RARE),
					"The needle leans toward the Crown's next signal.", null, 0, 80, BiomeRelicItem.RelicAction.CROWN_TUNE));
	public static final Item CROWN_SEAL = register("crown_seal",
			new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
	public static final Item NULL_QUILL = register("null_quill",
			new BiomeRelicItem(new Item.Properties().stacksTo(1).durability(16).rarity(Rarity.RARE),
					"A missing line returns for a moment.", MobEffects.NIGHT_VISION, 160, 160, BiomeRelicItem.RelicAction.NULL_RECALL));
	public static final Item THRESHOLD_KEY = register("threshold_key",
			new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

	// --- Void Skirts materials ---
	public static final Item VOID_INGOT = register("void_ingot",
			new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
	public static final Item VOID_NUGGET = register("void_nugget",
			new Item(new Item.Properties().stacksTo(64)));
	public static final Item VOID_GEM = register("void_gem",
			new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
	public static final Item UMBRAL_SHARD = register("umbral_shard",
			new Item(new Item.Properties().stacksTo(64)));
	public static final Item VOID_CORE = register("void_core",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

	// --- Void Skirts tools ---
	public static final Item VOID_SWORD = register("void_sword",
			new VoidSwordItem(VoidToolMaterial.INSTANCE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
					.attributes(SwordItem.createAttributes(VoidToolMaterial.INSTANCE, 3, -2.4F))));
	public static final Item VOID_PICKAXE = register("void_pickaxe",
			new com.infernodude777.endesium.item.VoidPickaxeItem(VoidToolMaterial.INSTANCE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
					.attributes(PickaxeItem.createAttributes(VoidToolMaterial.INSTANCE, 1.0F, -2.8F))));
	public static final Item VOID_AXE = register("void_axe",
			new com.infernodude777.endesium.item.VoidAxeItem(VoidToolMaterial.INSTANCE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
					.attributes(AxeItem.createAttributes(VoidToolMaterial.INSTANCE, 6.0F, -3.0F))));
	public static final Item VOID_SHOVEL = register("void_shovel",
			new ShovelItem(VoidToolMaterial.INSTANCE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
					.attributes(ShovelItem.createAttributes(VoidToolMaterial.INSTANCE, 1.5F, -3.0F))));
	public static final Item VOID_HOE = register("void_hoe",
			new HoeItem(VoidToolMaterial.INSTANCE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
					.attributes(HoeItem.createAttributes(VoidToolMaterial.INSTANCE, -2.0F, -0.5F))));

	// --- Void Skirts armor ---
	public static final Item VOID_HELMET = register("void_helmet",
			new VoidArmorItem(VoidArmorMaterials.VOID, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(620).rarity(Rarity.RARE)));
	public static final Item VOID_CHESTPLATE = register("void_chestplate",
			new VoidArmorItem(VoidArmorMaterials.VOID, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(850).rarity(Rarity.RARE)));
	public static final Item VOID_LEGGINGS = register("void_leggings",
			new VoidArmorItem(VoidArmorMaterials.VOID, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(800).rarity(Rarity.RARE)));
	public static final Item VOID_BOOTS = register("void_boots",
			new VoidArmorItem(VoidArmorMaterials.VOID, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(700).rarity(Rarity.RARE)));

	// --- Void Skirts functional items ---
	public static final Item VOID_COMPASS = register("void_compass",
			new VoidCompassItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
	public static final Item VOID_ANCHOR = register("void_anchor",
			new VoidAnchorItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
	public static final Item VOID_LANTERN = register("void_lantern",
			new VoidLanternItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
	public static final Item VOID_DASH = register("void_dash",
			new VoidDashItem(new Item.Properties().stacksTo(1).durability(250).rarity(Rarity.RARE)));

	// --- Block items ---
	// --- Ashen Expanse materials ---
	public static final Item ASHEN_EMBER = register("ashen_ember",
			new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
	public static final Item MAGMA_CORE = register("magma_core",
			new Item(new Item.Properties().stacksTo(8).rarity(Rarity.RARE)));
	public static final Item ASHWALKER_BOOTS = register("ashwalker_boots",
			new AshwalkerBootsItem(AshenArmorMaterials.ASHEN, ArmorItem.Type.BOOTS,
					new Item.Properties().stacksTo(1).durability(620).rarity(Rarity.RARE)));
	public static final Item EMBER_CHARM = register("ember_charm",
			new EmberCharmItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
	public static final Item ASHEN_CRUST_ITEM = blockItem("ashen_crust", ModBlocks.ASHEN_CRUST);

	public static final Item RESONANT_SLATE_ITEM = blockItem("resonant_slate", ModBlocks.RESONANT_SLATE);
	public static final Item END_GRAY_ITEM = blockItem("end_gray", ModBlocks.END_GRAY);
	public static final Item DORMANT_RESONANT_CRYSTAL_ITEM = blockItem("dormant_resonant_crystal", ModBlocks.DORMANT_RESONANT_CRYSTAL);
	public static final Item RESONANT_MECHANISM_ITEM = blockItem("resonant_mechanism", ModBlocks.RESONANT_MECHANISM);
	public static final Item CHORUS_SPROUT_ITEM = blockItem("chorus_sprout", ModBlocks.CHORUS_SPROUT);
	public static final Item WILD_TENDRIL_ITEM = blockItem("wild_tendril", ModBlocks.WILD_TENDRIL);
	public static final Item RESONANT_BLOOM_ITEM = blockItem("resonant_bloom", ModBlocks.RESONANT_BLOOM);
	public static final Item INSCRIBED_SLATE_ITEM = blockItem("inscribed_slate", ModBlocks.INSCRIBED_SLATE);
	public static final Item RESONANT_PILLAR_ITEM = blockItem("resonant_pillar", ModBlocks.RESONANT_PILLAR);
	public static final Item CRACKED_SPIRE_STONE_ITEM = blockItem("cracked_spire_stone", ModBlocks.CRACKED_SPIRE_STONE);

	public static final Item WASTES_STONE_ITEM = blockItem("wastes_stone", ModBlocks.WASTES_STONE);
	public static final Item WASTES_GRAVEL_ITEM = blockItem("wastes_gravel", ModBlocks.WASTES_GRAVEL);
	public static final Item DUST_REED_ITEM = blockItem("dust_reed", ModBlocks.DUST_REED);
	public static final Item VOID_GRASS_ITEM = blockItem("void_grass", ModBlocks.VOID_GRASS);
	public static final Item ELDER_CHORUS_WOOD_ITEM = blockItem("elder_chorus_wood", ModBlocks.ELDER_CHORUS_WOOD);
	public static final Item ELDER_CHORUS_BARK_ITEM = blockItem("elder_chorus_bark", ModBlocks.ELDER_CHORUS_BARK);
	public static final Item CHORUS_ROOT_ITEM = blockItem("chorus_root", ModBlocks.CHORUS_ROOT);
	public static final Item CHORUS_MOSS_ITEM = blockItem("chorus_moss", ModBlocks.CHORUS_MOSS);
	public static final Item HOLLOW_CHORUS_WOOD_ITEM = blockItem("hollow_chorus_wood", ModBlocks.HOLLOW_CHORUS_WOOD);
	public static final Item HIGHLAND_STONE_ITEM = blockItem("highland_stone", ModBlocks.HIGHLAND_STONE);
	public static final Item HIGHLAND_SLATE_ITEM = blockItem("highland_slate", ModBlocks.HIGHLAND_SLATE);
	public static final Item HIGHLAND_LENSSTONE_ITEM = blockItem("highland_lensstone", ModBlocks.HIGHLAND_LENSSTONE);
	public static final Item WINDSCAR_BRACKET_ITEM = blockItem("windscar_bracket", ModBlocks.WINDSCAR_BRACKET);
	public static final Item VOID_MARSH_SOIL_ITEM = blockItem("void_marsh_soil", ModBlocks.VOID_MARSH_SOIL);
	public static final Item VOID_REED_ITEM = blockItem("void_reed", ModBlocks.VOID_REED);
	public static final Item TIDE_IRON_ITEM = blockItem("tide_iron", ModBlocks.TIDE_IRON);
	public static final Item MIREGLASS_ITEM = blockItem("mireglass", ModBlocks.MIREGLASS);
	public static final Item MARSH_MOSS_ITEM = blockItem("marsh_moss", ModBlocks.MARSH_MOSS);
	public static final Item LUMEN_STONE_ITEM = blockItem("lumen_stone", ModBlocks.LUMEN_STONE);
	public static final Item LUMEN_MOSS_ITEM = blockItem("lumen_moss", ModBlocks.LUMEN_MOSS);
	public static final Item LUMEN_GRAFT_BLOCK_ITEM = blockItem("lumen_graft_block", ModBlocks.LUMEN_GRAFT_BLOCK);
	public static final Item PRISM_CANOPY_BLOCK_ITEM = blockItem("prism_canopy_block", ModBlocks.PRISM_CANOPY_BLOCK);
	public static final Item LUMEN_BLOOM_ITEM = blockItem("lumen_bloom", ModBlocks.LUMEN_BLOOM);
	public static final Item ASH_STONE_ITEM = blockItem("ash_stone", ModBlocks.ASH_STONE);
	public static final Item ASHEN_SOIL_ITEM = blockItem("ashen_soil", ModBlocks.ASHEN_SOIL);
	public static final Item CRYSTAL_SHARD_BLOCK_ITEM = blockItem("crystal_shard_block", ModBlocks.CRYSTAL_SHARD_BLOCK);
	public static final Item CRYSTAL_CLUSTER_ITEM = blockItem("crystal_cluster", ModBlocks.CRYSTAL_CLUSTER);
	public static final Item DARK_CRYSTAL_BLOCK_ITEM = blockItem("dark_crystal_block", ModBlocks.DARK_CRYSTAL_BLOCK);
	public static final Item PALE_CRYSTAL_BLOCK_ITEM = blockItem("pale_crystal_block", ModBlocks.PALE_CRYSTAL_BLOCK);
	public static final Item RESONANT_BASALT_ITEM = blockItem("resonant_basalt", ModBlocks.RESONANT_BASALT);
	public static final Item END_CLAY_ITEM = blockItem("end_clay", ModBlocks.END_CLAY);
	public static final Item VOIDSTONE_ITEM = blockItem("voidstone", ModBlocks.VOIDSTONE);

	// --- Void Skirts block items ---
	public static final Item VOID_SLATE_ITEM = blockItem("void_slate", ModBlocks.VOID_SLATE);
	public static final Item CROWN_NEEDLE_BLOCK_ITEM = blockItem("crown_needle_block", ModBlocks.CROWN_NEEDLE_BLOCK);
	public static final Item CROWN_SEAL_BLOCK_ITEM = blockItem("crown_seal_block", ModBlocks.CROWN_SEAL_BLOCK);
	public static final Item NULL_ARCHIVE_FRAME_ITEM = blockItem("null_archive_frame", ModBlocks.NULL_ARCHIVE_FRAME);
	public static final Item THRESHOLD_CORE_BLOCK_ITEM = blockItem("threshold_core_block", ModBlocks.THRESHOLD_CORE_BLOCK);
	public static final Item VOID_GRAVEL_ITEM = blockItem("void_gravel", ModBlocks.VOID_GRAVEL);
	public static final Item VOID_SOIL_ITEM = blockItem("void_soil", ModBlocks.VOID_SOIL);
	public static final Item VOID_GLASS_ITEM = blockItem("void_glass", ModBlocks.VOID_GLASS);
	public static final Item VOID_BRICK_ITEM = blockItem("void_brick", ModBlocks.VOID_BRICK);
	public static final Item VOID_BRICK_SLAB_ITEM = blockItem("void_brick_slab", ModBlocks.VOID_BRICK_SLAB);
	public static final Item VOID_BRICK_STAIRS_ITEM = blockItem("void_brick_stairs", ModBlocks.VOID_BRICK_STAIRS);
	public static final Item VOID_BRICK_WALL_ITEM = blockItem("void_brick_wall", ModBlocks.VOID_BRICK_WALL);
	public static final Item VOID_LAMP_ITEM = blockItem("void_lamp", ModBlocks.VOID_LAMP);
	public static final Item VOID_CRYSTAL_ITEM = blockItem("void_crystal", ModBlocks.VOID_CRYSTAL);
	public static final Item UMBRAL_GRASS_ITEM = blockItem("umbral_grass", ModBlocks.UMBRAL_GRASS);
	public static final Item VOID_FERN_ITEM = blockItem("void_fern", ModBlocks.VOID_FERN);
	public static final Item VOID_WEAVE_ITEM = blockItem("void_weave", ModBlocks.VOID_WEAVE);
	public static final Item VOID_SPIRE_ITEM = blockItem("void_spire", ModBlocks.VOID_SPIRE);
	public static final Item UMBRAL_STONE_ITEM = blockItem("umbral_stone", ModBlocks.UMBRAL_STONE);
	public static final Item VOID_ORE_ITEM = blockItem("void_ore", ModBlocks.VOID_ORE);

	public static final Item VOID_STALKER_SPAWN_EGG = register("void_stalker_spawn_egg",
			new SpawnEggItem(ModEntities.VOID_STALKER, 0x26232B, 0x7EA7A6, new Item.Properties()));

	// --- Ecology mob drops ---
	public static final Item DUST_CHITIN = register("dust_chitin", new Item(new Item.Properties().stacksTo(64)));
	public static final Item WASTES_SEED_POD = register("wastes_seed_pod", new Item(new Item.Properties().stacksTo(64)));
	public static final Item STALKER_TENDRIL = register("stalker_tendril", new Item(new Item.Properties().stacksTo(64)));
	public static final Item CHORUS_EYE = register("chorus_eye", new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
	public static final Item VOID_MEMBRANE = register("void_membrane", new Item(new Item.Properties().stacksTo(64)));
	public static final Item HIGHLAND_FEATHER = register("highland_feather", new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
	public static final Item VOID_SAP = register("void_sap", new Item(new Item.Properties().stacksTo(64)));
	public static final Item MARSH_TENDRIL = register("marsh_tendril", new Item(new Item.Properties().stacksTo(64)));
	public static final Item CRAWLER_EYE = register("crawler_eye", new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
	public static final Item LUMEN_DUST = register("lumen_dust", new Item(new Item.Properties().stacksTo(64)));
	public static final Item LUMEN_WING = register("lumen_wing", new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
	public static final Item WRAITH_ASH = register("wraith_ash", new Item(new Item.Properties().stacksTo(64)));
	public static final Item ASH_CORE = register("ash_core", new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
	public static final Item BURROWER_PLATE = register("burrower_plate", new Item(new Item.Properties().stacksTo(64)));
	public static final Item CRYSTAL_CORE = register("crystal_core", new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
	public static final Item CRYSTAL_FANG = register("crystal_fang", new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
	public static final Item NULL_FRAGMENT = register("null_fragment", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

	// --- Ecology spawn eggs ---
	public static final Item DUST_CRAWLER_SPAWN_EGG = register("dust_crawler_spawn_egg", new SpawnEggItem(ModEntities.DUST_CRAWLER, 0x8B7D6B, 0x2E2A33, new Item.Properties()));
	public static final Item CHORUS_STALKER_SPAWN_EGG = register("chorus_stalker_spawn_egg", new SpawnEggItem(ModEntities.CHORUS_STALKER, 0x6B5B8A, 0x1F1B2A, new Item.Properties()));
	public static final Item VOID_RAY_SPAWN_EGG = register("void_ray_spawn_egg", new SpawnEggItem(ModEntities.VOID_RAY, 0x2A2F3A, 0x7EA7A6, new Item.Properties()));
	public static final Item MARSH_CRAWLER_SPAWN_EGG = register("marsh_crawler_spawn_egg", new SpawnEggItem(ModEntities.MARSH_CRAWLER, 0x3A4A4A, 0x1F5F5F, new Item.Properties()));
	public static final Item LUMEN_MOTH_SPAWN_EGG = register("lumen_moth_spawn_egg", new SpawnEggItem(ModEntities.LUMEN_MOTH, 0x9FE7E7, 0x4A5A7A, new Item.Properties()));
	public static final Item ASH_WRAITH_SPAWN_EGG = register("ash_wraith_spawn_egg", new SpawnEggItem(ModEntities.ASH_WRAITH, 0x3A3A3A, 0x8A8A8A, new Item.Properties()));
	public static final Item CRYSTAL_BURROWER_SPAWN_EGG = register("crystal_burrower_spawn_egg", new SpawnEggItem(ModEntities.CRYSTAL_BURROWER, 0x8A6BBF, 0x3A2A5A, new Item.Properties()));
	public static final Item NULLWALKER_SPAWN_EGG = register("nullwalker_spawn_egg", new SpawnEggItem(ModEntities.NULLWALKER, 0x0A0A0A, 0xC7C7C7, new Item.Properties()));
	public static final Item VOID_WISP_SPAWN_EGG = register("void_wisp_spawn_egg", new SpawnEggItem(ModEntities.VOID_WISP, 0x1A2340, 0x9FE7E7, new Item.Properties()));
	public static final Item CROWN_SENTINEL_SPAWN_EGG = register("crown_sentinel_spawn_egg", new SpawnEggItem(ModEntities.CROWN_SENTINEL, 0x2A2A3A, 0xC9A227, new Item.Properties()));
	public static final Item END_WARDEN_SPAWN_EGG = register("end_warden_spawn_egg", new SpawnEggItem(ModEntities.END_WARDEN, 0x1F2B3A, 0x7EA7A6, new Item.Properties()));
	public static final Item END_GOLEM_SPAWN_EGG = register("end_golem_spawn_egg", new SpawnEggItem(ModEntities.END_GOLEM, 0x14101F, 0xB08AD2, new Item.Properties()));

	// --- Boss rewards ---
	public static final Item WARDEN_SIGIL = register("warden_sigil",
			new com.infernodude777.endesium.item.WardenSigilItem(
					new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC)));
	public static final Item GOLEM_CORE = register("golem_core",
			new com.infernodude777.endesium.item.GolemCoreItem(
					new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC)));
	public static final Item GOLEM_EFFIGY = register("golem_effigy",
			new com.infernodude777.endesium.item.GolemEffigyItem(
					new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));
	private ModItems() { }

	private static Item register(String id, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, Endesium.id(id), item);
	}

	private static Item blockItem(String id, Block block) {
		return register(id, new BlockItem(block, new Item.Properties()));
	}

	public static void register() {
		// The Endesium tab is the single canonical listing for all content.
		// Vanilla-tab duplication was removed so Ingredients/Tools/Combat/
		// Building Blocks are not flooded with Endesium entries (B-13).
		// Spawn eggs stay in the vanilla Spawn Eggs tab by common convention.
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
			entries.accept(VOID_STALKER_SPAWN_EGG);
			entries.accept(DUST_CRAWLER_SPAWN_EGG);
			entries.accept(CHORUS_STALKER_SPAWN_EGG);
			entries.accept(VOID_RAY_SPAWN_EGG);
			entries.accept(MARSH_CRAWLER_SPAWN_EGG);
			entries.accept(LUMEN_MOTH_SPAWN_EGG);
			entries.accept(ASH_WRAITH_SPAWN_EGG);
			entries.accept(CRYSTAL_BURROWER_SPAWN_EGG);
			entries.accept(NULLWALKER_SPAWN_EGG);
			entries.accept(VOID_WISP_SPAWN_EGG);
			entries.accept(CROWN_SENTINEL_SPAWN_EGG);
			entries.accept(END_WARDEN_SPAWN_EGG);
			entries.accept(END_GOLEM_SPAWN_EGG);
		});
		Endesium.LOGGER.info("Registered Endesium production items");
	}
}
