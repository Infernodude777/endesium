package com.infernodude777.endesium.world;

import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Sparse, weathered terrain formations for the End Wastes.
 *
 * <p>The design defines three core formations (Broken Shelf, Quiet Fissure,
 * Resonant Outcrop). This expansion adds a fractured spine, a dead chorus
 * remnant, a rare resonance trace, a grand void terrace, and a weathered
 * resonant ring so the wastes read as ancient and fractured rather than
 * uniformly flat. Everything stays chunk-local; the wastes remain sparse.</p>
 */
public final class EndWastesFeature extends Feature<NoneFeatureConfiguration> {
	public EndWastesFeature() {
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
		// This legacy scenic feature is still wired for the Wastes, but the
		// shared terrain pass may already have replaced vanilla End Stone with
		// Wastes Stone or another Endesium support block. Reject only genuinely
		// foreign terrain so the formations do not silently disappear.
		if (!ModBlocks.isPlantGround(level.getBlockState(base.below()))
				&& !level.getBlockState(base.below()).is(Blocks.END_STONE)) return false;

		int roll = random.nextInt(12);
		if (roll < 2) {
			buildBrokenShelf(level, base, random);
		} else if (roll < 4) {
			buildQuietFissure(level, base, random);
		} else if (roll < 6) {
			buildResonantOutcrop(level, base, random);
		} else if (roll < 7) {
			buildFracturedSpine(level, base, random);
		} else if (roll < 8) {
			buildDeadChorusRemnant(level, base, random);
		} else if (roll < 9) {
			buildResonanceTrace(level, base, random);
		} else if (roll < 10) {
			buildVoidTerrace(level, base, random);
		} else {
			buildResonantRing(level, base, random);
		}
		if (random.nextFloat() < 0.22F) {
			buildWastesScar(level, base, random);
		}
		if (random.nextFloat() < 0.12F) {
			buildDeadMesa(level, base, random);
		}
		return true;
	}

	private static void buildBrokenShelf(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int direction = random.nextBoolean() ? 1 : -1;
		for (int i = -2; i <= 2; i++) {
			placeIfAir(level, base.offset(i, 0, direction), ModBlocks.END_GRAY);
			if (Math.abs(i) != 2) placeIfAir(level, base.offset(i, -1, direction), ModBlocks.END_GRAY);
		}
		placeIfAir(level, base.offset(-2, 0, direction * 2), ModBlocks.RESONANT_SLATE);
		placeIfAir(level, base.offset(2, 0, direction * 2), ModBlocks.RESONANT_SLATE);
	}

	private static void buildQuietFissure(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		boolean alongX = random.nextBoolean();
		int length = 3 + random.nextInt(4);
		for (int i = 0; i < length; i++) {
			int offset = i - length / 2;
			BlockPos seam = alongX ? base.below().offset(offset, 0, 0) : base.below().offset(0, 0, offset);
			if (level.getBlockState(seam).is(Blocks.END_STONE)) {
				level.setBlock(seam, i % 3 == 0
						? ModBlocks.RESONANT_SLATE.defaultBlockState()
						: ModBlocks.END_GRAY.defaultBlockState(), 3);
			}
		}
		placeIfAir(level, base.above(), ModBlocks.DORMANT_RESONANT_CRYSTAL);
	}

	private static void buildResonantOutcrop(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int side = random.nextBoolean() ? 1 : -1;
		placeIfAir(level, base, ModBlocks.RESONANT_SLATE);
		placeIfAir(level, base.offset(side, 0, 0), ModBlocks.END_GRAY);
		placeIfAir(level, base.offset(-side, 1, 0), ModBlocks.DORMANT_RESONANT_CRYSTAL);
		if (random.nextBoolean()) placeIfAir(level, base.offset(0, 1, side), ModBlocks.DORMANT_RESONANT_CRYSTAL);
	}

	/** A short jagged ridge of End stone and End Gray that breaks sightlines. */
	private static void buildFracturedSpine(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int direction = random.nextBoolean() ? 1 : -1;
		for (int i = -3; i <= 3; i++) {
			int height = 2 + Math.abs(i) % 3;
			for (int dy = 0; dy < height; dy++) {
				BlockPos pos = base.offset(i * direction, dy, 0);
				if (level.getBlockState(pos).isAir()) {
					level.setBlock(pos, (dy == height - 1 && i % 2 == 0)
							? ModBlocks.END_GRAY.defaultBlockState()
							: Blocks.END_STONE.defaultBlockState(), 3);
				}
			}
		}
		if (random.nextBoolean()) {
			placeIfAir(level, base.offset(direction * 3, 2, 0), ModBlocks.DORMANT_RESONANT_CRYSTAL);
		}
	}

	/** A broken, long-dead chorus plant with rubble at its base. */
	private static void buildDeadChorusRemnant(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int trunk = 1 + random.nextInt(3);
		for (int dy = 0; dy < trunk; dy++) {
			BlockPos pos = base.offset(0, dy, 0);
			if (level.getBlockState(pos).isAir()) {
				level.setBlock(pos, Blocks.CHORUS_PLANT.defaultBlockState(), 3);
			}
		}
		BlockPos tip = base.offset(0, trunk - 1, 0);
		placeIfAir(level, tip, Blocks.CHORUS_FLOWER);
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (random.nextFloat() < 0.4F) {
					placeIfAir(level, base.offset(i, -1, j), ModBlocks.END_GRAY);
				}
			}
		}
		if (random.nextFloat() < 0.5F) {
			placeIfAir(level, base.offset(random.nextInt(3) - 1, -1, random.nextInt(3) - 1), ModBlocks.DORMANT_RESONANT_CRYSTAL);
		}
	}

	/** A rare surface trace of dormant resonance: a short crystal seam in the stone. */
	private static void buildResonanceTrace(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int length = 2 + random.nextInt(2);
		int direction = random.nextBoolean() ? 1 : -1;
		for (int i = 0; i < length; i++) {
			BlockPos pos = base.offset(i * direction, 0, 0);
			if (level.getBlockState(pos).isAir()) {
				level.setBlock(pos, (i % 2 == 0)
						? ModBlocks.DORMANT_RESONANT_CRYSTAL.defaultBlockState()
						: ModBlocks.RESONANT_SLATE.defaultBlockState(), 3);
			}
		}
		placeIfAir(level, base.offset(-direction, -1, 0), ModBlocks.RESONANT_SLATE);
	}

	/** A grand, broken stepped terrace rising three tiers off the waste floor. */
	private static void buildVoidTerrace(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int direction = random.nextBoolean() ? 1 : -1;
		int width = 5 + random.nextInt(3);
		for (int step = 0; step < 3; step++) {
			int stepZ = direction * (1 + step);
			for (int dx = -width / 2; dx <= width / 2; dx++) {
				if ((dx + step) % 2 == 0) continue; // gaps read as ancient damage
				BlockPos pos = base.offset(dx, step, stepZ);
				if (level.getBlockState(pos).isAir()) {
					level.setBlock(pos, dx % 3 == 0
							? ModBlocks.RESONANT_SLATE.defaultBlockState()
							: ModBlocks.END_GRAY.defaultBlockState(), 3);
				}
			}
		}
		placeIfAir(level, base.offset(0, 3, direction * 3), ModBlocks.DORMANT_RESONANT_CRYSTAL);
	}

	/** A wide, weathered ring of Resonant Slate with a dormant crystal at its heart. */
	private static void buildResonantRing(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int radius = 3 + random.nextInt(2);
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				int distSq = dx * dx + dz * dz;
				if (distSq < (radius - 1) * (radius - 1) || distSq > radius * radius) continue;
				if (random.nextInt(5) == 0) continue; // broken ring
				BlockPos pos = base.offset(dx, 0, dz);
				if (level.getBlockState(pos).isAir()) {
					level.setBlock(pos, ModBlocks.RESONANT_SLATE.defaultBlockState(), 3);
				}
			}
		}
		for (int i = 0; i < 3; i++) {
			placeIfAir(level, base.offset(random.nextInt(radius * 2 + 1) - radius, 0,
					random.nextInt(radius * 2 + 1) - radius), ModBlocks.DORMANT_RESONANT_CRYSTAL);
		}
		placeIfAir(level, base.offset(0, 0, 0), ModBlocks.DORMANT_RESONANT_CRYSTAL);
	}

	/** A long, shallow scar breaks the Wastes into readable geological bands. */
	private static void buildWastesScar(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		boolean alongX = random.nextBoolean();
		int length = 6 + random.nextInt(6);
		for (int i = -length; i <= length; i++) {
			int width = 1 + (Math.abs(i) % 3 == 0 ? 1 : 0);
			for (int side = -width; side <= width; side++) {
				BlockPos pos = alongX ? base.offset(i, -1, side) : base.offset(side, -1, i);
				if (level.getBlockState(pos).is(Blocks.END_STONE)) {
					level.setBlock(pos, (i + side) % 3 == 0
							? ModBlocks.RESONANT_SLATE.defaultBlockState()
							: ModBlocks.END_GRAY.defaultBlockState(), 3);
				}
			}
		}
	}

	/** A low dead mesa creates height contrast without becoming another landmark. */
	private static void buildDeadMesa(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
		int radius = 3 + random.nextInt(3);
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius) continue;
				int height = 1 + Math.max(0, 2 - (Math.abs(dx) + Math.abs(dz)) / 3);
				for (int dy = 0; dy < height; dy++) {
					BlockPos pos = base.offset(dx, dy, dz);
					if (!level.getBlockState(pos).isAir()) continue;
					level.setBlock(pos, dy == height - 1 && random.nextInt(4) == 0
							? ModBlocks.CRACKED_SPIRE_STONE.defaultBlockState()
							: Blocks.END_STONE.defaultBlockState(), 3);
				}
			}
		}
	}

	private static void placeIfAir(WorldGenLevel level, BlockPos pos, net.minecraft.world.level.block.Block block) {
		if (level.getBlockState(pos).isAir()) level.setBlock(pos, block.defaultBlockState(), 3);
	}
}
