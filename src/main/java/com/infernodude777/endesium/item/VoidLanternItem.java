package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * A hand-held void lantern. Right-clicking a ground block plants a small
 * glowing Void Lamp as a permanent light source, consumed on use. It is the
 * practical light of the void biomes — a pale, cold glow that keeps the deep
 * dark at bay without turning the world neon.
 */
public final class VoidLanternItem extends Item {
	public VoidLanternItem(Properties properties) {
		super(properties.stacksTo(16));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockPos above = pos.above();
		if (!level.getBlockState(above).isAir()
				|| !ModBlocks.isPlantGround(level.getBlockState(pos))) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			level.setBlock(above, ModBlocks.VOID_LAMP.defaultBlockState(), 3);
			level.playSound(null, above, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
			Player player = context.getPlayer();
			if (player != null && !player.getAbilities().instabuild) {
				context.getItemInHand().shrink(1);
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Right-click ground: plant a permanent Void Lamp").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Consumed on use").withStyle(ChatFormatting.GRAY));
	}
}