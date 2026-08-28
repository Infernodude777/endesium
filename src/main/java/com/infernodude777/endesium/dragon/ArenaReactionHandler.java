package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import com.infernodude777.endesium.world.ArenaGeometry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The island answers the fight. While an Endesium dragon is alive in the
 * arena, the ground reads the battle back to the players: the fracture lines
 * of the island crackle and glow, escalating with enrage, and every milestone
 * - a crystal falling, an enrage spike, the aegis breaking - shudders through
 * the stone with shockwave rings and rumbling sound.
 *
 * <p>Deliberately world-state free: it never edits blocks or terrain, only
 * particles and sound, so the arena stays intact for the whole fight.</p>
 */
public final class ArenaReactionHandler {
    private static final int TICK_INTERVAL = 10;
    private static final AABB ARENA_BOX =
            new AABB(-256.0D, 0.0D, -256.0D, 256.0D, 256.0D, 256.0D);

    private static final Map<ResourceKey<Level>, ReactionState> STATES = new HashMap<>();

    private ArenaReactionHandler() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ArenaReactionHandler::tick);
        Endesium.LOGGER.info("Arena reaction layer registered");
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
        ReactionState state = STATES.computeIfAbsent(level.dimension(), key -> new ReactionState());
        List<EnderDragon> dragons =
                level.getEntitiesOfClass(EnderDragon.class, ARENA_BOX, EnderDragon::isAlive);
        int enrage = DragonAssaultHandler.enrageOf(level);
        int crystals = countCrystals(level);

        if (dragons.isEmpty()) {
            state.active = false;
            state.enrage = 0;
            state.crystals = 0;
            return;
        }
        state.active = true;
        if (state.fractures == null) {
            state.fractures = ArenaGeometry.fracturePoints(level.getSeed());
        }

        // An enrage spike cracks the island open.
        if (enrage > state.enrage) {
            state.enrage = enrage;
            onEnrage(level, enrage);
        }
        // Every crystal that falls bites back at the ground.
        if (crystals < state.crystals) {
            state.crystals = crystals;
            if (crystals == 0) {
                onAegisBroken(level);
            } else {
                onCrystalLost(level);
            }
        } else if (crystals != state.crystals) {
            state.crystals = crystals;
        }

        long now = level.getGameTime();
        // Constant low-level crackling from the wounds of the island.
        if (enrage > 0 && now % 40 == 0) {
            crackle(level, state, enrage);
        }
        // A deep rumble rolls across the arena on a slow cadence.
        if (enrage >= 1 && now % 300 == 0) {
            rumble(level);
        }
    }

    private static int countCrystals(ServerLevel level) {
        return level.getEntitiesOfClass(EndCrystal.class, ARENA_BOX,
                c -> c.distanceToSqr(0.0D, 64.0D, 0.0D) < 25600.0D).size();
    }

    // ------------------------------------------------------------------
    // Reactions
    // ------------------------------------------------------------------

    private static void onEnrage(ServerLevel level, int enrage) {
        rumble(level);
        level.playSound(null, new BlockPos(0, 64, 0), ModSounds.DRAGON_ROAR,
                SoundSource.AMBIENT, 1.1F, 0.7F);
        // A shockwave ring rolls outward across the island surface.
        for (int radius = 10; radius <= 80; radius += 10) {
            for (int angle = 0; angle < 360; angle += 12) {
                double radians = Math.toRadians(angle);
                double x = Math.cos(radians) * radius;
                double z = Math.sin(radians) * radius;
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) x, (int) z);
                level.sendParticles(enrage >= 3 ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.END_ROD,
                        x, y + 1.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static void onCrystalLost(ServerLevel level) {
        level.playSound(null, new BlockPos(0, 64, 0), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.AMBIENT, 1.2F, 0.6F);
        for (ServerPlayer player : level.players()) {
            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() - 0.5D, player.getZ(),
                    8, 1.2D, 0.2D, 1.2D, 0.04D);
        }
    }

    private static void onAegisBroken(ServerLevel level) {
        level.playSound(null, new BlockPos(0, 64, 0), SoundEvents.ELDER_GUARDIAN_CURSE,
                SoundSource.AMBIENT, 1.5F, 0.7F);
        level.sendParticles(ParticleTypes.EXPLOSION, 0.0D, 64.0D, 0.0D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        // Two counter-rotating rings as the aegis dies.
        for (int ring = 0; ring < 2; ring++) {
            for (int radius = 8; radius <= 90; radius += 8) {
                for (int angle = 0; angle < 360; angle += 10) {
                    double radians = Math.toRadians(angle) + ring * 0.35D;
                    double x = Math.cos(radians) * radius;
                    double z = Math.sin(radians) * radius;
                    int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) x, (int) z);
                    level.sendParticles(ModParticles.RESONANCE_PULSE,
                            x, y + 1.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    private static void crackle(ServerLevel level, ReactionState state, int enrage) {
        if (state.fractures == null || state.fractures.isEmpty()) return;
        int samples = 2 + enrage * 2;
        for (int i = 0; i < samples; i++) {
            double[] fracture = state.fractures.get(level.random.nextInt(state.fractures.size()));
            double x = fracture[0] + (level.random.nextDouble() - 0.5D) * fracture[2] * 2.0D;
            double z = fracture[1] + (level.random.nextDouble() - 0.5D) * fracture[2] * 2.0D;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) x, (int) z);
            ParticleOptions particle = enrage >= 3 ? ParticleTypes.CRIMSON_SPORE
                    : enrage >= 2 ? ParticleTypes.END_ROD : ParticleTypes.CLOUD;
            level.sendParticles(particle, x, y + 0.2D, z, 1, 0.0D, 0.0D, 0.0D, 0.01D);
        }
    }

    private static void rumble(ServerLevel level) {
        level.playSound(null, new BlockPos(0, 64, 0), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.AMBIENT, 1.2F, 0.4F);
        for (ServerPlayer player : level.players()) {
            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() - 0.5D, player.getZ(),
                    6, 1.0D, 0.2D, 1.0D, 0.05D);
        }
    }

    private static final class ReactionState {
        private boolean active;
        private int enrage;
        private int crystals;
        private List<double[]> fractures;
    }
}
