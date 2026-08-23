package com.infernodude777.endesium.item;

import com.infernodude777.endesium.registry.ModSounds;
import com.infernodude777.endesium.resonance.ResonanceManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ProductionResonanceLensItem extends Item {
	public ProductionResonanceLensItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			level.playSound(null, serverPlayer.blockPosition(), ModSounds.RESONANCE_LENS_ACTIVATE,
					SoundSource.PLAYERS, 0.5F, 1.0F);
			ResonanceManager.get(serverPlayer.serverLevel()).emitLensFeedback(serverPlayer);
			serverPlayer.getCooldowns().addCooldown(this, 20);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Read the strongest resonance signal: band + direction")
				.withStyle(net.minecraft.ChatFormatting.GRAY));
		tooltip.add(Component.literal("Signals never carry coordinates; silence means")
				.withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
		tooltip.add(Component.literal("no loaded source is within reach").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
	}
}
