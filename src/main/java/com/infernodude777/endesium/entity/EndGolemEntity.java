package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The End Golem: the deep End's final answer. It wakes where the dragon fell
 * and fights in three movements — a patient colossus, then a resonance
 * artillery platform, then a desperate burning engine. Killing it is the
 * only source of Golem Cores, the key to permanent power.
 */
public class EndGolemEntity extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.end_golem.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.end_golem.walk");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.end_golem.attack");
	private static final RawAnimation CAST = RawAnimation.begin().thenPlay("animation.end_golem.cast");
	private static final RawAnimation STAGGER_ANIM = RawAnimation.begin().thenLoop("animation.end_golem.stagger");

	private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_PHASE =
			net.minecraft.network.syncher.SynchedEntityData.defineId(EndGolemEntity.class, EntityDataSerializers.INT);
	private static final net.minecraft.network.syncher.EntityDataAccessor<Byte> DATA_FLAGS =
			net.minecraft.network.syncher.SynchedEntityData.defineId(EndGolemEntity.class, EntityDataSerializers.BYTE);

	/** Phase thresholds as fractions of max health. */
	private static final double PHASE_TWO_AT = 0.66D;
	private static final double PHASE_THREE_AT = 0.33D;

	private final ServerBossEvent bossBar = new ServerBossEvent(
			Component.translatable("entity.endesium.end_golem"),
			BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	private static final byte FLAG_CASTING = 1;
	private static final byte FLAG_STAGGER = 2;

	public boolean isCasting() {
		return (getEntityData().get(DATA_FLAGS) & FLAG_CASTING) != 0;
	}

	public void setCasting(boolean casting) {
		byte flags = getEntityData().get(DATA_FLAGS);
		getEntityData().set(DATA_FLAGS, casting ? (byte) (flags | FLAG_CASTING) : (byte) (flags & ~FLAG_CASTING));
	}

	public boolean isStaggered() {
		return (getEntityData().get(DATA_FLAGS) & FLAG_STAGGER) != 0;
	}

	private void setStaggered(boolean staggered) {
		byte flags = getEntityData().get(DATA_FLAGS);
		getEntityData().set(DATA_FLAGS, staggered ? (byte) (flags | FLAG_STAGGER) : (byte) (flags & ~FLAG_STAGGER));
	}

	private int barrageCooldown = 100;
	private int summonCooldown = 200;
	private int shockwaveCooldown = 140;
	private int beamCooldown = 160;

	public EndGolemEntity(EntityType<? extends EndGolemEntity> type, Level level) {
		super(type, level);
		xpReward = 200;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 300.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.24D)
				.add(Attributes.ATTACK_DAMAGE, 14.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.ARMOR, 14.0D)
				.add(Attributes.FOLLOW_RANGE, 48.0D)
				.add(Attributes.STEP_HEIGHT, 1.0D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_PHASE, 1);
		builder.define(DATA_FLAGS, (byte) 0);
	}

	public int getPhase() {
		return getEntityData().get(DATA_PHASE);
	}

	private void setPhase(int phase) {
		getEntityData().set(DATA_PHASE, phase);
		if (phase == 3) {
			bossBar.setColor(BossEvent.BossBarColor.RED);
		} else if (phase == 2) {
			bossBar.setColor(BossEvent.BossBarColor.YELLOW);
		}
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		goalSelector.addGoal(2, new BarrageGoal(this));
		goalSelector.addGoal(3, new ShockwaveGoal(this));
		goalSelector.addGoal(4, new BeamSweepGoal(this));
		goalSelector.addGoal(5, new SummonGoal(this));
		goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.4D));
		goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	// --- Stagger system: sustained damage opens the punish window ---

	private float staggerAccumulator;
	private long lastStaggerHurtTick = -100;
	private int staggerTicks;
	private int staggerCooldown;
	/** True while the golem is hurting itself (phase-3 upkeep) — never feeds its own stagger meter. */
	private boolean bypassStagger;

	private static final float STAGGER_DAMAGE_THRESHOLD = 60.0F;
	private static final int STAGGER_WINDOW_TICKS = 160;
	private static final int STAGGER_DURATION_TICKS = 100;
	private static final int STAGGER_COOLDOWN_TICKS = 600;

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel server) {
			updatePhase(server);
			if (staggerTicks > 0 && --staggerTicks == 0) {
				setStaggered(false);
				playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.1F);
			}
			if (isStaggered()) {
				getNavigation().stop();
				getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0.0D);
				if (tickCount % 5 == 0) {
					server.sendParticles(ParticleTypes.CRIT,
							getX(), getY() + 2.5D, getZ(), 4, 0.8D, 0.8D, 0.8D, 0.05D);
				}
			}
			if (staggerCooldown > 0) staggerCooldown--;
			if (beamCooldown > 0) beamCooldown--;
			if (barrageCooldown > 0) barrageCooldown--;
			if (summonCooldown > 0) summonCooldown--;
			if (shockwaveCooldown > 0) shockwaveCooldown--;
			tickArenaTether(server);
			// Ambient dread: the golem's core hums visible resonance.
			server.sendParticles(ModParticles.RESONANCE_PULSE,
					getX(), getY() + 2.2D, getZ(), 1, 0.4D, 0.5D, 0.4D, 0.002D);
			float healthFraction = getHealth() / getMaxHealth();
			bossBar.setProgress(java.lang.Math.clamp(healthFraction, 0.0F, 1.0F));
		}
	}

	/** Keeps the fight honest: strays too far are dragged back toward the arena. */
	private void tickArenaTether(ServerLevel server) {
		if (getTarget() == null || !isAlive()) return;
		for (Player player : server.getEntitiesOfClass(Player.class,
				getBoundingBox().inflate(64.0D),
				p -> p.isAlive() && !p.isCreative() && !p.isSpectator())) {
			double distSqr = distanceToSqr(player);
			if (distSqr < 48.0D * 48.0D) continue;
			Vec3 pull = position().subtract(player.position()).normalize().scale(0.22D);
			player.setDeltaMovement(player.getDeltaMovement().add(pull.x, 0.05D, pull.z));
			player.hurtMarked = true;
			server.sendParticles(ParticleTypes.REVERSE_PORTAL,
					player.getX(), player.getY() + 1.0D, player.getZ(),
					6, 0.3D, 0.6D, 0.3D, 0.02D);
		}
	}

	private void updatePhase(ServerLevel server) {
		float fraction = getHealth() / getMaxHealth();
		int phase = fraction <= PHASE_THREE_AT ? 3 : fraction <= PHASE_TWO_AT ? 2 : 1;
		if (phase != getPhase()) {
			setPhase(phase);
			playSound(SoundEvents.WITHER_SPAWN, 1.0F, 1.4F);
			server.sendParticles(ParticleTypes.EXPLOSION,
					getX(), getY() + 2.0D, getZ(), 6, 1.5D, 1.0D, 1.5D, 0.05D);
			// Each movement sheds the old plating: brief resistance surge.
			addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 2));
		}
	}

	public boolean isEnragedPhase() {
		return getPhase() >= 3;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		// Phase three burns the ground it strides across.
		if (isEnragedPhase() && level() instanceof ServerLevel server && tickCount % 4 == 0) {
			server.sendParticles(ParticleTypes.FLAME,
					getX(), getY() + 0.3D, getZ(), 3, 0.8D, 0.1D, 0.8D, 0.02D);
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		// The engine never suffocates; arena terrain must never kill it.
		if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) return false;
		if (source.is(DamageTypeTags.IS_EXPLOSION)) amount *= 0.6F;
		// The punish window: a staggered golem's core is exposed.
		if (isStaggered()) amount *= 2.0F;
		// Stagger accumulation: 60+ damage inside 8 seconds breaks its stance.
		// Self-inflicted upkeep damage and non-entity sources never count.
		if (!bypassStagger && source.getEntity() != this) {
			long now = tickCount;
			if (now - lastStaggerHurtTick > STAGGER_WINDOW_TICKS) {
				staggerAccumulator = 0.0F;
			}
			lastStaggerHurtTick = now;
			staggerAccumulator += amount;
			if (staggerTicks <= 0 && staggerCooldown <= 0 && staggerAccumulator >= STAGGER_DAMAGE_THRESHOLD) {
				staggerAccumulator = 0.0F;
				enterStagger();
			}
		}
		return super.hurt(source, amount);
	}

	/** Phase-3 upkeep: hurts itself without feeding its own stagger meter. */
	private void selfHurt(float amount) {
		bypassStagger = true;
		try {
			hurt(damageSources().magic(), amount);
		} finally {
			bypassStagger = false;
		}
	}

	private void enterStagger() {
		staggerTicks = STAGGER_DURATION_TICKS;
		staggerCooldown = STAGGER_COOLDOWN_TICKS;
		setStaggered(true);
		getNavigation().stop();
		setCasting(false);
		playSound(SoundEvents.RAVAGER_STUNNED, 1.2F, 0.8F);
		if (level() instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.CRIT,
					getX(), getY() + 2.5D, getZ(), 30, 1.2D, 1.2D, 1.2D, 0.15D);
			server.sendParticles(ModParticles.RESONANCE_ACTIVE,
					getX(), getY() + 2.5D, getZ(), 20, 0.8D, 0.8D, 0.8D, 0.05D);
		}
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bossBar.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossBar.removePlayer(player);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends EndGolemEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().thenPlay("animation.end_golem.death"));
		if (isStaggered()) return state.setAndContinue(STAGGER_ANIM);
		if (isCasting()) return state.setAndContinue(CAST);
		if (swinging) return state.setAndContinue(ATTACK);
		if (state.isMoving()) return state.setAndContinue(WALK);
		return state.setAndContinue(IDLE);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return animationCache;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SOUL_ESCAPE.value();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.RAVAGER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.RAVAGER_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/end_golem"));
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		spawnAtLocation(new ItemStack(com.infernodude777.endesium.registry.ModItems.GOLEM_CORE,
				3 + random.nextInt(3)));
		spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.DRAGON_BREATH, 2));
		Endesium.LOGGER.info("End Golem felled at [{}, {}, {}]", getX(), getY(), getZ());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("EndesiumGolemPhase", getPhase());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setPhase(tag.getInt("EndesiumGolemPhase"));
		if (this.hasCustomName()) bossBar.setName(getDisplayName());
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
					getX(), getY() + 2.0D, getZ(), 2, 1.0D, 1.0D, 1.0D, 0.0D);
			server.sendParticles(ModParticles.RESONANCE_ACTIVE,
					getX(), getY() + 2.0D, getZ(), 80, 3.0D, 2.0D, 3.0D, 0.08D);
		}
	}

	/** Resonance artillery: fans of homing shards that force constant movement. */
	private static final class BarrageGoal extends Goal {
		private final EndGolemEntity golem;

		BarrageGoal(EndGolemEntity golem) {
			this.golem = golem;
		}

		@Override
		public boolean canUse() {
			if (golem.barrageCooldown > 0 || golem.isCasting() || golem.isStaggered()) return false;
			LivingEntity target = golem.getTarget();
			if (target == null) return false;
			double dist = golem.distanceToSqr(target);
			return dist > 16.0D && dist <= 900.0D && golem.hasLineOfSight(target);
		}

		@Override
		public void start() {
			golem.setCasting(true);
			golem.barrageCooldown = golem.isEnragedPhase() ? 90 : 160;
		}		@Override
		public boolean canContinueToUse() {
			return golem.isCasting();
		}

		@Override
		public void stop() {
			golem.setCasting(false);
		}

		private int windup = 25;

		@Override
		public void tick() {
			windup--;
			LivingEntity target = golem.getTarget();
			if (target != null) golem.getLookControl().setLookAt(target);
			if (windup > 0) return;
			golem.setCasting(false);
			windup = 25;
			if (target == null) return;
			ServerLevel server = (ServerLevel) golem.level();
			int count = golem.isEnragedPhase() ? 7 : 5;
			// ShulkerBullets choose their own flight vector, so the fan is built
			// from varied spawn offsets and starting axes instead of velocity.
			net.minecraft.core.Direction.Axis[] axes = {
					net.minecraft.core.Direction.Axis.X,
					net.minecraft.core.Direction.Axis.Y,
					net.minecraft.core.Direction.Axis.Z };
			for (int i = 0; i < count; i++) {
				double spread = (i - count / 2.0D) * 0.9D;
				net.minecraft.world.entity.projectile.ShulkerBullet shard =
						new net.minecraft.world.entity.projectile.ShulkerBullet(
								golem.level(), golem, target,
								axes[Math.floorMod(i, axes.length)]);
				shard.setPos(golem.getX() + spread * 0.4D, golem.getY() + 3.0D, golem.getZ() + spread * 0.3D);
				golem.level().addFreshEntity(shard);
			}
			server.sendParticles(ModParticles.RESONANCE_BEAM,
					golem.getX(), golem.getY() + 3.0D, golem.getZ(),
					16, 0.8D, 0.5D, 0.8D, 0.05D);
			golem.playSound(SoundEvents.SHULKER_SHOOT, 1.2F, 0.6F);
			if (golem.isEnragedPhase()) golem.selfHurt(2.0F);
			golem.swing(InteractionHand.MAIN_HAND);
		}
	}

	/** Ground pound: a wide shockwave around the golem's feet. */
	private static final class ShockwaveGoal extends Goal {
		private final EndGolemEntity golem;

		ShockwaveGoal(EndGolemEntity golem) {
			this.golem = golem;
		}

		@Override
		public boolean canUse() {
			if (golem.shockwaveCooldown > 0 || golem.isCasting() || golem.isStaggered()) return false;
			LivingEntity target = golem.getTarget();
			if (target == null) return false;
			return golem.distanceToSqr(target) <= 36.0D;
		}

		@Override
		public void start() {
			golem.setCasting(true);
			golem.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.4F, 0.5F);
			golem.shockwaveCooldown = golem.isEnragedPhase() ? 100 : 180;
		}

		@Override
		public boolean canContinueToUse() {
			return golem.isCasting();
		}

		@Override
		public void stop() {
			golem.setCasting(false);
		}

		private int windup = 30;

		@Override
		public void tick() {
			windup--;
			if (windup > 0) {
				if (golem.level() instanceof ServerLevel server && windup % 5 == 0) {
					// Telegraph: creeping dust ring.
					server.sendParticles(ParticleTypes.CLOUD,
							golem.getX(), golem.getY() + 0.2D, golem.getZ(),
							4, 1.5D, 0.05D, 1.5D, 0.01D);
				}
				return;
			}
			golem.setCasting(false);
			windup = 30;
			ServerLevel server = (ServerLevel) golem.level();
			server.sendParticles(ParticleTypes.POOF,
					golem.getX(), golem.getY() + 0.3D, golem.getZ(),
					50, 4.5D, 0.4D, 4.5D, 0.08D);
			for (Player p : server.getEntitiesOfClass(Player.class,
					golem.getBoundingBox().inflate(6.5D), Player::isAlive)) {
				Vec3 kb = p.position().subtract(golem.position()).normalize();
				p.setDeltaMovement(p.getDeltaMovement().add(kb.x * 1.5D, 0.7D, kb.z * 1.5D));
				p.hurtMarked = true;
				p.hurt(golem.damageSources().mobAttack(golem), 8.0F);
			}
			golem.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.2F, 0.7F);
			if (golem.isEnragedPhase()) golem.selfHurt(2.0F);
			golem.swing(InteractionHand.MAIN_HAND);
		}
	}

	/** Calls its wardens' lesser kin to the field. */
	private static final class SummonGoal extends Goal {
		private final EndGolemEntity golem;

		SummonGoal(EndGolemEntity golem) {
			this.golem = golem;
		}

		@Override
		public boolean canUse() {
			if (golem.summonCooldown > 0 || golem.isCasting() || golem.isStaggered()) return false;
			return golem.getHealth() < golem.getMaxHealth() * PHASE_TWO_AT
					&& golem.getTarget() != null;
		}

		@Override
		public void start() {
			golem.summonCooldown = golem.isEnragedPhase() ? 400 : 700;
			ServerLevel server = (ServerLevel) golem.level();
			int count = golem.isEnragedPhase() ? 2 : 3;
			for (int i = 0; i < count; i++) {
				double ang = golem.random.nextDouble() * Math.PI * 2.0D;
				double px = golem.getX() + Math.cos(ang) * 4.0D;
				double pz = golem.getZ() + Math.sin(ang) * 4.0D;
				Mob minion = golem.isEnragedPhase()
						? com.infernodude777.endesium.registry.ModEntities.CROWN_SENTINEL.create(server)
						: com.infernodude777.endesium.registry.ModEntities.VOID_WISP.create(server);
				if (minion == null) continue;
				minion.moveTo(px, golem.getY(), pz, golem.getYRot(), 0.0F);
				server.sendParticles(ParticleTypes.REVERSE_PORTAL,
						px, golem.getY() + 1.0D, pz, 20, 0.3D, 0.8D, 0.3D, 0.05D);
				server.addFreshEntity(minion);
			}
			golem.setCasting(false);
			golem.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.7F);
		}
	}

	/**
	 * Phase-2+ resonance artillery: locks a firing line, telegraphs it as a
	 * creeping particle beam, then sweeps everything standing in the corridor.
	 * Dodge sideways — that is the whole counterplay.
	 */
	private static final class BeamSweepGoal extends Goal {
		private final EndGolemEntity golem;
		private int windup;
		private Vec3 fireDir;

		BeamSweepGoal(EndGolemEntity golem) {
			this.golem = golem;
		}

		@Override
		public boolean canUse() {
			if (golem.beamCooldown > 0 || golem.isCasting() || golem.isStaggered()) return false;
			LivingEntity target = golem.getTarget();
			if (target == null || golem.getPhase() < 2) return false;
			double dist = golem.distanceToSqr(target);
			return dist > 36.0D && dist <= 900.0D && golem.hasLineOfSight(target);
		}

		@Override
		public void start() {
			windup = 30;
			golem.setCasting(true);
			golem.beamCooldown = golem.isEnragedPhase() ? 120 : 220;
			LivingEntity target = golem.getTarget();
			fireDir = target != null
					? new Vec3(target.getX() - golem.getX(), 0, target.getZ() - golem.getZ()).normalize()
					: golem.getLookAngle();
			golem.playSound(SoundEvents.BEACON_AMBIENT, 1.2F, 0.5F);
		}

		@Override
		public boolean canContinueToUse() {
			return windup > -10;
		}

		@Override
		public void stop() {
			golem.setCasting(false);
		}

		@Override
		public void tick() {
			windup--;
			ServerLevel server = (ServerLevel) golem.level();
			if (windup > 0) {
				// Telegraph: creeping beam dots along the locked line.
				for (int d = 3; d <= 28; d += 2) {
					server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
							golem.getX() + fireDir.x * d,
							golem.getY() + 1.5D,
							golem.getZ() + fireDir.z * d,
							1, 0.15D, 0.15D, 0.15D, 0.0D);
				}
				return;
			}
			if (windup == 0) {
				// Fire.
				server.sendParticles(ModParticles.RESONANCE_BEAM,
						golem.getX(), golem.getY() + 1.6D, golem.getZ(),
						40, 0.6D, 0.4D, 0.6D, 0.12D);
				for (Player player : server.getEntitiesOfClass(Player.class,
						golem.getBoundingBox().expandTowards(fireDir.scale(30.0D)).inflate(1.5D),
						Player::isAlive)) {
					Vec3 toPlayer = player.position().subtract(golem.position());
					double along = toPlayer.x * fireDir.x + toPlayer.z * fireDir.z;
					Vec3 lateral = toPlayer.add(fireDir.scale(-along));
					if (along > 0 && along < 30.0D && lateral.lengthSqr() <= 3.25D) {
						player.hurt(golem.damageSources().mobAttack(golem), 10.0F);
						player.setDeltaMovement(player.getDeltaMovement().add(fireDir.scale(1.1D)).add(0, 0.3D, 0));
						player.hurtMarked = true;
					}
				}
				golem.playSound(SoundEvents.GUARDIAN_ATTACK, 1.4F, 0.7F);
			}
		}
	}
}
