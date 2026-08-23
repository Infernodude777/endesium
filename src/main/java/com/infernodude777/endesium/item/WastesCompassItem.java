package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * An ancient navigation marker. It does not reveal coordinates: it reports a
 * heading and a rounded distance toward the Heart of the End (the Dragon's
 * central island), which is the one landmark a lost traveler always needs.
 */
public final class WastesCompassItem extends Item {
	private static final int COOLDOWN_TICKS = 30;

	public WastesCompassItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			if (serverPlayer.level().dimension() != Level.END) {
				serverPlayer.displayClientMessage(Component.literal("The compass only turns beneath the End sky."), true);
				return InteractionResultHolder.fail(stack);
			}
			double dx = -player.getX();
			double dz = -player.getZ();
			double distance = Math.sqrt(dx * dx + dz * dz);
			int rounded = (int) Math.round(distance / 50.0D) * 50;
			String direction = cardinal(Math.atan2(dz, dx) * (180.0D / Math.PI));
			serverPlayer.displayClientMessage(Component.literal(
					"The Heart of the End lies " + direction + ", roughly " + rounded + " blocks away."), true);
			serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	private static String cardinal(double degrees) {
		double d = (degrees + 360.0D) % 360.0D;
		// atan2(dz, dx) uses +X=east and +Z=south, matching Minecraft's
		// coordinate system. The old table was rotated 90 degrees, so the
		// compass consistently lied to players traveling along diagonals.
		if (d < 22.5D || d >= 337.5D) return "east";
		if (d < 67.5D) return "south-east";
		if (d < 112.5D) return "south";
		if (d < 157.5D) return "south-west";
		if (d < 202.5D) return "west";
		if (d < 247.5D) return "north-west";
		if (d < 292.5D) return "north";
		return "north-east";
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Heading toward the Heart of the End (the central island)").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Rounded distance only, never coordinates").withStyle(ChatFormatting.GRAY));
	}
}