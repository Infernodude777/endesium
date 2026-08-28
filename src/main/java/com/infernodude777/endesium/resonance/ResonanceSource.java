package com.infernodude777.endesium.resonance;

import net.minecraft.core.BlockPos;

/**
 * Immutable position + mutable signal data for a single resonance source.
 * Sources self-register through block entities and are pruned when their
 * chunk unloads.
 */
public final class ResonanceSource {
    private final BlockPos position;
    private ResonanceType type;
    private int detectionRadius;
    private float strength;
    private boolean active;

    public ResonanceSource(BlockPos position, ResonanceType type,
                           int detectionRadius, float strength, boolean active) {
        this.position = position.immutable();
        update(type, detectionRadius, strength, active);
    }

    public BlockPos position() {
        return position;
    }

    public ResonanceType type() {
        return type;
    }

    public int detectionRadius() {
        return detectionRadius;
    }

    public float strength() {
        return strength;
    }

    public boolean active() {
        return active;
    }

    /**
     * Updates the mutable fields of this source in-place.
     * Radius and strength are clamped to safe ranges.
     */
    public void update(ResonanceType type, int detectionRadius, float strength, boolean active) {
        this.type = type;
        this.detectionRadius = Math.clamp(detectionRadius, 1, 96);
        this.strength = Math.clamp(strength, 0.0F, 1.35F);
        this.active = active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
