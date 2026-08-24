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
	/** True while a vanilla Structure piece is delegating into a builder. */
	public static boolean structureDriven;

	/** Boxes of structures built during this generation session. */
	private static final java.util.List<StructureBox> STRUCTURE_BOXES = new java.util.ArrayList<>();

	/**
	 * Records a built structure's full bounding box. Terrain reskinning consults
	 * these boxes and leaves covered columns exactly as the structure built
	 * them, no matter how deep the geological reskin runs.
	 */
	public static void registerStructureBox(ChunkPos anchor, BoundingBox box) {
		STRUCTURE_BOXES.add(new StructureBox(anchor, box));
		STRUCTURE_BOXES.removeIf(entry -> Math.abs(entry.anchor.x - anchor.x) > 3
				|| Math.abs(entry.anchor.z - anchor.z) > 3);
	}

	/**
	 * Whether a column lies inside any recently built structure. Entries from
	 * far-away chunks are pruned as generation moves on.
	 */
	public static boolean insideStructureBox(int x, int z, ChunkPos current) {
		STRUCTURE_BOXES.removeIf(entry -> Math.abs(entry.anchor.x - current.x) > 3
				|| Math.abs(entry.anchor.z - current.z) > 3);
		for (StructureBox entry : STRUCTURE_BOXES) {
			if (x >= entry.box.minX() && x <= entry.box.maxX()
					&& z >= entry.box.minZ() && z <= entry.box.maxZ()) {
				return true;
			}
		}
		return false;
	}

	/** Clears recorded boxes (called when a server stops). */
	public static void clearBoxes() {
		STRUCTURE_BOXES.clear();
	}

	private record StructureBox(ChunkPos anchor, BoundingBox box) {
	}

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
