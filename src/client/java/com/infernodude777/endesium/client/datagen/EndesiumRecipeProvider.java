package com.infernodude777.endesium.client.datagen;

import com.infernodude777.endesium.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

public final class EndesiumRecipeProvider extends FabricRecipeProvider {
	public EndesiumRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void buildRecipes(RecipeOutput exporter) {
		oneToOneConversionRecipe(
				exporter,
				ModItems.FOUNDATION_TEST_ITEM,
				Items.STONE,
				"foundation_test_item"
		);
	}

	@Override
	public String getName() {
		return "Endesium foundation recipes";
	}
}
