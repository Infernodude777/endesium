package com.infernodude777.endesium.client.datagen;

import com.infernodude777.endesium.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public final class EndesiumLanguageProvider extends FabricLanguageProvider {
	public EndesiumLanguageProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
		translationBuilder.add(ModItems.FOUNDATION_TEST_ITEM, "Foundation Test Item");
		translationBuilder.add(ModItems.FOUNDATION_TEST_BLOCK_ITEM, "Foundation Test Block");
	}

	@Override
	public String getName() {
		return "Endesium foundation language";
	}
}
