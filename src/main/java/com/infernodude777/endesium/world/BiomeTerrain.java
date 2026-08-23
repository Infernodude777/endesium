package com.infernodude777.endesium.world;

/**
 * Deterministic, biome-specific relief for the outer-End islands.
 *
 * <p>The vanilla End noise router already shapes the floating island mass;
 * Endesium layers per-region relief on top without touching that router. Every
 * offset is a pure function of the world seed and the absolute column, so the
 * terrain feature can be invoked once per chunk and still produce seamless
 * ridges, basins, and shelves across chunk borders.</p>
 */
public final class BiomeTerrain {
	private BiomeTerrain() {
	}

	/**
	 * Vertical offset in blocks to apply to a column's current surface.
	 * Positive offsets raise terrain; negative offsets carve basins.
	 */
	public static int offsetAt(int region, long seed, double x, double z) {
		switch (region) {
			case EndesiumRegions.SHATTERED_HIGHLANDS:
				return highlandsOffset(seed, x, z);
			case EndesiumRegions.VOID_MARSHES:
				return marshOffset(seed, x, z);
			case EndesiumRegions.CHORUS_WILDS:
				return wildsOffset(seed, x, z);
			case EndesiumRegions.LUMINOUS_GROVES:
				return (int) Math.round((ArenaGeometry.fbm(seed + 4007L, x * 0.03D, z * 0.03D) - 0.5D) * 8.0D);
			case EndesiumRegions.ASHEN_EXPANSE:
				return ashenOffset(seed, x, z);
			case EndesiumRegions.CRYSTAL_BARRENS:
				return barrensOffset(seed, x, z);
			case EndesiumRegions.VOID_SKIRTS:
				return skirtsOffset(seed, x, z);
			case EndesiumRegions.VOID_CROWN:
				return crownOffset(seed, x, z);
			case EndesiumRegions.UMBRAL_REACH:
				return reachOffset(seed, x, z);
			case EndesiumRegions.END_WASTES:
			default:
				return wastesOffset(seed, x, z);
		}
	}

	private static int wastesOffset(long seed, double x, double z) {
		// Broad, barren, gently rolling plateaus with the occasional raised mesa.
		double roll = ArenaGeometry.fbm(seed + 3011L, x * 0.022D, z * 0.022D);
		int offset = (int) Math.round((roll - 0.5D) * 7.0D);
		double mesa = ArenaGeometry.fbm(seed + 3071L, x * 0.05D + 9.0D, z * 0.05D - 4.0D);
		if (mesa > 0.72D) {
			offset += (int) Math.round((mesa - 0.72D) * 40.0D);
		}
		return offset;
	}

	private static int highlandsOffset(long seed, double x, double z) {
		// Terraced ridges: quantization gives broad broken shelves instead of
		// smooth, conventional mountains. This pass pushes the tops higher so
		// the Highlands read as genuinely shattered.
		double n = ArenaGeometry.fbm(seed + 4011L, x * 0.02D, z * 0.02D);
		double shelf = Math.floor(n * 3.0D) / 3.0D;
		int offset = (int) Math.round((shelf - 0.5D) * 2.0D * 26.0D);
		// Meso cliffs: a second finer layer breaks shelf tops.
		double cliff = ArenaGeometry.fbm(seed + 4071L, x * 0.06D + 5.0D, z * 0.06D - 7.0D);
		if (cliff > 0.62D) {
			offset += (int) Math.round((cliff - 0.62D) * 20.0D);
		}
		return offset;
	}

	private static int marshOffset(long seed, double x, double z) {
		// Shallow enclosed basins with low separating ridges. Basins run a
		// little deeper so the marsh reads as a real lowland.
		double n = ArenaGeometry.fbm(seed + 5011L, x * 0.03D, z * 0.03D);
		if (n > 0.60D) {
			return (int) Math.round((n - 0.60D) * 12.0D);
		}
		return -5 - (int) Math.round((0.60D - n) * 11.0D);
	}

	private static int wildsOffset(long seed, double x, double z) {
		// Rolling, layered, root-bound hills with clearings.
		double roll = ArenaGeometry.fbm(seed + 6011L, x * 0.025D, z * 0.025D);
		int offset = (int) Math.round((roll - 0.5D) * 10.0D);
		double mound = ArenaGeometry.fbm(seed + 6071L, x * 0.07D + 3.0D, z * 0.07D - 2.0D);
		if (mound > 0.70D) {
			offset += (int) Math.round((mound - 0.70D) * 24.0D);
		}
		return offset;
	}

	private static int ashenOffset(long seed, double x, double z) {
		// Low ash dunes with occasional impact craters and rare volcanic swells
		// that hint at the dormant volcanoes the structure feature can place.
		double dune = ArenaGeometry.fbm(seed + 7011L, x * 0.04D, z * 0.04D);
		int offset = (int) Math.round((dune - 0.5D) * 6.0D);
		double crater = ArenaGeometry.fbm(seed + 7071L, x * 0.08D + 6.0D, z * 0.08D - 1.0D);
		if (crater > 0.78D) {
			offset -= 5;
		}
		double swell = ArenaGeometry.fbm(seed + 7091L, x * 0.015D + 3.0D, z * 0.015D - 8.0D);
		if (swell > 0.70D) {
			offset += (int) Math.round((swell - 0.70D) * 30.0D);
		}
		return offset;
	}

	private static int barrensOffset(long seed, double x, double z) {
		// Rocky, mineral shelves rising out of the stone — jagged and layered.
		double n = ArenaGeometry.fbm(seed + 8011L, x * 0.028D, z * 0.028D);
		double shelf = Math.floor(n * 2.0D) / 2.0D;
		int offset = (int) Math.round((shelf - 0.45D) * 2.0D * 12.0D);
		double spike = ArenaGeometry.fbm(seed + 8071L, x * 0.05D + 2.0D, z * 0.05D - 6.0D);
		if (spike > 0.72D) {
			offset += (int) Math.round((spike - 0.72D) * 16.0D);
		}
		return offset;
	}

	private static int skirtsOffset(long seed, double x, double z) {
		// The Void Skirts: a vast, relatively flat dark plain. Broad gentle
		// swells with the occasional shallow sinkhole — deliberately not
		// mountainous, so it reads as a flat void floor.
		double n = ArenaGeometry.fbm(seed + 9011L, x * 0.02D, z * 0.02D);
		int offset = (int) Math.round((n - 0.5D) * 6.0D);
		double sink = ArenaGeometry.fbm(seed + 9071L, x * 0.09D + 2.0D, z * 0.09D - 5.0D);
		if (sink > 0.74D) {
			offset -= 3;
		}
		return offset;
	}

	private static int crownOffset(long seed, double x, double z) {
		// The Void Crown: elevated dark mesas with flat, broken tops.
		double n = ArenaGeometry.fbm(seed + 10011L, x * 0.024D, z * 0.024D);
		double shelf = Math.floor(n * 2.0D) / 2.0D;
		int offset = (int) Math.round((shelf - 0.5D) * 2.0D * 18.0D);
		double edge = ArenaGeometry.fbm(seed + 10071L, x * 0.06D + 7.0D, z * 0.06D - 3.0D);
		if (edge > 0.66D) {
			offset += (int) Math.round((edge - 0.66D) * 14.0D);
		}
		return offset;
	}

	private static int reachOffset(long seed, double x, double z) {
		// The Umbral Reach: low basins and fissures, the deepest void region.
		double n = ArenaGeometry.fbm(seed + 11011L, x * 0.032D, z * 0.032D);
		if (n > 0.58D) {
			return (int) Math.round((n - 0.58D) * 10.0D);
		}
		return -5 - (int) Math.round((0.58D - n) * 12.0D);
	}
}
