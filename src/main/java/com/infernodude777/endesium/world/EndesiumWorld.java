package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.VoidStalkerEntity;
import com.infernodude777.endesium.registry.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;

public final class EndesiumWorld {
	private static final BlockPos RUIN_ORIGIN = new BlockPos(24, 65, 0);

	private EndesiumWorld() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(EndesiumWorld::tickEnd);
		Endesium.LOGGER.info("Registered Endesium End-world extensions");
	}

	private static void tickEnd(ServerLevel world) {
		if (world.dimension() != Level.END || world.getGameTime() % 20 != 0 || !world.hasChunkAt(RUIN_ORIGIN)) {
			return;
		}

		if (!world.getBlockState(RUIN_ORIGIN).is(ModBlocks.END_RUIN_BLOCK)) {
			placeRuin(world);
		}
	}

	private static void placeRuin(ServerLevel world) {
		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				BlockPos floor = RUIN_ORIGIN.offset(x, 0, z);
				world.setBlockAndUpdate(floor, Math.abs(x) == 3 || Math.abs(z) == 3
						? ModBlocks.END_RUIN_BLOCK.defaultBlockState()
						: Blocks.END_STONE_BRICKS.defaultBlockState());
			}
		}

		for (int y = 1; y <= 3; y++) {
			world.setBlockAndUpdate(RUIN_ORIGIN.offset(-3, y, -3), ModBlocks.END_RUIN_BLOCK.defaultBlockState());
			world.setBlockAndUpdate(RUIN_ORIGIN.offset(3, y, 3), ModBlocks.END_RUIN_BLOCK.defaultBlockState());
		}
		world.setBlockAndUpdate(RUIN_ORIGIN.offset(0, 1, 0), ModBlocks.END_RUIN_BLOCK.defaultBlockState());
		Endesium.LOGGER.info("Placed the first End Ruin at {}", RUIN_ORIGIN);
	}
}
