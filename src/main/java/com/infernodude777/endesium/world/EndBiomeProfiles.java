package com.infernodude777.endesium.world;

import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Maps a biome holder to its Endesium region index and describes the surface
 * geology for each region so terrain and vegetation features can be written
 * once and dispatch on the biome present at the placement position. The void
 * biomes deliberately use only void-family blocks — never End Stone — so their
 * terrain reads as a separate, darker geology.
 */
public final class EndBiomeProfiles {
	private EndBiomeProfiles() {
	}

	/** Region index for a biome holder, or -1 when it is not an Endesium biome. */
	public static int regionOf(Holder<Biome> biome) {
		if (biome.is(EndesiumBiomes.END_WASTES)) return EndesiumRegions.END_WASTES;
		if (biome.is(EndesiumBiomes.SHATTERED_HIGHLANDS)) return EndesiumRegions.SHATTERED_HIGHLANDS;
		if (biome.is(EndesiumBiomes.VOID_MARSHES)) return EndesiumRegions.VOID_MARSHES;
		if (biome.is(EndesiumBiomes.CHORUS_WILDS)) return EndesiumRegions.CHORUS_WILDS;
		if (biome.is(EndesiumBiomes.LUMINOUS_GROVES)) return EndesiumRegions.LUMINOUS_GROVES;
		if (biome.is(EndesiumBiomes.ASHEN_EXPANSE)) return EndesiumRegions.ASHEN_EXPANSE;
		if (biome.is(EndesiumBiomes.CRYSTAL_BARRENS)) return EndesiumRegions.CRYSTAL_BARRENS;
		if (biome.is(EndesiumBiomes.VOID_SKIRTS)) return EndesiumRegions.VOID_SKIRTS;
		if (biome.is(EndesiumBiomes.VOID_CROWN)) return EndesiumRegions.VOID_CROWN;
		if (biome.is(EndesiumBiomes.UMBRAL_REACH)) return EndesiumRegions.UMBRAL_REACH;
		return -1;
	}

	/** Primary exposed surface block for raised terrain in each region. */
	public static Block groundBlock(int region) {
		switch (region) {
			case EndesiumRegions.SHATTERED_HIGHLANDS: return ModBlocks.HIGHLAND_STONE;
			case EndesiumRegions.VOID_MARSHES: return ModBlocks.VOID_MARSH_SOIL;
			case EndesiumRegions.LUMINOUS_GROVES: return ModBlocks.LUMEN_STONE;
			case EndesiumRegions.ASHEN_EXPANSE: return ModBlocks.ASHEN_SOIL;
			case EndesiumRegions.CRYSTAL_BARRENS: return ModBlocks.CRYSTAL_SHARD_BLOCK;
			case EndesiumRegions.CHORUS_WILDS: return ModBlocks.CHORUS_MOSS;
			case EndesiumRegions.VOID_SKIRTS: return ModBlocks.VOID_SLATE;
			case EndesiumRegions.VOID_CROWN: return ModBlocks.UMBRAL_STONE;
			case EndesiumRegions.UMBRAL_REACH: return ModBlocks.VOID_SOIL;
			case EndesiumRegions.END_WASTES:
			default: return ModBlocks.WASTES_STONE;
		}
	}

	/** Secondary stone used for cliff faces and the bulk of raised columns. */
	public static Block substrateBlock(int region) {
		switch (region) {
			case EndesiumRegions.SHATTERED_HIGHLANDS: return ModBlocks.HIGHLAND_SLATE;
			case EndesiumRegions.VOID_MARSHES: return ModBlocks.END_CLAY;
			case EndesiumRegions.LUMINOUS_GROVES: return ModBlocks.LUMEN_STONE;
			case EndesiumRegions.ASHEN_EXPANSE: return ModBlocks.ASH_STONE;
			case EndesiumRegions.CRYSTAL_BARRENS: return ModBlocks.DARK_CRYSTAL_BLOCK;
			case EndesiumRegions.CHORUS_WILDS: return ModBlocks.CHORUS_ROOT;
			case EndesiumRegions.VOID_SKIRTS: return ModBlocks.VOIDSTONE;
			case EndesiumRegions.VOID_CROWN: return ModBlocks.VOID_SLATE;
			case EndesiumRegions.UMBRAL_REACH: return ModBlocks.VOID_SOIL;
			case EndesiumRegions.END_WASTES:
			default: return Blocks.END_STONE;
		}
	}

	/** Whether the region's terrain profile lowers toward basins rather than rising. */
	public static boolean isLowland(int region) {
		return region == EndesiumRegions.VOID_MARSHES
				|| region == EndesiumRegions.ASHEN_EXPANSE
				|| region == EndesiumRegions.UMBRAL_REACH;
	}

	/** Whether the region is one of the three void biomes. */
	public static boolean isVoidRegion(int region) {
		return region == EndesiumRegions.VOID_SKIRTS
				|| region == EndesiumRegions.VOID_CROWN
				|| region == EndesiumRegions.UMBRAL_REACH;
	}
}
