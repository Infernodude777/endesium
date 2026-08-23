package com.infernodude777.endesium.world;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/**
 * Statically resolved Endesium biome holders.
 *
 * <p>{@code BiomeSource.possibleBiomes()} is memoized on first access per
 * instance, and {@code TheEndBiomeSource} can be constructed either through
 * {@code create} or (in the dimension codec path) the private constructor.
 * Capturing the holders from the server registry at {@code SERVER_STARTING}
 * guarantees the fallback holders exist before any {@code possibleBiomes()}
 * call can memoize an Endesium-free set.</p>
 */
public final class EndesiumBiomeHolders {
	private static final int COUNT = EndesiumRegions.COUNT;
	private static final Holder<?>[] HOLDERS = new Holder<?>[COUNT];

	private EndesiumBiomeHolders() {
	}

	/** Replace the holders for the active registry, including when a client or
	 * integrated server opens a second world in the same JVM. */
	public static synchronized void set(Holder<Biome>[] holders) {
		for (int i = 0; i < COUNT; i++) {
			HOLDERS[i] = holders[i];
		}
	}

	public static synchronized void clear() {
		for (int i = 0; i < COUNT; i++) {
			HOLDERS[i] = null;
		}
	}

	@SuppressWarnings("unchecked")
	public static synchronized Holder<Biome> forRegion(int region) {
		if (region < 0 || region >= COUNT) {
			return null;
		}
		return (Holder<Biome>) HOLDERS[region];
	}

	public static synchronized Holder<Biome> wastes() {
		return forRegion(EndesiumRegions.END_WASTES);
	}

	public static synchronized Holder<Biome> wilds() {
		return forRegion(EndesiumRegions.CHORUS_WILDS);
	}
}
