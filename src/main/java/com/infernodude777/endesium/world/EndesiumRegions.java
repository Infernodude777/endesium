package com.infernodude777.endesium.world;

import net.minecraft.util.Mth;

/**
 * Deterministic, large-scale regional assignment for the Endesium biomes.
 *
 * <p>Vanilla End biomes (highlands/midlands/barrens/islands) form a fixed,
 * seed-independent ring system. Endesium replaces the habitable highlands and
 * midlands with ten continent-scale regions instead of scattered noise
 * patches. Regions span hundreds to well over a thousand blocks and form long
 * geographic belts, so a player walking through one biome stays inside it for
 * a meaningful amount of time.</p>
 *
 * <p><b>Adjacency guarantee.</b> The {@code END_WASTES} and {@code CHORUS_WILDS}
 * biomes must never touch. The assignment is a jittered square lattice whose
 * cells are colored {@code (gx + gz + offset) mod 10}. Every 8-connected grid
 * neighbor differs by at most &plusmn;2 in that sum, while Wastes (index 0) and
 * Wilds (index 3) differ by 3 &mdash; so they can never share an edge. Jitter
 * stays below 0.5 of a cell, which preserves the lattice's Voronoi adjacency,
 * and the bands are further differentiated by per-seed cell-size modulation so
 * some belts read as vast while others narrow.</p>
 */
public final class EndesiumRegions {
	public static final int END_WASTES = 0;
	public static final int SHATTERED_HIGHLANDS = 1;
	public static final int VOID_MARSHES = 2;
	public static final int CHORUS_WILDS = 3;
	public static final int LUMINOUS_GROVES = 4;
	public static final int ASHEN_EXPANSE = 5;
	public static final int CRYSTAL_BARRENS = 6;
	public static final int VOID_SKIRTS = 7;
	public static final int VOID_CROWN = 8;
	public static final int UMBRAL_REACH = 9;

	public static final int COUNT = 10;

	/** Base lattice cell size in blocks: large, continent-scale regions. */
	private static final double BASE_CELL_SIZE = 1500.0D;
	/** Maximum point jitter as a fraction of the cell size (kept &lt; 0.5). */
	private static final double JITTER = 0.34D;
	/** Slow per-seed cell-size modulation so belts vary in width. */
	private static final double CELL_VARIATION = 0.38D;

	private EndesiumRegions() {
	}

	/** Returns the Endesium region index for a block column, or -1 for vanilla. */
	public static int regionAt(long worldSeed, int blockX, int blockZ) {
		double cell = cellSize(worldSeed, blockX, blockZ);
		double u = blockX / cell;
		double v = blockZ / cell;
		int gx = floor(u);
		int gz = floor(v);

		// The nearest lattice point is always within one cell in each axis.
		double best = Double.POSITIVE_INFINITY;
		int bestGx = gx;
		int bestGz = gz;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				int cx = gx + dx;
				int cz = gz + dz;
				double px = cx + jitter(worldSeed, cx, cz, 0x5EED1L);
				double pz = cz + jitter(worldSeed, cx, cz, 0x5EED2L);
				double ddx = u - px;
				double ddz = v - pz;
				double d = ddx * ddx + ddz * ddz;
				if (d < best) {
					best = d;
					bestGx = cx;
					bestGz = cz;
				}
			}
		}

		int offset = (int) (mix(worldSeed, 0x3A5C4E7BL) & 0x7FFFFFFFL);
		return Mth.positiveModulo(bestGx + bestGz + offset, COUNT);
	}

	private static double cellSize(long worldSeed, int blockX, int blockZ) {
		// Very low-frequency smooth noise makes some belts expansive and others
		// narrow, while the underlying lattice coloring is preserved.
		double n = smoothNoise(worldSeed + 0x91EL, blockX / 9000.0D, blockZ / 9000.0D);
		return BASE_CELL_SIZE * (1.0D + (n - 0.5D) * 2.0D * CELL_VARIATION);
	}

	private static double jitter(long worldSeed, int x, int z, long salt) {
		return (hash01(worldSeed, x, z, salt) - 0.5D) * 2.0D * JITTER;
	}

	private static double hash01(long worldSeed, int x, int z, long salt) {
		return (mix(worldSeed ^ salt, x * 0x9E3779B97F4A7C15L ^ z * 0xBF58476D1CE4E5B9L) >>> 11)
				* (1.0D / 9007199254740992.0D);
	}

	/** One octave of smoothstep-interpolated value noise. */
	private static double smoothNoise(long seed, double u, double v) {
		int xi = floor(u);
		int zi = floor(v);
		double fx = u - xi;
		double fz = v - zi;
		double sx = fx * fx * (3.0D - 2.0D * fx);
		double sz = fz * fz * (3.0D - 2.0D * fz);
		double a = hash01(seed, xi, zi, 0x11L);
		double b = hash01(seed, xi + 1, zi, 0x11L);
		double c = hash01(seed, xi, zi + 1, 0x11L);
		double d = hash01(seed, xi + 1, zi + 1, 0x11L);
		return a + (b - a) * sx + (c - a) * sz + (a - b - c + d) * sx * sz;
	}

	private static int floor(double value) {
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

	/** SplitMix64 finalizer; a cheap, well-distributed integer mixer. */
	private static long mix(long value, long extra) {
		long h = value ^ extra;
		h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
		h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
		return h ^ (h >>> 31);
	}
}
