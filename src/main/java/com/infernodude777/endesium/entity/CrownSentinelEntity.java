package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
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

/**
 * A construct guardian of the Void Crown. It stands motionless at its post
 * until intruders approach the observatory tiers, then advances with heavy,
 * telegraphed slams that hurl anything caught in the shockwave.
 */
public class CrownSentinelEntity extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.crown_sentinel.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.crown_sentinel.walk");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.crown_sentinel.attack");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	public CrownSentinelEntity(EntityType<? extends CrownSentinelEntity> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_SLAMMING, false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 60.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.22D)
				.add(Attributes.ATTACK_DAMAGE, 8.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.ARMOR, 8.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		goalSelector.addGoal(2, new SlamGoal(this));
		goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.5D));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends CrownSentinelEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().thenPlay("animation.crown_sentinel.death"));
		if (swinging || isSlamming()) return state.setAndContinue(ATTACK);
		if (state.isMoving()) return state.setAndContinue(WALK);
		return state.setAndContinue(IDLE);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return animationCache;
	}

	private int slamTicks;

	/** Synced so the client can play the slam telegraph animation. */
	private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_SLAMMING =
			net.minecraft.network.syncher.SynchedEntityData.defineId(CrownSentinelEntity.class,
					net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

	public boolean isSlamming() {
		return getEntityData().get(DATA_SLAMMING);
	}

	private void setSlamming(boolean slamming) {
		getEntityData().set(DATA_SLAMMING, slamming);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (slamTicks > 0) {
			slamTicks--;
			if (slamTicks == 0) setSlamming(false);
			if (level().isClientSide()) return;
			// The slam lands at the end of the windup.
			if (slamTicks == 0 && getTarget() != null && distanceToSqr(getTarget()) <= 16.0D) {
				LivingEntity target = getTarget();
				Vec3 kb = target.position().subtract(position()).normalize();
				target.setDeltaMovement(target.getDeltaMovement().add(kb.x * 0.9D, 0.55D, kb.z * 0.9D));
				target.hurtMarked = true;
				target.hurt(damageSources().mobAttack(this), 5.0F);
				playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.8F, 0.7F);
				ServerLevel server = (ServerLevel) level();
				server.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
						getX(), getY() + 0.4D, getZ(), 14, 0.9D, 0.15D, 0.9D, 0.03D);
			}
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) return false;
		return super.hurt(source, amount);
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
		return 12;
	}

	/** Telegraphed area slam with a windup; the shockwave follows. */
	private static final class SlamGoal extends Goal {
		private final CrownSentinelEntity sentinel;
		/** Entity tick at which the next slam may start; poll-frequency independent. */
		private int readyAtTick;

		SlamGoal(CrownSentinelEntity sentinel) {
			this.sentinel = sentinel;
		}

		@Override
		public boolean canUse() {
			if (sentinel.tickCount < readyAtTick || sentinel.slamTicks > 0) return false;
			LivingEntity target = sentinel.getTarget();
			if (target == null) return false;
			double dist = sentinel.distanceToSqr(target);
			return dist <= 12.0D && sentinel.hasLineOfSight(target);
		}

		@Override
		public void start() {
			sentinel.slamTicks = 18;
			sentinel.setSlamming(true);
			readyAtTick = sentinel.tickCount + 160;
			sentinel.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 0.6F);
		}

		@Override
		public boolean canContinueToUse() {
			return sentinel.slamTicks > 0;
		}
	}
}
