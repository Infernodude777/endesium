# Void Stalker

The Void Stalker is Endesium's primary dangerous creature. It is a tall, slender,
non-human biped that prefers to observe before it engages, keeps a preferred
combat distance, uses a readable side reposition, and only commits when it has
line of sight.

## Model and rendering

- GeckoLib geometry: `geo/entity/void_stalker_v2.geo.json` (production) with
  `void_stalker_production` animation set.
- Texture: `textures/entity/void_stalker.png`.
- Renderer/model: `ProductionVoidStalkerRenderer` / `ProductionVoidStalkerModel`.
- The `void_stalker_v1_reference` and other `_reference` geometry files are
  retained prototypes, documented in `PROTOTYPE_STATUS.md`.

## Visual language

Charcoal and deep violet body, pale-lavender eyes that turn cyan only during
attack commitment, a narrow horizontal mouth split during anticipation, and a
short rear sensing filament. No full-body glow.

## AI state machine

The production entity synchronizes its state to the client so the model animation
matches the server behavior. It is intentionally readable rather than a normal
run-straight melee mob:

| State | Behavior |
|---|---|
| Idle | wanders slowly and listens when no player is detected |
| Aware | pauses for a short recognition beat and turns toward the player |
| Observe | holds roughly 6–10 blocks, faces the player, and stops advancing |
| Position | approaches only when the player is too far away |
| Search | follows the last known position briefly after line of sight is broken |
| Attack anticipation | stops, draws back, shows cyan/particle telegraph, then waits |
| Attack impact | one claw thrusts; damage occurs at the impact frame |
| Attack recovery | remains vulnerable briefly before observing again |
| Reposition | after sustained close pressure, attempts a safe side relocation |
| Retreat | backs away when badly hurt |
| Hurt / Death | recoil or inward collapse animations |

To test the behavior, stand 6–10 blocks away and wait: it should watch rather
than rush. Move far away to make it approach, move too close to provoke a side
reposition, then let it enter attack range and watch for the telegraph before the
hit. Breaking line of sight should make it search the last position and then give
up rather than tracking through walls.

## Spawning and ecology

It does not spawn uniformly. It prefers the Wastes and is rarer in the Wilds, so
players can learn roughly where to expect it. It is not a mandatory guard for
every structure. A killed Void Stalker always drops one Resonance Lens; it can
also independently drop a Void Shard. This gives the player a reliable alternate
way to obtain the Lens while keeping shards uncommon.

## Sounds

`entity.void_stalker.idle`, `attack`, `reposition`, `hurt`, `death` are registered
and reuse restrained amethyst-like vanilla sounds so the creature stays quiet
rather than noisy.
