package com.infernodude777.endesium.gear;

import com.infernodude777.endesium.registry.ModEndgear;
import com.infernodude777.endesium.registry.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Cross-set synergies for the non-void gear lines. Wearing a FULL set while
 * holding that line's tool merges armor and weapon into one fighting system —
 * a drastic deepening of the Luminous / Ash / Null identity beyond the
 * per-piece passives handled in {@link GearAbilities}.
 *
 * <p>Each line grants one lingering passive while its tools are held in the
 * full set, plus a burst rider on every hit with a matching tool.
 * <ul>
 *   <li><b>Luminous</b> (full set + any Luminous tool): Prism Grip — Speed II
 *       and Haste II; hits blind with a flash (3 true damage + Glowing) and
 *       spark light shards.</li>
 *   <li><b>Ash</b> (full set incl. Ashwalker boots + any Ash tool): Ember
 *       Core — Fire Resistance while channelling; hits stoke the burn for 2
 *       extra seconds and knock the foe back.</li>
 *   <li><b>Null</b> (full set + any Null tool): Erased Edge — Strength I and
 *       Haste I; every hit shreds 4 armor durability and drags the foe in.</li>
 * </ul>
 *
 * <p>Self-registering: building this object hooks the tick + hurt events, so
 * exactly instantiating it (see the loader in {@code GearAbilities}) activates
 * the layer with the rest of the gear system.
 */
public final class EndgearSetBonuses {

    public EndgearSetBonuses() {
        ServerTickEvents.END_SERVER_TICK.register(EndgearSetBonuses::tick);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(EndgearSetBonuses::onHurt);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            applyPassives(player);
        }
    }

    // ---------------------------------------------------------------
    // Passive lingering bonuses while a matched tool is held in the set
    // ---------------------------------------------------------------
    private static void applyPassives(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();

        if (isFullLuminous(player) && isLuminousTool(main)) {
            // Prism Grip: blazing speed + dig rate for the whole toolkit.
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 1, false, false, true));
        }

        if (isFullAsh(player) && isAshTool(main)) {
            // Ember Core: heat-immunity while channelling fire.
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false, true));
        }

        if (isFullNull(player) && isNullTool(main)) {
            // Erased Edge: heavier, faster strikes.
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, false, false, true));
        }
    }

    // ---------------------------------------------------------------
    // Hit riders: an extra kick every time a matched tool lands
    // ---------------------------------------------------------------
    private static void onHurt(LivingEntity target, DamageSource source,
            float sourceDamage, float currentDamage, boolean blocked) {
        if (!(target.level() instanceof ServerLevel level)) return;
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof ServerPlayer player)) return;
        ItemStack main = player.getMainHandItem();

        if (isFullLuminous(player) && isLuminousTool(main)) {
            // Radiant kick: a flash that adds true damage and lights the foe.
            if (!target.hasEffect(MobEffects.GLOWING)) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, false, true));
                target.hurt(player.damageSources().magic(), 3.0F);
                level.sendParticles(ParticleTypes.FLASH,
                        target.getX(), target.getY() + 0.8D, target.getZ(),
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            level.sendParticles(ParticleTypes.END_ROD,
                    target.getX(), target.getY() + 0.8D, target.getZ(),
                    6, 0.3D, 0.3D, 0.3D, 0.04D);
        }

        if (isFullAsh(player) && isAshTool(main)) {
            // Stoked flame: the burn lingers and throws weight behind the hit.
            target.igniteForSeconds(2);
            Vec3 kb = target.position().subtract(player.position()).normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(kb.x * 0.4D, 0.22D, kb.z * 0.4D));
            target.hurtMarked = true;
            level.sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + 0.8D, target.getZ(),
                    7, 0.3D, 0.3D, 0.3D, 0.05D);
        }

        if (isFullNull(player) && isNullTool(main)) {
            // Deleted durability and a gentle pull into the blade.
            shredArmor(target);
            Vec3 pull = player.position().subtract(target.position()).normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(pull.x * 0.4D, 0.02D, pull.z * 0.4D));
            target.hurtMarked = true;
            level.sendParticles(ParticleTypes.PORTAL,
                    target.getX(), target.getY() + 0.8D, target.getZ(),
                    5, 0.25D, 0.3D, 0.25D, 0.04D);
        }
    }

    private static void shredArmor(LivingEntity target) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack worn = target.getItemBySlot(slot);
            if (!worn.isEmpty() && worn.isDamageableItem()) {
                worn.setDamageValue(worn.getDamageValue() + 4);
            }
        }
    }

    // ---------------------------------------------------------------
    // Set / tool membership checks
    // ---------------------------------------------------------------
    private static boolean isFullLuminous(ServerPlayer p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(ModEndgear.LUMINOUS_HELMET)
                && p.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.LUMINOUS_CHESTPLATE)
                && p.getItemBySlot(EquipmentSlot.LEGS).is(ModEndgear.LUMINOUS_LEGGINGS)
                && p.getItemBySlot(EquipmentSlot.FEET).is(ModEndgear.LUMINOUS_BOOTS);
    }

    private static boolean isFullAsh(ServerPlayer p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(ModEndgear.ASH_HELMET)
                && p.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.ASH_CHESTPLATE)
                && p.getItemBySlot(EquipmentSlot.LEGS).is(ModEndgear.ASH_LEGGINGS)
                && p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ASHWALKER_BOOTS);
    }

    private static boolean isFullNull(ServerPlayer p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(ModEndgear.NULL_HELMET)
                && p.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.NULL_CHESTPLATE)
                && p.getItemBySlot(EquipmentSlot.LEGS).is(ModEndgear.NULL_LEGGINGS)
                && p.getItemBySlot(EquipmentSlot.FEET).is(ModEndgear.NULL_BOOTS);
    }

    private static boolean isLuminousTool(ItemStack s) {
        return s.is(ModEndgear.LUMINOUS_SWORD) || s.is(ModEndgear.LUMINOUS_PICKAXE)
                || s.is(ModEndgear.LUMINOUS_AXE) || s.is(ModEndgear.LUMINOUS_SHOVEL)
                || s.is(ModEndgear.LUMINOUS_HOE);
    }

    private static boolean isAshTool(ItemStack s) {
        return s.is(ModEndgear.ASH_SWORD) || s.is(ModEndgear.ASH_PICKAXE)
                || s.is(ModEndgear.ASH_AXE) || s.is(ModEndgear.ASH_SHOVEL)
                || s.is(ModEndgear.ASH_HOE);
    }

    private static boolean isNullTool(ItemStack s) {
        return s.is(ModEndgear.NULL_SWORD) || s.is(ModEndgear.NULL_PICKAXE)
                || s.is(ModEndgear.NULL_AXE) || s.is(ModEndgear.NULL_SHOVEL)
                || s.is(ModEndgear.NULL_HOE);
    }
}
