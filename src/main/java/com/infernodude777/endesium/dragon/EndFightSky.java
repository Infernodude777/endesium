package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The sky answers the fight. While an Endesium dragon lives in the arena the
 * void overhead stops being scenery: motes drift in the high air, tears of
 * light streak down on the later enrage levels, and every enrage spike pulls
 * a deep answering pulse from above the clouds.
 *
 * <p>Server-side only, particles and sound - the actual sky colour stays
 * vanilla, but the resonance above the island makes the sky read as alive.</p>
 */
public final class EndFightSky {
    private static final int TICK_INTERVAL = 20;
    private static final AABB ARENA_BOX =
            new AABB(-256.0D, 0.0D, -256.0D, 256.0D, 256.0D, 256.0D);

    private static final Map<ResourceKey<Level>, SkyState> STATES = new HashMap<>();

    private EndFightSky() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(EndFightSky::tick);
        Endesium.LOGGER.info("End fight sky layer registered");
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % TICK_INTERVAL != 0) return;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension() == Level.END) {
                tickLevel(level);
            }
        }
    }

    private static void tickLevel(ServerLevel level) {
        SkyState state = STATES.computeIfAbsent(level.dimension(), key -> new SkyState());
        List<EnderDragon> dragons =
                level.getEntitiesOfClass(EnderDragon.class, ARENA_BOX,
                d -> d.isAlive() && DragonCompanionSystem.isBossDragon(d));
        if (dragons.isEmpty()) {
            state.active = false;
            return;
        }
        state.active = true;
        int enrage = DragonAssaultHandler.enrageOf(level);
        if (enrage > state.enrage) {
            state.enrage = enrage;
            onSkyAnswers(level, enrage);
        }
        long now = level.getGameTime();
        if (enrage >= 2 && now % 60 == 0) {
            skyTear(level);
        }
        if (enrage >= 1 && now % 100 == 0) {
            driftMotes(level, enrage);
        }
    }

    // ------------------------------------------------------------------
    // Reactions
    // ------------------------------------------------------------------

    /** An enrage spike: the void overhead pulses and a deep voice answers. */
    private static void onSkyAnswers(ServerLevel level, int enrage) {
        level.playSound(null, new BlockPos(0, 150, 0), SoundEvents.ELDER_GUARDIAN_CURSE,
                SoundSource.AMBIENT, 1.4F, 0.8F);
        level.sendParticles(ModParticles.RESONANCE_PULSE, 0.0D, 150.0D, 0.0D,
                90, 40.0D, 12.0D, 40.0D, 0.08D);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, 0.0D, 160.0D, 0.0D,
                60, 50.0D, 10.0D, 50.0D, 0.10D);
        // A beam of light falls onto every player from the answering sky.
        for (ServerPlayer player : level.players()) {
            level.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), 160.0D, player.getZ(),
                    16, 0.0D, 30.0D, 0.0D, 0.0D);
        }
        if (enrage >= 3) {
            level.playSound(null, new BlockPos(0, 150, 0), SoundEvents.DRAGON_FIREBALL_EXPLODE,
                    SoundSource.AMBIENT, 1.8F, 0.5F);
        }
    }

    /** A streak of light tears down out of the high air. */
    private static void skyTear(ServerLevel level) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double dist = 20.0D + level.random.nextDouble() * 70.0D;
        double x = Math.cos(angle) * dist;
        double z = Math.sin(angle) * dist;
        level.sendParticles(ParticleTypes.END_ROD, x, 170.0D, z,
                24, 3.0D, 40.0D, 3.0D, 0.0D);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, x, 150.0D, z,
                8, 6.0D, 6.0D, 6.0D, 0.02D);
        level.playSound(null, new BlockPos((int) x, 160, (int) z),
                SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.AMBIENT, 1.6F, 0.6F);
    }

    /** Slow motes wandering the sky above the players' positions. */
    private static void driftMotes(ServerLevel level, int enrage) {
        for (ServerPlayer player : level.players()) {
            double x = player.getX() + (level.random.nextDouble() - 0.5D) * 80.0D;
            double z = player.getZ() + (level.random.nextDouble() - 0.5D) * 80.0D;
            double y = 120.0D + level.random.nextDouble() * 60.0D;
            level.sendParticles(enrage >= 3 ? ParticleTypes.CRIMSON_SPORE : ModParticles.RESONANCE_PULSE,
                    x, y, z, 2, 4.0D, 2.0D, 4.0D, 0.0D);
        }
    }

    private static final class SkyState {
        private boolean active;
        private int enrage;
    }
}
