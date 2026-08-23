package com.infernodude777.endesium.state;

import com.infernodude777.endesium.Endesium;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

/**
 * Wires the boss-reward meta progression into the server loop:
 * the Ascendant's End-only regen aura, and Golem's Resolve — a
 * once-per-day cheat against death, earned by absorbing ten cores.
 */
public final class BossRewardEvents {
	private BossRewardEvents() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 80 != 0) return;
			AttunementState state = AttunementState.get(server);
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!player.isAlive()) continue;
				if (player.level().dimension() != Level.END) continue;
				if (state.isAscendant(player)) {
					AttunementState.applyAscendantPulse(player);
				}
			}
		});

		// Golem's Resolve: once per in-game day, death itself is refused.
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
			if (!(entity instanceof ServerPlayer player)) return true;
			if (player.level().dimension() != Level.END) return true;
			AttunementState state = AttunementState.get(player.server);
			if (!state.isResolveUnlocked(player)) return true;
			DamageSource source = damageSource;
			if (source == null || source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
			if (!state.tryConsumeResolve(player)) return true;

			player.setHealth(Math.max(8.0F, player.getMaxHealth() * 0.4F));
			player.removeAllEffects();
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
			player.level().playSound(null, player.blockPosition(),
					SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.7F);
			player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING,
					player.getX(), player.getY() + 1.0D, player.getZ(),
					40, 0.6D, 0.8D, 0.6D, 0.08D);
			player.displayClientMessage(Component.literal(
					"\u00A75The golem's core holds your soul together. Until tomorrow."), true);
			return false;
		});

		Endesium.LOGGER.info("Registered Endesium boss reward events");
	}
}
