package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/** Resource keys for the ten Endesium outer-End biomes. */
public final class EndesiumBiomes {
	public static final ResourceKey<Biome> END_WASTES = key("end_wastes");
	public static final ResourceKey<Biome> CHORUS_WILDS = key("chorus_wilds");
	public static final ResourceKey<Biome> SHATTERED_HIGHLANDS = key("shattered_highlands");
	public static final ResourceKey<Biome> VOID_MARSHES = key("void_marshes");
	public static final ResourceKey<Biome> LUMINOUS_GROVES = key("luminous_groves");
	public static final ResourceKey<Biome> ASHEN_EXPANSE = key("ashen_expanse");
	public static final ResourceKey<Biome> CRYSTAL_BARRENS = key("crystal_barrens");
	public static final ResourceKey<Biome> VOID_SKIRTS = key("void_skirts");
	public static final ResourceKey<Biome> VOID_CROWN = key("void_crown");
	public static final ResourceKey<Biome> UMBRAL_REACH = key("umbral_reach");

	private EndesiumBiomes() {
	}

	private static ResourceKey<Biome> key(String path) {
		return ResourceKey.create(Registries.BIOME, Endesium.id(path));
	}
}
