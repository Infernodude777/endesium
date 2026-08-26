package com.infernodude777.endesium.gear;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModEndgear;
import com.infernodude777.endesium.registry.ModItems;
import java.util.Random;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Per-piece passives for the three non-void armor lines. Each individual
 * item carries a unique power; the full set upgrades a capstone.
 *
 * <p>Theme:
 * <ul>
 *   <li><b>Luminous</b> (grove light) - vision, mobility, and radiant retaliation.</li>
 *   <li><b>Ash</b> (volcanic) - fire mastery and burn-powered strength.</li>
 *   <li><b>Null</b> (deletion) - gravity/motion erasure and projectile phasing.</li>
 * </ul>
 *
 * <p>All effect refresh uses a BUFFER larger than its cadence so there is never
 * an unprotected tick, and all refreshes are silent (no particles, no flicker).
 */
public final class GearAbilities {
	private static final int REFRESH = 40;
	private static final int BUFFER = 120;
	private static final int LONG_BUFFER = 200;
	private static final Random RNG = new Random();
	private static final ThreadLocal<Boolean> RETALIATING = ThreadLocal.withInitial(() -> false);
	private static final AttributeModifier NULL_STEP = new AttributeModifier(
			Endesium.id("null_step"), 1.0D,
			AttributeModifier.Operation.ADD_VALUE);

	private GearAbilities() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(GearAbilities::tick);
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(GearAbilities::onDamage);
		Endesium.LOGGER.info("Registered gear ability layer (luminous / ash / null - per-piece + set bonuses)");
	}

	private static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPerPlayer(player, server);
		}
	}

	private static void tickPerPlayer(ServerPlayer player, MinecraftServer server) {
		// Per-tick things: null-leggings fall cancel + null-helmet affliction purge.
		if (player.getItemBySlot(EquipmentSlot.LEGS).is(ModEndgear.NULL_LEGGINGS)) {
			player.resetFallDistance();
		}
		if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModEndgear.NULL_HELMET)) {
			player.removeEffect(MobEffects.LEVITATION);
			player.removeEffect(MobEffects.DARKNESS);
			player.removeEffect(MobEffects.CONFUSION);
		}
		applyStepHeight(player);

		// Refresh-cadenced effect applications (keep every REFRESH ticks).
		if (server.getTickCount() % REFRESH != 0) return;

		// ---- Luminous per-piece ----
		if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModEndgear.LUMINOUS_HELMET)) {
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, BUFFER, 0, false, false, true));
		}
		boolean luminousLegs = player.getItemBySlot(EquipmentSlot.LEGS).is(ModEndgear.LUMINOUS_LEGGINGS);
		boolean luminousBoots = player.getItemBySlot(EquipmentSlot.FEET).is(ModEndgear.LUMINOUS_BOOTS);
		boolean luminousFull = isFullLuminous(player);
		if (luminousLegs) {
			int amp = luminousFull ? 1 : 0; // Speed I alone, Speed II as set bonus
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFFER, amp, false, false, true));
		}
		if (luminousBoots) {
			player.addEffect(new MobEffectInstance(MobEffects.JUMP, BUFFER, 1, false, false, true));
		}

		// ---- Ash per-piece ----
		if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModEndgear.ASH_HELMET)) {
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, BUFFER, 0, false, false, true));
		}
		if (player.getItemBySlot(EquipmentSlot.LEGS).is(ModEndgear.ASH_LEGGINGS)) {
			if (player.isOnFire() || isStandingOnLava(player)) {
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BUFFER, 0, false, false, true));
			}
		}
		if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.ASH_CHESTPLATE)) {
			if (player.isOnFire() || isStandingOnLava(player)) {
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFFER, 0, false, false, true));
			}
		}
		// Full Ash set: permanent Strength I capstone
		if (isFullAsh(player)) {
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFFER, 0, false, false, true));
		}

		// ---- Dragon wings (existing, untouched) ----
		if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.DRAGON_WINGS)) {
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BUFFER, 0, false, false, true));
		}
	}

	private static boolean isFullLuminous(ServerPlayer player) {
		return player.getItemBySlot(EquipmentSlot.HEAD).is(ModEndgear.LUMINOUS_HELMET)
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.LUMINOUS_CHESTPLATE)
				&& player.getItemBySlot(EquipmentSlot.LEGS).is(ModEndgear.LUMINOUS_LEGGINGS)
				&& player.getItemBySlot(EquipmentSlot.FEET).is(ModEndgear.LUMINOUS_BOOTS);
	}

	private static boolean isFullAsh(ServerPlayer player) {
		return player.getItemBySlot(EquipmentSlot.HEAD).is(ModEndgear.ASH_HELMET)
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.ASH_CHESTPLATE)
				&& player.getItemBySlot(EquipmentSlot.LEGS).is(ModEndgear.ASH_LEGGINGS)
				&& player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ASHWALKER_BOOTS);
	}

	private static boolean isFullNull(ServerPlayer player) {
		return player.getItemBySlot(EquipmentSlot.HEAD).is(ModEndgear.NULL_HELMET)
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.NULL_CHESTPLATE)
				&& player.getItemBySlot(EquipmentSlot.LEGS).is(ModEndgear.NULL_LEGGINGS)
				&& player.getItemBySlot(EquipmentSlot.FEET).is(ModEndgear.NULL_BOOTS);
	}

	private static boolean isStandingOnLava(ServerPlayer player) {
		BlockPos below = BlockPos.containing(player.getX(), player.getBoundingBox().minY - 0.1D, player.getZ());
		BlockState s = player.level().getBlockState(below);
		return s.is(Blocks.LAVA);
	}

	private static void applyStepHeight(ServerPlayer player) {
		AttributeInstance inst = player.getAttribute(Attributes.STEP_HEIGHT);
		if (inst == null) return;
		boolean worn = player.getItemBySlot(EquipmentSlot.FEET).is(ModEndgear.NULL_BOOTS);
		boolean has = inst.getModifier(NULL_STEP.id()) != null;
		if (worn && !has) {
			inst.addTransientModifier(NULL_STEP);
		} else if (!worn && has) {
			inst.removeModifier(NULL_STEP.id());
		}
	}

	private static boolean isProjectile(DamageSource source) {
		return source.is(DamageTypeTags.IS_PROJECTILE);
	}

	private static boolean onDamage(LivingEntity target, DamageSource source, float amount) {
		if (!(target instanceof ServerPlayer player)) return true;
		Entity direct = source.getDirectEntity();
		Entity attackerE = source.getEntity();
		boolean isFullLum = isFullLuminous(player);
		boolean isFullNull = isFullNull(player);

		// ---- Null full set: 25% projectile dodge ----
		if (isFullNull && isProjectile(source)) {
			if (RNG.nextFloat() < 0.25F) {
				if (player.level() instanceof ServerLevel sl) {
					sl.sendParticles(ParticleTypes.SMOKE,
							player.getX(), player.getY() + 1.0D, player.getZ(),
							10, 0.5D, 0.8D, 0.5D, 0.02D);
					sl.playSound(null, BlockPos.containing(player.position()),
							SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.5F, 1.6F);
				}
				return false; // cancel damage
			}
		}

		if (attackerE == null || !(attackerE instanceof LivingEntity attacker) || attacker == target) return true;
		if (Boolean.TRUE.equals(RETALIATING.get())) return true;

		RETALIATING.set(true);
		try {
			// ---- Luminous chest: radiant aegis ----
			if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.LUMINOUS_CHESTPLATE)) {
				attacker.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
				if (isFullLum) {
					attacker.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
					if (attacker.level() instanceof ServerLevel) {
						attacker.hurt(player.damageSources().magic(), 3.0F);
					}
				}
			}
			// ---- Ash chest: searing plate ----
			if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.ASH_CHESTPLATE)) {
				attacker.igniteForSeconds(4);
			}
			// ---- Null chest: erased wound ----
			if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.NULL_CHESTPLATE)) {
				player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0, false, false, true));
			}
		} finally {
			RETALIATING.set(false);
		}
		return true;
	}
}
