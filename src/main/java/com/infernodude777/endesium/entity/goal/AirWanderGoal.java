package com.infernodude777.endesium.entity.goal;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Picks a nearby airborne spot and drifts toward it; keeps flyers aloft.
 * Shared by every Endesium flyer so wander behavior stays consistent;
 * only the horizontal and vertical spread differ per creature.
 */
public final class AirWanderGoal extends Goal {
	private final Mob mob;
	private final int xzSpread;
	private final int ySpread;

	public AirWanderGoal(Mob mob, int xzSpread, int ySpread) {
		this.mob = mob;
		this.xzSpread = xzSpread;
		this.ySpread = ySpread;
		setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (mob.getRandom().nextInt(20) != 0) return false;
		BlockPos spot = mob.blockPosition().offset(
				mob.getRandom().nextInt(xzSpread * 2 + 1) - xzSpread,
				mob.getRandom().nextInt(ySpread * 2 + 1) - ySpread,
				mob.getRandom().nextInt(xzSpread * 2 + 1) - xzSpread);
		if (!mob.level().getBlockState(spot).isAir()
				|| !mob.level().getBlockState(spot.above()).isAir()) return false;
		mob.getNavigation().moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, 1.0D);
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return !mob.getNavigation().isDone();
	}

	@Override
	public void stop() {
		mob.getNavigation().stop();
	}
}
