package com.infernodude777.endesium.item;

import com.infernodude777.endesium.resonance.Resonance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ResonanceLensItem extends Item {
	public ResonanceLensItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			int resonance = Resonance.add(serverPlayer, 5);
			serverPlayer.getCooldowns().addCooldown(this, 20);
			serverPlayer.displayClientMessage(Component.literal("The lens resonates at " + resonance), true);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
