package com.infernodude777.endesium.state;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * The one-time transformation event fired when the Ender Dragon dies. This is
 * the moment the player should feel the End change: a deep resonant boom, a
 * contained surge of crimson and resonance particles at the death site, and a
 * bold title for everyone present in the End. Players outside the End receive
 * no announcement; they simply encounter the awakened world state later.
 */
public final class PostDragonEvents {
	private PostDragonEvents() {
	}

	public static void fireTransformation(ServerLevel endLevel, BlockPos at) {
		double x = at.getX() + 0.5D;
		double y = at.getY() + 1.0D;
		double z = at.getZ() + 0.5D;

		// The death moment: a deep boom plus a rising resonance swell.
		endLevel.playSound(null, at, ModSounds.DRAGON_TRANSFORMATION,
				SoundSource.AMBIENT, 1.4F, 0.5F);
		endLevel.playSound(null, at, SoundEvents.ENDER_DRAGON_DEATH,
				SoundSource.AMBIENT, 1.2F, 0.7F);

		// A contained surge of crimson and resonance particles at the event site.
		endLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, x, y, z, 220, 12.0D, 8.0D, 12.0D, 0.15D);
		endLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 60, 9.0D, 6.0D, 9.0D, 0.05D);

		// Wider resonance particle spread across the arena.
		for (int i = 0; i < 6; i++) {
			double angle = Math.toRadians(i * 60.0D);
			double dist = 15.0D + endLevel.random.nextDouble() * 10.0D;
			double px = x + Math.cos(angle) * dist;
			double pz = z + Math.sin(angle) * dist;
			endLevel.sendParticles(ModParticles.RESONANCE_PULSE,
				px, y + 2.0D, pz, 20, 3.0D, 2.0D, 3.0D, 0.06D);
		}
		endLevel.sendParticles(ModParticles.RESONANCE_ACTIVE, x, y, z, 60, 8.0D, 5.0D, 8.0D, 0.05D);
		endLevel.sendParticles(ModParticles.RESONANCE_PULSE, x, y, z, 30, 6.0D, 4.0D, 6.0D, 0.02D);

		// Players present in the End feel it directly: the transformation cue
		// is audio and particles only, no on-screen text.
		for (ServerPlayer player : endLevel.players()) {
			endLevel.playSound(null, player.blockPosition(), ModSounds.DRAGON_TRANSFORMATION,
					SoundSource.AMBIENT, 0.8F, 1.0F);
		}

		// The advancement belongs to the validated world-state transition.
		MinecraftServer server = endLevel.getServer();
		var advancements = server.getAdvancements();
		var holder = advancements.get(Endesium.id("dragon_transformation"));
		if (holder != null) {
			for (ServerPlayer player : endLevel.players()) {
				player.getAdvancements().award(holder, "transformed");
			}
		}

		// No Golem here: the colossus answers the egg, not the corpse. Losing
		// its keeper silences the End only through the world-state shift - the
		// End Golem is summoned deliberately by placing the dragon egg.
		Endesium.LOGGER.info("Endesium post-Dragon transformation event fired");
	}
}
