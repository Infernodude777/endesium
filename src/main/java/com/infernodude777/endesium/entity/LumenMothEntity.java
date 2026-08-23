package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModSounds;
import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
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
 * A near-silent, luminous glider of the Luminous Groves. Peaceful: it drifts
 * between glowing growth and never attacks. Monster AI over flying navigation
 * keeps its wander goals available.
 */
public class LumenMothEntity extends PathfinderMob implements GeoEntity {
	private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.lumen_moth.fly");
	private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("animation.lumen_moth.hurt");
	private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.lumen_moth.death");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	public LumenMothEntity(EntityType<? extends LumenMothEntity> type, Level level) {
		super(type, level);
		moveControl = new FlyingMoveControl(this, 20, true);
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 6.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.2D)
				.add(Attributes.FLYING_SPEED, 0.5D)
				.add(Attributes.FOLLOW_RANGE, 16.0D);
	}


	// The Lumen Moth follows players carrying Lumen Lanterns.
	private boolean isPlayerCarryingLight(ServerPlayer player) {
		ItemStack mainHand = player.getMainHandItem();
		ItemStack offHand = player.getOffhandItem();
		return mainHand.is(ModItems.LUMEN_LANTERN)
				|| offHand.is(ModItems.LUMEN_LANTERN);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
		nav.setCanFloat(true);
		return nav;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new FollowLightGoal(this));
		goalSelector.addGoal(2, new com.infernodude777.endesium.entity.goal.AirWanderGoal(this, 16, 5));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
	}

	/** Drifts toward the nearest player holding a lit Lumen Lantern. */
	private static final class FollowLightGoal extends Goal {
		private static final double RANGE = 16.0D;
		private static final double STOP_DISTANCE_SQR = 3.0D * 3.0D;

		private final LumenMothEntity moth;
		private ServerPlayer lanternHolder;

		FollowLightGoal(LumenMothEntity moth) {
			this.moth = moth;
			setFlags(java.util.EnumSet.of(Goal.Flag.MOVE));
		}

		private ServerPlayer findLanternHolder() {
			double nearest = Double.MAX_VALUE;
			ServerPlayer found = null;
			for (ServerPlayer player : moth.level().getEntitiesOfClass(ServerPlayer.class,
					moth.getBoundingBox().inflate(RANGE), moth::isPlayerCarryingLight)) {
				double distance = moth.distanceToSqr(player);
				if (distance < nearest) {
					nearest = distance;
					found = player;
				}
			}
			return found;
		}

		@Override
		public boolean canUse() {
			lanternHolder = findLanternHolder();
			return lanternHolder != null;
		}

		@Override
		public boolean canContinueToUse() {
			return lanternHolder != null && lanternHolder.isAlive()
					&& moth.distanceToSqr(lanternHolder) < RANGE * RANGE;
		}

		@Override
		public void start() {
			moth.getNavigation().moveTo(lanternHolder, 1.0D);
		}

		@Override
		public void tick() {
			if (lanternHolder == null) return;
			moth.getLookControl().setLookAt(lanternHolder);
			if (moth.distanceToSqr(lanternHolder) > STOP_DISTANCE_SQR) {
				moth.getNavigation().moveTo(lanternHolder, 1.0D);
			}
		}

		@Override
		public void stop() {
			lanternHolder = null;
			moth.getNavigation().stop();
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends LumenMothEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(DEATH);
		if (hurtTime > 0) return state.setAndContinue(HURT_ANIM);
		return state.setAndContinue(FLY);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return animationCache;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.LUMEN_MOTH_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.LUMEN_MOTH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.LUMEN_MOTH_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/lumen_moth"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 1;
	}
}
