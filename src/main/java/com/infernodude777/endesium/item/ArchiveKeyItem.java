package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import com.infernodude777.endesium.block.ResonantMechanismBlockEntity;
import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.world.EndRuinVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A rare skeleton key recovered from the ancient archives. Right-clicking the
 * dormant Resonant Archive core with it wakes that core directly—without a
 * Resonance Lens reading—and consumes the key. Ordinary ruin mechanisms still
 * require the Lens.
 */
public final class ArchiveKeyItem extends Item {
	public ArchiveKeyItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (!state.is(ModBlocks.RESONANT_MECHANISM)) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (level.getBlockEntity(pos) instanceof ResonantMechanismBlockEntity mechanism
				&& context.getPlayer() instanceof ServerPlayer serverPlayer) {
			// The key is an Archive key, not a progression bypass. Earlier ruin
			// mechanisms still require the Lens and cannot consume this item.
			if (mechanism.variant() != EndRuinVariant.ARCHIVE) {
				return InteractionResult.PASS;
			}
			if (mechanism.activate(serverPlayer)) {
				level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 0.9F);
				context.getItemInHand().shrink(1);
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Right-click the Resonant Archive core to wake it directly").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Consumed on use; ordinary mechanisms still need a Lens").withStyle(ChatFormatting.GRAY));
	}
}