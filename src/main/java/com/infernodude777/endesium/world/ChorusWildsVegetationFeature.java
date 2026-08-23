package com.infernodude777.endesium.world;

import com.infernodude777.endesium.registry.ModBlocks;	import net.minecraft.core.BlockPos;
	import net.minecraft.core.registries.Registries;
	import net.minecraft.data.worldgen.features.EndFeatures;
	import net.minecraft.util.RandomSource;
	import net.minecraft.world.level.WorldGenLevel;
	import net.minecraft.world.level.block.Blocks;
	import net.minecraft.world.level.levelgen.Heightmap;
	import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Populates the Chorus Wilds with its vegetation signature: dense clusters of
 * vanilla chorus plants, low Chorus Sprouts, and tall Wild Tendrils.
 *
 * <p>The vanilla chorus plant placement is reused directly through
 * {@link net.minecraft.world.level.levelgen.feature.Features#CHORUS_PLANT} so
 * the growth logic, height limits, and branching stay vanilla-accurate.</p>
 */
public final class ChorusWildsVegetationFeature extends Feature<NoneFeatureConfiguration> {
	public ChorusWildsVegetationFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		if (origin.getX() * origin.getX() + origin.getZ() * origin.getZ() < 160 * 160) return false;

		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
		BlockPos base = new BlockPos(origin.getX(), y, origin.getZ());
		if (!level.getBlockState(base.below()).is(Blocks.END_STONE)) return false;

		// 2-5 full chorus plants per attempt give the biome its dense canopy.
		int plants = 2 + random.nextInt(4);
		for (int i = 0; i < plants; i++) {
			BlockPos spot = base.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
			BlockPos ground = new BlockPos(spot.getX(),
					level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, spot.getX(), spot.getZ()), spot.getZ());
			if (level.getBlockState(ground.below()).is(Blocks.END_STONE)) {
				level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
						.getOrThrow(EndFeatures.CHORUS_PLANT)
						.place(level, context.chunkGenerator(), random, ground);
			}
		}

		// Ground cover: sprouts and tendrils in loose clusters.
		int sprouts = 3 + random.nextInt(5);
		for (int i = 0; i < sprouts; i++) {
			BlockPos spot = base.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
			placePlant(level, spot, ModBlocks.CHORUS_SPROUT);
		}
		int tendrils = 1 + random.nextInt(3);
		for (int i = 0; i < tendrils; i++) {
			BlockPos spot = base.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
			placePlant(level, spot, ModBlocks.WILD_TENDRIL);
		}
		// Resonant Blooms are the Wilds signature: sparse, pale, faintly charged.
		int blooms = 1 + random.nextInt(2);
		for (int i = 0; i < blooms; i++) {
			BlockPos spot = base.offset(random.nextInt(11) - 5, 0, random.nextInt(11) - 5);
			placePlant(level, spot, ModBlocks.RESONANT_BLOOM);
		}
		return true;
	}

	private static void placePlant(WorldGenLevel level, BlockPos spot, net.minecraft.world.level.block.Block plant) {
		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, spot.getX(), spot.getZ());
		BlockPos ground = new BlockPos(spot.getX(), y, spot.getZ());
		if (level.getBlockState(ground.below()).is(Blocks.END_STONE)
				&& level.getBlockState(ground).isAir()
				&& level.getBlockState(ground.above()).isAir()) {
			level.setBlock(ground, plant.defaultBlockState(), 3);
		}
	}
}
