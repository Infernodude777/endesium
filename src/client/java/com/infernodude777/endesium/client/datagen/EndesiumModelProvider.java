package com.infernodude777.endesium.client.datagen;

import com.infernodude777.endesium.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;

public final class EndesiumModelProvider extends FabricModelProvider {
	public EndesiumModelProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
		blockStateModelGenerator.createTrivialCube(ModBlocks.FOUNDATION_TEST_BLOCK);
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerator) {
		// ItemModelGenerators in the 1.21.1 official mappings do not expose
		// custom item generation publicly; the test item model is kept as a
		// small checked-in resource for this foundation milestone.
	}

	@Override
	public String getName() {
		return "Endesium foundation models";
	}
}
