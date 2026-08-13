package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
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

	private ModItems() {
	}

	private static Item register(String id, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, Endesium.id(id), item);
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries ->
				entries.accept(FOUNDATION_TEST_ITEM));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries ->
				entries.accept(FOUNDATION_TEST_BLOCK_ITEM));
		Endesium.LOGGER.info("Registered Endesium foundation items");
	}
}
