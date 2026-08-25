package com.infernodude777.endesium.world;

import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Per-biome relief for the Endesium regions.
 *
 * <p>Runs once per chunk (data-driven {@code in_square} placement) and resculpts
 * its own columns toward a smooth height field. The field is sampled with a
 * one-block apron and lightly blurred, so the surface never steps by more than
 * the noise itself: slopes read as natural grades, rises cap in region ground,
 * steep faces expose the substrate, and strata boundaries are dithered rather
 * than drawn as planes. Heights are a pure function of world seed + absolute
 * column, so adjacent chunks agree and no write ever leaves the generating
 * chunk. Void regions additionally receive a surface pass that replaces the
 * top layer with the region's void geology, so no End Stone shows through.</p>
 */
public final class BiomeTerrainFeature extends Feature<NoneFeatureConfiguration> {
	/** Depth of the geological reskin below the surface, so cliffs and basins
	 * show full region geology instead of vanilla End Stone beneath a cap. */
	private static final int SKIN_DEPTH = 56;
	/** Maximum carve depth per column, so basins never punch through islands. */
	private static final int MAX_CARVE = 14;
	/** Slope (blocks per column) above which a face reads as rock, not soil. */
	private static final double ROCK_FACE_SLOPE = 1.8D;

	public BiomeTerrainFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		if (origin.getX() * origin.getX() + origin.getZ() * origin.getZ() < 160 * 160) {
			return false;
		}

		Holder<Biome> biome = level.getBiome(origin);
		int region = EndBiomeProfiles.regionOf(biome);
		if (region < 0) {
			return false;
		}

		long seed = EndesiumWorldgenSeeds.get();
		ChunkPos chunk = new ChunkPos(origin);
		Block ground = EndBiomeProfiles.groundBlock(region);
		Block substrate = EndBiomeProfiles.substrateBlock(region);
		Block[] palette = EndBiomeProfiles.palette(region);
		boolean lowland = EndBiomeProfiles.isLowland(region);
		boolean voidRegion = EndBiomeProfiles.isVoidRegion(region);

		int minX = chunk.getMinBlockX();
		int minZ = chunk.getMinBlockZ();

		// Sample the offset field with a one-block apron so every column can
		// see its neighbors, then relax it once: single-column spikes vanish
		// while genuine ridges and basin walls survive the blur.
		int size = 18;
		double[][] offsets = new double[size][size];
		for (int gx = 0; gx < size; gx++) {
			for (int gz = 0; gz < size; gz++) {
				offsets[gx][gz] = BiomeTerrain.offsetAt(region, seed, minX + gx - 1, minZ + gz - 1);
			}
		}
		double[][] relaxed = new double[size][size];
		for (int gx = 0; gx < size; gx++) {
			for (int gz = 0; gz < size; gz++) {
				if (gx > 0 && gx < size - 1 && gz > 0 && gz < size - 1) {
					relaxed[gx][gz] = offsets[gx][gz] * 0.6D
							+ (offsets[gx - 1][gz] + offsets[gx + 1][gz]
							+ offsets[gx][gz - 1] + offsets[gx][gz + 1]) * 0.1D;
				} else {
					relaxed[gx][gz] = offsets[gx][gz];
				}
			}
		}

		for (int x = minX; x <= minX + 15; x++) {
			for (int z = minZ; z <= minZ + 15; z++) {
				if (StructurePlacement.insideStructureBox(x, z, chunk)) {
					continue; // structure-owned column - leave it exactly as built
				}
				int gx = x - minX + 1;
				int gz = z - minZ + 1;
				double offset = relaxed[gx][gz];
				double slope = Math.max(
						Math.abs(offset - relaxed[gx - 1][gz]),
						Math.max(Math.abs(offset - relaxed[gx + 1][gz]),
								Math.max(Math.abs(offset - relaxed[gx][gz - 1]),
										Math.abs(offset - relaxed[gx][gz + 1]))));
				applyColumn(level, seed, region, x, z, ground, substrate, palette, lowland, offset, slope);
				if (voidRegion) {
					applyVoidSurface(level, x, z, ground, substrate);
				}
				skinColumn(level, seed, x, z, voidRegion);
			}
		}
		return true;
	}

	/**
	 * Replaces the top {@link #SKIN_DEPTH} layers of a column with the region's
	 * full geological palette, painted as a dithered gradient: topsoil, a
	 * patchy transition band, deep stone, and a rare deep accent - so almost
	 * no vanilla End Stone remains visible anywhere in a region. Only
	 * End-family blocks are ever replaced; foreign or placed blocks stop the
	 * reskin.
	 */
	private static void skinColumn(WorldGenLevel level, long seed, int x, int z, boolean voidRegion) {
		int surfaceTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
		if (surfaceTop < level.getMinBuildHeight() + SKIN_DEPTH + 1) {
			return;
		}
		Block[] palette = EndBiomeProfiles.palette(
				EndBiomeProfiles.regionOf(level.getBiome(new BlockPos(x, surfaceTop, z))));
		if (palette.length == 0) {
			return;
		}
		for (int d = 0; d < SKIN_DEPTH; d++) {
			BlockPos p = new BlockPos(x, surfaceTop - d, z);
			BlockState state = level.getBlockState(p);
			if (!isEndFamily(state)) {
				break; // never eat vanilla structures, plants, or the void
			}
			Block target = paletteBlock(palette, d, seed, x, z);
			if (voidRegion && d == SKIN_DEPTH - 1) {
				target = ModBlocks.VOID_SOIL;
			}
			if (!state.is(target)) {
				level.setBlock(p, target.defaultBlockState(), 3);
			}
		}
	}

	/**
	 * Picks the palette block for a given depth: topsoil cap, patchy
	 * transition band, deep stone with dithered accents. A coarse patch hash
	 * varies the bands horizontally so strata look weathered, not drawn.
	 */
	private static Block paletteBlock(Block[] palette, int depth, long seed, int x, int z) {
		int n = palette.length;
		int patch = ((x >> 4) * 7 + (z >> 4) * 13) & 0x7;
		if (depth == 0) {
			return palette[0];
		}
		if (depth <= 2) {
			return dither(seed, x, depth, z, 4, patch) && n > 1 ? palette[1] : palette[0];
		}
		if (depth <= 5) {
			return dither(seed, x, depth, z, 3, patch) ? palette[0] : palette[Math.min(1, n - 1)];
		}
		if (n > 3 && depth > SKIN_DEPTH - 4 && dither(seed, x, depth, z, 7, patch)) {
			return palette[3];
		}
		if (n > 2 && dither(seed, x, depth, z, 6, patch)) {
			return palette[1];
		}
		return palette[Math.min(2, n - 1)];
	}

	private static void applyColumn(WorldGenLevel level, long seed, int region, int x, int z,
			Block ground, Block substrate, Block[] palette, boolean lowland, double offset, double slope) {
		int surfaceTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
		if (surfaceTop < level.getMinBuildHeight() + 4) {
			return;
		}
		if (!isEndFamily(level.getBlockState(new BlockPos(x, surfaceTop, z)))) {
			return; // never sculpt vanilla structures, plants, or the void
		}

		int target = surfaceTop + (int) Math.round(offset);
		if (target > surfaceTop) {
			raise(level, seed, x, z, surfaceTop, target, palette, slope);
		} else if (target < surfaceTop) {
			lower(level, seed, x, z, surfaceTop, Math.min(surfaceTop - target, MAX_CARVE),
					ground, substrate, lowland);
		}
	}

	/**
	 * Raises a column to its target height. The fill is stratified: a ground
	 * cap (rock substrate on steep faces), a dithered transition band, then
	 * clean substrate - so slopes read as weathered rock, not stacked blocks.
	 */
	private static void raise(WorldGenLevel level, long seed, int x, int z, int surfaceTop, int target,
			Block[] palette, double slope) {
		boolean rockyFace = slope >= ROCK_FACE_SLOPE;
		for (int y = surfaceTop + 1; y <= target; y++) {
			BlockPos pos = new BlockPos(x, y, z);
			if (!level.getBlockState(pos).isAir()) {
				break; // a structure or feature already owns this space
			}
			Block block;
			if (y >= target - 1) {
				block = rockyFace ? palette[Math.min(2, palette.length - 1)] : palette[0];
			} else if (y >= target - 3 && dither(seed, x, y, z, 5, 0)) {
				block = palette[1];
			} else {
				block = palette[Math.min(2, palette.length - 1)];
			}
			level.setBlock(pos, block.defaultBlockState(), 3);
		}
	}

	/**
	 * Carves a column down to its target height. Only loose, natural blocks
	 * are ever removed - end stone and the region's own ground and substrate -
	 * so structure shells, placed machinery, and foreign materials stop the
	 * carve immediately.
	 */
	private static void lower(WorldGenLevel level, long seed, int x, int z, int surfaceTop, int depth,
			Block ground, Block substrate, boolean lowland) {
		int newSurface = surfaceTop - depth;
		for (int y = surfaceTop; y > newSurface; y--) {
			BlockPos pos = new BlockPos(x, y, z);
			if (level.getBlockEntity(pos) != null) {
				return; // placed machinery - never carve through it
			}
			if (!carveable(level.getBlockState(pos), ground, substrate)) {
				return; // structure shell or foreign material - stop here
			}
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		}
		if (lowland) {
			// Cap the freshly carved basin floor with the region's soil.
			BlockPos floor = new BlockPos(x, newSurface, z);
			if (carveable(level.getBlockState(floor), ground, substrate)) {
				level.setBlock(floor, ground.defaultBlockState(), 3);
			}
		}
	}

	/** Only loose natural geology may be carved away. */
	private static boolean carveable(BlockState state, Block ground, Block substrate) {
		return state.is(Blocks.END_STONE) || state.is(ground) || state.is(substrate);
	}

	/** Cheap deterministic dither so strata boundaries never read as planes. */
	private static boolean dither(long seed, int x, int y, int z, int bound, int patch) {
		long h = seed + x * 341873128712L + y * 132897987541L + z * 604891948905L + patch * 740511037L;
		h ^= h >>> 33;
		h *= 0xFF51AFD7ED558CCDL;
		h ^= h >>> 29;
		return (int) Math.floorMod(h, 97L) % bound == 0;
	}

	private static void applyVoidSurface(WorldGenLevel level, int x, int z, Block ground, Block substrate) {
		int surfaceTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
		if (surfaceTop < level.getMinBuildHeight() + 4) {
			return;
		}
		BlockPos top = new BlockPos(x, surfaceTop, z);
		if (!isEndFamily(level.getBlockState(top)) && !level.getBlockState(top).isAir()) {
			return;
		}
		level.setBlock(top, ground.defaultBlockState(), 3);
		BlockPos below = top.below();
		if (isEndFamily(level.getBlockState(below))) {
			level.setBlock(below, substrate.defaultBlockState(), 3);
		}
	}

	private static boolean isEndFamily(BlockState state) {
		return state.is(Blocks.END_STONE)
				|| state.is(Blocks.END_STONE_BRICKS)
				|| state.is(ModBlocks.WASTES_STONE)
				|| state.is(ModBlocks.HIGHLAND_STONE)
				|| state.is(ModBlocks.HIGHLAND_SLATE)
				|| state.is(ModBlocks.VOID_MARSH_SOIL)
				|| state.is(ModBlocks.LUMEN_STONE)
				|| state.is(ModBlocks.ASHEN_SOIL)
				|| state.is(ModBlocks.ASH_STONE)
				|| state.is(ModBlocks.CRYSTAL_SHARD_BLOCK)
				|| state.is(ModBlocks.CHORUS_MOSS)
				|| state.is(ModBlocks.CHORUS_ROOT)
				|| state.is(ModBlocks.END_CLAY)
				|| state.is(ModBlocks.DARK_CRYSTAL_BLOCK)
				|| state.is(ModBlocks.VOID_SLATE)
				|| state.is(ModBlocks.VOID_GRAVEL)
				|| state.is(ModBlocks.VOID_SOIL)
				|| state.is(ModBlocks.VOID_BRICK)
				|| state.is(ModBlocks.UMBRAL_STONE)
				|| state.is(ModBlocks.VOIDSTONE)
				|| state.is(ModBlocks.VOID_ORE);
	}
}
