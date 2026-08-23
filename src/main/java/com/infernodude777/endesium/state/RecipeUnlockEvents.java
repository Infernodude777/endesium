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

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.getPlayer();
			var advancements = server.getAdvancements();
			// Derived from the loaded advancement registry instead of a
			// hand-maintained list, so datagen can add or rename recipe
			// unlocks without this class drifting out of sync.
			for (var holder : advancements.getAllAdvancements()) {
				if (holder.id().getNamespace() != Endesium.MOD_ID
						|| !holder.id().getPath().startsWith("recipes/")) {
					continue;
				}
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
