package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
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
import net.minecraft.world.phys.Vec3;
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
 * A heavy, mineral-plated burrower of the Crystal Barrens. It is a slow
 * surface threat that occasionally erupts near its target in a burst of
 * crystal motes, closing distance in a way no vanilla mob does.
 */
public class CrystalBurrowerEntity extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.crystal_burrower.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.crystal_burrower.walk");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.crystal_burrower.attack");
	private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("animation.crystal_burrower.hurt");
	private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.crystal_burrower.death");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	public CrystalBurrowerEntity(EntityType<? extends CrystalBurrowerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 30.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.24D)
				.add(Attributes.ATTACK_DAMAGE, 5.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
				.add(Attributes.FOLLOW_RANGE, 30.0D);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		goalSelector.addGoal(2, new BurrowEruptGoal(this));
		goalSelector.addGoal(3, new ShardVolleyGoal(this));
		goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.7D));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends CrystalBurrowerEntity> PlayState animate(AnimationState<E> state) {
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
		return ModSounds.CRYSTAL_BURROWER_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.CRYSTAL_BURROWER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.CRYSTAL_BURROWER_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/crystal_burrower"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 6;
	}

	/** Erupts near the target after a delay, bursting crystal motes. */
	private static final class BurrowEruptGoal extends Goal {
		private final CrystalBurrowerEntity burrower;
		/** Entity tick at which the next eruption may start; poll-frequency independent. */
		private int readyAtTick;

		BurrowEruptGoal(CrystalBurrowerEntity burrower) {
			this.burrower = burrower;
		}

		@Override
		public boolean canUse() {
			if (burrower.tickCount < readyAtTick) return false;
			LivingEntity target = burrower.getTarget();
			if (target == null) return false;
			double dist = burrower.distanceToSqr(target);
			return dist > 25.0D && dist < 900.0D;
		}

		@Override
		public void start() {
			LivingEntity target = burrower.getTarget();
			if (target == null) return;
			// Never erupt into a wall: only take the teleport when the whole
			// bounding box fits at the destination, otherwise retry later.
			Vec3 destination = target.position();
			if (!burrower.level().noCollision(burrower,
					burrower.getBoundingBox().move(destination.x - burrower.getX(),
							destination.y - burrower.getY(), destination.z - burrower.getZ()))) {
				readyAtTick = burrower.tickCount + 40;
				return;
			}
			burrower.teleportTo(destination.x, destination.y, destination.z);
			readyAtTick = burrower.tickCount + 140;
			if (burrower.level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ModParticles.CRYSTAL_MOTE,
						burrower.getX(), burrower.getY() + 1.0D, burrower.getZ(),
						8, 0.3D, 0.3D, 0.3D, 0.02D);
			}
			burrower.playSound(ModSounds.CRYSTAL_BURROWER_ATTACK, 1.0F, 1.0F);
		}
	}

	/** Fires homing crystal shards at mid range; the volley telegraphs via motes. */
	private static final class ShardVolleyGoal extends Goal {
		private final CrystalBurrowerEntity burrower;
		/** Entity tick at which the next volley may start; poll-frequency independent. */
		private int readyAtTick;

		ShardVolleyGoal(CrystalBurrowerEntity burrower) {
			this.burrower = burrower;
		}

		@Override
		public boolean canUse() {
			if (burrower.tickCount < readyAtTick) return false;
			LivingEntity target = burrower.getTarget();
			if (target == null) return false;
			double dist = burrower.distanceToSqr(target);
			return dist > 16.0D && dist <= 400.0D && burrower.hasLineOfSight(target);
		}

		@Override
		public void start() {
			LivingEntity target = burrower.getTarget();
			if (target == null) return;
			readyAtTick = burrower.tickCount + 160;
			burrower.getLookControl().setLookAt(target);
			burrower.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
			for (int i = 0; i < 3; i++) {
				net.minecraft.world.entity.projectile.ShulkerBullet shard =
						new net.minecraft.world.entity.projectile.ShulkerBullet(
								burrower.level(), burrower, target,
								net.minecraft.core.Direction.Axis.values()[i % 3]);
				shard.setPos(burrower.getX() + (i - 1) * 0.5D, burrower.getY() + 0.8D, burrower.getZ());
				burrower.level().addFreshEntity(shard);
			}
			if (burrower.level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ModParticles.CRYSTAL_MOTE,
						burrower.getX(), burrower.getY() + 1.2D, burrower.getZ(),
						12, 0.4D, 0.3D, 0.4D, 0.05D);
			}
			burrower.playSound(ModSounds.CRYSTAL_BURROWER_ATTACK, 1.0F, 1.4F);
		}
	}
}
