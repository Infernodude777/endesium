package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

/**
 * Void armor uses Minecraft's normal humanoid armor renderer with the
 * {@code void_layer_1} and {@code void_layer_2} textures. The {@code void_trim}
 * overlay recolors when Ancient Gold trim is applied, matching the
 * netherite-plus-trim workflow the set is designed around.
 */
public final class VoidArmorItem extends ArmorItem {
	public VoidArmorItem(Holder<ArmorMaterial> material, Type type, Item.Properties properties) {
		super(material, type, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Void plate; accepts trims like netherite").withStyle(ChatFormatting.GRAY));
	}
}