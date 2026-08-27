package com.infernodude777.endesium.item;

import com.infernodude777.endesium.particle.ModParticles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * The Void set's signature mobility: HOVER. With the full Void set worn,
 * sneaking while airborne cancels your fall and lets you drift — slowly
 * sinking instead of dropping, gently gliding instead of tumbling. It is a
 * true hover (not slow-fall), so you can stand mid-air as long as you hold
 * sneak, which pairs with the sword's singularity for total sky control.
 *
 * <p>Self-registering: constructing this instance hooks the server tick, so
 * simply loading the class (via the loader in GearAbilities) activates it.
 */
public final class VoidHoverAbility {
    // Sink rate in blocks/sec while hovering — slow enough to feel like
    // treading the void, fast enough to actually move around.
    private static final double HOVER_SINK = -0.12D;

    public VoidHoverAbility() {
        ServerTickEvents.END_SERVER_TICK.register(VoidHoverAbility::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            hoverIfEligible(player);
        }
    }

    private static void hoverIfEligible(ServerPlayer player) {
        // Hover needs the whole Void set — one piece alone can't command the
        // sky.
        if (!VoidEquipmentAbilities.isFullVoidArmor(player)) return;
        ServerLevel level = player.serverLevel();

        // Eligible: airborne (not grounded / swimming), sneaking, and above
        // the world floor. Holding shift in the air is the deliberate hover
        // gesture that the boots' tooltip promises.
        boolean airborne = !player.onGround() && !player.isInWater()
                && player.getY() > level.getMinBuildHeight() + 1.0D;
        if (!airborne || !player.isShiftKeyDown()) {
            return;
        }

        // Kill the fall and bleed the vertical velocity into a gentle sink.
        Vec3 motion = player.getDeltaMovement();
        player.resetFallDistance();
        player.fallDistance = 0.0F;
        player.setDeltaMovement(motion.x, HOVER_SINK, motion.z);

        // Subtle void motes trail beneath so it reads as a power, not a bug.
        if (player.tickCount % 8 == 0) {
            level.sendParticles(ModParticles.VOID_SKIRT_MOTE,
                    player.getX(), player.getY() - 0.4D, player.getZ(),
                    3, 0.3D, 0.05D, 0.3D, 0.02D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    player.getX(), player.getY() + 0.1D, player.getZ(),
                    2, 0.2D, 0.2D, 0.2D, 0.04D);
        }
    }
}
