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
		blockStateModelGenerator.createTrivialCube(ModBlocks.RESONANT_SLATE);
		blockStateModelGenerator.createTrivialCube(ModBlocks.END_GRAY);
		blockStateModelGenerator.createTrivialCube(ModBlocks.DORMANT_RESONANT_CRYSTAL);
		blockStateModelGenerator.createTrivialCube(ModBlocks.CRACKED_SPIRE_STONE);
		blockStateModelGenerator.createTrivialCube(ModBlocks.RESONANT_PILLAR);
		// The mechanism, the plant cross models, and the four-symbol inscribed
		// slate are checked-in resources: the mechanism uses the production
		// end_ruin_mechanism texture, the plants need cutout cross blockstates,
		// and the slate needs a per-symbol blockstate map.
		blockStateModelGenerator.skipAutoItemBlock(ModBlocks.INSCRIBED_SLATE);
		blockStateModelGenerator.skipAutoItemBlock(ModBlocks.RESONANT_BLOOM);

		// Void Skirts blocks — simple cubes and the slab/stairs/wall set.
		blockStateModelGenerator.createTrivialCube(ModBlocks.VOID_SLATE);
		blockStateModelGenerator.createTrivialCube(ModBlocks.VOID_GRAVEL);
		blockStateModelGenerator.createTrivialCube(ModBlocks.VOID_SOIL);
		blockStateModelGenerator.createTrivialCube(ModBlocks.VOID_GLASS);
		blockStateModelGenerator.createTrivialCube(ModBlocks.VOID_BRICK);
		blockStateModelGenerator.createTrivialCube(ModBlocks.VOID_LAMP);
		blockStateModelGenerator.createTrivialCube(ModBlocks.VOID_WEAVE);
		blockStateModelGenerator.createTrivialCube(ModBlocks.VOID_SPIRE);
		blockStateModelGenerator.createTrivialCube(ModBlocks.UMBRAL_STONE);
		blockStateModelGenerator.createTrivialCube(ModBlocks.VOID_ORE);
		// voidstone ships as checked-in resources (registered before the Void
		// Skirts tranche); generating it here would duplicate its blockstate.
		// The slab/stairs/wall blockstates and models ship as checked-in
		// resources (the 1.21.1 datagen API keeps those generators package-private).
		blockStateModelGenerator.skipAutoItemBlock(ModBlocks.VOID_BRICK_SLAB);
		blockStateModelGenerator.skipAutoItemBlock(ModBlocks.VOID_BRICK_STAIRS);
		blockStateModelGenerator.skipAutoItemBlock(ModBlocks.VOID_BRICK_WALL);
		blockStateModelGenerator.skipAutoItemBlock(ModBlocks.VOID_CRYSTAL);
		blockStateModelGenerator.skipAutoItemBlock(ModBlocks.UMBRAL_GRASS);
		blockStateModelGenerator.skipAutoItemBlock(ModBlocks.VOID_FERN);
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerator) {
		// ItemModelGenerators in the 1.21.1 official mappings do not expose
		// custom item generation publicly; production item models are kept as
		// small checked-in resources.
	}

	@Override
	public String getName() {
		return "Endesium production models";
	}
}
