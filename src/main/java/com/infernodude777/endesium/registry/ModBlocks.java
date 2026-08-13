package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;

public final class ModBlocks {
	public static final Block FOUNDATION_TEST_BLOCK = register(
			"foundation_test_block",
			new Block(BlockBehaviour.Properties.of().strength(1.5F))
	);
	public static final Block END_RUIN_BLOCK = register(
			"end_ruin_block",
			new Block(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.STONE))
	);

	private ModBlocks() {
	}

	private static Block register(String id, Block block) {
		return Registry.register(BuiltInRegistries.BLOCK, Endesium.id(id), block);
	}

	public static void register() {
		Endesium.LOGGER.info("Registered Endesium blocks");
	}
}
