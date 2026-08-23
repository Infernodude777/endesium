package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The Resonant Wings: Endesium's custom elytra. It behaves exactly like the
 * vanilla elytra in flight, but its worn model uses the Endesium wing texture
 * (swapped client-side by {@code ElytraLayerMixin}), and it repairs with
 * Phantom Membranes.
 */
public final class ResonantWingsItem extends ElytraItem {
	public ResonantWingsItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
		return repairCandidate.is(Items.PHANTOM_MEMBRANE);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Elytra flight in Endesium's wing silhouette").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Repairs with Phantom Membranes").withStyle(ChatFormatting.GRAY));
	}
}