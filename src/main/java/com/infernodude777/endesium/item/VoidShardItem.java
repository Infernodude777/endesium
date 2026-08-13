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

public class VoidShardItem extends Item {
	public VoidShardItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			int resonance = Resonance.add(serverPlayer, 1);
			serverPlayer.displayClientMessage(Component.literal("Resonance: " + resonance), true);
			stack.consume(1, player);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
