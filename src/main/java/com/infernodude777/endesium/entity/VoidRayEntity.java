package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
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
 * A wide-winged glider of the Shattered Highlands. Wildlife first: it hovers
 * and circles the high terrain, diving only when attacked. It uses monster
 * AI over flying navigation so standard goals remain available.
 */
public class VoidRayEntity extends PathfinderMob implements GeoEntity {
	private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.void_ray.fly");
	private static final RawAnimation DIVE = RawAnimation.begin().thenPlay("animation.void_ray.dive");
	private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("animation.void_ray.hurt");
	private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.void_ray.death");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	public VoidRayEntity(EntityType<? extends VoidRayEntity> type, Level level) {
		super(type, level);
		moveControl = new FlyingMoveControl(this, 20, true);
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 16.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.FLYING_SPEED, 0.6D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.FOLLOW_RANGE, 32.0D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
		nav.setCanFloat(true);
		return nav;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		// The dive-bomb is the ray's only attack; no ground melee goal.
		goalSelector.addGoal(2, new DiveBombGoal(this));
		goalSelector.addGoal(3, new com.infernodude777.endesium.entity.goal.AirWanderGoal(this, 20, 5));

		// The Void Ray circles at altitude and only dives with line of sight.
		// It returns to altitude after each pass.
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends VoidRayEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(DEATH);
		if (hurtTime > 0 && !swinging) return state.setAndContinue(HURT_ANIM);
		if (swinging) return state.setAndContinue(DIVE);
		return state.setAndContinue(FLY);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return animationCache;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.VOID_RAY_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.VOID_RAY_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.VOID_RAY_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/void_ray"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 3;
	}

	/**
	 * A committed swoop: the ray climbs, then dives through its target with a
	 * knockback strike before pulling back to altitude. Wildlife-first — it
	 * only triggers on an existing target (i.e. after being provoked).
	 */
	private static final class DiveBombGoal extends Goal {
		private final VoidRayEntity ray;
		/** Entity tick at which the next dive may start; poll-frequency independent. */
		private int readyAtTick;
		private int diveTicks;
		private LivingEntity target;

		DiveBombGoal(VoidRayEntity ray) {
			this.ray = ray;
			setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (ray.tickCount < readyAtTick) return false;
			target = ray.getTarget();
			if (target == null || !target.isAlive()) return false;
			double dist = ray.distanceToSqr(target);
			return dist > 9.0D && dist <= 400.0D;
		}

		@Override
		public void start() {
			diveTicks = 40;
			ray.swing(InteractionHand.MAIN_HAND);
			ray.playSound(ModSounds.VOID_RAY_HURT, 1.0F, 1.4F);
		}

		@Override
		public boolean canContinueToUse() {
			return diveTicks > 0 && target != null && target.isAlive();
		}

		@Override
		public void tick() {
			diveTicks--;
			Vec3 toTarget = target.position().add(0.0D, 0.4D, 0.0D).subtract(ray.position());
			Vec3 dir = toTarget.normalize();
			// First half of the dive accelerates; contact deals the strike.
			double speed = diveTicks > 20 ? 0.35D : 0.55D;
			ray.setDeltaMovement(ray.getDeltaMovement().scale(0.8D).add(dir.scale(speed)));
			ray.hurtMarked = true;
			ray.getLookControl().setLookAt(target);
			if (ray.distanceToSqr(target) <= 3.5D) {
				target.hurt(ray.damageSources().mobAttack(ray), 4.0F);
				Vec3 kb = target.position().subtract(ray.position()).normalize();
				target.setDeltaMovement(target.getDeltaMovement().add(kb.x * 0.8D, 0.4D, kb.z * 0.8D));
				target.hurtMarked = true;
				diveTicks = 0;
				readyAtTick = ray.tickCount + 140;
			}
		}

		@Override
		public void stop() {
			target = null;
			// A dive that expired without contact still waits a shorter cooldown.
			readyAtTick = Math.max(readyAtTick, ray.tickCount + 100);
		}
	}
}
