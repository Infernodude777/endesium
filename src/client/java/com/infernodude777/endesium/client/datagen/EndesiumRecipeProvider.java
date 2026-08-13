package com.infernodude777.endesium.client.datagen;

import com.infernodude777.endesium.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
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
		oneToOneConversionRecipe(
				exporter,
				ModItems.VOID_SHARD,
				Items.ENDER_PEARL,
				"void_shard"
		);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RESONANCE_LENS)
				.pattern(" S ")
				.pattern("SES")
				.pattern(" S ")
				.define('S', ModItems.VOID_SHARD)
				.define('E', Items.ENDER_EYE)
				.unlockedBy("has_void_shard", has(ModItems.VOID_SHARD))
				.save(exporter);
	}

	@Override
	public String getName() {
		return "Endesium foundation recipes";
	}
}
