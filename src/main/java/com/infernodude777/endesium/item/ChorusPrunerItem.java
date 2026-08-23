package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Harvests the Chorus Wilds without felling the parent growth. Right-clicking
 * an elder chorus trunk or a chorus plant prunes a fresh cutting (chorus root
 * or elder wood) while leaving the block intact, so rare growth can be farmed
 * sustainably.
 */
public final class ChorusPrunerItem extends Item {
	public ChorusPrunerItem(Properties properties) {
		super(properties.durability(96));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		ItemStack cutting;
		if (state.is(ModBlocks.ELDER_CHORUS_WOOD) || state.is(ModBlocks.ELDER_CHORUS_BARK)
				|| state.is(ModBlocks.HOLLOW_CHORUS_WOOD)) {
			cutting = new ItemStack(ModBlocks.ELDER_CHORUS_WOOD);
		} else if (state.is(Blocks.CHORUS_PLANT)) {
			cutting = new ItemStack(ModBlocks.CHORUS_ROOT);
		} else {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.9F, 1.1F);
			net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
					level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, cutting);
			level.addFreshEntity(itemEntity);
			Player player = context.getPlayer();
			if (player != null) {
				net.minecraft.world.entity.EquipmentSlot slot = context.getHand() == InteractionHand.MAIN_HAND
						? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
				context.getItemInHand().hurtAndBreak(1, player, slot);
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Right-click chorus growth: harvest a fresh cutting").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("The parent block always survives").withStyle(ChatFormatting.GRAY));
	}
}