package com.infernodude777.endesium.item;

import com.infernodude777.endesium.block.InscribedSlateBlock;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.resonance.ResonanceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Shared implementation for the expanded biome relics. The common cooldown,
 * durability, sound, and server authority stay centralized, while each relic
 * has one small, bounded action that actually matches its structure contract.
 */
public final class BiomeRelicItem extends Item {
	public enum RelicAction {
		WIND_LIFT,
		MARSH_PULSE,
		LUMEN_FLASH,
		CROWN_TUNE,
		NULL_RECALL
	}

	private final String message;
	private final Holder<MobEffect> effect;
	private final int effectTicks;
	private final int cooldownTicks;
	private final RelicAction action;

	public BiomeRelicItem(Properties properties, String message, Holder<MobEffect> effect,
			int effectTicks, int cooldownTicks, RelicAction action) {
		super(properties);
		this.message = message;
		this.effect = effect;
		this.effectTicks = effectTicks;
		this.cooldownTicks = cooldownTicks;
		this.action = action;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal(message));
		tooltip.add(Component.literal("Cooldown: " + Math.max(1, cooldownTicks / 20) + "s"));
		switch (action) {
			case CROWN_TUNE -> tooltip.add(Component.literal("Tunes to the strongest loaded resonance."));
			case MARSH_PULSE -> tooltip.add(Component.literal("Reveals nearby solid footing."));
			case NULL_RECALL -> tooltip.add(Component.literal("Recalls a nearby inscribed clue."));
			case LUMEN_FLASH -> tooltip.add(Component.literal("Briefly opens sight in the dark."));
			case WIND_LIFT -> tooltip.add(Component.literal("Catches a short upward current."));
		}
		if (stack.isDamageableItem()) {
			tooltip.add(Component.literal("Durability is consumed on use."));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			if (serverPlayer.level().dimension() != Level.END) {
				serverPlayer.displayClientMessage(Component.literal("The relic is silent outside the End."), true);
				return InteractionResultHolder.fail(stack);
			}
			ServerLevel serverLevel = serverPlayer.serverLevel();
			if (effect != null) {
				serverPlayer.addEffect(new MobEffectInstance(effect, effectTicks, 0, false, false, true));
			}
			String feedback = performAction(serverLevel, serverPlayer);
			serverPlayer.displayClientMessage(Component.literal(feedback), true);
			serverLevel.playSound(null, serverPlayer.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.PLAYERS, 0.75F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F);
			serverLevel.sendParticles(ModParticles.RESONANCE_PULSE, serverPlayer.getX(), serverPlayer.getY(0.7D),
					serverPlayer.getZ(), 10, 0.5D, 0.4D, 0.5D, 0.02D);
			serverPlayer.getCooldowns().addCooldown(this, cooldownTicks);
			if (stack.isDamageableItem()) {
				EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
				stack.hurtAndBreak(1, serverPlayer, slot);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	private String performAction(ServerLevel level, ServerPlayer player) {
		switch (action) {
			case WIND_LIFT:
				return message;
			case LUMEN_FLASH:
				level.sendParticles(ModParticles.RESONANCE_ACTIVE, player.getX(), player.getY(0.8D), player.getZ(),
						18, 1.2D, 1.0D, 1.2D, 0.03D);
				return message;
			case MARSH_PULSE:
				pulseSafeFooting(level, player);
				return message;
			case CROWN_TUNE:
				return tuneCrown(level, player);
			case NULL_RECALL:
				return recallInscription(level, player);
			default:
				return message;
		}
	}

	private static void pulseSafeFooting(ServerLevel level, ServerPlayer player) {
		BlockPos feet = player.blockPosition();
		int marked = 0;
		for (int dx = -4; dx <= 4 && marked < 8; dx++) {
			for (int dz = -4; dz <= 4 && marked < 8; dz++) {
				BlockPos pos = feet.offset(dx, -1, dz);
				BlockState state = level.getBlockState(pos);
				if (!state.isSolidRender(level, pos)) continue;
				level.sendParticles(ModParticles.RESONANCE_ACTIVE, pos.getX() + 0.5D, pos.getY() + 1.05D,
						pos.getZ() + 0.5D, 2, 0.12D, 0.05D, 0.12D, 0.01D);
				marked++;
			}
		}
	}

	private static String tuneCrown(ServerLevel level, ServerPlayer player) {
		ResonanceManager.Signal signal = ResonanceManager.get(level).sample(player);
		if (signal.band() == ResonanceManager.Band.NONE) {
			return "The Crown Needle finds no loaded signal.";
		}
		return "The Crown Needle leans " + ResonanceManager.cardinal(signal.directionBucket())
				+ " toward a " + signal.sourceType().name().toLowerCase().replace('_', ' ') + ".";
	}

	private static String recallInscription(ServerLevel level, ServerPlayer player) {
		BlockPos nearest = null;
		double nearestDistance = 12.0D * 12.0D;
		BlockPos center = player.blockPosition();
		for (int dx = -12; dx <= 12; dx++) {
			for (int dy = -5; dy <= 5; dy++) {
				for (int dz = -12; dz <= 12; dz++) {
					BlockPos pos = center.offset(dx, dy, dz);
					if (!level.getBlockState(pos).is(ModBlocks.INSCRIBED_SLATE)) continue;
					double distance = pos.distSqr(center);
					if (distance < nearestDistance) {
						nearestDistance = distance;
						nearest = pos.immutable();
					}
				}
			}
		}
		if (nearest == null) {
			return "The quill finds no erased line nearby.";
		}
		BlockState state = level.getBlockState(nearest);
		int symbol = state.getValue(InscribedSlateBlock.SYMBOL);
		level.sendParticles(ModParticles.RESONANCE_ACTIVE, nearest.getX() + 0.5D, nearest.getY() + 0.6D,
				nearest.getZ() + 0.5D, 8, 0.2D, 0.2D, 0.2D, 0.02D);
		return "The missing line returns: " + symbolName(symbol) + ".";
	}

	private static String symbolName(int symbol) {
		return switch (symbol) {
			case InscribedSlateBlock.SYMBOL_RING -> "the ring";
			case InscribedSlateBlock.SYMBOL_SPIRE -> "the spire";
			case InscribedSlateBlock.SYMBOL_EYE -> "the eye";
			default -> "a quiet mark";
		};
	}
}
