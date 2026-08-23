package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;

import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Detects nearby mineral formations in the Crystal Barrens. It pings the
 * nearest crystal deposits within a radius with a brief particle flash and
 * reports how many formations it can sense, never their exact positions.
 */
public final class CrystalResonatorItem extends Item {
	/** Scan a bounded, loaded-friendly sphere; do not promise a range the code does not use. */
	private static final int RADIUS = 24;
	private static final int COOLDOWN_TICKS = 100;

	public CrystalResonatorItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			ServerLevel serverLevel = serverPlayer.serverLevel();
			int found = 0;
			BlockPos center = player.blockPosition();
			for (int x = -RADIUS; x <= RADIUS; x += 2) {
				for (int y = -6; y <= 6; y++) {
					for (int z = -RADIUS; z <= RADIUS; z += 2) {
						if (x * x + z * z > RADIUS * RADIUS) continue;
						BlockPos pos = center.offset(x, y, z);
						if (isCrystal(serverLevel, pos)) {
							found++;
							if (found <= 6) {
								serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
										pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
										1, 0.0D, 0.0D, 0.0D, 0.0D);
							}
						}
					}
				}
			}
			level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_RESONATE,
					SoundSource.PLAYERS, 0.8F, 1.0F + Math.min(found, 6) * 0.1F);
			serverPlayer.displayClientMessage(Component.literal(
					found == 0 ? "The resonator is silent — no minerals nearby."
							: "The resonator hums: " + found + " mineral formations detected."), true);
			serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	private static boolean isCrystal(Level level, BlockPos pos) {
		var state = level.getBlockState(pos);
		return state.is(ModBlocks.CRYSTAL_SHARD_BLOCK)
				|| state.is(ModBlocks.CRYSTAL_CLUSTER)
				|| state.is(ModBlocks.DARK_CRYSTAL_BLOCK)
				|| state.is(ModBlocks.PALE_CRYSTAL_BLOCK)
				|| state.is(ModBlocks.DORMANT_RESONANT_CRYSTAL);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Senses nearby crystal formations (loaded chunks only)").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Reports a count, never positions; 5s cooldown").withStyle(ChatFormatting.GRAY));
	}
}