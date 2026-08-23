package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A short burst of forward velocity. A use hurls the player forward along
 * their look direction, useful for crossing void gaps and scaling broken
 * terrain. Each dash wears the band's stored charge; Unbreaking and Mending
 * keep a favorite band alive for the long End campaign.
 */
public final class VoidDashItem extends Item {
	private static final double POWER = 1.6D;
	private static final double UP = 0.35D;

	public VoidDashItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}

	@Override
	public int getEnchantmentValue() {
		return 15;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			player.setDeltaMovement(
					player.getDeltaMovement().add(
							player.getLookAngle().x * POWER,
							UP,
							player.getLookAngle().z * POWER));
			player.hurtMarked = true;
			level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP,
					SoundSource.PLAYERS, 0.8F, 1.6F);
			player.getCooldowns().addCooldown(this, 60);
			EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
			stack.hurtAndBreak(1, player, slot);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Use: dash along your look direction").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("3s cooldown; wears down with each dash").withStyle(ChatFormatting.GRAY));
	}
}