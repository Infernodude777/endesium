package com.infernodude777.endesium.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * A wall/floor tile carved with one of the recurring Endesium motifs.
 * The symbol property is set at generation time; players gradually learn
 * that the ring, the spire, and the eye always appear together near
 * places that matter.
 */
public class InscribedSlateBlock extends Block {
    public static final IntegerProperty SYMBOL = IntegerProperty.create("symbol", 0, 3);
    public static final int SYMBOL_RING = 0;
    public static final int SYMBOL_SPIRE = 1;
    public static final int SYMBOL_EYE = 2;
    public static final int SYMBOL_PLAIN = 3;

    private static final MapCodec<InscribedSlateBlock> CODEC = simpleCodec(InscribedSlateBlock::new);

    public InscribedSlateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SYMBOL, SYMBOL_RING));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SYMBOL);
    }
}
