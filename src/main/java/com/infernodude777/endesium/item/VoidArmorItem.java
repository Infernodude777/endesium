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
	private final Type armorType;
	public VoidArmorItem(Holder<ArmorMaterial> material, Type type, Item.Properties properties) {
		super(material, type, properties);
		this.armorType = type;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		switch (armorType) {
			case HELMET -> {
				tooltip.add(Component.literal("Void Sight: Night Vision + Water Breathing").withStyle(ChatFormatting.GRAY));
				tooltip.add(Component.literal("Marks void ore within 16 blocks with a faint glint").withStyle(ChatFormatting.DARK_GRAY));
			}
			case CHESTPLATE -> {
				tooltip.add(Component.literal("Gravitic Guard: Resistance I + emergency absorption").withStyle(ChatFormatting.GRAY));
				tooltip.add(Component.literal("below 50% health — the void holds you together").withStyle(ChatFormatting.DARK_GRAY));
			}
			case LEGGINGS -> tooltip.add(Component.literal("Void Channeling: Haste II while worn").withStyle(ChatFormatting.GRAY));
			case BOOTS -> tooltip.add(Component.literal("Anchor: +25% knockback resistance").withStyle(ChatFormatting.GRAY));
			default -> tooltip.add(Component.literal("Void plate; accepts trims like netherite").withStyle(ChatFormatting.GRAY));
		}
		if (armorType == Type.CHESTPLATE) {
			tooltip.add(Component.literal("Full set: unlocks singularity on Void Sword (hold 3s)").withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}