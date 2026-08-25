package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

/**
 * Post-build dressing and rewards for the four flagship builds. Each flagship
 * receives a hoard cache - a loot chest holding the structure's signature
 * treasure - plus decorative accents (gold inlays, lamp posts, crystal
 * clusters) that make the finished build read as a conquered, rewarding
 * landmark rather than an empty set.
 */
public final class StructureDressing {
	private StructureDressing() {
	}

	/** Called from the flagship build path after the builders finish. */
	public static void decorate(ServerLevel level, BlockPos origin, int region) {
		switch (region) {
			case EndesiumRegions.END_WASTES -> dustCathedral(level, origin);
			case EndesiumRegions.SHATTERED_HIGHLANDS -> skyrendKeep(level, origin);
			case EndesiumRegions.VOID_MARSHES -> drownedCathedral(level, origin);
			case EndesiumRegions.LUMINOUS_GROVES -> lumenCathedral(level, origin);
			default -> {
			}
		}
	}

	private static void hoard(ServerLevel level, BlockPos at, String table) {
		level.setBlock(at, Blocks.CHEST.defaultBlockState(), 3);
		if (level.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
			chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Endesium.id(table)), level.random.nextLong());
		}
	}

	private static void dustCathedral(ServerLevel level, BlockPos origin) {
		// Altar hoard behind the mechanism, gold inlay ringing the dais.
		hoard(level, origin.offset(0, 1, -2), "chests/dust_cathedral_hoard");
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -8; dz <= -4; dz++) {
				if ((dx + dz) % 2 == 0) {
					set(level, origin.offset(dx, 2, dz), Blocks.GOLD_BLOCK);
				}
			}
		}
		set(level, origin.offset(4, 1, 6), ModBlocks.VOID_LAMP);
		set(level, origin.offset(-4, 1, 6), ModBlocks.VOID_LAMP);
	}

	private static void skyrendKeep(ServerLevel level, BlockPos origin) {
		// Throne hoard at the donjon's heart, lensstone crown accents.
		hoard(level, origin.offset(0, 1, -6), "chests/skyrend_keep_hoard");
		set(level, origin.offset(0, 2, -6), Blocks.GOLD_BLOCK);
		for (int[] c : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) {
			set(level, origin.offset(c[0], 1, c[1]), ModBlocks.HIGHLAND_LENSSTONE);
		}
	}

	private static void drownedCathedral(ServerLevel level, BlockPos origin) {
		// Reliquary hoard beside the leaning tower, mire-glass offerings.
		hoard(level, origin.offset(-8, 1, 12), "chests/drowned_cathedral_hoard");
		set(level, origin.offset(-8, 2, 12), ModBlocks.MIREGLASS);
		set(level, origin.offset(8, 1, 12), Blocks.GOLD_BLOCK);
	}

	private static void lumenCathedral(ServerLevel level, BlockPos origin) {
		// Apse hoard behind the altar, crystal garden flanking it.
		hoard(level, origin.offset(0, 1, -16), "chests/lumen_cathedral_hoard");
		set(level, origin.offset(-3, 1, -16), ModBlocks.PALE_CRYSTAL_BLOCK);
		set(level, origin.offset(3, 1, -16), ModBlocks.PALE_CRYSTAL_BLOCK);
		set(level, origin.offset(0, 2, -16), ModBlocks.DORMANT_RESONANT_CRYSTAL);
	}

	private static void set(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.Block block) {
		if (level.getBlockState(pos).isAir()) {
			level.setBlock(pos, block.defaultBlockState(), 3);
		}
	}
}
