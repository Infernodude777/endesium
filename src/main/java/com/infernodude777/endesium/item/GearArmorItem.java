package com.infernodude777.endesium.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.core.Holder;

/**
 * Shared armor item for the Luminous / Ash / Null sets. Carries a per-piece
 * tooltip so the powers are visible in-game rather than hidden behind a wiki.
 */
public final class GearArmorItem extends ArmorItem {
	private final String[] tooltipLines;

	public GearArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties,
			String... tooltipLines) {
		super(material, type, properties);
		this.tooltipLines = tooltipLines;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip,
			TooltipFlag flag) {
		for (String line : tooltipLines) {
			tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
		}
	}
}
