package com.infernodude777.endesium.world;

/**
 * Deterministic, world-seed-dependent value noise used to select Endesium
 * biomes inside the vanilla outer End.
 *
 * <p>The vanilla End biome layout (the highlands/midlands/barrens rings) is
 * fixed and seed-independent. Endesium regions use this noise so that each
 * world still gets its own arrangement of End Wastes and Chorus Wilds, while
 * staying fully deterministic for multiplayer and chunk generation.</p>
 *
 * <p>Sampling is two octaves of smoothstep-interpolated lattice value noise.
 * One sample costs a handful of 64-bit hash mixes; the biome source calls it
 * once per 4x4 column, which is negligible next to vanilla noise sampling.</p>
 */
public final class EndesiumNoise {
	/** Base lattice cell size in blocks; ~1.8 km patches across the outer End. */
	private static final double CELL_SIZE = 1800.0D;
	private static final int OCTAVES = 2;
	private static final double AMPLITUDE_FALLOFF = 0.5D;

	/** Thresholds tuned so End Wastes occupies roughly 15-20% of eligible
	 * outer-End samples (a large, sweeping region) and Chorus Wilds roughly
	 * 8-12% (a smaller, more local pocket). */
	private static final double END_WASTES_THRESHOLD = 0.58D;
	private static final double CHORUS_WILDS_THRESHOLD = 0.70D;

	// Fixed salts keep the two region noises statistically independent.
	private static final long END_WASTES_SALT = 0x1D5E5DE5L;
	private static final long CHORUS_WILDS_SALT = 0x4C47A13AL;

	private EndesiumNoise() {
	}

	public static boolean isEndWastes(long worldSeed, int blockX, int blockZ) {
		return sample(worldSeed, blockX, blockZ, END_WASTES_SALT) > END_WASTES_THRESHOLD;
	}

	public static boolean isChorusWilds(long worldSeed, int blockX, int blockZ) {
		return sample(worldSeed, blockX, blockZ, CHORUS_WILDS_SALT) > CHORUS_WILDS_THRESHOLD;
	}

	private static double sample(long worldSeed, double blockX, double blockZ, long salt) {
		double sum = 0.0D;
		double amplitude = 1.0D;
		double total = 0.0D;
		double cell = CELL_SIZE;
		for (int octave = 0; octave < OCTAVES; octave++) {
			long octaveSeed = mix(worldSeed ^ salt, octave);
			sum += amplitude * valueNoise(octaveSeed, blockX / cell, blockZ / cell);
			total += amplitude;
			amplitude *= AMPLITUDE_FALLOFF;
			cell *= AMPLITUDE_FALLOFF;
		}
		return sum / total;
	}

	/** One octave of smoothstep-interpolated value noise over unit lattice cells. */
	private static double valueNoise(long seed, double u, double v) {
		int cellX = floor(u);
		int cellZ = floor(v);
		double fracX = u - cellX;
		double fracZ = v - cellZ;
		double sx = smoothstep(fracX);
		double sz = smoothstep(fracZ);
		double v00 = hash01(seed, cellX, cellZ);
		double v10 = hash01(seed, cellX + 1, cellZ);
		double v01 = hash01(seed, cellX, cellZ + 1);
		double v11 = hash01(seed, cellX + 1, cellZ + 1);
		double a = v00 + (v10 - v00) * sx;
		double b = v01 + (v11 - v01) * sx;
		return a + (b - a) * sz;
	}

	private static double smoothstep(double t) {
		return t * t * (3.0D - 2.0D * t);
	}

	private static int floor(double value) {
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

	/** Hash a coordinate pair plus a seed into a uniform [0, 1) value. */
	private static double hash01(long seed, int x, int z) {
		return (mix(seed, x * 0x9E3779B97F4A7C15L ^ z * 0xBF58476D1CE4E5B9L) >>> 11) * (1.0D / 9007199254740992.0D);
	}

	/** SplitMix64 finalizer; a cheap, well-distributed integer mixer. */
	private static long mix(long value, long extra) {
		long h = value ^ extra;
		h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
		h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
		return h ^ (h >>> 31);
	}
}
