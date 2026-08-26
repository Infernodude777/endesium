package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.DeepLurkerEntity;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Makes the empty End skies alive and the space beneath the islands ominous.
 *
 * Sky half: drifting biome motes around players flying far above the surface.
 * Deep half: a managed under-island spawner for Deep Lurkers - dark air pockets
 * well below the heightmap occasionally grow one, capped per player.
 */
public final class AmbientSkyManager {
	private AmbientSkyManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(AmbientSkyManager::tick);
		Endesium.LOGGER.info("Registered ambient sky life and deep-end lurker spawner");
	}

	private static void tick(MinecraftServer server) {
		long t = server.getTickCount();
		boolean ambience = t % 15 == 0;
		boolean lurkers = t % 100 == 0;
		if (!ambience && !lurkers) return;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!(player.level() instanceof ServerLevel level)) continue;
			if (level.dimension() != Level.END) continue;
			if (ambience) skyAmbience(player, level);
			if (lurkers) tryDeepLurker(player, level);
		}
	}

	private static void skyAmbience(ServerPlayer player, ServerLevel level) {
		int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				player.getBlockX(), player.getBlockZ());
		if (player.getBlockY() <= surface + 12) return;
		var rand = player.getRandom();
		double x = player.getX() + (rand.nextDouble() - 0.5D) * 40.0D;
		double y = player.getY() + rand.nextDouble() * 8.0D - 2.0D;
		double z = player.getZ() + (rand.nextDouble() - 0.5D) * 40.0D;
		var type = rand.nextInt(3) == 0 ? ModParticles.HIGHLAND_WIND
				: rand.nextInt(2) == 0 ? ModParticles.LUMEN_MOTE : ModParticles.END_WASTES_MOTE;
		level.sendParticles(type, x, y, z, 1, 0.5D, 0.25D, 0.5D, 0.004D);
	}

	private static void tryDeepLurker(ServerPlayer player, ServerLevel level) {
		var rand = level.getRandom();
		int x = (int) (player.getX() + (rand.nextDouble() - 0.5D) * 80.0D);
		int z = (int) (player.getZ() + (rand.nextDouble() - 0.5D) * 80.0D);
		int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		if (surface < 34) return;
		int y = surface - 14 - rand.nextInt(26);
		if (y < level.getMinBuildHeight() + 2) return;
		BlockPos pos = new BlockPos(x, y, z);
		if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) return;
		if (!level.getBlockState(pos.below()).isSolidRender(level, pos.below())) return;
		if (level.getMaxLocalRawBrightness(pos) > 7) return;
		if (!level.getEntitiesOfClass(DeepLurkerEntity.class,
				player.getBoundingBox().inflate(96.0D), e -> e.isAlive()).isEmpty()
				&& level.getEntitiesOfClass(DeepLurkerEntity.class,
				player.getBoundingBox().inflate(96.0D)).size() >= 6) return;
		var spawned = ModEntities.DEEP_LURKER.spawn(level, pos, MobSpawnType.TRIGGERED);
		if (spawned != null) {
			level.sendParticles(ParticleTypes.PORTAL,
					pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
					12, 0.4D, 0.6D, 0.4D, 0.02D);
		}
	}
}