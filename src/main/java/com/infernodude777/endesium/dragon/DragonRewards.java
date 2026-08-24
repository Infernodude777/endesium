package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The dragon's enhanced hoard. When the assault layer detects the dragon has
 * fallen, this drops a curated pile of dragon materials at the death site:
 * a full dragon heart, fangs and bone for crafting, and a pair of resonant
 * scales for the wings upgrade path. Vanilla already pays experience; this is
 * the material payoff that makes each kill worth looting.
 */
public final class DragonRewards {
	private DragonRewards() {
	}

	public static void dropEnhancedLoot(ServerLevel level, BlockPos pos) {
		BlockPos at = pos == null ? new BlockPos(0, 64, 0) : pos;
		double x = at.getX() + 0.5D;
		double y = at.getY() + 1.0D;
		double z = at.getZ() + 0.5D;
		drop(level, x, y, z, ModItems.DRAGON_HEART, 1);
		drop(level, x, y, z, ModItems.DRAGON_FANG, 3);
		drop(level, x, y, z, ModItems.DRAGONBONE, 8);
		drop(level, x, y, z, ModItems.RESONANT_DRAGON_SCALE, 2);
		level.playSound(null, at, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.AMBIENT, 1.0F, 0.8F);
	}

	private static void drop(ServerLevel level, double x, double y, double z, Item item, int count) {
		for (int i = 0; i < count; i++) {
			ItemEntity entity = new ItemEntity(level, x, y, z, new ItemStack(item));
			entity.setPickUpDelay(20);
			level.addFreshEntity(entity);
		}
	}
}
