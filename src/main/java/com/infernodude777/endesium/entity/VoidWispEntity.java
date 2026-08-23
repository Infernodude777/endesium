package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
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
 * A drifting lure-predator of the Void Skirts. From a distance it reads as a
 * harmless mote of light bobbing over the edge; up close it snaps into a
 * lunging strike that drags its prey toward the void.
 */
public class VoidWispEntity extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.void_wisp.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.void_wisp.float");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.void_wisp.lunge");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	public VoidWispEntity(EntityType<? extends VoidWispEntity> type, Level level) {
		super(type, level);
		moveControl = new FlyingMoveControl(this, 20, true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 14.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.FLYING_SPEED, 0.5D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
		nav.setCanOpenDoors(false);
		nav.setCanFloat(true);
		nav.setCanPassDoors(true);
		return nav;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
		goalSelector.addGoal(2, new LungePullGoal(this));
		goalSelector.addGoal(4, new WaterAvoidingRandomFlyingGoal(this, 0.6D));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public boolean isNoGravity() {
		return true;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends VoidWispEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().thenPlay("animation.void_wisp.death"));
		if (swinging) return state.setAndContinue(ATTACK);
		if (state.isMoving()) return state.setAndContinue(WALK);
		return state.setAndContinue(IDLE);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return animationCache;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level() instanceof ServerLevel server && tickCount % 12 == 0) {
			server.sendParticles(ParticleTypes.END_ROD,
					getX(), getY() + 0.6D, getZ(), 1, 0.15D, 0.15D, 0.15D, 0.005D);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ALLAY_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ALLAY_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/void_wisp"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 5;
	}

	/** A lunging pull that drags the target toward the wisp (and the edge). */
	private static final class LungePullGoal extends Goal {
		private final VoidWispEntity wisp;
		/** Entity tick at which the next lunge may start; poll-frequency independent. */
		private int readyAtTick;

		LungePullGoal(VoidWispEntity wisp) {
			this.wisp = wisp;
		}

		@Override
		public boolean canUse() {
			if (wisp.tickCount < readyAtTick) return false;
			LivingEntity target = wisp.getTarget();
			if (target == null) return false;
			double dist = wisp.distanceToSqr(target);
			return dist > 4.0D && dist <= 64.0D && wisp.hasLineOfSight(target);
		}

		@Override
		public void start() {
			LivingEntity target = wisp.getTarget();
			if (target == null) {
				readyAtTick = wisp.tickCount + 40;
				return;
			}
			Vec3 lunge = target.position().subtract(wisp.position()).normalize();
			wisp.setDeltaMovement(wisp.getDeltaMovement().add(lunge.scale(1.1D)));
			wisp.hurtMarked = true;
			Vec3 pull = target.position().subtract(wisp.position()).normalize().scale(0.45D);
			target.setDeltaMovement(target.getDeltaMovement().add(pull.x, -0.08D, pull.z));
			target.hurtMarked = true;
			target.hurt(wisp.damageSources().mobAttack(wisp), 2.0F);
			readyAtTick = wisp.tickCount + 90;
			if (wisp.level() instanceof ServerLevel server) {
				server.sendParticles(ParticleTypes.END_ROD,
						wisp.getX(), wisp.getY() + 0.5D, wisp.getZ(),
						8, 0.3D, 0.3D, 0.3D, 0.05D);
			}
		}
	}
}