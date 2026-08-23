package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.item.ItemEntity;
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

import java.util.List;

/**
 * A low, segmented scavenger of the End Wastes. It is not a pack predator:
 * it wanders slowly, seeks out dropped items, and only turns hostile when
 * provoked. Its silhouette reads as a thin-legged insectoid, never a zombie
 * reskin.
 */
public class DustCrawlerEntity extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.dust_crawler.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.dust_crawler.walk");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.dust_crawler.attack");
	private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("animation.dust_crawler.hurt");
	private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.dust_crawler.death");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	public DustCrawlerEntity(EntityType<? extends DustCrawlerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 10.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.22D)
				.add(Attributes.ATTACK_DAMAGE, 2.0D)
				.add(Attributes.FOLLOW_RANGE, 20.0D);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		goalSelector.addGoal(2, new SeekItemsGoal(this));
		goalSelector.addGoal(3, new DustCloudGoal(this));
		goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.7D));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends DustCrawlerEntity> PlayState animate(AnimationState<E> state) {
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
		return ModSounds.DUST_CRAWLER_IDLE;
	}

	private int burrowTicks;
	private int burrowCooldown;

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) return;
		if (burrowCooldown > 0) burrowCooldown--;
		if (burrowTicks > 0 && --burrowTicks == 0) {
			removeEffect(MobEffects.INVISIBILITY);
			removeEffect(MobEffects.MOVEMENT_SPEED);
		}
		// Signature escape: once per fight it liquefies into the dust and vanishes.
		if (burrowCooldown == 0 && burrowTicks == 0 && getHealth() <= getMaxHealth() * 0.30D) {
			burrowCooldown = 1200;
			burrowTicks = 60;
			addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
			addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 2, false, false));
			playSound(ModSounds.DUST_CRAWLER_IDLE, 1.0F, 0.6F);
			if (level() instanceof net.minecraft.server.level.ServerLevel server) {
				server.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
						getX(), getY() + 0.3D, getZ(), 20, 0.6D, 0.2D, 0.6D, 0.05D);
			}
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.DUST_CRAWLER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.DUST_CRAWLER_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/dust_crawler"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 2;
	}

	/** Seeks the nearest dropped item while it has no target. */
	private static final class SeekItemsGoal extends Goal {
		private final DustCrawlerEntity crawler;
		private ItemEntity item;

		SeekItemsGoal(DustCrawlerEntity crawler) {
			this.crawler = crawler;
		}

		@Override
		public boolean canUse() {
			if (crawler.getTarget() != null) return false;
			if (crawler.getRandom().nextInt(15) != 0) return false;
			List<ItemEntity> items = crawler.level().getEntitiesOfClass(
					ItemEntity.class, crawler.getBoundingBox().inflate(14.0D), e -> true);
			if (items.isEmpty()) return false;
			item = items.get(crawler.getRandom().nextInt(items.size()));
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			return item != null && item.isAlive() && crawler.getTarget() == null;
		}

		@Override
		public void tick() {
			if (item != null) crawler.getNavigation().moveTo(item, 1.0D);
		}

		@Override
		public void stop() {
			item = null;
		}
	}

	/**
	 * Kicks up a blinding dust cloud when a foe closes in — the crawler's
	 * escape and setup window while its target rubs the dust away.
	 */
	private static final class DustCloudGoal extends Goal {
		private final DustCrawlerEntity crawler;
		/** Entity tick at which the next cloud may start; poll-frequency independent. */
		private int readyAtTick;

		DustCloudGoal(DustCrawlerEntity crawler) {
			this.crawler = crawler;
		}

		@Override
		public boolean canUse() {
			if (crawler.tickCount < readyAtTick) return false;
			LivingEntity target = crawler.getTarget();
			if (target == null) return false;
			double dist = crawler.distanceToSqr(target);
			return dist <= 9.0D && crawler.hasLineOfSight(target);
		}

		@Override
		public void start() {
			readyAtTick = crawler.tickCount + 200;
			crawler.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
			if (crawler.level() instanceof net.minecraft.server.level.ServerLevel server) {
				server.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
						crawler.getX(), crawler.getY() + 0.4D, crawler.getZ(),
						14, 0.6D, 0.3D, 0.6D, 0.02D);
				for (Player p : server.getEntitiesOfClass(Player.class,
						crawler.getBoundingBox().inflate(4.0D), Player::isAlive)) {
					p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
					p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
				}
			}
			crawler.playSound(ModSounds.DUST_CRAWLER_IDLE, 1.0F, 0.7F);
		}
	}
}
