package com.infernodude777.endesium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * A thin, temporary crust formed over lava by Ashwalker Boots.
 * The flow level is copied into the crust state and restored when the
 * scheduled tick fires, preventing walking boots from turning every
 * flowing lava stream into a new source block.
 */
public final class AshenCrustBlock extends Block {
    private static final int LIFETIME_TICKS = 60;

    public AshenCrustBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(
            net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LEVEL);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos,
                           BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, LIFETIME_TICKS + level.getRandom().nextInt(40));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int lavaLevel = state.getValue(BlockStateProperties.LEVEL);
        level.setBlock(pos, Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, lavaLevel), 3);
    }
}
