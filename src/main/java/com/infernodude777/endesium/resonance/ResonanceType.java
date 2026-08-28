package com.infernodude777.endesium.resonance;

/**
 * Classifies resonance sources by their nature and detection range.
 * The client never sees this enum directly; it influences the
 * qualitative Lens description only.
 */
public enum ResonanceType {
    /** A dormant relic waiting to be activated. */
    DORMANT_RELIC,
    /** An activated mechanism emitting a steady pulse. */
    ACTIVE_MECHANISM,
    /** A stronger relic felt from much farther away. */
    STRONG_RELIC,
    /** Reserved for the Shattered Spire landmark. */
    SPIRE_CORE,
    /** The Resonant Archive core after the Dragon is defeated. */
    AWAKENED_ARCHIVE,
    /** Reserved for future Void Stalker traces; not registered yet. */
    CREATURE_TRACE
}
