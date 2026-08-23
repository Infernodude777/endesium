package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import com.infernodude777.endesium.state.PostDragonState;
import com.infernodude777.endesium.world.ArenaGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Endesium's Dragon fight: a four-phase combat controller that layers readable,
 * telegraphed attacks on top of the vanilla Dragon AI. It never replaces the
 * vanilla phases (the Dragon still flies, perches, and breathes as usual) and
 * never touches world state; {@code PostDragonState} remains the only authority
 * for the transformation.
 *
 * <p>All damage uses the vanilla armor-respecting mob attack source. Every
 * attack has a telegraph window, an effect, and a recovery window, and the
 * schedule pauses briefly at each phase transition so players always recognize
 * "the Dragon has changed".</p>
 */
public final class DragonFightController {
	private DragonFightController() {
	}

	/** Per-Dragon combat state; held by the mixin as a unique field. */
	public static final class State {
		int phase = 1;
		int transitionTicks;
		int attackTimer = 120;
		AttackType attackType = AttackType.NONE;
		int attackTick;
		int targetId = -1;
		int targetLockTicks;
		boolean transformed;
		boolean finalRoarPlayed;
		int lastAttack = -1;
		Vec3 divePoint = Vec3.ZERO;
		int galeTicks;
		int galeCooldown;
		int screechCooldown;
		int stormCooldown;
		int collapseCooldown;
		int catastrophicCooldown;
		int diveCooldown;
		int talonCooldown;
		int voidRiftCooldown;
		int howlCooldown;
		int meteorCooldown;
		int perchPulseTicks;
		int fissureTicks;
		int zoneTick;
		final List<Zone> breathZones = new ArrayList<>();
		final List<Zone> stormZones = new ArrayList<>();
		final List<Zone> collapseZones = new ArrayList<>();
		final List<Zone> riftZones = new ArrayList<>();
		final List<Zone> meteorZones = new ArrayList<>();
		/** Cached arena fractures; rebuilding these per player was needlessly expensive. */
		final List<double[]> fracturePoints = new ArrayList<>();

		public void clearZones() {
			fracturePoints.clear();
			attackType = AttackType.NONE;
			breathZones.clear();
			stormZones.clear();
			collapseZones.clear();
			riftZones.clear();
			meteorZones.clear();
		}

		/**
		 * Persists the fight state on the entity so a server restart cannot
		 * reset the phase mid-fight, replay the Final Roar, or re-apply the
		 * transformation buff (which heals a wounded Dragon back to full).
		 * Transient combat data (zones, attack ticks, dive points) is safely
		 * left to reset.
		 */
		public void save(net.minecraft.nbt.CompoundTag tag) {
			tag.putInt("EndesiumFightVersion", 1);
			tag.putInt("EndesiumPhase", phase);
			tag.putInt("EndesiumTransitionTicks", transitionTicks);
			tag.putInt("EndesiumAttackTimer", attackTimer);
			tag.putInt("EndesiumAttackType", attackType.ordinal());
			tag.putInt("EndesiumTargetId", targetId);
			tag.putInt("EndesiumTargetLockTicks", targetLockTicks);
			tag.putBoolean("EndesiumTransformed", transformed);
			tag.putBoolean("EndesiumFinalRoarPlayed", finalRoarPlayed);
			tag.putInt("EndesiumLastAttack", lastAttack);
			tag.putInt("EndesiumGaleCooldown", galeCooldown);
			tag.putInt("EndesiumScreechCooldown", screechCooldown);
			tag.putInt("EndesiumStormCooldown", stormCooldown);
			tag.putInt("EndesiumCollapseCooldown", collapseCooldown);
			tag.putInt("EndesiumCatastrophicCooldown", catastrophicCooldown);
			tag.putInt("EndesiumDiveCooldown", diveCooldown);
			tag.putInt("EndesiumTalonCooldown", talonCooldown);
			tag.putInt("EndesiumVoidRiftCooldown", voidRiftCooldown);
			tag.putInt("EndesiumHowlCooldown", howlCooldown);
			tag.putInt("EndesiumMeteorCooldown", meteorCooldown);
		}

		public void load(net.minecraft.nbt.CompoundTag tag) {
			if (tag.getInt("EndesiumFightVersion") != 1) return;
			phase = tag.getInt("EndesiumPhase");
			transitionTicks = tag.getInt("EndesiumTransitionTicks");
			attackTimer = tag.getInt("EndesiumAttackTimer");
			int attackOrdinal = tag.getInt("EndesiumAttackType");
			attackType = attackOrdinal >= 0 && attackOrdinal < AttackType.values().length
					? AttackType.values()[attackOrdinal] : AttackType.NONE;
			targetId = tag.getInt("EndesiumTargetId");
			targetLockTicks = tag.getInt("EndesiumTargetLockTicks");
			transformed = tag.getBoolean("EndesiumTransformed");
			finalRoarPlayed = tag.getBoolean("EndesiumFinalRoarPlayed");
			lastAttack = tag.getInt("EndesiumLastAttack");
			galeCooldown = tag.getInt("EndesiumGaleCooldown");
			screechCooldown = tag.getInt("EndesiumScreechCooldown");
			stormCooldown = tag.getInt("EndesiumStormCooldown");
			collapseCooldown = tag.getInt("EndesiumCollapseCooldown");
			catastrophicCooldown = tag.getInt("EndesiumCatastrophicCooldown");
			diveCooldown = tag.getInt("EndesiumDiveCooldown");
			talonCooldown = tag.getInt("EndesiumTalonCooldown");
			voidRiftCooldown = tag.getInt("EndesiumVoidRiftCooldown");
			howlCooldown = tag.getInt("EndesiumHowlCooldown");
			meteorCooldown = tag.getInt("EndesiumMeteorCooldown");
		}
	}

	/** A ground effect zone: center, radius, and a server-side lifetime. */
	private static final class Zone {
		private final double x;
		private final double y;
		private final double z;
		private final double radius;
		private int remaining;

		private Zone(double x, double y, double z, double radius, int ticks) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.radius = radius;
			this.remaining = ticks;
		}

		double x() { return x; }
		double y() { return y; }
		double z() { return z; }
		double radius() { return radius; }
		int ticks() { return remaining; }
		void tick() { remaining--; }
	}

	private enum AttackType {
		NONE, VOID_DIVE, HUNTING_DIVE, TRIPLE_DIVE, WING_SHOCKWAVE, BREATH,
		SCREECH, BARRAGE, BARRAGE_PLUS, GALE, TALON, SWEEP, STORM, COLLAPSE,
		FINAL_ROAR, CATASTROPHE,
		// Transformed-Dragon exclusive attacks
		VOID_RIFT, RESONANCE_HOWL, METEOR_SHOWER
	}

	public static void tick(EnderDragon dragon, ServerLevel level, State state) {
		// aiStep can still be entered on the first death-animation tick. Never
		// start or advance a delayed attack while vanilla is finalizing the fight.
		if (!dragon.isAlive() || dragon.dragonDeathTime != 0) {
			state.clearZones();
			return;
		}
		tickCooldowns(state);
		PostDragonState postDragon = PostDragonState.get(level);
		boolean transformed = postDragon.isTransformationActive();
		// The first Dragon is intentionally vanilla-compatible. The custom
		// scheduler belongs to the awakened, respawned Dragon only; otherwise
		// players would be punished by a post-Dragon system before reaching the
		// milestone that unlocks it.
		if (!transformed) {
			state.clearZones();
			return;
		}

		// --- Transformed (post-Dragon respawn) buffs, applied exactly once. ---
		if (transformed && !state.transformed) {
			state.transformed = true;
			var scale = dragon.getAttribute(Attributes.SCALE);
			if (scale != null) scale.setBaseValue(1.6F);
			var maxHealthAttribute = dragon.getAttribute(Attributes.MAX_HEALTH);
			if (maxHealthAttribute != null) {
				double maxHealth = Math.min(maxHealthAttribute.getBaseValue() * 2.0D, 400.0D);
				// On a reload the attribute base value may already be boosted
				// (the buff is persisted). Only top the health up when the buff
				// is genuinely new, so a server restart mid-fight cannot heal a
				// wounded Dragon back to full.
				boolean alreadyBuffed = maxHealthAttribute.getBaseValue() >= 200.0D;
				maxHealthAttribute.setBaseValue(maxHealth);
				if (!alreadyBuffed) dragon.setHealth((float) maxHealth);
			}
		}

		// Crimson motes communicate the phase and the transformation.
		if (dragon.tickCount % 2 == 0) {
			int count = transformed ? 3 : 1;
			level.sendParticles(transformed ? ParticleTypes.CRIMSON_SPORE : ModParticles.RESONANCE_ACTIVE,
					dragon.getX() + (level.random.nextDouble() - 0.5D) * 14.0D,
					dragon.getY() + level.random.nextDouble() * 8.0D,
					dragon.getZ() + (level.random.nextDouble() - 0.5D) * 14.0D,
					count, 0.0D, 0.0D, 0.0D, 0.0D);
		}

		updatePhase(dragon, level, state);
		if (state.transitionTicks > 0) {
			state.transitionTicks--;
			return;
		}

		LivingEntity target = selectTarget(dragon, level, state);
		tickZones(dragon, level, state, target);
		tickFissureHazard(dragon, level, state);
		tickPerchPulse(dragon, level, state);

		// --- Attack scheduler. ---
		if (state.attackType == AttackType.NONE) {
			if (state.attackTimer > 0) {
				state.attackTimer--;
				return;
			}
			chooseAttack(dragon, level, state, target);
			return;
		}
		advanceAttack(dragon, level, state, target);
	}

	// ------------------------------------------------------------------
	// Phases
	// ------------------------------------------------------------------

	private static void updatePhase(EnderDragon dragon, ServerLevel level, State state) {
		float maxHealth = Math.max(1.0F, dragon.getMaxHealth());
		float fraction = Math.clamp(dragon.getHealth() / maxHealth, 0.0F, 1.0F);
		// Keep the server thresholds identical to the client renderer and the
		// documented 75/45/20 stage bands. Divergent thresholds made a visual
		// stage change arrive before the attack pool changed.
		int phase = fraction > 0.75F ? 1 : fraction > 0.45F ? 2 : fraction > 0.20F ? 3 : 4;

		// Transformed Dragons announce different phases.
		String title;
		String subtitle;
		if (state.transformed) {
			title = switch (phase) {
				case 2 -> "The Awakened Hunts";
				case 3 -> "The Deep Resonance Trembles";
				case 4 -> "The End's Eternal Fury";
				default -> "The Awakened Watches";
			};
			subtitle = switch (phase) {
				case 2 -> "Resonance sharpens its ancient hunt";
				case 3 -> "The island remembers its wounds";
				case 4 -> "Nothing was ever held back";
				default -> "It remembers you";
			};
		} else {
			title = switch (phase) {
				case 2 -> "The Dragon Hunts";
				case 3 -> "The Wastes Tremble";
				case 4 -> "The End's Fury";
				default -> "The Dragon Watches";
			};
			subtitle = switch (phase) {
				case 2 -> "Resonance sharpens its hunting";
				case 3 -> "The island answers the wounds";
				case 4 -> "Nothing is held back";
				default -> "";
			};
		}
		if (phase == state.phase) return;
		state.phase = phase;
		state.transitionTicks = 80;
		state.attackType = AttackType.NONE;
		state.attackTimer = 100;

		// The Final Roar opens phase IV: scheduled to fire as soon as the
		// transition grace ends, then the desperate pool takes over.
		if (phase == 4 && !state.finalRoarPlayed) {
			state.finalRoarPlayed = true;
			state.attackType = AttackType.FINAL_ROAR;
			state.attackTick = 0;
		}

		level.playSound(null, dragon.blockPosition(), ModSounds.DRAGON_ROAR, SoundSource.HOSTILE, 1.4F, 1.0F);
		level.sendParticles(ModParticles.RESONANCE_PULSE, dragon.getX(), dragon.getY() + 2.0D, dragon.getZ(),
				90, 10.0D, 6.0D, 10.0D, 0.08D);
		level.sendParticles(ParticleTypes.CRIMSON_SPORE, dragon.getX(), dragon.getY() + 2.0D, dragon.getZ(),
				60, 9.0D, 5.0D, 9.0D, 0.10D);
		for (ServerPlayer player : level.players()) {
			player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
			player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
		}
	}

	// ------------------------------------------------------------------
	// Targeting
	// ------------------------------------------------------------------

	private static LivingEntity selectTarget(EnderDragon dragon, ServerLevel level, State state) {
		if (state.targetLockTicks > 0) {
			state.targetLockTicks--;
			LivingEntity locked = level.getEntity(state.targetId) instanceof LivingEntity living && living.isAlive()
					? living : null;
			if (locked != null && dragon.distanceToSqr(locked) < 160.0D * 160.0D) {
				return locked;
			}
		}
		ServerPlayer nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double distance = dragon.distanceToSqr(player);
			if (distance < nearestDistance) {
				nearest = player;
				nearestDistance = distance;
			}
		}
		if (nearest == null) return null;
		state.targetId = nearest.getId();
		state.targetLockTicks = 120;
		return nearest;
	}

	// ------------------------------------------------------------------
	// Attack selection
	// ------------------------------------------------------------------

	private static void chooseAttack(EnderDragon dragon, ServerLevel level, State state, LivingEntity target) {
		if (target == null) {
			state.attackTimer = 40;
			return;
		}
		double distance = Math.sqrt(dragon.distanceToSqr(target));
		boolean close = distance < 26.0D;
		boolean mid = distance < 70.0D;

		List<AttackType> pool = new ArrayList<>();
		switch (state.phase) {
			case 1 -> {
				pool.add(AttackType.VOID_DIVE);
				pool.add(AttackType.WING_SHOCKWAVE);
				pool.add(AttackType.BREATH);
				pool.add(AttackType.BARRAGE);
				if (close) pool.add(AttackType.SCREECH);
			}
			case 2 -> {
				pool.add(AttackType.HUNTING_DIVE);
				pool.add(AttackType.BREATH);
				pool.add(AttackType.BARRAGE);
				if (close) pool.add(AttackType.WING_SHOCKWAVE);
				if (close) pool.add(AttackType.SCREECH);
				if (close) pool.add(AttackType.TALON);
				if (mid) pool.add(AttackType.SWEEP);
				pool.add(AttackType.GALE);
			}
			case 3 -> {
				pool.add(AttackType.HUNTING_DIVE);
				pool.add(AttackType.BREATH);
				pool.add(AttackType.BARRAGE);
				pool.add(AttackType.GALE);
				pool.add(AttackType.STORM);
				pool.add(AttackType.COLLAPSE);
				if (close) pool.add(AttackType.TALON);
			}
			default -> {
				pool.add(AttackType.TRIPLE_DIVE);
				pool.add(AttackType.BARRAGE_PLUS);
				pool.add(AttackType.STORM);
				pool.add(AttackType.BREATH);
				pool.add(AttackType.CATASTROPHE);
				if (close) pool.add(AttackType.SCREECH);
			}
		}

		// --- Transformed (post-Dragon) exclusive attacks ---
		// A respawned Dragon wields resonance abilities the first never had.
		if (state.transformed) {
			if (state.phase >= 2) pool.add(AttackType.VOID_RIFT);
			if (state.phase >= 2 && close) pool.add(AttackType.RESONANCE_HOWL);
			if (state.phase >= 3) pool.add(AttackType.METEOR_SHOWER);
		}

		// Cooldowns gate the heavy attacks.
		if (state.galeCooldown > 0) pool.remove(AttackType.GALE);
		if (state.screechCooldown > 0) pool.remove(AttackType.SCREECH);
		if (state.stormCooldown > 0) pool.remove(AttackType.STORM);
		if (state.collapseCooldown > 0) pool.remove(AttackType.COLLAPSE);
		if (state.catastrophicCooldown > 0) pool.remove(AttackType.CATASTROPHE);
		if (state.diveCooldown > 0) pool.remove(AttackType.VOID_DIVE);
		if (state.diveCooldown > 0) pool.remove(AttackType.HUNTING_DIVE);
		if (state.diveCooldown > 0) pool.remove(AttackType.TRIPLE_DIVE);
		if (state.talonCooldown > 0) pool.remove(AttackType.TALON);
		if (state.voidRiftCooldown > 0) pool.remove(AttackType.VOID_RIFT);
		if (state.howlCooldown > 0) pool.remove(AttackType.RESONANCE_HOWL);
		if (state.meteorCooldown > 0) pool.remove(AttackType.METEOR_SHOWER);
		// Never repeat the same attack twice in a row.
		pool.removeIf(type -> type.ordinal() == state.lastAttack);
		if (pool.isEmpty()) {
			state.attackTimer = 30;
			return;
		}

		AttackType chosen = pool.get(level.random.nextInt(pool.size()));
		state.attackType = chosen;
		state.attackTick = 0;
		state.lastAttack = chosen.ordinal();
		beginAttack(dragon, level, state, chosen, target);
	}

	private static void beginAttack(EnderDragon dragon, ServerLevel level, State state, AttackType type,
			LivingEntity target) {
		switch (type) {
			case VOID_DIVE, HUNTING_DIVE -> {
				state.divePoint = predictedPoint(target, type == AttackType.HUNTING_DIVE);
				telegraphCircle(level, state.divePoint, 30, ParticleTypes.END_ROD);
				level.playSound(null, dragon.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.1F, 0.7F);
			}
			case TRIPLE_DIVE -> {
				state.divePoint = predictedPoint(target, true);
				telegraphCircle(level, state.divePoint, 24, ParticleTypes.END_ROD);
				level.playSound(null, dragon.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.2F, 0.6F);
			}
			case WING_SHOCKWAVE -> {
				level.playSound(null, dragon.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.0F, 1.3F);
			}
			case BREATH -> {
				level.sendParticles(ModParticles.RESONANCE_ACTIVE, dragon.getX(), dragon.getY() + 2.5D, dragon.getZ(),
						24, 1.0D, 1.0D, 1.0D, 0.04D);
				level.playSound(null, dragon.blockPosition(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 0.8F, 0.6F);
			}
			case SCREECH -> {
				level.playSound(null, dragon.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.3F, 1.6F);
			}
			case GALE -> {
				state.galeTicks = 80;
				level.playSound(null, dragon.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 1.6F, 0.5F);
			}
			case STORM -> {
				state.stormZones.clear();
				for (int i = 0; i < 5 + level.random.nextInt(3); i++) {
					double angle = level.random.nextDouble() * Math.PI * 2.0D;
					double radius = 22.0D + level.random.nextDouble() * 22.0D;
					double x = Math.cos(angle) * radius;
					double z = Math.sin(angle) * radius;
					BlockPos pos = new BlockPos((int) x, 0, (int) z);
					if (!level.isLoaded(pos)) continue;
					int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
							(int) x, (int) z);
					state.stormZones.add(new Zone(x, y, z, 3.5D, 70));
					telegraphCircle(level, new Vec3(x, y, z), 50, ModParticles.RESONANCE_PULSE);
				}
				level.playSound(null, dragon.blockPosition(), ModSounds.RESONANCE_STRIKE, SoundSource.HOSTILE, 0.9F, 0.5F);
			}
			case COLLAPSE -> {
				Vec3 point = target.position().add(
						(level.random.nextDouble() - 0.5D) * 14.0D, 0.0D,
						(level.random.nextDouble() - 0.5D) * 14.0D);
				BlockPos pos = new BlockPos((int) point.x, 0, (int) point.z);
				int y = level.isLoaded(pos)
						? level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
								(int) point.x, (int) point.z) : (int) point.y;
				state.collapseZones.add(new Zone(point.x, y, point.z, 5.0D, 100));
				telegraphCircle(level, new Vec3(point.x, y, point.z), 30, ParticleTypes.LARGE_SMOKE);
				level.playSound(null, dragon.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.7F, 0.4F);
			}
			case FINAL_ROAR -> {
				level.playSound(null, dragon.blockPosition(), ModSounds.DRAGON_ROAR, SoundSource.HOSTILE, 1.6F, 0.8F);
			}
			case CATASTROPHE -> {
				level.playSound(null, dragon.blockPosition(), ModSounds.DRAGON_ROAR, SoundSource.HOSTILE, 1.3F, 0.6F);
				level.sendParticles(ModParticles.RESONANCE_PULSE, 0.0D, 72.0D, 0.0D, 80, 14.0D, 2.0D, 14.0D, 0.05D);
			}
			case VOID_RIFT -> {
				// Transformed-exclusive: tears a resonance rift across the ground.
				Vec3 riftDir = target.position().subtract(dragon.position()).normalize();
				Vec3 riftStart = dragon.position().add(riftDir.scale(8.0D));
				state.riftZones.clear();
				for (int i = 0; i < 6; i++) {
					Vec3 rp = riftStart.add(riftDir.scale(i * 4.0D));
					BlockPos pos = new BlockPos((int) rp.x, 0, (int) rp.z);
					int y = level.isLoaded(pos)
							? level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
									(int) rp.x, (int) rp.z) : (int) rp.y;
					state.riftZones.add(new Zone(rp.x, y, rp.z, 3.0D, 70));
					telegraphCircle(level, new Vec3(rp.x, y, rp.z), 40, ModParticles.RESONANCE_ACTIVE);
				}
				level.playSound(null, dragon.blockPosition(), ModSounds.RESONANCE_STRIKE, SoundSource.HOSTILE, 1.4F, 0.4F);
			}
			case RESONANCE_HOWL -> {
				// Transformed-exclusive: a disorienting howl that slows nearby players.
				level.playSound(null, dragon.blockPosition(), ModSounds.DRAGON_SCREECH, SoundSource.HOSTILE, 2.0F, 0.6F);
				level.sendParticles(ModParticles.RESONANCE_PULSE, dragon.getX(), dragon.getY() + 2.0D, dragon.getZ(),
						60, 16.0D, 4.0D, 16.0D, 0.14D);
			}
			case METEOR_SHOWER -> {
				// Transformed-exclusive: calls down resonance projectiles from above.
				state.meteorZones.clear();
				level.playSound(null, dragon.blockPosition(), ModSounds.DRAGON_ROAR, SoundSource.HOSTILE, 1.5F, 0.4F);
				for (int i = 0; i < 4; i++) {
					double mx = target.getX() + (level.random.nextDouble() - 0.5D) * 16.0D;
					double mz = target.getZ() + (level.random.nextDouble() - 0.5D) * 16.0D;
					int my = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
							(int) mx, (int) mz);
					Zone zone = new Zone(mx, my, mz, 3.5D, 60);
					state.meteorZones.add(zone);
					telegraphCircle(level, new Vec3(mx, my, mz), 30, ParticleTypes.END_ROD);
				}
			}
			default -> {
			}
		}
	}

	// ------------------------------------------------------------------
	// Attack execution
	// ------------------------------------------------------------------

	private static void advanceAttack(EnderDragon dragon, ServerLevel level, State state, LivingEntity target) {
		state.attackTick++;
		if (target == null || !target.isAlive()) {
			// A disconnected/dead target must cancel delayed impacts. Otherwise a
			// meteor or rift can remain armed with no valid target until the Dragon
			// dies, and stale zones accumulate across target swaps.
			state.clearZones();
			state.attackTimer = 40;
			return;
		}

		switch (state.attackType) {
			case VOID_DIVE -> {
				if (state.attackTick == 30) impactDive(dragon, level, state, 6.0F, 0.9D);
				if (state.attackTick >= 44) {
					finishAttack(dragon, state, 120);
					state.diveCooldown = 220;
				}
			}
			case HUNTING_DIVE -> {
				if (state.attackTick == 30) impactDive(dragon, level, state, 8.0F, 1.1D);
				if (state.attackTick >= 44) {
					finishAttack(dragon, state, 130);
					state.diveCooldown = 220;
				}
			}
			case TRIPLE_DIVE -> {
				if (state.attackTick == 24) impactDive(dragon, level, state, 6.0F, 0.9D);
				if (state.attackTick == 54) {
					state.divePoint = predictedPoint(target, true);
					telegraphCircle(level, state.divePoint, 20, ParticleTypes.END_ROD);
				}
				if (state.attackTick == 74) {
					state.divePoint = predictedPoint(target, true);
					telegraphCircle(level, state.divePoint, 20, ParticleTypes.END_ROD);
				}
				if (state.attackTick == 94) impactDive(dragon, level, state, 8.0F, 1.2D);
				if (state.attackTick >= 110) {
					finishAttack(dragon, state, 140);
					state.diveCooldown = 260;
				}
			}
			case WING_SHOCKWAVE -> {
				if (state.attackTick == 24) {
					shockwave(dragon, level, state);
					finishAttack(dragon, state, 110);
				}
			}
			case BREATH -> {
				if (state.attackTick == 26) {
					Vec3 point = predictedPoint(target, false);
					BlockPos pos = new BlockPos((int) point.x, 0, (int) point.z);
					int y = level.isLoaded(pos)
							? level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
									(int) point.x, (int) point.z) : (int) point.y;
					state.breathZones.add(new Zone(point.x, y, point.z, 3.5D, 60));
					level.playSound(null, dragon.blockPosition(), ModSounds.RESONANCE_STRIKE, SoundSource.HOSTILE, 0.8F, 0.7F);
				}
				if (state.attackTick >= 40) finishAttack(dragon, state, 100);
			}
			case SCREECH -> {
				if (state.attackTick == 20) {
					screech(dragon, level, state);
					finishAttack(dragon, state, 120);
					state.screechCooldown = 320;
				}
			}
			case BARRAGE -> {
				if (state.attackTick == 14) {
					int count = switch (state.phase) { case 1 -> 3; case 2 -> 5; default -> 7; };
					fireBarrage(dragon, level, target, count, 0.22D);
					finishAttack(dragon, state, 100);
				}
			}
			case BARRAGE_PLUS -> {
				if (state.attackTick == 12) {
					fireBarrage(dragon, level, target, 8, 0.28D);
					finishAttack(dragon, state, 110);
				}
			}
			case GALE -> {
				if (state.galeTicks > 0) {
					state.galeTicks--;
					gale(dragon, level, state);
				} else {
					finishAttack(dragon, state, 90);
					state.galeCooldown = 420;
				}
			}
			case TALON -> {
				if (state.attackTick == 10 && dragon.distanceToSqr(target) < 10.0D * 10.0D) {
					target.hurt(level.damageSources().mobAttack(dragon), 8.0F);
					target.knockback(1.5D, target.getX() - dragon.getX(), target.getZ() - dragon.getZ());
					level.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY(1.0D), target.getZ(),
							16, 0.4D, 0.6D, 0.4D, 0.04D);
					level.playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.0F, 0.5F);
				}
				if (state.attackTick >= 10) {
					finishAttack(dragon, state, 100);
					state.talonCooldown = 260;
				}
			}
			case SWEEP -> {
				if (state.attackTick == 20) {
					sweep(dragon, level, state);
					finishAttack(dragon, state, 90);
				}
			}
			case STORM -> {
				if (state.attackTick == 50) {
					for (Zone zone : state.stormZones) {
						strikeZone(dragon, level, zone, 6.0F);
					}
					state.stormZones.clear();
					finishAttack(dragon, state, 120);
					state.stormCooldown = 380;
				}
			}
			case COLLAPSE -> {
				if (state.attackTick == 30) {
					for (Zone zone : state.collapseZones) {
						strikeZone(dragon, level, zone, 5.0F);
					}
					state.collapseZones.clear();
					finishAttack(dragon, state, 110);
					state.collapseCooldown = 360;
				}
			}
			case FINAL_ROAR -> {
				if (state.attackTick == 20) {
					finalRoar(dragon, level, state);
					finishAttack(dragon, state, 90);
				}
			}
			case CATASTROPHE -> {
				if (state.attackTick == 60) {
					catastrophe(dragon, level, state);
					finishAttack(dragon, state, 160);
					state.catastrophicCooldown = 620;
				}
			}
			// --- Transformed-exclusive attack execution ---
			case VOID_RIFT -> {
				if (state.attackTick == 30) {
					for (Zone zone : state.riftZones) {
						strikeZone(dragon, level, zone, 7.0F);
					}
					state.riftZones.clear();
					finishAttack(dragon, state, 140);
					state.voidRiftCooldown = 480;
				}
			}
			case RESONANCE_HOWL -> {
				if (state.attackTick == 15) {
					// Howl: damage + Slowness I for 5s to all players within 30 blocks.
					for (ServerPlayer player : level.players()) {
						if (!player.isAlive() || player.isSpectator()) continue;
						double dist = dragon.distanceToSqr(player);
						if (dist > 30.0D * 30.0D) continue;
						player.hurt(level.damageSources().mobAttack(dragon), 4.0F);
						player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
								net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
					}
					finishAttack(dragon, state, 100);
					state.howlCooldown = 360;
				}
			}
			case METEOR_SHOWER -> {
				// Four stored markers strike once each, five ticks apart. The old
				// implementation recreated them only on tick 25, so three impacts
				// never happened.
				if (state.attackTick >= 25 && state.attackTick <= 40
						&& (state.attackTick - 25) % 5 == 0) {
					int index = (state.attackTick - 25) / 5;
					if (index < state.meteorZones.size()) {
						strikeZone(dragon, level, state.meteorZones.get(index), 5.0F);
					}
				}
				if (state.attackTick >= 50) {
					state.meteorZones.clear();
					finishAttack(dragon, state, 120);
					state.meteorCooldown = 400;
				}
			}
			default -> {
			}
		}
	}

	private static void finishAttack(EnderDragon dragon, State state, int cooldown) {
		state.attackType = AttackType.NONE;
		state.attackTick = 0;
		state.attackTimer = cooldown + dragon.level().random.nextInt(40);
	}

	private static void tickCooldowns(State state) {
		state.galeCooldown = Math.max(0, state.galeCooldown - 1);
		state.screechCooldown = Math.max(0, state.screechCooldown - 1);
		state.stormCooldown = Math.max(0, state.stormCooldown - 1);
		state.collapseCooldown = Math.max(0, state.collapseCooldown - 1);
		state.catastrophicCooldown = Math.max(0, state.catastrophicCooldown - 1);
		state.diveCooldown = Math.max(0, state.diveCooldown - 1);
		state.talonCooldown = Math.max(0, state.talonCooldown - 1);
		state.voidRiftCooldown = Math.max(0, state.voidRiftCooldown - 1);
		state.howlCooldown = Math.max(0, state.howlCooldown - 1);
		state.meteorCooldown = Math.max(0, state.meteorCooldown - 1);
	}

	// ------------------------------------------------------------------
	// Attack effects
	// ------------------------------------------------------------------

	private static Vec3 predictedPoint(LivingEntity target, boolean predict) {
		Vec3 position = target.position();
		if (!predict) return position;
		Vec3 velocity = target.getDeltaMovement();
		Vec3 predicted = position.add(velocity.x * 14.0D, 0.0D, velocity.z * 14.0D);
		return new Vec3(predicted.x, position.y, predicted.z);
	}

	private static void impactDive(EnderDragon dragon, ServerLevel level, State state, float damage, double knock) {
		Vec3 point = state.divePoint;
		level.sendParticles(ParticleTypes.EXPLOSION, point.x, point.y + 0.5D, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
		level.sendParticles(ParticleTypes.END_ROD, point.x, point.y + 1.0D, point.z, 40, 3.0D, 1.0D, 3.0D, 0.12D);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, point.x, point.y + 0.5D, point.z, 24, 2.5D, 0.5D, 2.5D, 0.05D);
		level.playSound(null, BlockPos.containing(point), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2F, 0.7F);
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double distance = Math.sqrt(player.distanceToSqr(point));
			if (distance > 4.5D) continue;
			player.hurt(level.damageSources().mobAttack(dragon), damage * (float) (1.0D - distance / 9.0D));
			player.knockback(knock, player.getX() - point.x, player.getZ() - point.z);
			player.setDeltaMovement(player.getDeltaMovement().add(0.0D, 0.35D, 0.0D));
		}
	}

	private static void shockwave(EnderDragon dragon, ServerLevel level, State state) {
		Vec3 center = dragon.position();
		level.playSound(null, dragon.blockPosition(), ModSounds.WING_SHOCKWAVE, SoundSource.HOSTILE, 1.5F, 0.9F);
		for (int radius = 4; radius <= 16; radius += 4) {
			for (int angle = 0; angle < 360; angle += 15) {
				double radians = Math.toRadians(angle);
				level.sendParticles(ParticleTypes.END_ROD,
						center.x + Math.cos(radians) * radius, center.y + 0.5D,
						center.z + Math.sin(radians) * radius, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double distance = Math.sqrt(player.distanceToSqr(center.x, center.y, center.z));
			if (distance > 16.0D) continue;
			float damage = 5.0F * (float) (1.0D - distance / 16.0D);
			player.hurt(level.damageSources().mobAttack(dragon), damage);
			player.knockback(1.2D, player.getX() - center.x, player.getZ() - center.z);
		}
	}

	private static void screech(EnderDragon dragon, ServerLevel level, State state) {
		Vec3 center = dragon.position();
		level.playSound(null, dragon.blockPosition(), ModSounds.DRAGON_SCREECH, SoundSource.HOSTILE, 1.6F, 1.0F);
		level.sendParticles(ModParticles.RESONANCE_PULSE, center.x, center.y + 1.0D, center.z,
				40, 14.0D, 3.0D, 14.0D, 0.10D);
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double distance = Math.sqrt(player.distanceToSqr(center.x, center.y, center.z));
			if (distance > 34.0D) continue;
			float damage = 4.0F * (float) (1.0D - distance / 34.0D);
			player.hurt(level.damageSources().mobAttack(dragon), damage);
			player.knockback(0.8D, player.getX() - center.x, player.getZ() - center.z);
		}
	}

	private static void fireBarrage(EnderDragon dragon, ServerLevel level, LivingEntity target, int count,
			double spread) {
		Vec3 eye = dragon.getEyePosition();
		Vec3 base = target.getEyePosition().subtract(eye).normalize();
		for (int i = 0; i < count; i++) {
			double offset = (i - (count - 1) / 2.0D) * spread;
			Vec3 direction = new Vec3(base.x - base.z * offset, base.y + (i % 2) * 0.08D,
					base.z + base.x * offset).normalize();
			DragonFireball fireball = new DragonFireball(level, dragon, direction);
			fireball.setPos(eye.x, eye.y, eye.z);
			level.addFreshEntity(fireball);
		}
	}

	private static void gale(EnderDragon dragon, ServerLevel level, State state) {
		Vec3 center = dragon.position();
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double distance = Math.sqrt(player.distanceToSqr(center.x, center.y, center.z));
			if (distance > 60.0D || distance < 2.0D) continue;
			double dx = (player.getX() - center.x) / distance;
			double dz = (player.getZ() - center.z) / distance;
			player.setDeltaMovement(player.getDeltaMovement().add(dx * 0.09D, 0.0D, dz * 0.09D));
			if (state.galeTicks % 10 == 0) {
				level.sendParticles(ParticleTypes.END_ROD,
						player.getX() + (level.random.nextDouble() - 0.5D) * 2.0D,
						player.getY() + 1.5D,
						player.getZ() + (level.random.nextDouble() - 0.5D) * 2.0D,
						1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	private static void sweep(EnderDragon dragon, ServerLevel level, State state) {
		Vec3 center = dragon.position();
		level.playSound(null, dragon.blockPosition(), ModSounds.WING_SHOCKWAVE, SoundSource.HOSTILE, 1.2F, 0.6F);
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double distance = Math.sqrt(player.distanceToSqr(center.x, center.y, center.z));
			if (distance > 6.0D) continue;
			player.hurt(level.damageSources().mobAttack(dragon), 6.0F);
			player.knockback(1.3D, player.getX() - center.x, player.getZ() - center.z);
			player.setDeltaMovement(player.getDeltaMovement().add(0.0D, 0.25D, 0.0D));
		}
	}

	private static void strikeZone(EnderDragon dragon, ServerLevel level, Zone zone, float damage) {
		level.sendParticles(ParticleTypes.EXPLOSION, zone.x(), zone.y() + 0.5D, zone.z(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
		level.sendParticles(ParticleTypes.END_ROD, zone.x(), zone.y() + 1.0D, zone.z(), 30, 2.5D, 1.5D, 2.5D, 0.10D);
		level.sendParticles(ModParticles.RESONANCE_ACTIVE, zone.x(), zone.y() + 1.0D, zone.z(), 20, 2.0D, 1.0D, 2.0D, 0.06D);
		level.playSound(null, new BlockPos((int) zone.x(), (int) zone.y(), (int) zone.z()),
				ModSounds.RESONANCE_STRIKE, SoundSource.HOSTILE, 1.1F, 1.1F);
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double dx = player.getX() - zone.x();
			double dz = player.getZ() - zone.z();
			if (dx * dx + dz * dz > zone.radius() * zone.radius()) continue;
			player.hurt(level.damageSources().mobAttack(dragon), damage);
			player.knockback(0.9D, dx, dz);
		}
	}

	private static void finalRoar(EnderDragon dragon, ServerLevel level, State state) {
		Vec3 center = dragon.position();
		level.playSound(null, dragon.blockPosition(), ModSounds.DRAGON_ROAR, SoundSource.HOSTILE, 1.8F, 0.6F);
		level.sendParticles(ModParticles.RESONANCE_PULSE, center.x, center.y + 1.0D, center.z,
				120, 20.0D, 5.0D, 20.0D, 0.12D);
		level.sendParticles(ParticleTypes.CRIMSON_SPORE, center.x, center.y + 1.0D, center.z,
				80, 16.0D, 4.0D, 16.0D, 0.10D);
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double distance = Math.sqrt(player.distanceToSqr(center.x, center.y, center.z));
			if (distance > 44.0D) continue;
			player.hurt(level.damageSources().mobAttack(dragon), 6.0F);
			player.knockback(1.5D, player.getX() - center.x, player.getZ() - center.z);
		}
	}

	private static void catastrophe(EnderDragon dragon, ServerLevel level, State state) {
		level.playSound(null, dragon.blockPosition(), ModSounds.DRAGON_ROAR, SoundSource.HOSTILE, 1.8F, 0.5F);
		level.sendParticles(ModParticles.RESONANCE_PULSE, 0.0D, 66.0D, 0.0D, 160, 26.0D, 3.0D, 26.0D, 0.14D);
		level.sendParticles(ParticleTypes.CRIMSON_SPORE, 0.0D, 66.0D, 0.0D, 90, 20.0D, 3.0D, 20.0D, 0.12D);
		// The Resonance Ring is the safe zone: inside radius 8 nothing happens.
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double dx = player.getX();
			double dz = player.getZ();
			double distance = Math.sqrt(dx * dx + dz * dz);
			if (distance < ArenaGeometry.SAFE_ZONE_RADIUS) continue;
			if (distance > 36.0D) continue;
			player.hurt(level.damageSources().mobAttack(dragon), 10.0F);
			player.knockback(2.0D, dx, dz);
		}
	}

	// ------------------------------------------------------------------
	// Passive hazards and zones
	// ------------------------------------------------------------------

	private static void tickZones(EnderDragon dragon, ServerLevel level, State state, LivingEntity target) {
		state.zoneTick++;
		for (Zone zone : state.breathZones) zone.tick();
		for (Zone zone : state.stormZones) zone.tick();
		for (Zone zone : state.collapseZones) zone.tick();
		for (Zone zone : state.riftZones) zone.tick();
		for (Zone zone : state.meteorZones) zone.tick();
		boolean damageTick = state.zoneTick % 20 == 0;

		state.breathZones.removeIf(zone -> zone.ticks() <= 0);
		for (Zone zone : state.breathZones) {
			level.sendParticles(ParticleTypes.DRAGON_BREATH, zone.x(), zone.y() + 0.5D, zone.z(),
					3, zone.radius() * 0.6D, 0.4D, zone.radius() * 0.6D, 0.02D);
			level.sendParticles(ModParticles.RESONANCE_ACTIVE, zone.x(), zone.y() + 0.8D, zone.z(),
					1, zone.radius() * 0.5D, 0.3D, zone.radius() * 0.5D, 0.01D);
			if (!damageTick) continue;
			for (ServerPlayer player : level.players()) {
				if (!player.isAlive() || player.isSpectator()) continue;
				double dx = player.getX() - zone.x();
				double dz = player.getZ() - zone.z();
				if (dx * dx + dz * dz > zone.radius() * zone.radius()) continue;
				player.hurt(level.damageSources().mobAttack(dragon), 3.0F);
				player.knockback(0.35D, dx, dz);
			}
		}

		state.stormZones.removeIf(zone -> zone.ticks() <= 0);
		for (Zone zone : state.stormZones) {
			if (zone.ticks() % 5 == 0) {
				level.sendParticles(ModParticles.RESONANCE_PULSE, zone.x(), zone.y() + 0.5D, zone.z(),
						4, zone.radius() * 0.7D, 0.3D, zone.radius() * 0.7D, 0.02D);
			}
		}

		state.collapseZones.removeIf(zone -> zone.ticks() <= 0);
		for (Zone zone : state.collapseZones) {
			if (zone.ticks() % 6 == 0) {
				level.sendParticles(ParticleTypes.LARGE_SMOKE, zone.x(), zone.y() + 0.5D, zone.z(),
						3, zone.radius() * 0.5D, 0.5D, zone.radius() * 0.5D, 0.02D);
			}
		}

		// Delayed attacks normally clear their own markers at impact. Expiry is
		// the safety net for a disconnected target or an interrupted fight.
		state.riftZones.removeIf(zone -> zone.ticks() <= 0);
		for (Zone zone : state.riftZones) {
			if (zone.ticks() % 5 == 0) {
				level.sendParticles(ModParticles.RESONANCE_PULSE, zone.x(), zone.y() + 0.5D, zone.z(),
						3, zone.radius() * 0.7D, 0.3D, zone.radius() * 0.7D, 0.02D);
			}
		}
		state.meteorZones.removeIf(zone -> zone.ticks() <= 0);
		for (Zone zone : state.meteorZones) {
			if (zone.ticks() % 5 == 0) {
				level.sendParticles(ParticleTypes.END_ROD, zone.x(), zone.y() + 0.6D, zone.z(),
						2, zone.radius() * 0.6D, 0.4D, zone.radius() * 0.6D, 0.01D);
			}
		}
	}

	private static void tickFissureHazard(EnderDragon dragon, ServerLevel level, State state) {
		if (state.phase < 3) return;
		state.fissureTicks++;
		if (state.fissureTicks % 40 != 0) return;
		if (state.fracturePoints.isEmpty()) {
			state.fracturePoints.addAll(ArenaGeometry.fracturePoints(level.getSeed()));
		}
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double distance = distanceToFracture(state.fracturePoints, player.getX(), player.getZ());
			if (distance > 2.5D) continue;
			player.hurt(level.damageSources().mobAttack(dragon), 2.0F);
			player.knockback(0.3D, player.getX(), player.getZ());
			level.sendParticles(ModParticles.RUIN_GOLD_CONTACT, player.getX(), player.getY() + 0.2D, player.getZ(),
					8, 0.5D, 0.1D, 0.5D, 0.03D);
		}
	}

	private static void tickPerchPulse(EnderDragon dragon, ServerLevel level, State state) {
		if (state.phase < 2) return;
		if (!dragon.getPhaseManager().getCurrentPhase().isSitting()) return;
		state.perchPulseTicks++;
		if (state.perchPulseTicks < 80) return;
		state.perchPulseTicks = 0;
		Vec3 center = dragon.position();
		level.playSound(null, dragon.blockPosition(), ModSounds.DRAGON_SCREECH, SoundSource.HOSTILE, 1.0F, 0.7F);
		level.sendParticles(ModParticles.RESONANCE_PULSE, center.x, center.y + 1.0D, center.z,
				30, 8.0D, 3.0D, 8.0D, 0.08D);
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator()) continue;
			double distance = Math.sqrt(player.distanceToSqr(center.x, center.y, center.z));
			if (distance > 24.0D) continue;
			player.hurt(level.damageSources().mobAttack(dragon), 3.0F);
			player.knockback(0.6D, player.getX() - center.x, player.getZ() - center.z);
		}
	}

	private static double distanceToFracture(List<double[]> points, double x, double z) {
		double nearest = Double.MAX_VALUE;
		for (double[] point : points) {
			double dx = x - point[0];
			double dz = z - point[1];
			nearest = Math.min(nearest, dx * dx + dz * dz);
		}
		return Math.sqrt(nearest);
	}

	private static void telegraphCircle(ServerLevel level, Vec3 center, int ticks, net.minecraft.core.particles.ParticleOptions particle) {
		for (int angle = 0; angle < 360; angle += 15) {
			double radians = Math.toRadians(angle);
			level.sendParticles(particle, center.x + Math.cos(radians) * 3.2D, center.y + 0.6D,
					center.z + Math.sin(radians) * 3.2D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
		}
	}
}
