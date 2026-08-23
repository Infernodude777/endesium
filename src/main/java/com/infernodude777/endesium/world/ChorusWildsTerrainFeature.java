package com.infernodude777.endesium.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Gives Chorus Wilds a heavier, rolling island shape without touching the
 * shared End noise router (which is biome-independent).
 *
 * <p>Two cheap, chunk-local operations create the organic feel:</p>
 * <ul>
 *   <li><b>Pocket filling:</b> small air voids trapped under the island
 *       surface are filled with End stone, making the islands read as larger,
 *       more solid landmasses with fewer thin edges.</li>
 *   <li><b>Surface mounds:</b> a low rounded dome of End stone is raised so
 *       the terrain rolls instead of staying flat-topped like vanilla
 *       shelves.</li>
 * </ul>
 */
public final class ChorusWildsTerrainFeature extends Feature<NoneFeatureConfiguration> {
	public ChorusWildsTerrainFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		var random = context.random();
		BlockPos origin = context.origin();
		if (origin.getX() * origin.getX() + origin.getZ() * origin.getZ() < 160 * 160) return false;

		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
		BlockPos base = new BlockPos(origin.getX(), y, origin.getZ());
		if (!level.getBlockState(base.below()).is(Blocks.END_STONE)) return false;

		fillSurfacePockets(level, base, random);
		if (random.nextFloat() < 0.75F) {
			buildMound(level, base, random);
		}
		if (random.nextFloat() < 0.34F) {
			buildWildsRidge(level, base, random);
		}
		if (random.nextFloat() < 0.18F) {
			buildResonanceNeedles(level, base, random);
		}
		return true;
	}

	/** Fill small air pockets trapped beneath the island surface so islands feel solid and rolling. */
	private static void fillSurfacePockets(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int radius = 3 + random.nextInt(3);
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG,
						base.offset(dx, 0, dz));
				int surfaceY = surface.getY();
				if (surfaceY < base.getY() - 4) continue;
				for (int dy = -1; dy >= -5; dy--) {
					BlockPos pos = new BlockPos(surface.getX(), surfaceY + dy, surface.getZ());
					if (!level.getBlockState(pos).isAir()) continue;
					if (!isEndStoneFamily(level.getBlockState(pos.above()))) break;
					if (!isEndStoneFamily(level.getBlockState(pos.below()))) continue;
					level.setBlock(pos, Blocks.END_STONE.defaultBlockState(), 3);
				}
			}
		}
	}

	/** Raise a low dome of End stone; a scaled radial falloff keeps edges gentle. */
	private static void buildMound(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int radius = 2 + random.nextInt(3);
		int height = 1 + random.nextInt(2);
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				double distance = Mth.sqrt(dx * dx + dz * dz);
				if (distance > radius) continue;
				int lift = (int) Math.round(Math.max(0.0D, 1.0D - distance / radius) * height);
				if (lift == 0) continue;
				BlockPos top = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, base.offset(dx, 0, dz));
				// Only raise over End stone so mounds never grow onto vegetation
				// or replace other terrain features.
				if (!isEndStoneFamily(level.getBlockState(top.below()))) continue;
				level.setBlock(top, Blocks.END_STONE.defaultBlockState(), 3);
				for (int dy = 1; dy <= lift; dy++) {
					level.setBlock(top.above(dy), Blocks.END_STONE.defaultBlockState(), 3);
				}
			}
		}
	}

	/** A broad, asymmetrical ridge gives the Wilds a recognizable skyline. */
	private static void buildWildsRidge(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		boolean alongX = random.nextBoolean();
		int length = 5 + random.nextInt(5);
		for (int i = -length; i <= length; i++) {
			int height = 1 + Math.max(0, 3 - Math.abs(i) / 3);
			for (int dy = 0; dy < height; dy++) {
				BlockPos pos = alongX ? base.offset(i, dy, 0) : base.offset(0, dy, i);
				if (level.getBlockState(pos).isAir() || isEndStoneFamily(level.getBlockState(pos))) {
					level.setBlock(pos, dy == height - 1 && i % 3 == 0
							? Blocks.END_STONE_BRICKS.defaultBlockState()
							: Blocks.END_STONE.defaultBlockState(), 3);
				}
			}
		}
	}

	/** Thin mineral needles make the Wilds feel charged without turning it neon. */
	private static void buildResonanceNeedles(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		for (int i = 0; i < 3; i++) {
			int dx = random.nextInt(9) - 4;
			int dz = random.nextInt(9) - 4;
			int height = 2 + random.nextInt(3);
			BlockPos ground = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, base.offset(dx, 0, dz));
			if (!isEndStoneFamily(level.getBlockState(ground.below()))) continue;
			for (int dy = 0; dy < height; dy++) {
				BlockPos pos = ground.above(dy);
				if (level.getBlockState(pos).isAir()) {
					level.setBlock(pos, dy == height - 1
							? net.minecraft.world.level.block.Blocks.AMETHYST_BLOCK.defaultBlockState()
							: Blocks.END_STONE_BRICKS.defaultBlockState(), 3);
				}
			}
		}
	}

	private static boolean isEndStoneFamily(net.minecraft.world.level.block.state.BlockState state) {
		return state.is(Blocks.END_STONE) || state.is(Blocks.END_STONE_BRICKS);
	}
}
