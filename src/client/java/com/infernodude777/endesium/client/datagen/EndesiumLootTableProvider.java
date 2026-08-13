package com.infernodude777.endesium.client.datagen;

import com.infernodude777.endesium.registry.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;

public final class EndesiumLootTableProvider extends FabricBlockLootTableProvider {
	public EndesiumLootTableProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void generate() {
		dropSelf(ModBlocks.FOUNDATION_TEST_BLOCK);
		dropSelf(ModBlocks.END_RUIN_BLOCK);
	}

	@Override
	public String getName() {
		return "Endesium foundation loot tables";
	}
}
