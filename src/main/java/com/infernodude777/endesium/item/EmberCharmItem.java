package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

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
 * A small charm of compressed ember, worn as a talisman. Using it grants a
 * short burst of Fire Resistance — a handy survival tool for exploring the
 * Ashen Expanse and its volcano interiors. Reusable, with a meaningful
 * cooldown so it is a deliberate tool rather than a permanent immunity.
 */
public final class EmberCharmItem extends Item {
	private static final int DURATION = 15 * 20;
	private static final int COOLDOWN = 30 * 20;

	public EmberCharmItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION, 0, false, false, true));
			level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
					SoundSource.PLAYERS, 0.7F, 1.4F);
			player.getCooldowns().addCooldown(this, COOLDOWN);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Use: Fire Resistance for 30s").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("60s cooldown").withStyle(ChatFormatting.GRAY));
	}
}