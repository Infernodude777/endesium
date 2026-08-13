package com.infernodude777.endesium.client;

import com.infernodude777.endesium.client.datagen.EndesiumLanguageProvider;
import com.infernodude777.endesium.client.datagen.EndesiumLootTableProvider;
import com.infernodude777.endesium.client.datagen.EndesiumModelProvider;
import com.infernodude777.endesium.client.datagen.EndesiumRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class EndesiumDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(EndesiumModelProvider::new);
		pack.addProvider(EndesiumRecipeProvider::new);
		pack.addProvider(EndesiumLootTableProvider::new);
		pack.addProvider(EndesiumLanguageProvider::new);
	}
}
