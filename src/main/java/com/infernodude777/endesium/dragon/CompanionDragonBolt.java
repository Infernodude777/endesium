package com.infernodude777.endesium.dragon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The companion dragon's charged breath: a small fireball that keeps its
 * entity damage but never lights blocks on fire. The vanilla small fireball
 * ignites whatever it hits, which is fine for a hostile mob and not fine for
 * a pet that flies around your base.
 */
public class CompanionDragonBolt extends SmallFireball {
	public CompanionDragonBolt(EntityType<CompanionDragonBolt> type, Level level) {
		super(type, level);
	}

	public CompanionDragonBolt(Level level, LivingEntity owner, Vec3 velocity) {
		super(level, owner, velocity);
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		// Deliberately no fire: a tame dragon should never burn your builds.
	}
}
