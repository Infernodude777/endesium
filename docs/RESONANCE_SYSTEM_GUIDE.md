# Endesium Resonance System Guide

The resonance system is the narrative and mechanical backbone of Endesium.
It ties together the ruins, the tools, and the post-dragon story.

## What is resonance?

Resonance is a form of energy left behind by the ancient Endesium civilization.
It lingers in Resonant Slate, Resonant Basalt, and the Resonant Mechanisms
scattered across the End's ruins. The player interacts with resonance using
specialized tools.

## Core concepts

### Resonance Lens

The **Resonance Lens** is the primary tool for interacting with resonance. It
activates Resonant Mechanisms, revealing structures and unlocking progression.
Craft it from four Void Shards and an Ender Eye.

### Resonance Sources and Types

The `resonance` package defines:

- `ResonanceType` — the kinds of resonance (e.g. station, tower, archive).
- `ResonanceSource` — a single source of resonance in the world, with a
  position and type.
- `Resonance` — a detected signal with a source and strength.
- `ResonanceManager` — tracks all known sources and answers queries about
  signals near the player.

### Echo Compass

The **Echo Compass** turns a known resonance signal into a heading. If you
have detected a source, the compass points toward it, letting you follow the
resonance from ruin to ruin.

## The journey

1. **First Resonance** — craft a Lens and activate your first mechanism.
2. **Fractured Station** — activate a broken station in the Wastes.
3. **Sunken Archive** — unearth a buried station in the Marshes.
4. **The Long Resonance** — follow a signal from a ruin to the tower.
5. **Deep Resonance** — follow a signal to its source in the deep End.
6. **The Archive Awakens** — wake the Resonant Archive in the Umbral Reach.

## Implementation notes

The ResonanceManager is server-authoritative; the client reads state through
the appropriate packets or synced state. The Echo Compass and guidebook both
consume resonance data. See `src/main/java/.../resonance/` for the code and
`docs/RESONANCE_SYSTEM.md` for the full design.
