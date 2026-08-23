package com.infernodude777.endesium.client;

import com.infernodude777.endesium.client.datagen.EndesiumLanguageProvider;
import com.infernodude777.endesium.client.datagen.EndesiumLootTableProvider;
import com.infernodude777.endesium.client.datagen.EndesiumModelProvider;
import com.infernodude777.endesium.client.datagen.EndesiumRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import com.infernodude777.endesium.world.EndesiumBiomes;

public class EndesiumDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		// The production biome definitions remain data-driven JSON resources. The
		// datagen registry still needs bootstrap entries so strict registry
		// validation treats those dynamic keys as intentionally referenced.
		registryBuilder.add(Registries.BIOME, context -> {
			context.register(EndesiumBiomes.END_WASTES, datagenBiome());
			context.register(EndesiumBiomes.CHORUS_WILDS, datagenBiome());
			context.register(EndesiumBiomes.SHATTERED_HIGHLANDS, datagenBiome());
			context.register(EndesiumBiomes.VOID_MARSHES, datagenBiome());
			context.register(EndesiumBiomes.LUMINOUS_GROVES, datagenBiome());
			context.register(EndesiumBiomes.ASHEN_EXPANSE, datagenBiome());
			context.register(EndesiumBiomes.CRYSTAL_BARRENS, datagenBiome());
			context.register(EndesiumBiomes.VOID_SKIRTS, datagenBiome());
			context.register(EndesiumBiomes.VOID_CROWN, datagenBiome());
			context.register(EndesiumBiomes.UMBRAL_REACH, datagenBiome());
		});
	}

	private static Biome datagenBiome() {
		return new Biome.BiomeBuilder()
				.hasPrecipitation(false)
				.temperature(0.5F)
				.downfall(0.5F)
				.specialEffects(new BiomeSpecialEffects.Builder()
						.fogColor(10518688)
						.waterColor(4159204)
						.waterFogColor(329011)
						.skyColor(0)
						.build())
				.mobSpawnSettings(MobSpawnSettings.EMPTY)
				.generationSettings(BiomeGenerationSettings.EMPTY)
				.build();
	}

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(EndesiumModelProvider::new);
		pack.addProvider(EndesiumRecipeProvider::new);
		pack.addProvider(EndesiumLootTableProvider::new);
		pack.addProvider(EndesiumLanguageProvider::new);
	}
}
