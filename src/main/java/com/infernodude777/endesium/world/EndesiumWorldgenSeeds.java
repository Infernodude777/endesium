package com.infernodude777.endesium.world;

/** Captures the active world seed for deterministic Endesium biome selection. */
public final class EndesiumWorldgenSeeds {
	private static volatile long seed;
	private static volatile boolean captured;

	private EndesiumWorldgenSeeds() {
	}

	public static void capture(long worldSeed) {
		seed = worldSeed;
		captured = true;
	}

	public static long get() {
		return seed;
	}

	public static boolean isCaptured() {
		return captured;
	}

	/** Forget the captured seed (called on server stop). */
	public static void clear() {
		captured = false;
		seed = 0L;
	}
}
