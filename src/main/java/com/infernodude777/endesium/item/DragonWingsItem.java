package com.infernodude777.endesium.item;

import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * The Dragon Wings: the dragon's own flight, worn. Elytra flight with the
 * resonant wings' silhouette (recrimson), repaired with Resonant Dragon
 * Scales, and iron-chestplate defense baked into the item's attribute
 * component. Enchantment rules follow the elytra: only Unbreaking and
 * Mending apply. The cost is permanent Slowness I while worn (see
 * {@link com.infernodude777.endesium.gear.GearAbilities}).
 */
public class DragonWingsItem extends ElytraItem {
	public DragonWingsItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
		return repairCandidate.is(ModItems.RESONANT_DRAGON_SCALE);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Elytra flight, iron-chestplate defense").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Repairs with Resonant Dragon Scales").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("The weight of wings: permanent Slowness I").withStyle(ChatFormatting.DARK_GRAY));
	}
}
