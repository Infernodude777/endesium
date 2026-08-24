package com.infernodude777.endesium.world;

/**
 * Deterministic, biome-specific terrain relief for the outer-End islands.
 *
 * <p>The vanilla End noise router shapes the floating island mass; Endesium
 * layers a smooth, domain-warped height field on top of it. Every value is a
 * pure function of the world seed and the absolute column, so chunks agree at
 * their borders while the surface stays continuous: no terracing, no stacked
 * columns, no grid-aligned artifacts. Each region gets its own landform
 * character - ridged highlands, anisotropic dune fields, sunken marsh basins,
 * flat-topped dark mesas - all built from the same continuous math.</p>
 */
public final class BiomeTerrain {
	private BiomeTerrain() {
	}

	/**
	 * Smooth vertical offset (in blocks, fractional) to apply to a column's
	 * current surface. Positive raises terrain; negative carves basins. The
	 * sample coordinates are domain-warped by a second low-frequency noise so
	 * no landform ever reads against the noise grid.
	 */
	public static double offsetAt(int region, long seed, double x, double z) {
		double warpX = (fbm(seed + 91L, x * 0.0085D, z * 0.0085D) - 0.5D) * 26.0D;
		double warpZ = (fbm(seed + 137L, x * 0.0085D + 5.3D, z * 0.0085D + 1.7D) - 0.5D) * 26.0D;
		double wx = x + warpX;
		double wz = z + warpZ;
		return switch (region) {
			case EndesiumRegions.SHATTERED_HIGHLANDS -> highlands(seed, wx, wz);
			case EndesiumRegions.VOID_MARSHES -> marsh(seed, wx, wz);
			case EndesiumRegions.CHORUS_WILDS -> wilds(seed, wx, wz);
			case EndesiumRegions.LUMINOUS_GROVES -> groves(seed, wx, wz);
			case EndesiumRegions.ASHEN_EXPANSE -> ashen(seed, wx, wz);
			case EndesiumRegions.CRYSTAL_BARRENS -> barrens(seed, wx, wz);
			case EndesiumRegions.VOID_SKIRTS -> skirts(seed, wx, wz);
			case EndesiumRegions.VOID_CROWN -> crown(seed, wx, wz);
			case EndesiumRegions.UMBRAL_REACH -> reach(seed, wx, wz);
			default -> wastes(seed, wx, wz);
		};
	}

	/** Broad barren plains with rare smooth-rising mesas. */
	private static double wastes(long seed, double wx, double wz) {
		double n = fbm(seed + 3011L, wx * 0.016D, wz * 0.016D);
		double offset = (n - 0.5D) * 10.0D;
		double mesa = fbm(seed + 3071L, wx * 0.03D + 9.0D, wz * 0.03D - 4.0D);
		offset += smoothstep(0.60D, 0.80D, mesa) * 18.0D;
		return offset + detail(seed, 3013L, wx, wz, 0.09D, 1.6D);
	}

	/** Ridged highlands: sharp broken crests falling away into smooth flanks. */
	private static double highlands(long seed, double wx, double wz) {
		double n = fbm(seed + 4011L, wx * 0.017D, wz * 0.017D);
		double ridge = 1.0D - Math.abs(2.0D * n - 1.0D);
		double shaped = Math.pow(ridge, 1.7D);
		double offset = (shaped - 0.38D) * 30.0D;
		return offset + detail(seed, 4013L, wx, wz, 0.07D, 2.2D);
	}

	/** Sunken marsh basins with broad, low separating levees. */
	private static double marsh(long seed, double wx, double wz) {
		double n = fbm(seed + 5011L, wx * 0.022D, wz * 0.022D);
		double offset = (n - 0.60D) * 30.0D;
		if (offset < -13.0D) {
			offset = -13.0D;
		}
		return offset + detail(seed, 5013L, wx, wz, 0.08D, 1.2D);
	}

	/** Rolling, layered root-bound hills with soft clearings. */
	private static double wilds(long seed, double wx, double wz) {
		double n = fbm(seed + 6011L, wx * 0.02D, wz * 0.02D);
		double offset = (n - 0.5D) * 13.0D;
		double mound = fbm(seed + 6071L, wx * 0.045D + 3.0D, wz * 0.045D - 2.0D);
		offset += smoothstep(0.58D, 0.78D, mound) * 11.0D;
		return offset + detail(seed, 6013L, wx, wz, 0.09D, 1.4D);
	}

	/** Gentle glowing hollows and soft rises. */
	private static double groves(long seed, double wx, double wz) {
		double n = fbm(seed + 4007L, wx * 0.024D, wz * 0.024D);
		double offset = (n - 0.5D) * 9.0D;
		double hollow = fbm(seed + 4047L, wx * 0.05D + 6.0D, wz * 0.05D + 2.0D);
		offset -= smoothstep(0.60D, 0.78D, hollow) * 4.5D;
		return offset + detail(seed, 4009L, wx, wz, 0.1D, 1.0D);
	}

	/** Anisotropic ash dunes with broad volcanic swells. */
	private static double ashen(long seed, double wx, double wz) {
		double n = fbm(seed + 7011L, wx * 0.03D, wz * 0.03D);
		double dune = Math.sin((wx * 0.72D + wz * 0.28D) * 0.085D + n * 5.0D) * 2.4D;
		double swell = fbm(seed + 7091L, wx * 0.012D + 3.0D, wz * 0.012D - 8.0D);
		double offset = dune + (n - 0.5D) * 7.0D + smoothstep(0.62D, 0.80D, swell) * 15.0D;
		return offset + detail(seed, 7013L, wx, wz, 0.11D, 0.9D);
	}

	/** Jagged mineral shelves: ridged stone with a fine crystal-chatter octave. */
	private static double barrens(long seed, double wx, double wz) {
		double n = fbm(seed + 8011L, wx * 0.02D, wz * 0.02D);
		double ridge = 1.0D - Math.abs(2.0D * n - 1.0D);
		double offset = Math.pow(ridge, 2.2D) * 17.0D - 4.0D;
		return offset + detail(seed, 8013L, wx, wz, 0.13D, 2.0D);
	}

	/** A vast, near-flat dark void floor with broad swells and shallow sinks. */
	private static double skirts(long seed, double wx, double wz) {
		double n = fbm(seed + 9011L, wx * 0.014D, wz * 0.014D);
		double offset = (n - 0.5D) * 8.0D;
		double sink = fbm(seed + 9071L, wx * 0.06D + 2.0D, wz * 0.06D - 5.0D);
		offset -= smoothstep(0.70D, 0.86D, sink) * 3.5D;
		return offset + detail(seed, 9013L, wx, wz, 0.08D, 0.8D);
	}

	/** Flat-topped dark mesas rising out of the void floor. */
	private static double crown(long seed, double wx, double wz) {
		double n = fbm(seed + 10011L, wx * 0.018D, wz * 0.018D);
		double plateau = smoothstep(0.42D, 0.58D, n);
		double edge = fbm(seed + 10071L, wx * 0.05D + 7.0D, wz * 0.05D - 3.0D);
		double offset = plateau * 17.0D + (edge - 0.5D) * 6.0D - 5.0D;
		return offset + detail(seed, 10013L, wx, wz, 0.08D, 1.3D);
	}

	/** Deep fissured basins: the lowest, darkest region. */
	private static double reach(long seed, double wx, double wz) {
		double n = fbm(seed + 11011L, wx * 0.026D, wz * 0.026D);
		double offset = (n - 0.58D) * 26.0D;
		if (offset < -14.0D) {
			offset = -14.0D;
		}
		if (offset > 8.0D) {
			offset = 8.0D;
		}
		return offset + detail(seed, 11013L, wx, wz, 0.09D, 1.1D);
	}

	/** A fine high-frequency octave so surfaces never read as bare low bands. */
	private static double detail(long seed, long salt, double wx, double wz, double freq, double amp) {
		return (fbm(seed + salt, wx * freq + 11.0D, wz * freq - 7.0D) - 0.5D) * 2.0D * amp;
	}

	private static double fbm(long seed, double x, double z) {
		return ArenaGeometry.fbm(seed, x, z);
	}

	private static double smoothstep(double edge0, double edge1, double value) {
		double t = (value - edge0) / (edge1 - edge0);
		if (t < 0.0D) {
			t = 0.0D;
		} else if (t > 1.0D) {
			t = 1.0D;
		}
		return t * t * (3.0D - 2.0D * t);
	}
}
