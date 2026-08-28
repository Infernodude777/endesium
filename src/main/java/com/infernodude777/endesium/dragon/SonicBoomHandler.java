package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModItems;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * The Resonant Elytra's signature ability. Fires a straight 40-block
 * resonance ray: 8 damage through the sonic-boom damage source (ignores
 * armor), strong knockback, and a distinctive sound. The 15-second cooldown
 * is persisted in {@link SonicCooldownData}.
 */
public final class SonicBoomHandler {
    private static final double RANGE = 40.0D;
    private static final float DAMAGE = 8.0F;
    private static final int COOLDOWN_TICKS = 300;

    private SonicBoomHandler() { }

    public static void fire(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) return;
        ServerLevel level = player.serverLevel();
        long gameTime = level.getGameTime();
        SonicCooldownData cooldowns = SonicCooldownData.get(player.server);
        if (cooldowns.onCooldown(player.getUUID(), gameTime)) return;
        if (!player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_WINGS)) return;

        cooldowns.setCooldown(player.getUUID(), gameTime + COOLDOWN_TICKS);
        player.getItemBySlot(EquipmentSlot.CHEST).hurtAndBreak(1, player, EquipmentSlot.CHEST);

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Set<LivingEntity> victims = new HashSet<>();

        for (double distance = 1.0D; distance <= RANGE; distance += 1.0D) {
            Vec3 point = eye.add(look.scale(distance));
            if (distance % 3 == 0) {
                level.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            } else if (distance % 2 == 0) {
                level.sendParticles(ModParticles.RESONANCE_BEAM, point.x, point.y, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(point, point).inflate(1.75D),
                    entity -> entity != player && entity.isAlive())) {
                victims.add(entity);
            }
        }

        for (LivingEntity entity : victims) {
            if (entity.isDeadOrDying()) continue;
            entity.hurt(level.damageSources().sonicBoom(player), DAMAGE);
            Vec3 away = entity.position().subtract(eye).normalize();
            entity.knockback(1.4D, away.x, away.z);
        }

        level.playSound(null, player.blockPosition(), ModSounds.SONIC_BOOM,
                SoundSource.PLAYERS, 1.2F, 1.0F);
        level.sendParticles(ParticleTypes.SONIC_BOOM,
                eye.x + look.x * 2.0D, eye.y + look.y * 2.0D,
                eye.z + look.z * 2.0D, 6, 0.4D, 0.4D, 0.4D, 0.02D);
    }
}
