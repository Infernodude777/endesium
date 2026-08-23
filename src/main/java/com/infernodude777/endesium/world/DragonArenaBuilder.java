package com.infernodude777.endesium.world;

import com.infernodude777.endesium.block.InscribedSlateBlock;
import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/**
 * Sculpts the central End island as a natural End landscape with an ancient
 * Dragon arena embedded in it — NOT as a circular ruin.
 *
 * <p>Order mirrors the design hierarchy: terrain first (an irregular island
 * silhouette raised and carved into ridges, plateaus, terraces, basins, and
 * void cliffs), then a few major fractures and collapsed hollows, then a
 * restrained broken Resonance Ring, then a handful of sparse, varied ruins.
 * End stone dominates; Endesium materials appear only as veins and accents.</p>
 *
 * <p>Every function is a pure function of (world seed, x, z), so each chunk
 * draws only its own slice and neighbouring chunks stitch seamlessly with no
 * cross-chunk writes. Vanilla pillars, crystals, the portal, and the platform
 * are never touched.</p>
 */
public final class DragonArenaBuilder {
	private DragonArenaBuilder() {
	}

	public static void build(WorldGenLevel level, ChunkPos chunk, RandomSource random) {
		long seed = level.getSeed();
		sculptTerrain(level, chunk, seed);
		carveFractures(level, chunk, seed);
		carveHollows(level, chunk, seed);
		resonanceRing(level, chunk, seed);
		arenaRemnants(level, chunk, seed);
		structures(level, chunk, seed);
		crystalSeams(level, chunk, seed);
	}

	private static boolean inChunk(ChunkPos chunk, int x, int z) {
		return x >= chunk.getMinBlockX() && x <= chunk.getMaxBlockX()
				&& z >= chunk.getMinBlockZ() && z <= chunk.getMaxBlockZ();
	}

	private static int surfaceTopY(WorldGenLevel level, int x, int z) {
		// The WORLD_SURFACE_WG heightmap is NOT updated by setBlock during
		// decoration, so after sculptTerrain raises or lowers terrain the map is
		// stale. Walk from the map value to the actual top solid block instead.
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
		while (y < 126 && !level.getBlockState(pos.setY(y + 1)).isAir()) y++;
		while (y > 0 && level.getBlockState(pos.setY(y)).isAir()) y--;
		return y;
	}

	// ------------------------------------------------------------------
	// Terrain sculpt: raise, carve, and extend shelves to the height field.
	// ------------------------------------------------------------------

	private static void sculptTerrain(WorldGenLevel level, ChunkPos chunk, long seed) {
		for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
			for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
				if (!ArenaGeometry.isLand(seed, x, z)) continue;
				double r = Math.sqrt((double) x * x + (double) z * z);
				if (r < 6.0D) continue; // keep the portal and platform clear
				if (ArenaGeometry.isNearPillar(x, z, 4.5D)) continue;

				int target = ArenaGeometry.heightAt(seed, x, z);
				int currentTop = surfaceTopY(level, x, z);

				if (currentTop < 48) {
					// Void column: build a fresh shelf with an underside.
					for (int y = ArenaGeometry.SHELF_BOTTOM; y <= target; y++) {
						setEndStone(level, new BlockPos(x, y, z));
					}
				} else if (target > currentTop) {
					for (int y = currentTop + 1; y <= target; y++) {
						setEndStone(level, new BlockPos(x, y, z));
					}
				} else if (target < currentTop) {
					for (int y = currentTop; y > target; y--) {
						removeEndStone(level, new BlockPos(x, y, z));
					}
				}
			}
		}
	}

	private static void setEndStone(WorldGenLevel level, BlockPos pos) {
		BlockState current = level.getBlockState(pos);
		if (current.isAir() || current.is(Blocks.END_STONE)) {
			level.setBlock(pos, Blocks.END_STONE.defaultBlockState(), 3);
		}
	}

	private static void removeEndStone(WorldGenLevel level, BlockPos pos) {
		if (level.getBlockState(pos).is(Blocks.END_STONE)) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		}
	}

	// ------------------------------------------------------------------
	// Fractures: a few major, meandering trenches.
	// ------------------------------------------------------------------

	private static void carveFractures(WorldGenLevel level, ChunkPos chunk, long seed) {
		// Column-driven so the whole crevasse (including its wide shoulders) is
		// carved coherently regardless of which chunk a given sample lands in.
		for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
			for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
				if (ArenaGeometry.isNearPillar(x, z, 3.0D)) continue;
				double distance = ArenaGeometry.distanceToFracture(seed, x, z);
				double width = ArenaGeometry.fractureWidth(seed, x, z);
				if (distance > width) continue;
				int y = surfaceTopY(level, x, z);
				if (y < 50) continue;
				// Deeper near the centerline, shallower at the shoulders. The two
				// major scars reach into the hollow; minor seams remain readable
				// without making the island look shredded.
				double edge = Math.clamp(distance / width, 0.0D, 1.0D);
				int depth = 3 + (int) Math.round((1.0D - edge) * (width > 4.0D ? 22.0D : 5.0D));
				for (int d = 0; d <= depth; d++) {
					BlockPos pos = new BlockPos(x, y - d, z);
					if (d == depth) {
						if (level.getBlockState(pos).is(Blocks.END_STONE)) {
							level.setBlock(pos, ModBlocks.RESONANT_SLATE.defaultBlockState(), 3);
						}
					} else {
						removeEndStone(level, pos);
					}
				}
				// Sparse dormant crystals along the deepest line.
				if (edge < 0.25D && ArenaGeometry.valueNoise(seed, x, z) > 0.96D) {
					placeIfEndStone(level, new BlockPos(x, y - depth + 1, z), ModBlocks.DORMANT_RESONANT_CRYSTAL);
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// Hollows: a few collapsed openings that hint at what is below.
	// ------------------------------------------------------------------

	private static void carveHollows(WorldGenLevel level, ChunkPos chunk, long seed) {
		RandomSource random = RandomSource.create(seed ^ 0x484F4C4CL);			int count = 2 + random.nextInt(2);
			for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double radius = 42.0D + random.nextDouble() * 46.0D;
			int cx = (int) Math.round(Math.cos(angle) * radius);
			int cz = (int) Math.round(Math.sin(angle) * radius);
			if (ArenaGeometry.isNearPillar(cx, cz, 6.0D) || !ArenaGeometry.isLand(seed, cx, cz)) continue;
			int halfWidth = i == 0 ? 2 : 1;
			for (int x = cx - halfWidth; x <= cx + halfWidth; x++) {
				for (int z = cz - halfWidth; z <= cz + halfWidth; z++) {
					if (!inChunk(chunk, x, z)) continue;
					int y = surfaceTopY(level, x, z);
					if (y < 50) continue;
					boolean center = x == cx && z == cz;
					for (int depth = 0; depth <= (i == 0 ? 7 : 5); depth++) {
						BlockPos pos = new BlockPos(x, y - depth, z);
						if (!level.getBlockState(pos).is(Blocks.END_STONE)) continue;
						if (center && depth == (i == 0 ? 7 : 5)) {
							level.setBlock(pos, ModBlocks.DORMANT_RESONANT_CRYSTAL.defaultBlockState(), 3);
						} else if (center && depth == (i == 0 ? 6 : 4)) {
							level.setBlock(pos, ModBlocks.INSCRIBED_SLATE.defaultBlockState(), 3);
						} else if (!center && depth >= 2) {
							level.setBlock(pos, ModBlocks.END_GRAY.defaultBlockState(), 3);
						} else {
							level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
						}
					}
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// Resonance Ring: a broken, restrained circle of ancient markings.
	// ------------------------------------------------------------------

	private static void resonanceRing(WorldGenLevel level, ChunkPos chunk, long seed) {
		for (int angle = 0; angle < 360; angle += 6) {
			if (!ArenaGeometry.ringArcPresent(seed, angle)) continue;
			double radians = Math.toRadians(angle);
			int x = (int) Math.round(Math.cos(radians) * ArenaGeometry.RING_RADIUS);
			int z = (int) Math.round(Math.sin(radians) * ArenaGeometry.RING_RADIUS);
			if (!inChunk(chunk, x, z)) continue;
			int y = surfaceTopY(level, x, z);
			if (y < 50) continue;
			int symbol = angle % 120 == 0 ? InscribedSlateBlock.SYMBOL_EYE : InscribedSlateBlock.SYMBOL_RING;
			placeOnEndStone(level, new BlockPos(x, y, z), ModBlocks.INSCRIBED_SLATE.defaultBlockState()
					.setValue(InscribedSlateBlock.SYMBOL, symbol));
			// Occasional inward channel (a Resonance seam feeding the ring),
			// placed by noise rather than a fixed angular interval.
			if (ArenaGeometry.fbm(seed, Math.cos(radians) * 3.0D + 19.0D, Math.sin(radians) * 3.0D - 6.0D) > 0.72D) {
				int x2 = (int) Math.round(Math.cos(radians) * (ArenaGeometry.RING_RADIUS - 3.0D));
				int z2 = (int) Math.round(Math.sin(radians) * (ArenaGeometry.RING_RADIUS - 3.0D));
				if (inChunk(chunk, x2, z2)) {
					placeOnEndStone(level, new BlockPos(x2, surfaceTopY(level, x2, z2), z2), ModBlocks.RESONANT_SLATE);
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// Arena remnants: sparse broken stone around the arena rim.
	// ------------------------------------------------------------------

	private static void arenaRemnants(WorldGenLevel level, ChunkPos chunk, long seed) {
		// Sparse, irregular scatter around the arena rim — a few fallen stones,
		// never a continuous wall.
		for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
			for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
				double r = Math.sqrt((double) x * x + (double) z * z);
				if (r < 10.0D || r > 38.0D) continue;
				double n = ArenaGeometry.fbm(seed + 1901L, x * 0.075D + 3.0D, z * 0.075D - 4.0D);
				if (n < 0.92D) continue;
				int y = surfaceTopY(level, x, z);
				if (y < 50) continue;
				if (n > 0.97D) {
					placeOnEndStone(level, new BlockPos(x, y, z), Blocks.END_STONE_BRICKS);
					placeIfAir(level, new BlockPos(x, y + 1, z), ModBlocks.RESONANT_PILLAR);
				} else {
					placeOnEndStone(level, new BlockPos(x, y, z), ModBlocks.CRACKED_SPIRE_STONE);
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// Structures: one major broken ruin and three distinct medium ruins.
	// ------------------------------------------------------------------

	private static void structures(WorldGenLevel level, ChunkPos chunk, long seed) {
		majorStructure(level, chunk, seed);
		mediumRuins(level, chunk, seed);
	}

	private static double[] structurePosition(long seed, long salt, double minRadius, double maxRadius) {
		double angle = ArenaGeometry.fbm(seed, salt * 1.7D, salt * 0.9D) * Math.PI * 2.0D;
		double radius = minRadius + ArenaGeometry.fbm(seed, salt * 0.3D, salt * 2.1D) * (maxRadius - minRadius);
		double x = Math.cos(angle) * radius;
		double z = Math.sin(angle) * radius;
		if (ArenaGeometry.isNearPillar(x, z, 10.0D)) {
			angle += 1.1D;
			x = Math.cos(angle) * radius;
			z = Math.sin(angle) * radius;
		}
		return new double[] { x, z };
	}

	private static void majorStructure(WorldGenLevel level, ChunkPos chunk, long seed) {
		double[] p = structurePosition(seed, 0x4F4253L, 58.0D, 96.0D);
		int cx = (int) Math.round(p[0]);
		int cz = (int) Math.round(p[1]);
		// A collapsed observatory: a few buried remnants, not a complete ring.
		for (int angle = 0; angle < 360; angle += 30) {
			double radians = Math.toRadians(angle);
			int x = cx + (int) Math.round(Math.cos(radians) * 6.0D);
			int z = cz + (int) Math.round(Math.sin(radians) * 6.0D);
			if (!inChunk(chunk, x, z)) continue;
			if (ArenaGeometry.isNearPillar(x, z, 3.0D)) continue;
			int y = surfaceTopY(level, x, z);
			if (y < 50) continue;
			double intact = ArenaGeometry.valueNoise(seed + 2303L, x, z);
			if (intact < 0.66D) continue; // most of it collapsed or is buried
			placeOnEndStone(level, new BlockPos(x, y, z), ModBlocks.CRACKED_SPIRE_STONE);
			if (intact > 0.88D) {
				placeIfAir(level, new BlockPos(x, y + 1, z), ModBlocks.END_GRAY);
			}
		}
		// Fallen pillar stub and a dormant crystal at the heart.
		if (inChunk(chunk, cx, cz)) {
			int y = surfaceTopY(level, cx, cz);
			if (y >= 50) {
				placeOnEndStone(level, new BlockPos(cx, y, cz), ModBlocks.RESONANT_SLATE);
				placeIfAir(level, new BlockPos(cx, y + 1, cz), ModBlocks.DORMANT_RESONANT_CRYSTAL);
			}
		}
	}

	private static void mediumRuins(WorldGenLevel level, ChunkPos chunk, long seed) {
		// RUIN A: broken Resonance platform.
		ruinA(level, chunk, seed);
		// RUIN B: collapsed watchtower.
		ruinB(level, chunk, seed);
		// RUIN C: half-buried gateway.
		ruinC(level, chunk, seed);
	}

	private static void ruinA(WorldGenLevel level, ChunkPos chunk, long seed) {
		double[] p = structurePosition(seed, 0x415052L, 34.0D, 78.0D);
		int cx = (int) Math.round(p[0]);
		int cz = (int) Math.round(p[1]);
		for (int x = cx - 1; x <= cx + 1; x++) {
			for (int z = cz - 1; z <= cz + 1; z++) {
				if (!inChunk(chunk, x, z)) continue;
				if (ArenaGeometry.isNearPillar(x, z, 3.0D)) continue;
				int y = surfaceTopY(level, x, z);
				if (y < 50) continue;
				if (x == cx && z == cz) {
					placeOnEndStone(level, new BlockPos(x, y, z), ModBlocks.INSCRIBED_SLATE);
				} else if (ArenaGeometry.valueNoise(seed, x, z) > 0.4D) {
					placeOnEndStone(level, new BlockPos(x, y, z), ModBlocks.RESONANT_SLATE);
				}
			}
		}
		if (inChunk(chunk, cx, cz)) {
			placeIfAir(level, new BlockPos(cx, surfaceTopY(level, cx, cz) + 1, cz), ModBlocks.DORMANT_RESONANT_CRYSTAL);
		}
	}

	private static void ruinB(WorldGenLevel level, ChunkPos chunk, long seed) {
		double[] p = structurePosition(seed, 0x425752L, 40.0D, 88.0D);
		int cx = (int) Math.round(p[0]);
		int cz = (int) Math.round(p[1]);
		int height = 3 + (int) (ArenaGeometry.valueNoise(seed, cx, cz) * 3.0D);
		for (int x = cx; x <= cx + 1; x++) {
			for (int z = cz; z <= cz + 1; z++) {
				if (!inChunk(chunk, x, z)) continue;
				if (ArenaGeometry.isNearPillar(x, z, 3.0D)) continue;
				int y = surfaceTopY(level, x, z);
				if (y < 50) continue;
				for (int dy = 0; dy < height; dy++) {
					Block block = dy == height - 1 ? ModBlocks.CRACKED_SPIRE_STONE : Blocks.END_STONE_BRICKS;
					placeIfAir(level, new BlockPos(x, y + 1 + dy, z), block);
				}
			}
		}
	}

	private static void ruinC(WorldGenLevel level, ChunkPos chunk, long seed) {
		double[] p = structurePosition(seed, 0x435741L, 46.0D, 92.0D);
		int cx = (int) Math.round(p[0]);
		int cz = (int) Math.round(p[1]);
		// Two worn gateway pillars; one has collapsed.
		int[][] pillars = { {0, 0}, {4, 0} };
		for (int i = 0; i < pillars.length; i++) {
			int x = cx + pillars[i][0];
			int z = cz + pillars[i][1];
			if (!inChunk(chunk, x, z)) continue;
			if (ArenaGeometry.isNearPillar(x, z, 3.0D)) continue;
			int y = surfaceTopY(level, x, z);
			if (y < 50) continue;
			int height = i == 0 ? 3 : 1; // the second pillar has fallen
			for (int dy = 0; dy < height; dy++) {
				placeIfAir(level, new BlockPos(x, y + 1 + dy, z), Blocks.END_STONE_BRICKS);
			}
		}
	}

	// ------------------------------------------------------------------
	// Crystal seams: sparse Resonance veins in the terrain.
	// ------------------------------------------------------------------

	private static void crystalSeams(WorldGenLevel level, ChunkPos chunk, long seed) {
		// Low-frequency "vein" envelope gates a high-frequency detail field, so
		// Resonant Slate appears as sparse, winding seams rather than uniform
		// speckle across the whole island.
		for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
			for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
				double vein = ArenaGeometry.fbm(seed + 2701L, x * 0.018D + 21.0D, z * 0.018D - 13.0D);
				if (vein < 0.80D) continue;
				double detail = ArenaGeometry.valueNoise(seed, x * 3, z * 3);
				if (detail < 0.72D) continue;
				double r = Math.sqrt((double) x * x + (double) z * z);
				if (r < 12.0D || r > 104.0D) continue;
				if (ArenaGeometry.isNearPillar(x, z, 3.0D)) continue;
				int y = surfaceTopY(level, x, z);
				if (y < 50) continue;
				placeOnEndStone(level, new BlockPos(x, y, z), ModBlocks.RESONANT_SLATE);
				if (vein > 0.90D && detail > 0.92D) {
					placeIfAir(level, new BlockPos(x, y + 1, z), ModBlocks.DORMANT_RESONANT_CRYSTAL);
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// Placement helpers.
	// ------------------------------------------------------------------

	private static void placeOnEndStone(WorldGenLevel level, BlockPos pos, BlockState state) {
		BlockState below = level.getBlockState(pos.below());
		if (below.is(Blocks.END_STONE) || below.is(ModBlocks.END_GRAY)
				|| below.is(ModBlocks.RESONANT_SLATE)) {
			BlockState current = level.getBlockState(pos);
			if (current.isAir() || current.is(Blocks.END_STONE)) {
				level.setBlock(pos, state, 3);
			}
		}
	}

	private static void placeOnEndStone(WorldGenLevel level, BlockPos pos, Block block) {
		placeOnEndStone(level, pos, block.defaultBlockState());
	}

	private static void placeIfEndStone(WorldGenLevel level, BlockPos pos, Block block) {
		if (level.getBlockState(pos).is(Blocks.END_STONE)) {
			level.setBlock(pos, block.defaultBlockState(), 3);
		}
	}

	private static void placeIfAir(WorldGenLevel level, BlockPos pos, Block block) {
		if (level.getBlockState(pos).isAir()) {
			level.setBlock(pos, block.defaultBlockState(), 3);
		}
	}
}
