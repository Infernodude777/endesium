package com.infernodude777.endesium.item;

import java.util.List;

import com.infernodude777.endesium.menu.LoreBookMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The Progression Guide: the Endesium lore book. Place any item in its socket
 * and it tells you everything the archives know - what it is, how to get it,
 * and what it is for. Right-clicking opens the socket menu; a button inside
 * opens the written field-guide pages.
 */
public class ProgressionGuideItem extends Item {
    public ProgressionGuideItem(Properties properties) {
        super(properties);
    }

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		// The server opens the lore book's menu; the slot syncs to the client
		// screen, which reads the archives for whatever item is socketed.
		if (player instanceof net.minecraft.server.level.ServerPlayer server) {
			server.openMenu(new net.minecraft.world.SimpleMenuProvider(
					(id, inv, p) -> new LoreBookMenu(id, inv),
					Component.literal("Endesium Lore")));
		}
		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
	}

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Place an item in the socket to read its lore."));
        tooltip.add(Component.literal("Every path through the End, written down."));
    }
}
