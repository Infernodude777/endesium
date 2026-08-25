package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.menu.LoreBookMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/**
 * Registry for Endesium's container menus.
 */
public final class ModMenus {
	public static final MenuType<LoreBookMenu> LORE_BOOK =
			Registry.register(BuiltInRegistries.MENU, Endesium.id("lore_book"),
					new MenuType<>(LoreBookMenu::new, FeatureFlags.VANILLA_SET));

	private ModMenus() {
	}

	public static void register() {
		Endesium.LOGGER.info("Registered Endesium menus");
	}
}
