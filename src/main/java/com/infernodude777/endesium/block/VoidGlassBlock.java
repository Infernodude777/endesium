package com.infernodude777.endesium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A dark, faintly translucent void glass. It lets the pale void light through
 * while keeping the deep mineral tint, so it reads as a window into the void
 * rather than a bright pane.
 */
public class VoidGlassBlock extends Block {
	private static final com.mojang.serialization.MapCodec<VoidGlassBlock> CODEC =
			simpleCodec(VoidGlassBlock::new);

	public VoidGlassBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
		return 0;
	}
}
