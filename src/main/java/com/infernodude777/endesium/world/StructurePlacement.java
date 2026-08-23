package com.infernodude777.endesium.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Safe write boundary for Endesium generation. Registered Structures have
 * bounding-box arbitration; these builders do not, so every write must refuse
 * to erase vanilla arena infrastructure or another block entity written by an
 * earlier generator.
 *
 * <p>Writes are gated twice. During Feature placement they are limited to the
 * generating 3x3 chunk region. During Structure-piece generation the active
 * piece BoundingBox (see {@link #beginPiece}) additionally clips every write,
 * which is how vanilla keeps large multi-chunk builds ordered: each chunk only
 * ever realizes the part of the build that falls inside it.
 */
public final class StructurePlacement {
	private StructurePlacement() {
	}

	/**
	 * The bounding box of the structure piece currently being generated, or
	 * null outside piece generation. World generation for one chunk batch runs
	 * on a single worker thread, so a ThreadLocal is a safe channel between
	 * {@code beginPiece}/{@code endPiece} and the shared write helpers.
	 */
	private static final ThreadLocal<BoundingBox> ACTIVE_PIECE_BOX = new ThreadLocal<>();

	/** Enters piece mode: every subsequent write is clipped to {@code box}. */
	public static void beginPiece(BoundingBox box) {
		ACTIVE_PIECE_BOX.set(box);
	}

	/** Leaves piece mode; the clip reverts to the 3x3 feature-region gate. */
	public static void endPiece() {
		ACTIVE_PIECE_BOX.remove();
	}

	public static boolean set(WorldGenLevel level, BlockPos pos, BlockState state, int flags) {
		BoundingBox box = ACTIVE_PIECE_BOX.get();
		if (box != null && !box.isInside(pos)) {
			return false;
		}
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
		// Full-strength level (e.g. /place structure): no region restriction.
		return true;
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