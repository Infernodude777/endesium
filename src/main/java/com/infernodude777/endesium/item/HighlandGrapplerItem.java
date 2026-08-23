package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A temporary mobility tool for the Shattered Highlands. Each use flings the
 * player upward and forward along their look direction, enough to scale a
 * cliff shelf without an elytra. Durability limits it; it never grants flight.
 */
public final class HighlandGrapplerItem extends Item {
	private static final int COOLDOWN_TICKS = 40;

	public HighlandGrapplerItem(Properties properties) {
		super(properties.durability(96));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			Vec3 look = player.getLookAngle();
			player.setDeltaMovement(look.x * 1.1D, 0.95D, look.z * 1.1D);
			player.hurtMarked = true;
			player.fallDistance = 0.0F;
			level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
					SoundSource.PLAYERS, 0.6F, 1.4F);
			net.minecraft.world.entity.EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
					? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
			stack.hurtAndBreak(1, serverPlayer, slot);
			serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Use: fling upward along your look direction").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("2s cooldown; wears down with use; never grants flight").withStyle(ChatFormatting.GRAY));
	}
}