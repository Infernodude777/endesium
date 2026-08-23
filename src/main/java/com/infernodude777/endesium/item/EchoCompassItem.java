package com.infernodude777.endesium.item;

import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import com.infernodude777.endesium.resonance.ResonanceManager;
import com.infernodude777.endesium.resonance.ResonanceManager.Signal;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The first progression capability beyond the Lens. The Lens reports a bounded,
 * qualitative reading; the Echo Compass turns a signal the player has already
 * proven they can interpret (by waking a mechanism for its Resonance Token)
 * into a navigable heading and distance. It never reveals coordinates.
 */
public class EchoCompassItem extends Item {
	private static final int COOLDOWN_TICKS = 60;
	/** Maximum resonance strength exposed in a tooltip or client effect. */
	public static final float MAX_STRENGTH = 2.0F;

	public EchoCompassItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			ServerLevel serverLevel = serverPlayer.serverLevel();
			level.playSound(null, serverPlayer.blockPosition(), ModSounds.ECHO_COMPASS_USE,
					SoundSource.PLAYERS, 0.5F, 1.0F);
			Signal signal = ResonanceManager.get(serverLevel).sample(serverPlayer);
			if (signal.band() == ResonanceManager.Band.NONE) {
				serverPlayer.displayClientMessage(Component.literal("The echo compass is still."), true);
			} else {
				double distance = Math.sqrt(signal.sourcePosition().distSqr(serverPlayer.blockPosition()));
				int rounded = (int) Math.round(distance / 10.0D) * 10;
				String direction = ResonanceManager.cardinal(signal.directionBucket());
				if (rounded < 10) {
					serverPlayer.displayClientMessage(
							Component.literal("The needle trembles — right here, to the " + direction + "."), true);
				} else {
					serverPlayer.displayClientMessage(Component.literal(
							"The needle pulls " + direction + ", about " + rounded + " blocks away."), true);
				}
				emitDirectionTrail(serverLevel, serverPlayer, signal);
			}
			serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Turns a signal you have already learned to read"));
		tooltip.add(Component.literal("into a heading and a distance."));
	}

	/** A short white particle line toward the source, so the reading is physical. */
	private static void emitDirectionTrail(ServerLevel level, ServerPlayer player, Signal signal) {
		Vec3 from = player.getEyePosition();
		Vec3 to = Vec3.atCenterOf(signal.sourcePosition());
		Vec3 direction = to.subtract(from).normalize();
		for (int i = 1; i <= 6; i++) {
			Vec3 point = from.add(direction.scale(i * 0.35D));
			level.sendParticles(ModParticles.RESONANCE_BEAM, point.x, point.y, point.z,
					1, 0.0D, 0.0D, 0.0D, 0.0D);
		}
	}
}
