package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
 * A patient ambusher of the Chorus Wilds. It holds still while distant and
 * closes distance with short-range blinks rather than a straight chase. Its
 * teleport is intentionally limited and telegraphed, never an Enderman clone.
 */
public class ChorusStalkerEntity extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.chorus_stalker.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.chorus_stalker.walk");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.chorus_stalker.attack");
	private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("animation.chorus_stalker.hurt");
	private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.chorus_stalker.death");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	public ChorusStalkerEntity(EntityType<? extends ChorusStalkerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 22.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 4.0D)
				.add(Attributes.FOLLOW_RANGE, 28.0D);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		goalSelector.addGoal(2, new BlinkGoal(this));
		goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends ChorusStalkerEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(DEATH);
		if (hurtTime > 0) return state.setAndContinue(HURT_ANIM);
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
		return ModSounds.CHORUS_STALKER_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.CHORUS_STALKER_HURT;
	}

	// Root snare is intentionally kept as a small helper for future ambush
	// goals; the regular melee goal remains responsible for hit timing.

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.CHORUS_STALKER_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/chorus_stalker"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 5;
	}

	/**
	 * Blink-strike: a short, cooldown-gated blink that lands the stalker
	 * behind its target, facing its back — setting up an unguarded strike
	 * rather than simply closing distance.
	 */
	private static final class BlinkGoal extends Goal {
		private final ChorusStalkerEntity stalker;
		/** Entity tick at which the next blink may start; poll-frequency independent. */
		private int readyAtTick;

		BlinkGoal(ChorusStalkerEntity stalker) {
			this.stalker = stalker;
		}

		@Override
		public boolean canUse() {
			if (stalker.tickCount < readyAtTick) return false;
			LivingEntity target = stalker.getTarget();
			if (target == null) return false;
			double dist = stalker.distanceToSqr(target);
			return dist > 9.0D && dist < 256.0D;
		}

		@Override
		public void start() {
			LivingEntity target = stalker.getTarget();
			if (target == null) return;
			// Land two blocks behind the target's back, on solid footing.
			Vec3 behind = target.position().add(target.getLookAngle().scale(-2.5D));
			Vec3 destination = new Vec3(behind.x, target.getY(), behind.z);
			BlockPos below = BlockPos.containing(destination).below();
			boolean safe = stalker.level().getBlockState(below).isSolidRender(stalker.level(), below)
					&& stalker.level().noCollision(stalker,
							stalker.getBoundingBox().move(
									destination.x - stalker.getX(),
									destination.y - stalker.getY(),
									destination.z - stalker.getZ()));
			if (!safe) {
				// Fall back to a straight-line close-in when the flank is not safe.
				Vec3 direction = target.position().subtract(stalker.position()).normalize();
				destination = stalker.position().add(direction.scale(6.0D));
			}
			stalker.teleportTo(destination.x, destination.y, destination.z);
			stalker.getLookControl().setLookAt(target);
			readyAtTick = stalker.tickCount + 100;
			if (stalker.level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ModParticles.CHORUS_SPORE,
						stalker.getX(), stalker.getY() + 1.0D, stalker.getZ(),
						6, 0.2D, 0.2D, 0.2D, 0.01D);
			}
		}
	}
}
