package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class EndesiumBiomes {
	public static final ResourceKey<Biome> END_WASTES = ResourceKey.create(
			Registries.BIOME,
			Endesium.id("end_wastes")
	);

	private EndesiumBiomes() {
	}
}
