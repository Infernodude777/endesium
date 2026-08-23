package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;

import com.infernodude777.endesium.world.EndBiomeProfiles;
import com.infernodude777.endesium.world.EndesiumRegions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A reusable exploration tool. Using it records the biome you are standing in
 * into the item itself and reports how many of the ten Endesium regions you
 * have charted, turning exploration into a personal log rather than a score.
 */
public final class EndCartographerItem extends Item {
	private static final int COOLDOWN_TICKS = 20;

	public EndCartographerItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			int region = EndBiomeProfiles.regionOf(level.getBiome(serverPlayer.blockPosition()));
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			int charted = 0;
			for (int i = 0; i < EndesiumRegions.COUNT; i++) {
				if (tag.getBoolean("region_" + i)) {
					charted++;
				}
			}
			if (region >= 0 && !tag.getBoolean("region_" + region)) {
				tag.putBoolean("region_" + region, true);
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
				charted++;
				serverPlayer.displayClientMessage(Component.literal(
						"Charted " + regionName(region) + ". (" + charted + "/" + EndesiumRegions.COUNT + " regions recorded)"), true);
			} else if (region >= 0) {
				serverPlayer.displayClientMessage(Component.literal(
						regionName(region) + " already charted. (" + charted + "/" + EndesiumRegions.COUNT + " regions recorded)"), true);
			} else {
				serverPlayer.displayClientMessage(Component.literal(
						"This is not one of the ten Endesium regions. (" + charted + "/" + EndesiumRegions.COUNT + " recorded)"), true);
			}
			serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	private static String regionName(int region) {
		switch (region) {
			case EndesiumRegions.END_WASTES: return "the End Wastes";
			case EndesiumRegions.SHATTERED_HIGHLANDS: return "the Shattered Highlands";
			case EndesiumRegions.VOID_MARSHES: return "the Void Marshes";
			case EndesiumRegions.CHORUS_WILDS: return "the Chorus Wilds";
			case EndesiumRegions.LUMINOUS_GROVES: return "the Luminous Groves";
			case EndesiumRegions.ASHEN_EXPANSE: return "the Ashen Expanse";
			case EndesiumRegions.CRYSTAL_BARRENS: return "the Crystal Barrens";
			case EndesiumRegions.VOID_SKIRTS: return "the Void Skirts";
			case EndesiumRegions.VOID_CROWN: return "the Void Crown";
			case EndesiumRegions.UMBRAL_REACH: return "the Umbral Reach";
			default: return "an unknown region";
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Use: record your current region into this log").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Tracks how many of the ten regions you have charted").withStyle(ChatFormatting.GRAY));
	}
}