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
import net.minecraft.world.level.block.state.BlockState;

/**
 * Processes the Ashen Expanse. Right-clicking ashen soil sifts the ash loose,
 * turning the block to ash stone and, sometimes, recovering a buried remnant	 * (an ashen ember, magma core, or echo shard). It is deliberately not a
	 * renewable Void Shard or Dragonbone source. The sifter wears down with use.
 */
public final class AshSifterItem extends Item {
	public AshSifterItem(Properties properties) {
		super(properties.durability(128));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (!state.is(ModBlocks.ASHEN_SOIL)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos, ModBlocks.ASH_STONE.defaultBlockState(), 3);
			level.playSound(null, pos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 0.9F, 0.7F);
			double roll = level.random.nextDouble();
			ItemStack drop;
			if (roll < 0.08D) {
				drop = new ItemStack(ModItems.ECHO_SHARD);
			} else if (roll < 0.24D) {
				drop = new ItemStack(ModItems.ASHEN_EMBER);
			} else if (roll < 0.32D) {
				drop = new ItemStack(ModItems.MAGMA_CORE);
			} else {
				drop = ItemStack.EMPTY;
			}
			if (!drop.isEmpty()) {
				net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
						level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, drop);
				level.addFreshEntity(itemEntity);
			}
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
		tooltip.add(Component.literal("Right-click ashen soil: sift out buried remnants").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Turns the soil to ash stone; wears down with use").withStyle(ChatFormatting.GRAY));
	}
}