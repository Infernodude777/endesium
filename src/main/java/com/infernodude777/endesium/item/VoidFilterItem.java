package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A protective utility for the Void Marshes. Using it purges the toxic or
 * slowing effects that linger near the pools and grants a short window of
 * resistance, at a durability cost.
 */
public final class VoidFilterItem extends Item {
	private static final int COOLDOWN_TICKS = 600;

	public VoidFilterItem(Properties properties) {
		super(properties.durability(48));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.removeEffect(MobEffects.POISON);
			serverPlayer.removeEffect(MobEffects.WITHER);
			serverPlayer.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
			serverPlayer.removeEffect(MobEffects.BLINDNESS);
			serverPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false, true));
			level.playSound(null, serverPlayer.blockPosition(), SoundEvents.BUCKET_EMPTY,
					SoundSource.PLAYERS, 0.7F, 0.8F);
			net.minecraft.world.entity.EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
					? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
			stack.hurtAndBreak(1, serverPlayer, slot);
			serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Use: purge marsh toxins and gain brief resistance").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("30s cooldown; wears down with use").withStyle(ChatFormatting.GRAY));
	}
}