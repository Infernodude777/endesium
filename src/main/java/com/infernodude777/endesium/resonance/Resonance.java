package com.infernodude777.endesium.resonance;

import com.infernodude777.endesium.Endesium;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

/**
 * Persistent player attachment tracking accumulated resonance.
 * Resonance is a soft progression currency that gates later recipes.
 */
public final class Resonance {
    private static final int MAX_RESONANCE = 100;

    public static final AttachmentType<Integer> LEVEL = AttachmentRegistry.createPersistent(
            Endesium.id("resonance"),
            Codec.INT
    );

    private Resonance() { }

    /** Returns the player's current resonance level (0-100). */
    public static int get(ServerPlayer player) {
        return player.getAttachedOrElse(LEVEL, 0);
    }

    /**
     * Adds resonance for this player, clamped to [0, MAX_RESONANCE].
     * @return the new resonance level after the grant.
     */
    public static int add(ServerPlayer player, int amount) {
        int next = Math.clamp(get(player) + amount, 0, MAX_RESONANCE);
        player.setAttached(LEVEL, next);
        return next;
    }
}
