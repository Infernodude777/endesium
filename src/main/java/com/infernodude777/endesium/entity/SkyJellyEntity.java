package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
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
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SkyJellyEntity extends PathfinderMob implements GeoEntity {
	private static final RawAnimation PULSE = RawAnimation.begin().thenLoop("animation.endesium.sky_jelly.pulse");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public SkyJellyEntity(EntityType<? extends SkyJellyEntity> type, Level level) {
		super(type, level);
		moveControl = new FlyingMoveControl(this, 12, true);
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.2D)
				.add(Attributes.FLYING_SPEED, 0.35D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
		nav.setCanFloat(false);
		return nav;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(2, new com.infernodude777.endesium.entity.goal.AirWanderGoal(this, 14, 6));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) return;
		// Gentle jelly pulse: occasional soft upward drift plus a light mote.
		if (random.nextInt(40) == 0) {
			setDeltaMovement(getDeltaMovement().add(
					(random.nextDouble() - 0.5D) * 0.04D,
					0.06D + random.nextDouble() * 0.05D,
					(random.nextDouble() - 0.5D) * 0.04D));
		}
		if (random.nextInt(30) == 0) {
			level().addParticle(ParticleTypes.END_ROD,
					getX(), getY() + 0.9D, getZ(), 0.0D, 0.01D, 0.0D);
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "main", 5, state -> state.setAndContinue(PULSE)));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/sky_jelly"));
	}
}