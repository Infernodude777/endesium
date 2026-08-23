# Resonance System

Resonance is Endesium's discovery mechanic. It is a quiet, qualitative signal
system: it tells the player that something is nearby and how strong it is, never
where exactly it is.

## Concepts

- `ResonanceType` — the strength/kind of a source (weak, strong, dormant,
  active, distant, linked).
- `ResonanceSource` — a source of resonance, typically a mechanism block entity
  inside a ruin or the spire.
- `ResonanceManager` — the server-side registry that reports the nearest source
  strength to a Lens user.

## Server authority

Only the server computes resonance. The client receives a bounded, qualitative
result (for example "a faint pull", "a strong resonance") — never coordinates.
One player's discovery never reveals positions to another player.

## Activation and persistence

A dormant mechanism is activated by using the Resonance Lens on it. Activation
is recorded in the mechanism block entity, so it survives chunk unload/reload
and server restart. Rewards are granted once; re-activation does not duplicate
them. Inspecting an already-active mechanism with the Lens re-checks its
advancement criteria, which repairs progression for worlds created before an
advancement reload.

For about twelve server ticks after activation, the mechanism draws a white,
advancing particle line from the player's Lens position to the mechanism. It is
a short confirmation effect, not a permanent waypoint or a coordinate beam.

## The Lens

- Reports no phantom signals: a source that is unloaded or destroyed reports
  nothing rather than a stale reading.
- Has a cooldown so rapid spam does not produce packet or particle spam.
- The activation line uses the separate `resonance_beam` particle and only
  exists during the short mechanism wake-up sequence.
- Works across the End; sources in other dimensions do not leak into readings.

## Discovery chains

The first chain is environmental rather than quest-driven:

1. A minor ruin carries a weak source.
2. The Lens gives a faint reading near it.
3. Activating it grants a token and hints at a stronger, more distant signal.
4. That distant signal points toward the Shattered Spire.

There is no quest UI and no waypoint. The world itself communicates the chain.
