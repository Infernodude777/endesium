package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The Endesium Guidebook, granted on the first Ender Dragon kill. Right-clicking
 * opens the in-game guidebook screen. The screen itself lives in the client
 * source set, so it is opened by a client-side UseItemCallback hook registered
 * in EndesiumClient rather than from this class. Here we simply report a
 * successful, non-consuming use so the server never sees anything unusual.
 */
public class EndesiumGuidebookItem extends Item {
	public EndesiumGuidebookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		// The actual screen is opened by the client-side UseItemCallback hook in
		// EndesiumClient. This method only reports the use as successful.
		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Right-click: open the guidebook").withStyle(ChatFormatting.GRAY));
	}
}