package com.infernodude777.endesium.client.datagen;

import com.infernodude777.endesium.registry.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class EndesiumLootTableProvider extends FabricBlockLootTableProvider {
	public EndesiumLootTableProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void generate() {
		dropSelf(ModBlocks.RESONANT_SLATE);
		dropSelf(ModBlocks.END_GRAY);
		dropSelf(ModBlocks.DORMANT_RESONANT_CRYSTAL);
		dropSelf(ModBlocks.RESONANT_MECHANISM);
		dropSelf(ModBlocks.CHORUS_SPROUT);
		dropSelf(ModBlocks.WILD_TENDRIL);
		dropSelf(ModBlocks.RESONANT_BLOOM);
		dropSelf(ModBlocks.INSCRIBED_SLATE);
		dropSelf(ModBlocks.RESONANT_PILLAR);
		dropSelf(ModBlocks.CRACKED_SPIRE_STONE);

		dropSelf(ModBlocks.WASTES_STONE);
		dropSelf(ModBlocks.WASTES_GRAVEL);
		dropSelf(ModBlocks.DUST_REED);
		dropSelf(ModBlocks.VOID_GRASS);
		dropSelf(ModBlocks.ELDER_CHORUS_WOOD);
		dropSelf(ModBlocks.ELDER_CHORUS_BARK);
		dropSelf(ModBlocks.CHORUS_ROOT);
		dropSelf(ModBlocks.CHORUS_MOSS);
		dropSelf(ModBlocks.HOLLOW_CHORUS_WOOD);
		dropSelf(ModBlocks.HIGHLAND_STONE);
		dropSelf(ModBlocks.HIGHLAND_SLATE);
		dropSelf(ModBlocks.HIGHLAND_LENSSTONE);
		dropSelf(ModBlocks.WINDSCAR_BRACKET);
		dropSelf(ModBlocks.VOID_MARSH_SOIL);
		dropSelf(ModBlocks.VOID_REED);
		dropSelf(ModBlocks.TIDE_IRON);
		dropSelf(ModBlocks.MIREGLASS);
		dropSelf(ModBlocks.MARSH_MOSS);
		dropSelf(ModBlocks.LUMEN_STONE);
		dropSelf(ModBlocks.LUMEN_MOSS);
		dropSelf(ModBlocks.LUMEN_GRAFT_BLOCK);
		dropSelf(ModBlocks.PRISM_CANOPY_BLOCK);
		dropSelf(ModBlocks.LUMEN_BLOOM);
		dropSelf(ModBlocks.ASH_STONE);
		dropSelf(ModBlocks.ASHEN_SOIL);
		dropSelf(ModBlocks.ASHEN_CRUST);
		dropSelf(ModBlocks.CRYSTAL_SHARD_BLOCK);
		dropSelf(ModBlocks.CRYSTAL_CLUSTER);
		dropSelf(ModBlocks.DARK_CRYSTAL_BLOCK);
		dropSelf(ModBlocks.PALE_CRYSTAL_BLOCK);
		dropSelf(ModBlocks.RESONANT_BASALT);
		dropSelf(ModBlocks.END_CLAY);
		dropSelf(ModBlocks.VOIDSTONE);

		// Void Skirts blocks.
		dropSelf(ModBlocks.VOID_SLATE);
		dropSelf(ModBlocks.CROWN_NEEDLE_BLOCK);
		dropSelf(ModBlocks.CROWN_SEAL_BLOCK);
		dropSelf(ModBlocks.NULL_ARCHIVE_FRAME);
		dropSelf(ModBlocks.THRESHOLD_CORE_BLOCK);
		dropSelf(ModBlocks.VOID_GRAVEL);
		dropSelf(ModBlocks.VOID_SOIL);
		dropSelf(ModBlocks.VOID_GLASS);
		dropSelf(ModBlocks.VOID_BRICK);
		dropSelf(ModBlocks.VOID_BRICK_SLAB);
		dropSelf(ModBlocks.VOID_BRICK_STAIRS);
		dropSelf(ModBlocks.VOID_BRICK_WALL);
		dropSelf(ModBlocks.VOID_LAMP);
		dropSelf(ModBlocks.VOID_CRYSTAL);
		dropSelf(ModBlocks.UMBRAL_GRASS);
		dropSelf(ModBlocks.VOID_FERN);
		dropSelf(ModBlocks.VOID_WEAVE);
		dropSelf(ModBlocks.VOID_SPIRE);
		dropSelf(ModBlocks.UMBRAL_STONE);
		// Void Ore drops itself so a silk-touch pick keeps the ore.
		dropSelf(ModBlocks.VOID_ORE);
	}

	@Override
	public String getName() {
		return "Endesium production loot tables";
	}
}
