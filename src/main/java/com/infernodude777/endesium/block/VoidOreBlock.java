package com.infernodude777.endesium.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * A rare mineral seam found only in the void biomes. Drops itself, and the
 * ore block smelts directly into Void Ingots. The deep, near-black stone
 * reads as an intrusive ore rather than a decorative block.
 */
public class VoidOreBlock extends DropExperienceBlock {
    private static final com.mojang.serialization.MapCodec<VoidOreBlock> CODEC =
            simpleCodec(VoidOreBlock::new);

    public VoidOreBlock(BlockBehaviour.Properties properties) {
        super(UniformInt.of(3, 7), properties);
    }

    @Override
    public com.mojang.serialization.MapCodec<? extends DropExperienceBlock> codec() {
        return CODEC;
    }
}
