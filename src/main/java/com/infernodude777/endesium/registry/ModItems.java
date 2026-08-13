package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.item.ResonanceLensItem;
import com.infernodude777.endesium.item.VoidShardItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;

public final class ModItems {
	public static final Item FOUNDATION_TEST_ITEM = register(
			"foundation_test_item",
			new Item(new Item.Properties())
	);
	public static final Item FOUNDATION_TEST_BLOCK_ITEM = register(
			"foundation_test_block",
			new BlockItem(ModBlocks.FOUNDATION_TEST_BLOCK, new Item.Properties())
	);
	public static final Item VOID_SHARD = register(
			"void_shard",
			new VoidShardItem(new Item.Properties())
	);
	public static final Item RESONANCE_LENS = register(
			"resonance_lens",
			new ResonanceLensItem(new Item.Properties().stacksTo(1))
	);
	public static final Item END_RUIN_BLOCK_ITEM = register(
			"end_ruin_block",
			new BlockItem(ModBlocks.END_RUIN_BLOCK, new Item.Properties())
	);

	private ModItems() {
	}

	private static Item register(String id, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, Endesium.id(id), item);
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			entries.accept(FOUNDATION_TEST_ITEM);
			entries.accept(VOID_SHARD);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries ->
				entries.accept(RESONANCE_LENS));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.accept(FOUNDATION_TEST_BLOCK_ITEM);
			entries.accept(END_RUIN_BLOCK_ITEM);
		});
		Endesium.LOGGER.info("Registered Endesium items");
	}
}
