package com.infernodude777.endesium.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Safe write boundary for hand-authored Features. Registered Structures have
 * bounding-box arbitration; these legacy builders do not, so every landmark
 * write must refuse to erase vanilla arena infrastructure or another block
 * entity written by an earlier generator.
 *
 * <p>Writes are also gated to the currently generating 3x3 chunk region.
 * During feature placement the chunk reader has already primed those chunks,
 * so a write there is safe. A write into a chunk outside that region would
 * land in a chunk that may already be saved on disk - Minecraft rejects it
 * with a "setBlock in a far chunk" warning - so we refuse it up front. The
 * landmark builders keep their footprints inside the owning chunk plus at
 * most a few blocks of neighbor, which is always inside the 3x3 region.
 */
public final class StructurePlacement {
	private StructurePlacement() {
	}

	public static boolean set(WorldGenLevel level, BlockPos pos, BlockState state, int flags) {
		if (isProtected(level, pos)) {
			return false;
		}
		if (!isWithinGeneratingRegion(level, pos)) {
			return false;
		}
		level.setBlock(pos, state, flags);
		return true;
	}

	public static boolean set(WorldGenLevel level, BlockPos pos, BlockState state) {
		return set(level, pos, state, 3);
	}

	/**
	 * Refuse writes to a chunk outside the 3x3 region centered on the chunk
	 * currently being generated. Feature placement runs with those nine chunks
	 * loaded, so staying inside them is always safe and never requires the
	 * chunk to be generated out of order.
	 *
	 * <p>In 1.21.1 Mojang mappings {@code WorldGenRegion} lives in
	 * {@code net.minecraft.server.level}, not {@code world.level.chunk}; the
	 * earlier import pointed at the wrong package and broke the build.
	 */
	private static boolean isWithinGeneratingRegion(WorldGenLevel level, BlockPos pos) {
		if (level instanceof WorldGenRegion region) {
			ChunkPos center = region.getCenter();
			int cx = pos.getX() >> 4;
			int cz = pos.getZ() >> 4;
			return Math.abs(cx - center.x) <= 1 && Math.abs(cz - center.z) <= 1;
		}
		return false;
	}

	private static boolean isProtected(WorldGenLevel level, BlockPos pos) {
		BlockState existing = level.getBlockState(pos);
		return existing.is(Blocks.BEDROCK)
				|| existing.is(Blocks.OBSIDIAN)
			|| existing.is(Blocks.END_PORTAL)
			|| existing.is(Blocks.END_PORTAL_FRAME)
			|| existing.is(Blocks.END_PORTAL_FRAME)
			|| existing.is(Blocks.END_GATEWAY)
			|| existing.is(Blocks.CHORUS_PLANT)
			|| existing.is(Blocks.CHORUS_FLOWER)
			|| existing.is(Blocks.BEACON)
			|| existing.is(Blocks.SPAWNER)
			|| existing.is(Blocks.DRAGON_EGG)
			|| level.getBlockEntity(pos) != null;
	}
}