package com.infernodude777.endesium.client.datagen;

import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import static net.minecraft.data.recipes.RecipeProvider.has;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.data.recipes.RecipeOutput;

public final class EndesiumRecipeProvider extends FabricRecipeProvider {
	public EndesiumRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void buildRecipes(RecipeOutput exporter) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RESONANCE_LENS)
				.define('S', ModItems.VOID_SHARD)
				.define('E', Items.ENDER_EYE)
				.pattern("S S")
				.pattern(" E ")
				.pattern("S S")
				.unlockedBy("has_void_shard", has(ModItems.VOID_SHARD))
				.save(exporter);

		// The Resonant Elytra: a vanilla Elytra wrapped in Dragon materials.
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RESONANT_WINGS)
				.define('E', ModItems.ENDER_ESSENCE)
				.define('S', ModItems.RESONANT_DRAGON_SCALE)
				.define('W', Items.ELYTRA)
				.define('B', ModItems.DRAGONBONE)
				.pattern("ESE")
				.pattern("SWS")
				.pattern("BSB")
				.unlockedBy("has_dragon_scale", has(ModItems.RESONANT_DRAGON_SCALE))
				.unlockedBy("has_elytra", has(Items.ELYTRA))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ECHO_COMPASS)
				.define('S', ModItems.VOID_SHARD)
				.define('T', ModItems.RESONANCE_TOKEN)
				.pattern("S S")
				.pattern(" T ")
				.pattern("S S")
				.unlockedBy("has_resonance_token", has(ModItems.RESONANCE_TOKEN))
				.save(exporter);

		// ── Void Skirts material chain ──
		// Void Gem smelts into a Void Ingot.
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.VOID_GEM), RecipeCategory.MISC,
						ModItems.VOID_INGOT, 0.7F, 200)
				.unlockedBy("has_void_gem", has(ModItems.VOID_GEM))
				.save(exporter, "void_ingot_from_smelting");
		// 1 ingot -> 9 nuggets.
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VOID_NUGGET, 9)
				.requires(ModItems.VOID_INGOT)
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter, "void_nugget_from_ingot");
		// 9 nuggets -> 1 ingot.
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.VOID_INGOT)
				.pattern("NNN")
				.pattern("NNN")
				.pattern("NNN")
				.define('N', ModItems.VOID_NUGGET)
				.unlockedBy("has_void_nugget", has(ModItems.VOID_NUGGET))
				.save(exporter, "void_ingot_from_nuggets");
		// Void brick from void slate.
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.VOID_BRICK, 4)
				.pattern("SS")
				.pattern("SS")
				.define('S', ModBlocks.VOID_SLATE)
				.unlockedBy("has_void_slate", has(ModBlocks.VOID_SLATE))
				.save(exporter);
		// Void brick slab / stairs / wall.
		slabBuilder(RecipeCategory.BUILDING_BLOCKS, ModBlocks.VOID_BRICK_SLAB, Ingredient.of(ModBlocks.VOID_BRICK))
				.unlockedBy("has_void_brick", has(ModBlocks.VOID_BRICK))
				.save(exporter);
		stairBuilder(ModBlocks.VOID_BRICK_STAIRS, Ingredient.of(ModBlocks.VOID_BRICK))
				.unlockedBy("has_void_brick", has(ModBlocks.VOID_BRICK))
				.save(exporter);
		wallBuilder(RecipeCategory.BUILDING_BLOCKS, ModBlocks.VOID_BRICK_WALL, Ingredient.of(ModBlocks.VOID_BRICK))
				.unlockedBy("has_void_brick", has(ModBlocks.VOID_BRICK))
				.save(exporter);

		// ── Void tools ──
		toolRecipes(exporter);

		// Ashen Expanse: Ashwalker Boots - leather boots wrapped in ember and magma.
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ASHWALKER_BOOTS)
				.define('E', ModItems.ASHEN_EMBER)
				.define('M', ModItems.MAGMA_CORE)
				.define('B', Items.LEATHER_BOOTS)
				.pattern("E E")
				.pattern("MBM")
				.unlockedBy("has_ember", has(ModItems.ASHEN_EMBER))
				.unlockedBy("has_magma", has(ModItems.MAGMA_CORE))
				.save(exporter);
		// Ashen Crust is a transient world block, but give it a decorative recipe
		// so it can be built with ashen embers and ash stone.
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ASHEN_CRUST, 4)
				.define('E', ModItems.ASHEN_EMBER)
				.define('A', ModBlocks.ASH_STONE)
				.pattern(" A ")
				.pattern("AEA")
				.pattern(" A ")
				.unlockedBy("has_ember", has(ModItems.ASHEN_EMBER))
				.save(exporter);
		// Ember Charm: a talisman of ember and string, reusable fire resistance.
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EMBER_CHARM)
				.define('E', ModItems.ASHEN_EMBER)
				.define('S', Items.STRING)
				.pattern(" E ")
				.pattern("ESE")
				.pattern(" E ")
				.unlockedBy("has_ember", has(ModItems.ASHEN_EMBER))
				.save(exporter);

				armorRecipes(exporter);

		biomeRelicRecipes(exporter);

		// ── Void functional items ──
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.VOID_COMPASS)
				.define('I', ModItems.VOID_INGOT)
				.define('E', Items.ENDER_PEARL)
				.pattern(" I ")
				.pattern("IEI")
				.pattern(" I ")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.VOID_ANCHOR)
				.define('I', ModItems.VOID_INGOT)
				.define('C', ModItems.VOID_CORE)
				.pattern("I I")
				.pattern(" C ")
				.pattern("I I")
				.unlockedBy("has_void_core", has(ModItems.VOID_CORE))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.VOID_LANTERN)
				.define('G', ModBlocks.VOID_GLASS)
				.define('I', ModItems.VOID_INGOT)
				.pattern(" G ")
				.pattern("GIG")
				.pattern(" G ")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.VOID_DASH)
				.define('I', ModItems.VOID_INGOT)
				.define('M', Items.PHANTOM_MEMBRANE)
				.pattern(" I ")
				.pattern("IMI")
				.pattern(" I ")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
	}

	private static void biomeRelicRecipes(RecipeOutput exporter) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WINDSCAR_WINCH)
				.define('S', ModItems.SKYGLASS_SHARD)
				.define('I', Items.IRON_INGOT)
				.define('T', Items.STRING)
				.pattern("SIS")
				.pattern("ITI")
				.pattern(" I ")
				.unlockedBy("has_skyglass", has(ModItems.SKYGLASS_SHARD))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MIRE_BELL_CLAPPER)
				.define('T', ModBlocks.TIDE_IRON)
				.define('I', Items.IRON_INGOT)
				.define('S', Items.STRING)
				.pattern(" T ")
				.pattern("ISI")
				.pattern(" S ")
				.unlockedBy("has_tide_iron", has(ModBlocks.TIDE_IRON))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.LUMEN_GRAFT)
				.define('L', ModBlocks.LUMEN_GRAFT_BLOCK)
				.define('D', ModItems.LUMEN_DUST)
				.pattern(" D ")
				.pattern("DLD")
				.pattern(" D ")
				.unlockedBy("has_lumen_dust", has(ModItems.LUMEN_DUST))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PRISM_SEED)
				.define('P', ModBlocks.PRISM_CANOPY_BLOCK)
				.define('E', ModItems.CHORUS_EYE)
				.pattern(" P ")
				.pattern("PEP")
				.pattern(" P ")
				.unlockedBy("has_chorus_eye", has(ModItems.CHORUS_EYE))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CROWN_NEEDLE)
				.define('N', ModBlocks.CROWN_NEEDLE_BLOCK)
				.define('V', ModItems.VOID_INGOT)
				.define('C', ModItems.VOID_CORE)
				.pattern(" N ")
				.pattern("VCV")
				.pattern(" V ")
				.unlockedBy("has_void_core", has(ModItems.VOID_CORE))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CROWN_SEAL)
				.define('S', ModBlocks.CROWN_SEAL_BLOCK)
				.define('E', ModItems.ENDER_ESSENCE)
				.pattern("SES")
				.pattern("EEE")
				.pattern("SES")
				.unlockedBy("has_ender_essence", has(ModItems.ENDER_ESSENCE))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.NULL_QUILL)
				.define('F', Items.FEATHER)
				.define('A', ModItems.ARCHIVE_FRAGMENT)
				.define('I', Items.INK_SAC)
				.pattern("  F")
				.pattern(" AI")
				.pattern("A  ")
				.unlockedBy("has_archive_fragment", has(ModItems.ARCHIVE_FRAGMENT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.THRESHOLD_KEY)
				.define('S', ModItems.CROWN_SEAL)
				.define('C', ModItems.VOID_CORE)
				.define('A', ModItems.ARCHIVE_FRAGMENT)
				.pattern(" S ")
				.pattern("CAC")
				.pattern(" C ")
				.unlockedBy("has_crown_seal", has(ModItems.CROWN_SEAL))
				.save(exporter);
	}

	private static void toolRecipes(RecipeOutput exporter) {
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.VOID_SWORD)
				.define('I', ModItems.VOID_INGOT)
				.define('S', Items.STICK)
				.pattern(" I ")
				.pattern(" I ")
				.pattern(" S ")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.VOID_PICKAXE)
				.define('I', ModItems.VOID_INGOT)
				.define('S', Items.STICK)
				.pattern("III")
				.pattern(" S ")
				.pattern(" S ")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.VOID_AXE)
				.define('I', ModItems.VOID_INGOT)
				.define('S', Items.STICK)
				.pattern("II")
				.pattern("IS")
				.pattern(" S")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.VOID_SHOVEL)
				.define('I', ModItems.VOID_INGOT)
				.define('S', Items.STICK)
				.pattern(" I ")
				.pattern(" S ")
				.pattern(" S ")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.VOID_HOE)
				.define('I', ModItems.VOID_INGOT)
				.define('S', Items.STICK)
				.pattern("II")
				.pattern(" S")
				.pattern(" S")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
	}

	private static void armorRecipes(RecipeOutput exporter) {
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.VOID_HELMET)
				.define('I', ModItems.VOID_INGOT)
				.pattern("III")
				.pattern("I I")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.VOID_CHESTPLATE)
				.define('I', ModItems.VOID_INGOT)
				.pattern("I I")
				.pattern("III")
				.pattern("III")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.VOID_LEGGINGS)
				.define('I', ModItems.VOID_INGOT)
				.pattern("III")
				.pattern("I I")
				.pattern("I I")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.VOID_BOOTS)
				.define('I', ModItems.VOID_INGOT)
				.pattern("I I")
				.pattern("I I")
				.unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
				.save(exporter);
	}

	@Override
	public String getName() {
		return "Endesium production recipes";
	}
}
