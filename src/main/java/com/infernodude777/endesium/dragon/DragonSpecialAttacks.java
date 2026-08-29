package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.Endesium;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scripted showpiece attacks for the Endesium dragon. Where the fight
 * controller steers the dragon's ordinary combat loop, this layer seizes the
 * dragon outright — parking it in the hover phase and puppeting its position
 * tick by tick — to perform moves the phase system cannot express:
 *
 * • Abyssal Burrow (enrage 1+): dives below the island, erupts beneath a player.
 * • Skyward Seize (enrage 2+): swoops a target, carries them aloft, hurls them.
 * • Gravity Rifts (enrage 3): tears rifts that drag players toward their cores.
 */
public final class DragonSpecialAttacks {
    private static final Map<net.minecraft.resources.ResourceKey<Level>, SeqState> SEQ = new HashMap<>();
    private static final List<Rift> RIFTS = new ArrayList<>();
    private static final AABB ARENA_BOX = new AABB(-256.0D, 0.0D, -256.0D, 256.0D, 256.0D, 256.0D);

    private DragonSpecialAttacks() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(DragonSpecialAttacks::tick);
        Endesium.LOGGER.info("Dragon special attacks registered");
    }

    private static void tick(MinecraftServer server) {
        tickRifts();
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension() != Level.END) continue;
            List<EnderDragon> dragons = level.getEntitiesOfClass(EnderDragon.class, ARENA_BOX, EnderDragon::isAlive);
            if (dragons.isEmpty()) continue;
            tickDragon(level, dragons.get(0), DragonAssaultHandler.enrageOf(level));
        }
    }

    private static void tickDragon(ServerLevel level, EnderDragon dragon, int enrage) {
        SeqState state = SEQ.computeIfAbsent(level.dimension(), key -> new SeqState());
        if (state.attack != null && !dragon.isAlive()) {
            restore(dragon, state);
            return;
        }
        if (state.attack != null) {
            script(level, dragon, state, enrage);
            return;
        }
        if (enrage <= 0) return;
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;
        if (state.burrowCd > 0) state.burrowCd--;
        if (state.seizeCd > 0) state.seizeCd--;
        if (state.riftCd > 0) state.riftCd--;
        if (level.random.nextInt(100) >= 30) return;
        List<String> options = new ArrayList<>();
        if (state.burrowCd <= 0) options.add("BURROW");
        if (enrage >= 2 && state.seizeCd <= 0) options.add("SEIZE");
        if (enrage >= 3 && state.riftCd <= 0) options.add("RIFT");
        if (options.isEmpty()) return;
        begin(level, dragon, state, options.get(level.random.nextInt(options.size())), enrage);
    }

    private static void begin(ServerLevel level, EnderDragon dragon, SeqState state, String attack, int enrage) {
        state.attack = attack;
        state.phaseTick = 0;
        dragon.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
        dragon.setNoAi(true);
        state.startPos = dragon.position();
        switch (attack) {
            case "BURROW" -> {
                dragon.setInvulnerable(true);
                level.playSound(null, dragon.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.AMBIENT, 2.0F, 0.5F);
            }
            case "SEIZE" -> {
                ServerPlayer target = nearestPlayer(level, dragon, 48.0D);
                if (target == null) {
                    restore(dragon, state);
                    return;
                }
                state.grabbed = target;
                target.hurt(level.damageSources().mobAttack(dragon), 2 + 2 * enrage);
                level.playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.AMBIENT, 2.0F, 0.7F);
            }
            case "RIFT" -> {
                int count = 2 + enrage;
                for (int i = 0; i < count; i++) {
                    ServerPlayer anchor = level.players().get(level.random.nextInt(level.players().size()));
                    double angle = level.random.nextDouble() * Math.PI * 2.0D;
                    double dist = 4.0D + level.random.nextDouble() * 5.0D;
                    Vec3 at = anchor.position().add(Math.cos(angle) * dist, -1.0D, Math.sin(angle) * dist);
                    RIFTS.add(new Rift(level, at, 160, 0.05D + 0.02D * enrage, 2 + enrage));
                    level.playSound(null, BlockPos.containing(at), SoundEvents.PORTAL_TRIGGER, SoundSource.AMBIENT, 1.2F, 1.4F);
                }
                finish(level, dragon, state, 30 * 20);
            }
            default -> restore(dragon, state);
        }
    }

    private static void script(ServerLevel level, EnderDragon dragon, SeqState state, int enrage) {
        state.phaseTick++;
        int t = state.phaseTick;
        switch (state.attack) {
            case "BURROW" -> {
                if (t <= 30) {
                    double k = t / 30.0D;
                    Vec3 p = state.startPos;
                    dragon.setPos(p.x, p.y + (-48.0D - p.y) * k, p.z);
                    if (t == 30) dragon.setInvisible(true);
                } else if (t <= 90) {
                    if (t % 20 == 0) {
                        for (ServerPlayer player : level.players()) {
                            level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.AMBIENT, 1.5F, 0.4F);
                            level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() - 1.0D, player.getZ(), 40, 1.5D, 0.2D, 1.5D, 0.05D);
                        }
                    }
                    if (t == 85) {
                        state.target = nearestPlayer(level, dragon, 128.0D);
                        if (state.target == null) { restore(dragon, state); return; }
                    }
                } else if (t <= 104) {
                    if (t == 91) dragon.setInvisible(false);
                    ServerPlayer target = state.target;
                    if (target == null) { restore(dragon, state); return; }
                    double k = (t - 91) / 13.0D;
                    double y = -40.0D + (target.getY() + 14.0D + 40.0D) * k;
                    dragon.setPos(target.getX(), y, target.getZ());
                    if (t == 104) erupt(level, dragon, target.position(), enrage);
                } else if (t >= 125) {
                    finish(level, dragon, state, (26 - 4 * enrage) * 20);
                }
            }
            case "SEIZE" -> {
                ServerPlayer held = state.grabbed;
                if (held == null || !held.isAlive() || held.isRemoved()) {
                    finish(level, dragon, state, (30 - 5 * (enrage - 1)) * 20);
                    return;
                }
                if (t <= 15) {
                    double k = t / 15.0D;
                    Vec3 p = state.startPos;
                    Vec3 hover = held.position().add(0.0D, 7.0D, 0.0D);
                    dragon.setPos(p.x + (hover.x - p.x) * k, p.y + (hover.y - p.y) * k, p.z + (hover.z - p.z) * k);
                } else if (t <= 75) {
                    dragon.setPos(dragon.getX(), dragon.getY() + 0.3D, dragon.getZ());
                    double angle = t * 0.15D;
                    held.teleportTo(dragon.getX() + Math.sin(angle) * 3.2D, dragon.getY() - 2.0D, dragon.getZ() + Math.cos(angle) * 3.2D);
                    held.setDeltaMovement(Vec3.ZERO);
                    held.hurtMarked = true;
                    held.fallDistance = 0.0F;
                    level.sendParticles(ParticleTypes.PORTAL, held.getX(), held.getY() + 1.0D, held.getZ(), 8, 0.3D, 0.5D, 0.3D, 0.02D);
                    if (t % 20 == 0) held.hurt(level.damageSources().mobAttack(dragon), 1.0F);
                } else if (t <= 80) {
                    dragon.setPos(dragon.getX() + (level.random.nextDouble() - 0.5D) * 0.6D, dragon.getY(), dragon.getZ() + (level.random.nextDouble() - 0.5D) * 0.6D);
                    if (t == 80) throwPlayer(level, dragon, held, enrage);
                } else if (t >= 90) {
                    finish(level, dragon, state, (30 - 5 * (enrage - 1)) * 20);
                }
            }
            default -> finish(level, dragon, state, 200);
        }
    }

    private static void erupt(ServerLevel level, EnderDragon dragon, Vec3 at, int enrage) {
        float damage = 7.0F + 2.0F * enrage;
        double launch = 1.0D + 0.15D * enrage;
        for (ServerPlayer player : level.players()) {
            Vec3 delta = player.position().subtract(at);
            if (delta.lengthSqr() > 81.0D) continue;
            player.hurt(level.damageSources().mobAttack(dragon), damage);
            Vec3 push = new Vec3(delta.x, 0.0D, delta.z);
            if (push.lengthSqr() < 0.01D) push = new Vec3(1.0D, 0.0D, 0.0D);
            push = push.normalize().scale(0.9D);
            player.setDeltaMovement(push.x, launch, push.z);
            player.hurtMarked = true;
        }
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 12, 2.0D, 1.0D, 2.0D, 0.05D);
        level.sendParticles(ParticleTypes.PORTAL, at.x, at.y, at.z, 120, 3.0D, 2.0D, 3.0D, 0.4D);
        level.playSound(null, BlockPos.containing(at), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.AMBIENT, 2.5F, 0.8F);
    }

    private static void throwPlayer(ServerLevel level, EnderDragon dragon, ServerPlayer held, int enrage) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double speed = 0.4D + 0.2D * enrage;
        held.setDeltaMovement(Math.cos(angle) * speed, 2.4D + 0.6D * enrage, Math.sin(angle) * speed);
        held.hurtMarked = true;
        held.hurt(level.damageSources().mobAttack(dragon), 3 + 2 * enrage);
        if (enrage < 3) {
            held.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, enrage == 2 ? 160 : 240, 0));
        }
        level.playSound(null, held.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.AMBIENT, 1.5F, 1.6F);
    }

    private static void finish(ServerLevel level, EnderDragon dragon, SeqState state, int cooldownTicks) {
        restore(dragon, state);
        if ("BURROW".equals(state.lastAttack)) state.burrowCd = cooldownTicks;
        else if ("SEIZE".equals(state.lastAttack)) state.seizeCd = cooldownTicks;
        else if ("RIFT".equals(state.lastAttack)) state.riftCd = cooldownTicks;
    }

    private static void restore(EnderDragon dragon, SeqState state) {
        state.lastAttack = state.attack;
        state.attack = null;
        state.phaseTick = 0;
        state.grabbed = null;
        state.target = null;
        dragon.setNoAi(false);
        dragon.setInvisible(false);
        dragon.setInvulnerable(false);
        dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
    }

    private static ServerPlayer nearestPlayer(ServerLevel level, EnderDragon dragon, double range) {
        ServerPlayer best = null;
        double bestDist = range * range;
        for (ServerPlayer player : level.players()) {
            double d = player.distanceToSqr(dragon);
            if (d < bestDist) {
                bestDist = d;
                best = player;
            }
        }
        return best;
    }

    private static void tickRifts() {
        if (RIFTS.isEmpty()) return;
        List<Rift> dead = null;
        for (Rift rift : RIFTS) {
            rift.tick();
            if (rift.dead) {
                if (dead == null) dead = new ArrayList<>();
                dead.add(rift);
            }
        }
        if (dead != null) RIFTS.removeAll(dead);
    }

    private static final class Rift {
        private final ServerLevel level;
        private final Vec3 center;
        private final int duration;
        private final double pull;
        private final float coreDamage;
        private int age;
        private boolean dead;

        private Rift(ServerLevel level, Vec3 center, int duration, double pull, float coreDamage) {
            this.level = level;
            this.center = center;
            this.duration = duration;
            this.pull = pull;
            this.coreDamage = coreDamage;
        }

        private void tick() {
            age++;
            if (age > duration || level.players().isEmpty()) {
                dead = true;
                return;
            }
            double spiral = age * 0.45D;
            for (int k = 0; k < 6; k++) {
                double angle = spiral + k * 1.05D;
                double radius = 3.0D - (age % 20) * 0.1D;
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x + Math.cos(angle) * radius, center.y + 0.5D + (age % 20) * 0.12D, center.z + Math.sin(angle) * radius, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            if (age % 4 == 0) {
                level.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 0.5D, center.z, 10, 0.8D, 0.3D, 0.8D, 0.3D);
            }
            for (ServerPlayer player : level.players()) {
                Vec3 delta = center.subtract(player.position());
                double dist = delta.length();
                if (dist > 10.0D) continue;
                if (dist < 2.5D) {
                    if (age % 20 == 0) player.hurt(level.damageSources().magic(), coreDamage);
                } else {
                    Vec3 pullVec = delta.normalize().scale(pull);
                    player.setDeltaMovement(player.getDeltaMovement().add(pullVec.x, 0.015D, pullVec.z));
                    player.hurtMarked = true;
                }
            }
            if (age % 40 == 0) {
                level.playSound(null, BlockPos.containing(center), SoundEvents.PORTAL_AMBIENT, SoundSource.AMBIENT, 1.0F, 0.6F);
            }
        }
    }

    private static final class SeqState {
        private String attack;
        private String lastAttack;
        private int phaseTick;
        private int burrowCd;
        private int seizeCd;
        private int riftCd;
        private ServerPlayer grabbed;
        private ServerPlayer target;
        private Vec3 startPos;
    }
}
