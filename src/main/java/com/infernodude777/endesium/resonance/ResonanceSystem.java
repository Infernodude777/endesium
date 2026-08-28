package com.infernodude777.endesium.resonance;

import com.infernodude777.endesium.Endesium;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Wires the persistent {@link Resonance} attachment into gameplay.
 * Grants resonance when a player defeats an Endesium mob and emits a
 * short chat notice when the player crosses a resonance tier.
 */
public final class ResonanceSystem {
    private static final int[] TIER_THRESHOLDS = { 0, 20, 40, 60, 80, 100 };
    private static final String[] TIER_NAMES = {
            "Still", "Attuned", "Awakened", "Resonant", "Harmonized", "Ascendant"
    };

    private ResonanceSystem() { }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(ResonanceSystem::onMobDeath);
        Endesium.LOGGER.info("Endesium resonance system registered (kills grant resonance)");
    }

    private static void onMobDeath(LivingEntity entity, DamageSource source) {
        if (entity.level().isClientSide) return;
        if (!isEndesiumMob(entity)) return;
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        int tierBefore = tierFor(Resonance.get(player));
        int next = Resonance.add(player, grantFor(entity));
        int tierAfter = tierFor(next);

        if (tierAfter > tierBefore) {
            player.sendSystemMessage(Component.literal(
                    "Resonance rises: you are now " + TIER_NAMES[tierAfter]
                            + " (" + next + "/100)."));
        }
    }

    /** Larger, rarer creatures carry more resonance. */
    private static int grantFor(LivingEntity entity) {
        switch (entity.getType().getDescriptionId()) {
            case "entity.endesium.void_stalker":
            case "entity.endesium.ash_wraith":
                return 4;
            case "entity.endesium.chorus_stalker":
            case "entity.endesium.nullwalker":
                return 3;
            default:
                return 2;
        }
    }

    private static boolean isEndesiumMob(LivingEntity entity) {
        ResourceLocation id = entity.getType().builtInRegistryHolder().key().location();
        return "endesium".equals(id.getNamespace());
    }

    public static int tierFor(int resonance) {
        int tier = 0;
        for (int i = 0; i < TIER_THRESHOLDS.length; i++) {
            if (resonance >= TIER_THRESHOLDS[i]) {
                tier = i;
            }
        }
        return tier;
    }

    public static String tierName(int resonance) {
        return TIER_NAMES[tierFor(resonance)];
    }
}
