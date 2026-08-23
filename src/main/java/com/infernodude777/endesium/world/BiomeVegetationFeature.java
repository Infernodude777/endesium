package com.infernodude777.endesium.world;

import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Biome-specific growth. Placed once per chunk and dispatches on the biome
 * present at the origin, scattering each region's growth family with layered
 * density: ground cover first, small plants, then rare giant growths. Giant
 * growth stays rare so the terrain always remains readable. The void regions
 * grow sparse umbral grass, void ferns, and rare glowing void crystals.
 */
public final class BiomeVegetationFeature extends Feature<NoneFeatureConfiguration> {
	public BiomeVegetationFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		if (origin.getX() * origin.getX() + origin.getZ() * origin.getZ() < 160 * 160) {
			return false;
		}
		Holder<Biome> biome = level.getBiome(origin);
		int region = EndBiomeProfiles.regionOf(biome);
		if (region < 0) {
			return false;
		}

		switch (region) {
			case EndesiumRegions.END_WASTES -> scatterWastes(level, random, origin);
			case EndesiumRegions.CHORUS_WILDS -> scatterWilds(level, random, origin);
			case EndesiumRegions.SHATTERED_HIGHLANDS -> scatterHighlands(level, random, origin);
			case EndesiumRegions.VOID_MARSHES -> scatterMarshes(level, random, origin);
			case EndesiumRegions.LUMINOUS_GROVES -> scatterGroves(level, random, origin);
			case EndesiumRegions.ASHEN_EXPANSE -> scatterAshen(level, random, origin);
			case EndesiumRegions.CRYSTAL_BARRENS -> scatterBarrens(level, random, origin);
			case EndesiumRegions.VOID_SKIRTS -> scatterSkirts(level, random, origin);
			case EndesiumRegions.VOID_CROWN -> scatterCrown(level, random, origin);
			case EndesiumRegions.UMBRAL_REACH -> scatterReach(level, random, origin);
			default -> { }
		}
		return true;
	}

	private static void scatterWastes(WorldGenLevel level, RandomSource random, BlockPos origin) {
		for (int i = 0; i < 10; i++) {
			BlockPos spot = offset(origin, random, 7);
			placePlant(level, spot, ModBlocks.VOID_GRASS);
		}
		for (int i = 0; i < 2; i++) {
			BlockPos spot = offset(origin, random, 8);
			placePlant(level, spot, ModBlocks.DUST_REED);
		}
		if (random.nextFloat() < 0.12F) {
			BlockPos spot = offset(origin, random, 8);
			placeGroundPatch(level, spot, ModBlocks.WASTES_GRAVEL, 2);
		}
		if (random.nextFloat() < 0.06F) {
			BlockPos spot = offset(origin, random, 9);
			placePlant(level, spot, ModBlocks.DORMANT_RESONANT_CRYSTAL);
		}
	}

	private static void scatterWilds(WorldGenLevel level, RandomSource random, BlockPos origin) {
		// Ground cover: visible, not a solid carpet.
		for (int i = 0; i < 5; i++) {
			BlockPos spot = offset(origin, random, 7);
			placeGroundPatch(level, spot, ModBlocks.CHORUS_MOSS, 2);
		}
		for (int i = 0; i < 12; i++) {
			placePlant(level, offset(origin, random, 8), ModBlocks.CHORUS_SPROUT);
		}
		for (int i = 0; i < 5; i++) {
			placePlant(level, offset(origin, random, 8), ModBlocks.WILD_TENDRIL);
		}
		for (int i = 0; i < 2; i++) {
			placePlant(level, offset(origin, random, 9), ModBlocks.RESONANT_BLOOM);
		}
		// Rare elder chorus: a thick woody trunk with a moss crown, not a forest.
		if (random.nextFloat() < 0.05F) {
			growElderChorus(level, random, origin);
		}
	}

	private static void scatterHighlands(WorldGenLevel level, RandomSource random, BlockPos origin) {
		for (int i = 0; i < 3; i++) {
			placePlant(level, offset(origin, random, 9), ModBlocks.VOID_GRASS);
		}
		if (random.nextFloat() < 0.08F) {
			placePlant(level, offset(origin, random, 10), ModBlocks.DORMANT_RESONANT_CRYSTAL);
		}
		if (random.nextFloat() < 0.05F) {
			placeGroundPatch(level, offset(origin, random, 10), ModBlocks.PALE_CRYSTAL_BLOCK, 1);
		}
	}

	private static void scatterMarshes(WorldGenLevel level, RandomSource random, BlockPos origin) {
		for (int i = 0; i < 7; i++) {
			placePlant(level, offset(origin, random, 8), ModBlocks.VOID_REED);
		}
		for (int i = 0; i < 4; i++) {
			placeGroundPatch(level, offset(origin, random, 7), ModBlocks.MARSH_MOSS, 2);
		}
		if (random.nextFloat() < 0.10F) {
			placePlant(level, offset(origin, random, 9), ModBlocks.LUMEN_MOSS);
		}
	}

	private static void scatterGroves(WorldGenLevel level, RandomSource random, BlockPos origin) {
		for (int i = 0; i < 6; i++) {
			placePlant(level, offset(origin, random, 8), ModBlocks.LUMEN_MOSS);
		}
		for (int i = 0; i < 3; i++) {
			placePlant(level, offset(origin, random, 8), ModBlocks.LUMEN_BLOOM);
		}
		if (random.nextFloat() < 0.08F) {
			placeGroundPatch(level, offset(origin, random, 9), ModBlocks.LUMEN_STONE, 2);
		}
		if (random.nextFloat() < 0.06F) {
			placePlant(level, offset(origin, random, 9), ModBlocks.CRYSTAL_CLUSTER);
		}
	}

	private static void scatterAshen(WorldGenLevel level, RandomSource random, BlockPos origin) {
		// Deliberately nearly lifeless.
		for (int i = 0; i < 3; i++) {
			placePlant(level, offset(origin, random, 9), ModBlocks.VOID_GRASS);
		}
		if (random.nextFloat() < 0.05F) {
			placeGroundPatch(level, offset(origin, random, 10), ModBlocks.DARK_CRYSTAL_BLOCK, 1);
		}
	}

	private static void scatterBarrens(WorldGenLevel level, RandomSource random, BlockPos origin) {
		for (int i = 0; i < 4; i++) {
			placePlant(level, offset(origin, random, 9), ModBlocks.CRYSTAL_CLUSTER);
		}
		if (random.nextFloat() < 0.10F) {
			placeGroundPatch(level, offset(origin, random, 9), ModBlocks.PALE_CRYSTAL_BLOCK, 2);
		}
		if (random.nextFloat() < 0.10F) {
			placeGroundPatch(level, offset(origin, random, 9), ModBlocks.DARK_CRYSTAL_BLOCK, 2);
		}
		if (random.nextFloat() < 0.08F) {
			placePlant(level, offset(origin, random, 10), ModBlocks.DORMANT_RESONANT_CRYSTAL);
		}
	}

	private static void scatterSkirts(WorldGenLevel level, RandomSource random, BlockPos origin) {
		// Sparse, low growth on the vast dark plain.
		for (int i = 0; i < 6; i++) {
			placePlant(level, offset(origin, random, 8), ModBlocks.UMBRAL_GRASS);
		}
		for (int i = 0; i < 2; i++) {
			placePlant(level, offset(origin, random, 9), ModBlocks.VOID_FERN);
		}
		if (random.nextFloat() < 0.08F) {
			placePlant(level, offset(origin, random, 9), ModBlocks.VOID_CRYSTAL);
		}
		if (random.nextFloat() < 0.06F) {
			placeGroundPatch(level, offset(origin, random, 9), ModBlocks.VOID_GRAVEL, 2);
		}
	}

	private static void scatterCrown(WorldGenLevel level, RandomSource random, BlockPos origin) {
		for (int i = 0; i < 4; i++) {
			placePlant(level, offset(origin, random, 9), ModBlocks.UMBRAL_GRASS);
		}
		if (random.nextFloat() < 0.10F) {
			placePlant(level, offset(origin, random, 9), ModBlocks.VOID_CRYSTAL);
		}
		if (random.nextFloat() < 0.06F) {
			placeGroundPatch(level, offset(origin, random, 10), ModBlocks.VOID_ORE, 1);
		}
	}

	private static void scatterReach(WorldGenLevel level, RandomSource random, BlockPos origin) {
		for (int i = 0; i < 5; i++) {
			placePlant(level, offset(origin, random, 8), ModBlocks.VOID_FERN);
		}
		if (random.nextFloat() < 0.12F) {
			placePlant(level, offset(origin, random, 9), ModBlocks.VOID_CRYSTAL);
		}
		if (random.nextFloat() < 0.08F) {
			placeGroundPatch(level, offset(origin, random, 9), ModBlocks.VOID_ORE, 1);
		}
	}

	private static void growElderChorus(WorldGenLevel level, RandomSource random, BlockPos origin) {
		BlockPos spot = offset(origin, random, 6);
		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, spot.getX(), spot.getZ());
		BlockPos ground = new BlockPos(spot.getX(), y, spot.getZ());
		if (!ModBlocks.isPlantGround(level.getBlockState(ground.below()))) {
			return;
		}
		int trunk = 3 + random.nextInt(3);
		for (int dy = 0; dy < trunk; dy++) {
			BlockPos pos = ground.above(dy);
			if (!level.getBlockState(pos).isAir()) break;
			level.setBlock(pos, ModBlocks.ELDER_CHORUS_WOOD.defaultBlockState(), 3);
		}
		BlockPos crown = ground.above(trunk);
		if (level.getBlockState(crown).isAir()) {
			level.setBlock(crown, ModBlocks.CHORUS_MOSS.defaultBlockState(), 3);
		}
	}

	private static void placePlant(WorldGenLevel level, BlockPos spot, Block plant) {
		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, spot.getX(), spot.getZ());
		BlockPos ground = new BlockPos(spot.getX(), y, spot.getZ());
		if (ModBlocks.isPlantGround(level.getBlockState(ground.below()))
				&& level.getBlockState(ground).isAir()) {
			level.setBlock(ground, plant.defaultBlockState(), 3);
		}
	}

	private static void placeGroundPatch(WorldGenLevel level, BlockPos center, Block patch, int radius) {
		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, center.getX(), center.getZ());
		BlockPos ground = new BlockPos(center.getX(), y, center.getZ());
		if (!ModBlocks.isPlantGround(level.getBlockState(ground.below()))) {
			return;
		}
		if (level.getBlockState(ground).isAir() || ModBlocks.isPlantGround(level.getBlockState(ground))) {
			level.setBlock(ground, patch.defaultBlockState(), 3);
		}
	}

	private static BlockPos offset(BlockPos origin, RandomSource random, int spread) {
		return origin.offset(random.nextInt(spread * 2 + 1) - spread, 0, random.nextInt(spread * 2 + 1) - spread);
	}
}
