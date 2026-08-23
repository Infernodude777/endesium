package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A hollow, floating remnant of the Ashen Expanse. It keeps its distance and
 * attacks with a telegraphed ash bolt. Monster AI over flying navigation keeps
 * its ranged goal and target goals available.
 */
public class AshWraithEntity extends Monster implements GeoEntity {
	private static final RawAnimation FLOAT = RawAnimation.begin().thenLoop("animation.ash_wraith.float");
	private static final RawAnimation BOLT = RawAnimation.begin().thenPlay("animation.ash_wraith.bolt");
	private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("animation.ash_wraith.hurt");
	private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.ash_wraith.death");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
	private boolean charging;
	/** Ticks until the next Ash Veil pulse; refreshed after each use. */
	private int veilTicks = 100;
	/** Set once when the wraith drops below half health; triggers its enrage. */
	private boolean enraged;
	/** Saved so the enrage speed boost can be reverted on heal. */
	private boolean enrageApplied;

	public AshWraithEntity(EntityType<? extends AshWraithEntity> type, Level level) {
		super(type, level);
		moveControl = new FlyingMoveControl(this, 20, true);
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 24.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.25D)
				.add(Attributes.FLYING_SPEED, 0.6D)
				.add(Attributes.ATTACK_DAMAGE, 5.0D)
				.add(Attributes.FOLLOW_RANGE, 40.0D);
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
		goalSelector.addGoal(1, new AshBoltGoal());
		goalSelector.addGoal(3, new com.infernodude777.endesium.entity.goal.AirWanderGoal(this, 16, 5));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && isAlive()) {
			if (veilTicks > 0) {
				veilTicks--;
			} else {
				performAshVeil((ServerLevel) level());
				// An enraged wraith sheds veils far more often.
				veilTicks = (enraged ? 140 : 300) + random.nextInt(200);
			}
			updateEnrage((ServerLevel) level());
		}
	}

	/**
	 * Ember Enrage: below half health the wraith's core flares — faster
	 * movement, quicker bolts, and a burst of embers announcing the shift.
	 */
	private void updateEnrage(ServerLevel level) {
		boolean shouldEnrage = getHealth() < getMaxHealth() * 0.5D;
		if (shouldEnrage && !enrageApplied) {
			getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
					getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() + 0.12D);
			enrageApplied = true;
			level.sendParticles(ModParticles.ASH_MOTE,
					getX(), getY() + 1.0D, getZ(), 24, 0.5D, 0.5D, 0.5D, 0.08D);
			playSound(ModSounds.ASH_WRAITH_ATTACK, 1.2F, 0.6F);
		} else if (!shouldEnrage && enrageApplied) {
			getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
					getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() - 0.12D);
			enrageApplied = false;
		}
		enraged = shouldEnrage;
	}

	/** True once the wraith has dropped below half health this life. */
	public boolean isEnraged() {
		return enraged;
	}

	private <E extends AshWraithEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(DEATH);
		if (hurtTime > 0) return state.setAndContinue(HURT_ANIM);
		if (charging) return state.setAndContinue(BOLT);
		return state.setAndContinue(FLOAT);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return animationCache;
	}

	// The Ash Veil creates localized darkness, reducing visibility.
	private void performAshVeil(ServerLevel level) {
		for (ServerPlayer player : level.players()) {
			if (this.distanceToSqr(player) < 144.0D) {
				player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
			}
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.ASH_WRAITH_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.ASH_WRAITH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.ASH_WRAITH_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/ash_wraith"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 6;
	}

	/** A ranged, telegraphed ash bolt with a 30-tick windup and a recovery cooldown. */
	private final class AshBoltGoal extends Goal {
		private int ticks;
		/** Entity tick at which the next bolt may be charged; poll-frequency independent. */
		private int readyAtTick;
		private LivingEntity target;

		@Override
		public boolean canUse() {
			if (tickCount < readyAtTick) return false;
			target = getTarget();
			return target != null && target.isAlive() && distanceToSqr(target) > 9.0D && hasLineOfSight(target);
		}

		@Override
		public void start() {
			ticks = 30;
			charging = true;
		}

		@Override
		public boolean canContinueToUse() {
			return ticks > 0 && target != null && target.isAlive();
		}

		@Override
		public void stop() {
			charging = false;
		}

		@Override
		public void tick() {
			getNavigation().stop();
			if (target != null) lookAt(target, 30.0F, 30.0F);
			if (--ticks == 0 && target != null) {
				target.hurt(damageSources().mobAttack(AshWraithEntity.this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
				// An enraged wraith's bolts burn what they touch.
				if (isEnraged()) target.igniteForSeconds(4);
				if (level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ModParticles.ASH_MOTE,
							target.getX(), target.getY() + 1.0D, target.getZ(),
							10, 0.2D, 0.2D, 0.2D, 0.01D);
				}
				playSound(ModSounds.ASH_WRAITH_ATTACK, 1.0F, 1.0F);
				readyAtTick = tickCount + (isEnraged() ? 50 : 100) + random.nextInt(80);
			}
		}
	}
}
