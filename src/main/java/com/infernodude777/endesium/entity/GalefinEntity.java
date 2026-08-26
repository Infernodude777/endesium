package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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

public class GalefinEntity extends PathfinderMob implements GeoEntity {
	private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.endesium.galefin.swim");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public GalefinEntity(EntityType<? extends GalefinEntity> type, Level level) {
		super(type, level);
		moveControl = new FlyingMoveControl(this, 20, true);
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 4.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.FLYING_SPEED, 0.75D)
				.add(Attributes.FOLLOW_RANGE, 20.0D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
		nav.setCanFloat(false);
		return nav;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 12.0F, 1.7D, 2.0D));
		goalSelector.addGoal(2, new com.infernodude777.endesium.entity.goal.AirWanderGoal(this, 18, 4));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "main", 3, state -> state.setAndContinue(SWIM)));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/galefin"));
	}
}