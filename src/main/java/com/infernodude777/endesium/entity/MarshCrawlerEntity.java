package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
 * A low, wide-bodied ambusher of the Void Marshes. It prefers the pools and
 * uses a cooldown-gated tendril pull to drag players into the wet ground,
 * rather than simply chasing them.
 */
public class MarshCrawlerEntity extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.marsh_crawler.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.marsh_crawler.walk");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.marsh_crawler.attack");
	private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("animation.marsh_crawler.hurt");
	private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.marsh_crawler.death");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	public MarshCrawlerEntity(EntityType<? extends MarshCrawlerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.26D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		goalSelector.addGoal(2, new PounceGoal(this));
		goalSelector.addGoal(3, new TendrilPullGoal(this));
		// A marsh ambusher must not avoid its own pools; plain strolling lets
		// it wander into and out of the shallows naturally.
		goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(this, 0.7D));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		// Crocodilian surge: it moves noticeably faster through its pools,
		// making water a hunter's ground rather than an obstacle.
		if (!level().isClientSide() && isInWaterOrBubble()) {
			getNavigation().setSpeedModifier(1.4D);
		} else {
			getNavigation().setSpeedModifier(1.0D);
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends MarshCrawlerEntity> PlayState animate(AnimationState<E> state) {
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
		return ModSounds.MARSH_CRAWLER_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.MARSH_CRAWLER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.MARSH_CRAWLER_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/marsh_crawler"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 4;
	}

	/** Signature pounce: a crocodile lunge that closes the gap in one bound. */
	private static final class PounceGoal extends Goal {
		private final MarshCrawlerEntity crawler;
		/** Entity tick at which the next pounce may start; poll-frequency independent. */
		private int readyAtTick;
		private boolean lunged;

		PounceGoal(MarshCrawlerEntity crawler) {
			this.crawler = crawler;
		}

		@Override
		public boolean canUse() {
			if (crawler.tickCount < readyAtTick) return false;
			LivingEntity target = crawler.getTarget();
			if (target == null || !crawler.onGround()) return false;
			double dist = crawler.distanceToSqr(target);
			return dist > 3.0D && dist <= 36.0D && crawler.hasLineOfSight(target);
		}

		@Override
		public void start() {
			LivingEntity target = crawler.getTarget();
			if (target == null) {
				readyAtTick = crawler.tickCount + 40;
				return;
			}
			lunged = true;
			Vec3 leap = target.position().subtract(crawler.position()).normalize();
			crawler.setDeltaMovement(leap.x * 0.85D, 0.42D, leap.z * 0.85D);
			crawler.hurtMarked = true;
			readyAtTick = crawler.tickCount + 140;
			crawler.playSound(ModSounds.MARSH_CRAWLER_ATTACK, 1.0F, 0.8F);
		}

		@Override
		public boolean canContinueToUse() {
			return lunged && !crawler.onGround();
		}

		@Override
		public void stop() {
			lunged = false;
		}
	}

	/** Drags the target toward the crawler on a long cooldown. */
	private static final class TendrilPullGoal extends Goal {
		private final MarshCrawlerEntity crawler;
		/** Entity tick at which the next pull may start; poll-frequency independent. */
		private int readyAtTick;

		TendrilPullGoal(MarshCrawlerEntity crawler) {
			this.crawler = crawler;
		}

		@Override
		public boolean canUse() {
			if (crawler.tickCount < readyAtTick) return false;
			LivingEntity target = crawler.getTarget();
			if (target == null) return false;
			double dist = crawler.distanceToSqr(target);
			return dist > 4.0D && dist <= 36.0D && crawler.hasLineOfSight(target);
		}

		@Override
		public void start() {
			LivingEntity target = crawler.getTarget();
			if (target == null) {
				readyAtTick = crawler.tickCount + 40;
				return;
			}
			Vec3 pull = crawler.position().subtract(target.position()).normalize().scale(0.9D);
			target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.22D, pull.z));
			target.hurtMarked = true;
			readyAtTick = crawler.tickCount + 120;
			crawler.playSound(ModSounds.MARSH_CRAWLER_ATTACK, 1.0F, 1.0F);
		}
	}
}
