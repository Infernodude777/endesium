package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
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

import java.util.EnumSet;

/**
 * A construct guardian of the Void Crown. It stands motionless at its post
 * until intruders approach the observatory tiers, then advances with heavy,
 * telegraphed slams that hurl anything caught in the shockwave.
 *
 * <p>Mid-fight escalation: at range it locks a CROWN RAY lane and sweeps it,
 * below half health it calls phantoms down from the observatory sky, and
 * below a third it enrages - faster, harder, longer reach on everything.</p>
 */
public class CrownSentinelEntity extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.crown_sentinel.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.crown_sentinel.walk");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.crown_sentinel.attack");
	private static final RawAnimation RAY_ANIM = RawAnimation.begin().thenPlay("animation.crown_sentinel.ray");

	private static final EntityDataAccessor<Boolean> DATA_SLAMMING = SynchedEntityData.defineId(
			CrownSentinelEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_CASTING = SynchedEntityData.defineId(
			CrownSentinelEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_GRABBING = SynchedEntityData.defineId(
			CrownSentinelEntity.class, EntityDataSerializers.BOOLEAN);

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	/** Damage that must be dealt mid-hold to force an early drop. */
	private static final float GRAB_BREAK_DAMAGE = 18.0F;

	private int slamTicks;
	private int slamReadyAt;
	private int rayCooldown = 120;
	private int phantomCallCooldown = 200;
	private int grabCooldown;
	private int grabHoldTicks;
	private float grabDamageTaken;
	private boolean phantomsCalled;
	private boolean enragedAnnounced;

	public CrownSentinelEntity(EntityType<? extends CrownSentinelEntity> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_SLAMMING, false);
		builder.define(DATA_CASTING, false);
		builder.define(DATA_GRABBING, false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		// One entry per attribute - duplicates silently overwrite (last wins)
		// and once cut this boss to 60 health while the docs promised 160.
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 220.0D)
				.add(Attributes.ATTACK_DAMAGE, 16.0D)
				.add(Attributes.ARMOR, 14.0D)
				.add(Attributes.ARMOR_TOUGHNESS, 4.0D)
				.add(Attributes.FOLLOW_RANGE, 48.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.27D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.STEP_HEIGHT, 1.0D);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		goalSelector.addGoal(2, new SlamGoal(this));
		goalSelector.addGoal(3, new GrabGoal(this));
		goalSelector.addGoal(4, new CrownRayGoal(this));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5D));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends CrownSentinelEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().thenPlay("animation.crown_sentinel.death"));
		if (isCasting()) return state.setAndContinue(RAY_ANIM);
		if (swinging || isSlamming() || isGrabbing()) return state.setAndContinue(ATTACK);
		if (state.isMoving()) return state.setAndContinue(WALK);
		return state.setAndContinue(IDLE);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return animationCache;
	}

	public boolean isSlamming() {
		return getEntityData().get(DATA_SLAMMING);
	}

	private void setSlamming(boolean slamming) {
		getEntityData().set(DATA_SLAMMING, slamming);
	}

	public boolean isCasting() {
		return getEntityData().get(DATA_CASTING);
	}

	private void setCasting(boolean casting) {
		getEntityData().set(DATA_CASTING, casting);
	}

	public boolean isGrabbing() {
		return getEntityData().get(DATA_GRABBING);
	}

	private void setGrabbing(boolean grabbing) {
		getEntityData().set(DATA_GRABBING, grabbing);
	}

	public boolean isEnraged() {
		return getHealth() > 0.0D && getHealth() < getMaxHealth() * 0.35D;
	}

	@Override
	public void aiStep() {

		// --- Difficulty pass: harden the sentinel ---
		if (this.getMaxHealth() < 300.0D) {
			var hp = getAttribute(Attributes.MAX_HEALTH);
			if (hp != null) {
				hp.setBaseValue(340.0D);
				setHealth(getMaxHealth());
			}
			var atk = getAttribute(Attributes.ATTACK_DAMAGE);
			if (atk != null) atk.setBaseValue(24.0D);
			var armor = getAttribute(Attributes.ARMOR);
			if (armor != null) armor.setBaseValue(22.0D);
			var tough = getAttribute(Attributes.ARMOR_TOUGHNESS);
			if (tough != null) tough.setBaseValue(9.0D);
		}
		// Below half health the sentinel reforges its plating slowly.
		if (tickCount > 20 && tickCount % 180 == 0 && this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth() * 0.5F) {
			this.heal(18.0F);
		}
		super.aiStep();
		if (level() instanceof ServerLevel server) {
			// Cooldowns tick centrally so goal polling can never stall them.
			if (rayCooldown > 0) rayCooldown--;
			if (phantomCallCooldown > 0) phantomCallCooldown--;
			if (grabCooldown > 0) grabCooldown--;
			if (slamTicks > 0) {
				slamTicks--;
				if (slamTicks == 0) setSlamming(false);
				// The slam lands at the end of the windup.
				if (slamTicks == 0 && getTarget() != null && distanceToSqr(getTarget()) <= 25.0D) {
					landSlam(server);
				}
			}
			// Grab-and-hurl: while the victim is pinned the sentinel holds them
			// in front of its chest, then hurls them across the room.
			if (grabHoldTicks > 0) {
				grabHoldTicks--;
				LivingEntity grabbed = getTarget();
				if (grabbed != null && grabbed.isAlive()) {
					Vec3 hold = position().add(getLookAngle().scale(2.2D)).add(0.0D, 1.3D, 0.0D);
					grabbed.setDeltaMovement(hold.subtract(grabbed.position()).scale(0.5D).add(0.0D, 0.35D, 0.0D));
					grabbed.hurtMarked = true;
					if (grabHoldTicks == 0) {
						hurlGrabbed(server, grabbed);
					}
				} else {
					grabHoldTicks = 0;
					setGrabbing(false);
				}
			}
			// Enrage: below a third the sentinel burns hotter - once, loudly.
			if (isEnraged() && !enragedAnnounced) {
				enragedAnnounced = true;
				setAttributeSafe(Attributes.ATTACK_DAMAGE, 20.0D);
				setAttributeSafe(Attributes.MOVEMENT_SPEED, 0.32D);
				playSound(SoundEvents.RAVAGER_ROAR, 1.2F, 0.7F);
				server.sendParticles(ParticleTypes.FLAME,
						getX(), getY() + 2.0D, getZ(), 24, 1.0D, 0.6D, 1.0D, 0.05D);
			} else if (!isEnraged()) {
				enragedAnnounced = false;
			}
			if (isEnraged() && tickCount % 8 == 0) {
				server.sendParticles(ParticleTypes.FLAME,
						getX(), getY() + 2.2D, getZ(), 1, 0.3D, 0.4D, 0.3D, 0.01D);
			}
			maybeCallPhantoms(server);
		}
	}

	private void setAttributeSafe(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
		var instance = getAttribute(attribute);
		if (instance != null) instance.setBaseValue(value);
	}

	private void landSlam(ServerLevel server) {
		LivingEntity target = getTarget();
		if (target != null) {
			Vec3 kb = target.position().subtract(position()).normalize();
			target.setDeltaMovement(target.getDeltaMovement().add(kb.x * 1.2D, 0.7D, kb.z * 1.2D));
			target.hurtMarked = true;
			target.hurt(damageSources().mobAttack(this), isEnraged() ? 12.0F : 10.0F);
		}
		// The shockwave also catches everyone near the impact.
		for (Player p : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(5.0D), Player::isAlive)) {
			Vec3 kb = p.position().subtract(position()).normalize();
			p.setDeltaMovement(p.getDeltaMovement().add(kb.x * 0.9D, 0.55D, kb.z * 0.9D));
			p.hurtMarked = true;
			p.hurt(damageSources().mobAttack(this), isEnraged() ? 8.0F : 6.0F);
		}
		playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.9F, 0.7F);
		server.sendParticles(ParticleTypes.POOF,
				getX(), getY() + 0.4D, getZ(), 20, 1.4D, 0.15D, 1.4D, 0.04D);
		server.sendParticles(ParticleTypes.EXPLOSION,
				getX(), getY() + 0.6D, getZ(), 2, 0.8D, 0.2D, 0.8D, 0.0D);
	}

	/** Hurls a grabbed victim across the room with heavy knockback. */
	private void hurlGrabbed(ServerLevel server, LivingEntity victim) {
		setGrabbing(false);
		Vec3 away = victim.position().subtract(position()).normalize();
		victim.setDeltaMovement(away.scale(2.3D).add(0.0D, 1.4D, 0.0D));
		victim.hurtMarked = true;
		victim.resetFallDistance();
		victim.hurt(damageSources().mobAttack(this), isEnraged() ? 20.0F : 14.0F);
		playSound(SoundEvents.RAVAGER_ROAR, 1.3F, 0.6F);
		server.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0D, victim.getZ(),
				20, 0.4D, 0.4D, 0.4D, 0.12D);
	}

	/** Below half health the sentinel calls the observatory sky down: phantoms. */
	private void maybeCallPhantoms(ServerLevel server) {
		if (!isAlive() || phantomCallCooldown > 0) return;
		float fraction = getHealth() / getMaxHealth();
		boolean due = (!phantomsCalled && fraction <= 0.5D) || (phantomsCalled && isEnraged());
		if (!due || getTarget() == null) return;
		int nearbyPhantoms = server.getEntitiesOfClass(Phantom.class,
				getBoundingBox().inflate(64.0D), Entity::isAlive).size();
		if (nearbyPhantoms >= 4) return;
		phantomCallCooldown = phantomsCalled ? 600 : 100;
		phantomsCalled = true;
		playSound(SoundEvents.PHANTOM_SWOOP, 1.4F, 0.6F);
		server.sendParticles(ParticleTypes.LARGE_SMOKE,
				getX(), getY() + 4.0D, getZ(), 20, 1.2D, 1.0D, 1.2D, 0.04D);
		int count = isEnraged() ? 2 : 1 + random.nextInt(2);
		for (int i = 0; i < count && nearbyPhantoms + i < 4; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double dist = 6.0D + random.nextDouble() * 6.0D;
			Phantom phantom = EntityType.PHANTOM.create(server);
			if (phantom == null) continue;
			phantom.moveTo(getX() + Math.cos(angle) * dist, getY() + 8.0D, getZ() + Math.sin(angle) * dist,
					getYRot(), 0.0F);
			phantom.setPersistenceRequired();
			if (getTarget() != null) phantom.setTarget(getTarget());
			server.addFreshEntity(phantom);
			server.sendParticles(ParticleTypes.LARGE_SMOKE,
					phantom.getX(), phantom.getY(), phantom.getZ(), 12, 0.6D, 0.6D, 0.6D, 0.03D);
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) return false;
		// Counterplay for the grab: chunky damage mid-hold forces an early drop,
		// matching the Warden's and Golem's carry-break rules.
		if (isGrabbing()) {
			grabDamageTaken += amount;
			if (grabDamageTaken >= GRAB_BREAK_DAMAGE) {
				grabDamageTaken = 0;
				grabHoldTicks = 0;
				setGrabbing(false);
				grabCooldown = Math.max(grabCooldown, 80);
				playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F);
			}
		}
		return super.hurt(source, amount);
	}

	@Override
	public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("EndesiumSentinelPhantomsCalled", phantomsCalled);
	}

	@Override
	public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		phantomsCalled = tag.getBoolean("EndesiumSentinelPhantomsCalled");
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.IRON_GOLEM_REPAIR;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.IRON_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.IRON_GOLEM_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/crown_sentinel"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 40;
	}

	/** Telegraphed area slam with a windup; the shockwave follows. */
	private static final class SlamGoal extends Goal {
		private final CrownSentinelEntity sentinel;

		SlamGoal(CrownSentinelEntity sentinel) {
			this.sentinel = sentinel;
		}

		@Override
		public boolean canUse() {
			if (sentinel.tickCount < sentinel.slamReadyAt || sentinel.slamTicks > 0 || sentinel.isCasting()) return false;
			LivingEntity target = sentinel.getTarget();
			if (target == null) return false;
			double dist = sentinel.distanceToSqr(target);
			return dist <= 20.0D && sentinel.hasLineOfSight(target);
		}

		@Override
		public void start() {
			sentinel.slamTicks = 16;
			sentinel.setSlamming(true);
			sentinel.slamReadyAt = sentinel.tickCount + (sentinel.isEnraged() ? 110 : 160);
			sentinel.getNavigation().stop();
			sentinel.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 0.6F);
		}

		@Override
		public boolean canContinueToUse() {
			return sentinel.slamTicks > 0;
		}
	}

	/**
	 * GRAB-AND-HURL: seizes a target that crowds the sentinel, holds it aloft
	 * for a beat, then throws it across the room. Getting grabbed is a death
	 * sentence near a wall - keep your distance after it slams.
	 */
	private static final class GrabGoal extends Goal {
		private final CrownSentinelEntity sentinel;

		GrabGoal(CrownSentinelEntity sentinel) {
			this.sentinel = sentinel;
		}

		@Override
		public boolean canUse() {
			if (sentinel.grabCooldown > 0 || sentinel.isCasting() || sentinel.isSlamming()
					|| sentinel.grabHoldTicks > 0) return false;
			LivingEntity target = sentinel.getTarget();
			if (target == null) return false;
			double dist = sentinel.distanceToSqr(target);
			// The grab punishes crowding after a slam; it needs to actually reach.
			return dist <= 9.0D && sentinel.hasLineOfSight(target);
		}

		@Override
		public void start() {
			sentinel.grabHoldTicks = 26;
			sentinel.grabDamageTaken = 0;
			sentinel.setGrabbing(true);
			sentinel.grabCooldown = sentinel.isEnraged() ? 140 : 220;
			sentinel.getNavigation().stop();
			sentinel.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.2F, 0.5F);
		}

		@Override
		public boolean canContinueToUse() {
			return sentinel.grabHoldTicks > 0;
		}

		@Override
		public void stop() {
			sentinel.setGrabbing(false);
		}
	}

	/**
	 * CROWN RAY: locks a firing lane through its target, telegraphs it with
	 * crown-light for a second, then sweeps a line of force down the lane.
	 * Dodge sideways - that is the whole counterplay.
	 */
	private static final class CrownRayGoal extends Goal {
		private final CrownSentinelEntity sentinel;
		private int windup;
		private Vec3 fireDir;

		CrownRayGoal(CrownSentinelEntity sentinel) {
			this.sentinel = sentinel;
			setFlags(EnumSet.of(Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (sentinel.rayCooldown > 0 || sentinel.isCasting() || sentinel.slamTicks > 0) return false;
			LivingEntity target = sentinel.getTarget();
			if (target == null) return false;
			double dist = sentinel.distanceToSqr(target);
			// The ray is the answer to staying far away: it punishes range.
			return dist > 36.0D && dist <= 900.0D && sentinel.hasLineOfSight(target);
		}

		@Override
		public void start() {
			windup = 22;
			sentinel.setCasting(true);
			sentinel.rayCooldown = sentinel.isEnraged() ? 140 : 220;
			sentinel.getNavigation().stop();
			LivingEntity target = sentinel.getTarget();
			fireDir = target != null
					? new Vec3(target.getX() - sentinel.getX(), 0, target.getZ() - sentinel.getZ()).normalize()
					: sentinel.getLookAngle();
			sentinel.playSound(SoundEvents.BEACON_AMBIENT, 1.2F, 0.5F);
		}

		@Override
		public boolean canContinueToUse() {
			return windup > -12;
		}

		@Override
		public void stop() {
			sentinel.setCasting(false);
		}

		@Override
		public void tick() {
			windup--;
			ServerLevel server = (ServerLevel) sentinel.level();
			if (windup > 0) {
				// Telegraph: creeping crown-light dots along the locked lane.
				for (int d = 3; d <= 18; d += 2) {
					server.sendParticles(ParticleTypes.END_ROD,
							sentinel.getX() + fireDir.x * d,
							sentinel.getY() + 2.0D,
							sentinel.getZ() + fireDir.z * d,
							1, 0.1D, 0.1D, 0.1D, 0.0D);
				}
				return;
			}
			if (windup == 0) {
				// Fire.
				server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
						sentinel.getX() + fireDir.x * 2.0D, sentinel.getY() + 2.0D, sentinel.getZ() + fireDir.z * 2.0D,
						30, 0.4D, 0.4D, 0.4D, 0.25D);
				for (Player player : server.getEntitiesOfClass(Player.class,
						sentinel.getBoundingBox().expandTowards(fireDir.scale(20.0D)).inflate(1.6D),
						Player::isAlive)) {
					Vec3 toPlayer = player.position().subtract(sentinel.position());
					double along = toPlayer.x * fireDir.x + toPlayer.z * fireDir.z;
					Vec3 lateral = toPlayer.add(fireDir.scale(-along));
					if (along > 0 && along < 20.0D && lateral.lengthSqr() <= 2.6D) {
						player.hurt(sentinel.damageSources().mobAttack(sentinel), sentinel.isEnraged() ? 14.0F : 12.0F);
						player.setDeltaMovement(player.getDeltaMovement().add(fireDir.scale(1.2D)).add(0, 0.3D, 0));
						player.hurtMarked = true;
					}
				}
				sentinel.playSound(SoundEvents.GUARDIAN_ATTACK, 1.4F, 0.6F);
			}
		}
	}
}
