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
 * <p>Runs once per chunk (data-driven {@code in_square} placement) and applies
 * {@link BiomeTerrain#offsetAt} to its own columns only. Heights are a pure
 * function of world seed + absolute column, so adjacent chunks agree and no
 * write ever leaves the generating chunk. Void regions additionally receive a
 * surface pass that replaces the top layer with the region's void geology, so
 * no End Stone shows through.</p>
 */
public final class BiomeTerrainFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MAX_CARVE = 9;
	/** Depth of the geological reskin below the surface, so cliffs and basins
	 * show full region geology instead of vanilla End Stone beneath a cap. */
	private static final int SKIN_DEPTH = 8;

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
		boolean lowland = EndBiomeProfiles.isLowland(region);
		boolean voidRegion = EndBiomeProfiles.isVoidRegion(region);

		int minX = chunk.getMinBlockX();
		int maxX = chunk.getMaxBlockX();
		int minZ = chunk.getMinBlockZ();
		int maxZ = chunk.getMaxBlockZ();
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				applyColumn(level, seed, region, x, z, ground, substrate, lowland);
				if (voidRegion) {
					applyVoidSurface(level, x, z, ground, substrate);
				}
				skinColumn(level, x, z, ground, substrate, voidRegion);
			}
		}
		return true;
	}

	/**
	 * Replaces the top {@link #SKIN_DEPTH} layers of a column with the region's
	 * geology: ground cap on top, substrate beneath, and — for the void
	 * regions — a sealed VOID_SOIL floor at the bottom so the dark biomes read
	 * as their own strata rather than painted End Stone. Only End-family blocks
	 * are ever replaced; foreign or placed blocks stop the reskin.
	 */
	private static void skinColumn(WorldGenLevel level, int x, int z, Block ground, Block substrate, boolean voidRegion) {
		int surfaceTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
		if (surfaceTop < level.getMinBuildHeight() + SKIN_DEPTH + 1) {
			return;
		}
		for (int d = 0; d < SKIN_DEPTH; d++) {
			BlockPos p = new BlockPos(x, surfaceTop - d, z);
			BlockState state = level.getBlockState(p);
			if (!isEndFamily(state)) {
				break; // never eat vanilla structures, plants, or the void
			}
			Block target;
			if (voidRegion && d == SKIN_DEPTH - 1) {
				target = ModBlocks.VOID_SOIL;
			} else if (d < 2) {
				target = ground;
			} else {
				target = substrate;
			}
			if (!state.is(target)) {
				level.setBlock(p, target.defaultBlockState(), 3);
			}
		}
	}

	private static void applyColumn(WorldGenLevel level, long seed, int region, int x, int z,
			Block ground, Block substrate, boolean lowland) {
		int surfaceTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
		if (surfaceTop < level.getMinBuildHeight() + 4) {
			return;
		}
		if (!isEndFamily(level.getBlockState(new BlockPos(x, surfaceTop, z)))) {
			return; // never sculpt vanilla structures, plants, or the void
		}

		int offset = BiomeTerrain.offsetAt(region, seed, x, z);
		if (offset > 0) {
			raise(level, x, z, surfaceTop, offset, ground, substrate);
		} else if (offset < 0) {
			lower(level, x, z, surfaceTop, Math.max(offset, -MAX_CARVE), ground, lowland);
		}
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

	private static void raise(WorldGenLevel level, int x, int z, int surfaceTop, int offset,
			Block ground, Block substrate) {
		int target = surfaceTop + offset;
		for (int y = surfaceTop + 1; y <= target; y++) {
			BlockPos pos = new BlockPos(x, y, z);
			if (!level.getBlockState(pos).isAir()) {
				break;
			}
			level.setBlock(pos, (y == target ? ground : substrate).defaultBlockState(), 3);
		}
	}

	private static void lower(WorldGenLevel level, int x, int z, int surfaceTop, int depth,
			Block ground, boolean lowland) {
		int newSurface = surfaceTop - depth;
		for (int y = surfaceTop; y > newSurface; y--) {
			BlockPos pos = new BlockPos(x, y, z);
			if (!isEndFamily(level.getBlockState(pos))) {
				return; // hit something we should not destroy
			}
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		}
		if (lowland) {
			// Cap the freshly carved basin with the region's soil.
			level.setBlock(new BlockPos(x, newSurface, z), ground.defaultBlockState(), 3);
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
