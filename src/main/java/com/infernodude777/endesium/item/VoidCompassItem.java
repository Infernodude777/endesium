package com.infernodude777.endesium.item;
import net.minecraft.world.item.TooltipFlag;

import com.infernodude777.endesium.world.EndesiumRegions;
import com.infernodude777.endesium.world.EndesiumWorldgenSeeds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A navigation tool for the void biomes. Using it reports the direction and
 * distance to the nearest void region boundary, so a lost player can always
 * find their way back to the Void Skirts. Server-side only: the client never
 * learns absolute coordinates, preserving the mod's quiet-signal language.
 */
public final class VoidCompassItem extends Item {
	private static final int SCAN_RADIUS = 128;

	public VoidCompassItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.sidedSuccess(stack, true);
		}
		// Region math only means something where Endesium biomes generate;
		// elsewhere the needle would spin on an uncaptured seed.
		if (level.dimension() != Level.END) {
			player.displayClientMessage(Component.literal("The void compass only turns beneath the End sky.")
					.withStyle(ChatFormatting.GRAY), true);
			return InteractionResultHolder.fail(stack);
		}
		ServerLevel server = (ServerLevel) level;
		long seed = EndesiumWorldgenSeeds.get();
		int px = player.getBlockX();
		int pz = player.getBlockZ();
		int bestX = 0;
		int bestZ = 0;
		double bestDist = Double.MAX_VALUE;
		boolean found = false;
		for (int x = px - SCAN_RADIUS; x <= px + SCAN_RADIUS; x += 8) {
			for (int z = pz - SCAN_RADIUS; z <= pz + SCAN_RADIUS; z += 8) {
				int region = EndesiumRegions.regionAt(seed, x, z);
				if (isVoidRegion(region)) {
					double d = Math.hypot(x - px, z - pz);
					if (d < bestDist) {
						bestDist = d;
						bestX = x;
						bestZ = z;
						found = true;
					}
				}
			}
		}
		if (found) {
			double dx = bestX - px;
			double dz = bestZ - pz;
			double angle = Math.toDegrees(Math.atan2(dz, dx));
			String dir = directionName(angle);
			player.displayClientMessage(Component.literal("Void " + dir + " · " + (int) bestDist + " blocks")
					.withStyle(ChatFormatting.DARK_AQUA), true);
		} else {
			player.displayClientMessage(Component.literal("No void region nearby")
					.withStyle(ChatFormatting.GRAY), true);
		}
		level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
				SoundSource.PLAYERS, 0.6F, 0.8F);
		player.getCooldowns().addCooldown(this, 40);
		return InteractionResultHolder.sidedSuccess(stack, true);
	}

	private static boolean isVoidRegion(int region) {
		return region == EndesiumRegions.VOID_SKIRTS
				|| region == EndesiumRegions.VOID_CROWN
				|| region == EndesiumRegions.UMBRAL_REACH;
	}

	private static String directionName(double angle) {
		if (angle < -157.5 || angle >= 157.5) return "West";
		if (angle < -112.5) return "Northwest";
		if (angle < -67.5) return "North";
		if (angle < -22.5) return "Northeast";
		if (angle < 22.5) return "East";
		if (angle < 67.5) return "Southeast";
		if (angle < 112.5) return "South";
		if (angle < 157.5) return "Southwest";
		return "West";
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Points toward the nearest void region boundary").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Direction and distance band only, never coordinates").withStyle(ChatFormatting.GRAY));
	}
}