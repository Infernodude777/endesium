package com.infernodude777.endesium.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The Progression Guide: a step-by-step companion covering how to obtain and
 * craft everything Endesium adds, from the first Void Shard to the Archive
 * Sigil. Right-clicking opens its screen via the UseItemCallback hook in
 * {@link com.infernodude777.endesium.client.EndiumClient}; this class only
 * reports a clean use so both sides of the interaction stay consistent.
 */
public class ProgressionGuideItem extends Item {
    public ProgressionGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Every path through the End, written down."));
        tooltip.add(Component.literal("How to get it. How to craft it. Where it lives."));
    }
}
