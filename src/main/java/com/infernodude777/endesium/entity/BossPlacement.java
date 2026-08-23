package com.infernodude777.endesium.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Shared helpers for placing bosses safely. World generation is a hostile
 * place for entity spawns: most heightmaps are not populated yet, chunk
 * sections may not exist for entity queries, and the terrain around a feature
 * is only partially written. Every helper here therefore degrades gracefully:
 * it prefers {@code WORLD_SURFACE_WG} (the one heightmap that is meaningful
 * during decoration), validates footing with real collision shapes, searches
 * a bounded vertical window for legal footing, and refuses rather than
 * embedding a boss inside stone or dropping it into the void.
 */
public final class BossPlacement {
	/** How far above/below an intended spot a boss may hunt for footing. */
	private static final int VERTICAL_SEARCH = 6;

	private BossPlacement() {
	}

	/**
	 * Moves a boss onto solid, unobstructed open ground near {@code x, z}.
	 * Starts from the {@code WORLD_SURFACE_WG} surface, which stays reliable
	 * even while the chunk is still decorating, then hunts for legal footing
	 * in a small vertical window around it.
	 *
	 * @return true if the mob ended up standing clear.
	 */
	public static boolean settleOnGround(Mob mob, LevelReader level, double x, double z) {
		int bx = (int) Math.floor(x);
		int bz = (int) Math.floor(z);
		int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, bx, bz);
		return settleAt(mob, level, x, surfaceY + 1, z);
	}

	/**
	 * Settles a boss near an exact intended position: the interior of a
	 * vault, a balcony ring, a crypt floor. Unlike {@link #settleOnGround}
	 * this never teleports the boss to the world surface; it verifies that
	 * the given Y (or a nearby one) has a solid floor below and enough
	 * headroom for a standing mob.
	 *
	 * @return true if the mob ended up standing clear at a valid spot.
	 */
	public static boolean settleAt(Mob mob, LevelReader level, double x, double y, double z) {
		if (tryStand(mob, level, x, y, z)) return true;
		for (int d = 1; d <= VERTICAL_SEARCH; d++) {
			if (tryStand(mob, level, x, y + d, z)) return true;
			if (tryStand(mob, level, x, y - d, z)) return true;
		}
		return false;
	}

	/** Places the mob at one candidate Y and checks footing plus clearance. */
	private static boolean tryStand(Mob mob, LevelReader level, double x, double y, double z) {
		mob.moveTo(x, y, z, mob.getYRot(), 0.0F);
		AABB box = mob.getBoundingBox();
		if (!level.noCollision(mob, box)) return false;
		BlockPos support = BlockPos.containing(x, y - 0.5D, z);
		BlockState floor = level.getBlockState(support);
		return !floor.isAir() && !floor.getCollisionShape(level, support).isEmpty();
	}

	/** True when another live instance of this boss type is already nearby. */
	public static boolean duplicateNearby(Mob mob, double radius) {
		try {
			return !mob.level().getEntitiesOfClass(mob.getClass(),
					mob.getBoundingBox().inflate(radius),
					existing -> existing != mob && existing.isAlive()).isEmpty();
		} catch (Exception e) {
			// During decoration the entity section may not be resolvable yet;
			// treat the check as passed rather than aborting the spawn.
			return false;
		}
	}

	/**
	 * All-in-one boss spawn: settle at the intended spot, refuse duplicates,
	 * mark persistent, and insert into the level. Works both during world
	 * generation ({@code WorldGenLevel}) and at runtime ({@code ServerLevel})
	 * because both implement {@code ServerLevelAccessor}. Only returns true
	 * when the boss genuinely exists in the world with safe footing.
	 */
	public static boolean spawnBoss(Mob mob, ServerLevelAccessor access,
			double x, double y, double z, double duplicateRadius) {
		if (!settleAt(mob, access, x, y, z)) return false;
		if (duplicateRadius > 0.0D && duplicateNearby(mob, duplicateRadius)) return false;
		mob.setPersistenceRequired();
		return access.addFreshEntity(mob);
	}
}
