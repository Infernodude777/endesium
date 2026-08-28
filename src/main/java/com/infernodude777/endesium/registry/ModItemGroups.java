package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * The dedicated Endesium creative inventory tab. It gathers every Endesium
 * item and block in one place so players can find the mod's content without
 * hunting through the vanilla tabs.
 */
public final class ModItemGroups {
	public static final CreativeModeTab ENDESIUM = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			Endesium.id("endesium"),
			FabricItemGroup.builder()
					.title(Component.translatable("itemGroup.endesium"))
					.icon(() -> new ItemStack(ModItems.VOID_SHARD))
					.displayItems((parameters, output) -> {
						output.accept(ModItems.VOID_SHARD);
						output.accept(ModItems.PROGRESSION_GUIDE);
						output.accept(ModEndgear.LUMINOUS_HELMET);
						output.accept(ModEndgear.LUMINOUS_CHESTPLATE);
						output.accept(ModEndgear.LUMINOUS_LEGGINGS);
						output.accept(ModEndgear.LUMINOUS_BOOTS);
						output.accept(ModEndgear.LUMINOUS_SWORD);
						output.accept(ModEndgear.LUMINOUS_PICKAXE);
						output.accept(ModEndgear.LUMINOUS_AXE);
						output.accept(ModEndgear.LUMINOUS_SHOVEL);
						output.accept(ModEndgear.LUMINOUS_HOE);
						output.accept(ModEndgear.ASH_HELMET);
						output.accept(ModEndgear.ASH_CHESTPLATE);
						output.accept(ModEndgear.ASH_LEGGINGS);
						output.accept(ModItems.ASHWALKER_BOOTS);
						output.accept(ModEndgear.ASH_SWORD);
						output.accept(ModEndgear.ASH_PICKAXE);
						output.accept(ModEndgear.ASH_AXE);
						output.accept(ModEndgear.ASH_SHOVEL);
						output.accept(ModEndgear.ASH_HOE);
						output.accept(ModEndgear.NULL_HELMET);
						output.accept(ModEndgear.NULL_CHESTPLATE);
						output.accept(ModEndgear.NULL_LEGGINGS);
						output.accept(ModEndgear.NULL_BOOTS);
						output.accept(ModEndgear.NULL_SWORD);
						output.accept(ModEndgear.NULL_PICKAXE);
						output.accept(ModEndgear.NULL_AXE);
						output.accept(ModEndgear.NULL_SHOVEL);
						output.accept(ModEndgear.NULL_HOE);
						output.accept(ModItems.SKYGLASS_SHARD);
						output.accept(ModItems.WINDSCAR_WINCH);
						output.accept(ModItems.MIRE_BELL_CLAPPER);
						output.accept(ModItems.LUMEN_GRAFT);
						output.accept(ModItems.PRISM_SEED);
						output.accept(ModItems.CROWN_NEEDLE);
						output.accept(ModItems.CROWN_SEAL);
						output.accept(ModItems.NULL_QUILL);
						output.accept(ModItems.THRESHOLD_KEY);
						output.accept(ModItems.SKY_JELLY_SPAWN_EGG);
						output.accept(ModItems.GALEFIN_SPAWN_EGG);
						output.accept(ModItems.DEEP_LURKER_SPAWN_EGG);
						output.accept(ModEndgear.DRAGON_WINGS);
						output.accept(ModItems.RESONANCE_LENS);
						output.accept(ModItems.RESONANCE_TOKEN);
						output.accept(ModItems.ECHO_COMPASS);
						output.accept(ModItems.ARCHIVE_SIGIL);
						output.accept(ModItems.RESONANT_WINGS);
						output.accept(ModItems.RESONANT_DRAGON_SCALE);
						output.accept(ModItems.DRAGONBONE);
						output.accept(ModItems.DRAGON_FANG);
						output.accept(ModItems.DRAGON_HEART);
						output.accept(ModItems.ENDER_ESSENCE);
						output.accept(ModItems.ECHO_SHARD);
						output.accept(ModItems.VOID_PEARL);
						output.accept(ModItems.ABYSSAL_THREAD);
						output.accept(ModItems.RESONANCE_CORE);
						output.accept(ModItems.ARCHIVE_FRAGMENT);
						output.accept(ModItems.WASTES_COMPASS);
						output.accept(ModItems.HIGHLAND_GRAPPLER);
						output.accept(ModItems.LUMEN_LANTERN);
						output.accept(ModItems.VOID_FILTER);
						output.accept(ModItems.CRYSTAL_RESONATOR);
						output.accept(ModItems.ASH_SIFTER);
						output.accept(ModItems.CHORUS_PRUNER);
						output.accept(ModItems.ARCHIVE_KEY);
						output.accept(ModItems.VOID_FLARE);
						output.accept(ModItems.END_CARTOGRAPHER);
						output.accept(ModItems.VOID_INGOT);
						output.accept(ModItems.VOID_NUGGET);
						output.accept(ModItems.VOID_GEM);
						output.accept(ModItems.UMBRAL_SHARD);
						output.accept(ModItems.VOID_CORE);
						output.accept(ModItems.VOID_SWORD);
						output.accept(ModItems.VOID_PICKAXE);
						output.accept(ModItems.VOID_AXE);
						output.accept(ModItems.VOID_SHOVEL);
						output.accept(ModItems.VOID_HOE);
						output.accept(ModItems.VOID_HELMET);
						output.accept(ModItems.VOID_CHESTPLATE);
						output.accept(ModItems.VOID_LEGGINGS);
						output.accept(ModItems.VOID_BOOTS);
						output.accept(ModItems.VOID_COMPASS);
						output.accept(ModItems.VOID_ANCHOR);
						output.accept(ModItems.VOID_LANTERN);
						output.accept(ModItems.VOID_DASH);
						output.accept(ModItems.VOID_SLATE_ITEM);
						output.accept(ModItems.CROWN_NEEDLE_BLOCK_ITEM);
						output.accept(ModItems.CROWN_SEAL_BLOCK_ITEM);
						output.accept(ModItems.NULL_ARCHIVE_FRAME_ITEM);
						output.accept(ModItems.THRESHOLD_CORE_BLOCK_ITEM);
						output.accept(ModItems.VOID_GRAVEL_ITEM);
						output.accept(ModItems.VOID_SOIL_ITEM);
						output.accept(ModItems.VOID_GLASS_ITEM);
						output.accept(ModItems.VOID_BRICK_ITEM);
						output.accept(ModItems.VOID_BRICK_SLAB_ITEM);
						output.accept(ModItems.VOID_BRICK_STAIRS_ITEM);
						output.accept(ModItems.VOID_BRICK_WALL_ITEM);
						output.accept(ModItems.VOID_LAMP_ITEM);
						output.accept(ModItems.VOID_CRYSTAL_ITEM);
						output.accept(ModItems.UMBRAL_GRASS_ITEM);
						output.accept(ModItems.VOID_FERN_ITEM);
						output.accept(ModItems.VOID_WEAVE_ITEM);
						output.accept(ModItems.VOID_SPIRE_ITEM);
						output.accept(ModItems.UMBRAL_STONE_ITEM);
						output.accept(ModItems.VOID_ORE_ITEM);
						output.accept(ModItems.ASHEN_EMBER);
						output.accept(ModItems.MAGMA_CORE);
						output.accept(ModItems.EMBER_CHARM);
						output.accept(ModItems.ASHEN_CRUST_ITEM);
						output.accept(ModItems.RESONANT_SLATE_ITEM);
						output.accept(ModItems.END_GRAY_ITEM);
						output.accept(ModItems.DORMANT_RESONANT_CRYSTAL_ITEM);
						output.accept(ModItems.RESONANT_MECHANISM_ITEM);
						output.accept(ModItems.CHORUS_SPROUT_ITEM);
						output.accept(ModItems.WILD_TENDRIL_ITEM);
						output.accept(ModItems.RESONANT_BLOOM_ITEM);
						output.accept(ModItems.INSCRIBED_SLATE_ITEM);
						output.accept(ModItems.RESONANT_PILLAR_ITEM);
						output.accept(ModItems.CRACKED_SPIRE_STONE_ITEM);
						output.accept(ModItems.WASTES_STONE_ITEM);
						output.accept(ModItems.WASTES_GRAVEL_ITEM);
						output.accept(ModItems.DUST_REED_ITEM);
						output.accept(ModItems.VOID_GRASS_ITEM);
						output.accept(ModItems.ELDER_CHORUS_WOOD_ITEM);
						output.accept(ModItems.ELDER_CHORUS_BARK_ITEM);
						output.accept(ModItems.CHORUS_ROOT_ITEM);
						output.accept(ModItems.CHORUS_MOSS_ITEM);
						output.accept(ModItems.HOLLOW_CHORUS_WOOD_ITEM);
						output.accept(ModItems.HIGHLAND_STONE_ITEM);
						output.accept(ModItems.HIGHLAND_SLATE_ITEM);
						output.accept(ModItems.HIGHLAND_LENSSTONE_ITEM);
						output.accept(ModItems.WINDSCAR_BRACKET_ITEM);
						output.accept(ModItems.VOID_MARSH_SOIL_ITEM);
						output.accept(ModItems.VOID_REED_ITEM);
						output.accept(ModItems.TIDE_IRON_ITEM);
						output.accept(ModItems.MIREGLASS_ITEM);
						output.accept(ModItems.MARSH_MOSS_ITEM);
						output.accept(ModItems.LUMEN_STONE_ITEM);
						output.accept(ModItems.LUMEN_MOSS_ITEM);
						output.accept(ModItems.LUMEN_GRAFT_BLOCK_ITEM);
						output.accept(ModItems.PRISM_CANOPY_BLOCK_ITEM);
						output.accept(ModItems.LUMEN_BLOOM_ITEM);
						output.accept(ModItems.ASH_STONE_ITEM);
						output.accept(ModItems.ASHEN_SOIL_ITEM);
						output.accept(ModItems.CRYSTAL_SHARD_BLOCK_ITEM);
						output.accept(ModItems.CRYSTAL_CLUSTER_ITEM);
						output.accept(ModItems.DARK_CRYSTAL_BLOCK_ITEM);
						output.accept(ModItems.PALE_CRYSTAL_BLOCK_ITEM);
						output.accept(ModItems.RESONANT_BASALT_ITEM);
						output.accept(ModItems.END_CLAY_ITEM);
						output.accept(ModItems.VOIDSTONE_ITEM);
						output.accept(ModItems.VOID_STALKER_SPAWN_EGG);
						output.accept(ModItems.DUST_CRAWLER_SPAWN_EGG);
						output.accept(ModItems.CHORUS_STALKER_SPAWN_EGG);
						output.accept(ModItems.VOID_RAY_SPAWN_EGG);
						output.accept(ModItems.MARSH_CRAWLER_SPAWN_EGG);
						output.accept(ModItems.LUMEN_MOTH_SPAWN_EGG);
						output.accept(ModItems.ASH_WRAITH_SPAWN_EGG);
						output.accept(ModItems.CRYSTAL_BURROWER_SPAWN_EGG);
						output.accept(ModItems.NULLWALKER_SPAWN_EGG);
						output.accept(ModItems.VOID_WISP_SPAWN_EGG);
						output.accept(ModItems.CROWN_SENTINEL_SPAWN_EGG);
						output.accept(ModItems.END_WARDEN_SPAWN_EGG);
						output.accept(ModItems.END_GOLEM_SPAWN_EGG);
						output.accept(ModItems.WARDEN_SIGIL);
						output.accept(ModItems.GOLEM_CORE);
						output.accept(ModItems.GOLEM_EFFIGY);
						output.accept(ModItems.DUST_CHITIN);
						output.accept(ModItems.WASTES_SEED_POD);
						output.accept(ModItems.STALKER_TENDRIL);
						output.accept(ModItems.CHORUS_EYE);
						output.accept(ModItems.VOID_MEMBRANE);
						output.accept(ModItems.HIGHLAND_FEATHER);
						output.accept(ModItems.VOID_SAP);
						output.accept(ModItems.MARSH_TENDRIL);
						output.accept(ModItems.CRAWLER_EYE);
						output.accept(ModItems.LUMEN_DUST);
						output.accept(ModItems.LUMEN_WING);
						output.accept(ModItems.WRAITH_ASH);
						output.accept(ModItems.ASH_CORE);
						output.accept(ModItems.BURROWER_PLATE);
						output.accept(ModItems.CRYSTAL_CORE);
						output.accept(ModItems.CRYSTAL_FANG);
						output.accept(ModItems.NULL_FRAGMENT);
					})
					.build()
	);

	private ModItemGroups() {
	}

	public static void register() {
		Endesium.LOGGER.info("Registered the Endesium creative inventory tab");
	}
}
