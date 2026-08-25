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
			default: return ModBlocks.CRACKED_SPIRE_STONE;
		}
	}

	/**
	 * The full geological palette for a region, ordered surface to core. The
	 * terrain reskin paints every column as a dithered gradient through these
	 * blocks - topsoil, transition band, deep stone, and a rare accent - so
	 * almost no vanilla End Stone remains visible anywhere in a region.
	 */
	public static Block[] palette(int region) {
		return switch (region) {
			case EndesiumRegions.SHATTERED_HIGHLANDS -> new Block[] {
					ModBlocks.HIGHLAND_STONE, ModBlocks.HIGHLAND_SLATE,
					ModBlocks.HIGHLAND_STONE, ModBlocks.HIGHLAND_LENSSTONE };
			case EndesiumRegions.VOID_MARSHES -> new Block[] {
					ModBlocks.VOID_MARSH_SOIL, ModBlocks.MARSH_MOSS,
					ModBlocks.END_CLAY, ModBlocks.MIREGLASS };
			case EndesiumRegions.CHORUS_WILDS -> new Block[] {
					ModBlocks.CHORUS_MOSS, ModBlocks.CHORUS_ROOT,
					ModBlocks.ELDER_CHORUS_BARK, ModBlocks.END_GRAY };
			case EndesiumRegions.LUMINOUS_GROVES -> new Block[] {
					ModBlocks.LUMEN_STONE, ModBlocks.PALE_CRYSTAL_BLOCK,
					ModBlocks.LUMEN_STONE, ModBlocks.END_GRAY };
			case EndesiumRegions.ASHEN_EXPANSE -> new Block[] {
					ModBlocks.ASHEN_SOIL, ModBlocks.ASH_STONE,
					ModBlocks.ASHEN_CRUST, ModBlocks.RESONANT_BASALT };
			case EndesiumRegions.CRYSTAL_BARRENS -> new Block[] {
					ModBlocks.CRYSTAL_SHARD_BLOCK, ModBlocks.END_CLAY,
					ModBlocks.DARK_CRYSTAL_BLOCK, ModBlocks.PALE_CRYSTAL_BLOCK };
			case EndesiumRegions.VOID_SKIRTS -> new Block[] {
					ModBlocks.VOID_SLATE, ModBlocks.VOIDSTONE,
					ModBlocks.VOID_GRAVEL, ModBlocks.VOID_SOIL };
			case EndesiumRegions.VOID_CROWN -> new Block[] {
					ModBlocks.UMBRAL_STONE, ModBlocks.VOID_SLATE,
					ModBlocks.VOIDSTONE, ModBlocks.CROWN_SEAL_BLOCK };
			case EndesiumRegions.UMBRAL_REACH -> new Block[] {
					ModBlocks.VOID_SOIL, ModBlocks.UMBRAL_STONE,
					ModBlocks.VOIDSTONE, ModBlocks.VOID_WEAVE };
			default -> new Block[] {
					ModBlocks.WASTES_STONE, ModBlocks.WASTES_GRAVEL,
					ModBlocks.CRACKED_SPIRE_STONE, ModBlocks.END_GRAY };
		};
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
