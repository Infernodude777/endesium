package com.infernodude777.endesium.state;

import com.infernodude777.endesium.Endesium;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerPlayer;

/**
 * Recipe book visibility: vanilla only shows a recipe in the crafting table
 * book once its unlock advancement has been granted, and Endesium's unlock
 * advancements are gated on holding rare ingredients - so custom items never
 * appeared in the book. This grants every {@code endesium:recipes/*}
 * advancement when a player joins, putting all Endesium recipes in the book
 * from the start.
 */
public final class RecipeUnlockEvents {
	private RecipeUnlockEvents() {
	}

	/** Every Endesium recipe unlock id under recipes/. Keep in sync with datagen. */
	private static final String[] RECIPE_PATHS = {
			"ashen_crust", "ashwalker_boots", "crown_needle", "crown_seal", "echo_compass",
			"ember_charm", "lumen_graft", "mire_bell_clapper", "null_quill", "prism_seed",
			"resonance_lens", "resonant_wings", "threshold_key", "void_anchor", "void_axe",
			"void_brick", "void_brick_slab", "void_brick_stairs", "void_brick_wall",
			"void_boots", "void_chestplate", "void_compass", "void_dash", "void_helmet",
			"void_hoe", "void_ingot_from_nuggets", "void_ingot_from_smelting",
			"void_lantern", "void_leggings", "void_nugget_from_ingot", "void_pickaxe",
			"void_shovel", "void_sword", "windscar_winch"
	};

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.getPlayer();
			var advancements = server.getAdvancements();
			for (String path : RECIPE_PATHS) {
				var holder = advancements.get(Endesium.id("recipes/" + path));
				if (holder == null) continue;
				AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
				if (!progress.isDone()) {
					for (String criterion : progress.getRemainingCriteria()) {
						player.getAdvancements().award(holder, criterion);
					}
				}
			}
		});
	}
}
