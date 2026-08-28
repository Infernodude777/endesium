package com.infernodude777.endesium.block;

import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A low decorative End plant. It shares BushBlock's cross-model behavior
 * and can root on vanilla End stone, chorus growth, and every Endesium
 * ground block registered in {@link ModBlocks#isPlantGround}.
 */
public class EndPlantBlock extends BushBlock {
    private static final com.mojang.serialization.MapCodec<EndPlantBlock> CODEC =
            simpleCodec(EndPlantBlock::new);

    public EndPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return ModBlocks.isPlantGround(state);
    }
}
