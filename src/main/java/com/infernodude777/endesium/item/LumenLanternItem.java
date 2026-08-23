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
 * A portable End light source. Activating it bathes the holder in a soft
 * luminosity (night vision) with a brief glow marker, letting them navigate
 * the Luminous Groves without placing blocks. Durability limits how long it
 * can be relied on.
 */
public final class LumenLanternItem extends Item {
	private static final int COOLDOWN_TICKS = 1200;

	public LumenLanternItem(Properties properties) {
		super(properties.durability(192));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false, true));
			serverPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false, true));
			level.playSound(null, serverPlayer.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.PLAYERS, 0.7F, 1.2F);
			net.minecraft.world.entity.EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
					? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
			stack.hurtAndBreak(1, serverPlayer, slot);
			serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Use: Night Vision for 30s and a brief glow").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("60s cooldown; wears down with use").withStyle(ChatFormatting.GRAY));
	}
}