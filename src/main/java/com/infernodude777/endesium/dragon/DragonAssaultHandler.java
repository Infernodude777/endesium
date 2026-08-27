package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.command.DragonFightCommand;
import com.infernodude777.endesium.registry.ModEntities;
import com.infernodude777.endesium.entity.VoidWispEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The assault layer of the Endesium dragon fight. Three systems run here:
 * • Crystal aegis: while crystals survive the dragon regenerates.
 * • Enrage waves: health thresholds trigger escalating harassment.
 * • Victory rewards: an enhanced hoard drops when the dragon falls.
 */
public final class DragonAssaultHandler {
    private static final int TICK_INTERVAL = 20;
    private static final int MAX_ADDS = 8;
    private static final double ARENA_CRYSTAL_RADIUS_SQR = 25600.0D;
    private static final AABB ARENA_BOX =
            new AABB(-256.0D, 0.0D, -256.0D, 256.0D, 256.0D, 256.0D);

    /** Damage reduction granted per surviving pillar crystal (capped). */
    private static final double AEGIS_REDUCTION_PER_CRYSTAL = 0.10D;
    private static final double AEGIS_MAX_REDUCTION = 0.60D;
    /** How long the dragon stays staggered (extra damage) after the last crystal falls. */
    private static final int AEGIS_BROKEN_WINDOW = 100;

    private static final Map<net.minecraft.resources.ResourceKey<Level>, FightState> STATES =
            new HashMap<>();
    private static final java.util.Set<java.util.UUID> BUFFED_DRAGONS = new java.util.HashSet<>();

    private DragonAssaultHandler() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(DragonAssaultHandler::tick);
        DragonSpecialAttacks.register();
        DragonFightCommand.register();
        Endesium.LOGGER.info("Dragon assault layer registered");
    }

    /** Current enrage level (0-3) for the fight in the given dimension. */
    public static int enrageOf(ServerLevel level) {
        FightState state = STATES.get(level.dimension());
        return state == null ? 0 : state.enrageLevel;
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
        FightState state = STATES.computeIfAbsent(level.dimension(), key -> new FightState());
        List<EnderDragon> dragons =
                level.getEntitiesOfClass(EnderDragon.class, ARENA_BOX, EnderDragon::isAlive);
        if (dragons.isEmpty()) {
            if (state.active) {
                state.active = false;
                DragonRewards.dropEnhancedLoot(level, state.lastDragonPos);
                announce(level, "The Sky Falls Silent", "The dragon's hoard reveals itself",
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
            }
            return;
        }

        EnderDragon dragon = dragons.get(0);
        state.active = true;
        state.lastDragonPos = dragon.blockPosition();

        // Final boss buff: the dragon opens at 600 health (vanilla 200).
        if (!BUFFED_DRAGONS.contains(dragon.getUUID())
                && dragon.getHealth() >= dragon.getMaxHealth() - 0.01D) {
            BUFFED_DRAGONS.add(dragon.getUUID());
            var hp = dragon.getAttribute(
                    net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (hp != null) {
                hp.setBaseValue(600.0D);
            }
            dragon.setHealth(600.0F);
        }

        // Crystal aegis: track the pillars individually so a destroyed crystal
        // can burst at its pillar and the last one can stagger the dragon.
        Map<java.util.UUID, Vec3> currentCrystals = new HashMap<>();
        for (EndCrystal crystal : level.getEntitiesOfClass(EndCrystal.class, ARENA_BOX,
                c -> c.distanceToSqr(0.0D, 64.0D, 0.0D) < ARENA_CRYSTAL_RADIUS_SQR)) {
            currentCrystals.put(crystal.getUUID(), crystal.position());
        }
        for (Map.Entry<java.util.UUID, Vec3> gone : state.crystalPositions.entrySet()) {
            if (!currentCrystals.containsKey(gone.getKey())) {
                crystalShattered(level, dragon, gone.getValue());
            }
        }
        state.crystalPositions.clear();
        state.crystalPositions.putAll(currentCrystals);
        int crystals = currentCrystals.size();
        int previousCrystals = state.lastCrystalCount;
        state.lastCrystalCount = crystals;

        // Crystal aegis: pillars hold the dragon together.
        if (crystals >= 3 && dragon.getHealth() < dragon.getMaxHealth()) {
            dragon.heal(crystals >= 5 ? 2.0F : 1.0F);
        }
        // The last crystal falling breaks the aegis: the dragon is staggered,
        // takes bonus damage for a few seconds, and a set-piece is forced.
        if (previousCrystals > 0 && crystals == 0) {
            state.aegisBrokenUntil = level.getGameTime() + AEGIS_BROKEN_WINDOW;
            announce(level, "Aegis Broken", "The Dragon is staggered - strike now",
                    SoundEvents.ENDER_DRAGON_DEATH);
            DragonSpecialAttacks.forceNext(level);
            spawnWave(level, dragon, Math.max(1, state.enrageLevel));
        }

        float fraction = dragon.getHealth() / dragon.getMaxHealth();
        int enrage = 0;
        if (fraction < 0.60F) enrage = 1;
        if (fraction < 0.35F) enrage = 2;
        if (fraction < 0.15F) enrage = 3;
        if (enrage > state.enrageLevel) {
            state.enrageLevel = enrage;
            onEnrage(level, dragon, enrage);
        }

        if (state.enrageLevel >= 2 && --state.breathDelay <= 0) {
            state.breathDelay = 10;
            spawnBreathCloud(level, dragon);
        }
        if (state.enrageLevel >= 1 && --state.waveDelay <= 0) {
            state.waveDelay = Math.max(3, 12 - state.enrageLevel * 3);
            spawnWave(level, dragon, state.enrageLevel);
        }
    }

    private static void onEnrage(ServerLevel level, EnderDragon dragon, int enrage) {
        String[] titles = {"", "The Dragon Remembers", "The Dragon Unbound",
                "The Dragon Transcendent"};
        String[] subtitles = {"", "The air begins to shake", "Wisps tear free of its wake",
                "Reality thins around its wings"};
        announce(level, titles[enrage], subtitles[enrage], SoundEvents.ENDER_DRAGON_GROWL);
        // Every enrage escalation guarantees a scripted set-piece.
        DragonSpecialAttacks.onEnrage(level);
        spawnWave(level, dragon, enrage);
    }

    private static void crystalShattered(ServerLevel level, EnderDragon dragon, Vec3 at) {
        level.playSound(null, BlockPos.containing(at), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, 1.4F, 0.8F);
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5D, at.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.0D, at.z,
                40, 2.5D, 2.0D, 2.5D, 0.10D);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, at.x, at.y + 1.0D, at.z,
                24, 2.0D, 1.5D, 2.0D, 0.06D);
    }

    /**
     * Scales damage dealt to an Endesium dragon by the state of the crystal
     * aegis. Surviving pillars reduce damage; the window after the last pillar
     * falls increases it. Hooked from the hurt mixin on every incoming hit.
     */
    public static float modifyDragonDamage(ServerLevel level, float amount) {
        if (level.dimension() != Level.END) return amount;
        FightState state = STATES.get(level.dimension());
        if (state == null) return amount;
        if (state.lastCrystalCount > 0) {
            double reduction = Math.min(AEGIS_MAX_REDUCTION,
                    state.lastCrystalCount * AEGIS_REDUCTION_PER_CRYSTAL);
            return amount * (float) (1.0D - reduction);
        }
        if (state.aegisBrokenUntil > level.getGameTime()) {
            return amount * 1.4F;
        }
        return amount;
    }

    /** Percentage reduction currently granted by the aegis (0-60). */
    public static int aegisReductionPercent(ServerLevel level) {
        FightState state = STATES.get(level.dimension());
        if (state == null || state.lastCrystalCount <= 0) return 0;
        return (int) Math.round(Math.min(AEGIS_MAX_REDUCTION,
                state.lastCrystalCount * AEGIS_REDUCTION_PER_CRYSTAL) * 100.0D);
    }

    private static void spawnWave(ServerLevel level, EnderDragon dragon, int enrage) {
        int alive = level.getEntitiesOfClass(VoidWispEntity.class,
                dragon.getBoundingBox().inflate(128.0D), w -> true).size();
        int wanted = Math.min(MAX_ADDS - alive, 2 + enrage);
        if (wanted <= 0) return;

        List<ServerPlayer> players = level.players();
        for (int i = 0; i < wanted; i++) {
            Vec3 anchor = players.isEmpty()
                    ? dragon.position()
                    : players.get(level.random.nextInt(players.size())).position();
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double dist = 10.0D + level.random.nextDouble() * 8.0D;
            VoidWispEntity wisp = ModEntities.VOID_WISP.create(level);
            if (wisp == null) continue;
            wisp.moveTo(anchor.x + Math.cos(angle) * dist,
                    anchor.y + 2.0D + level.random.nextDouble() * 4.0D,
                    anchor.z + Math.sin(angle) * dist,
                    level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(wisp);
        }
    }

    private static void spawnBreathCloud(ServerLevel level, EnderDragon dragon) {
        AreaEffectCloud cloud = new AreaEffectCloud(level,
                dragon.getX(), dragon.getY() - 4.0D, dragon.getZ());
        cloud.setRadius(3.5F);
        cloud.setDuration(160);
        cloud.setWaitTime(5);
        cloud.setParticle(ParticleTypes.DRAGON_BREATH);
        level.addFreshEntity(cloud);
    }

    private static void announce(ServerLevel level, String title, String subtitle,
                                net.minecraft.sounds.SoundEvent sound) {
        level.playSound(null, new BlockPos(0, 64, 0), sound, SoundSource.AMBIENT, 1.2F, 0.9F);
    }

    public static String snapshot(ServerLevel level) {
        FightState state = STATES.get(level.dimension());
        List<EnderDragon> dragons =
                level.getEntitiesOfClass(EnderDragon.class, ARENA_BOX, EnderDragon::isAlive);
        if (dragons.isEmpty()) return "no dragon present";
        EnderDragon dragon = dragons.get(0);
        int crystals = level.getEntitiesOfClass(EndCrystal.class, ARENA_BOX,
                c -> c.distanceToSqr(0.0D, 64.0D, 0.0D) < ARENA_CRYSTAL_RADIUS_SQR).size();
        int adds = level.getEntitiesOfClass(VoidWispEntity.class,
                dragon.getBoundingBox().inflate(128.0D), w -> true).size();
        int enrage = state == null ? 0 : state.enrageLevel;
        int aegis = aegisReductionPercent(level);
        return "dragon hp=" + (int) dragon.getHealth() + "/" + (int) dragon.getMaxHealth()
                + " crystals=" + crystals + " aegis=" + aegis + "% enrage=" + enrage
                + " adds=" + adds;
    }

    private static final class FightState {
        private boolean active;
        private int enrageLevel;
        private int breathDelay = 12;
        private int waveDelay = 8;
        private BlockPos lastDragonPos;
        private int lastCrystalCount;
        private long aegisBrokenUntil;
        private final Map<java.util.UUID, Vec3> crystalPositions = new HashMap<>();
    }
}
