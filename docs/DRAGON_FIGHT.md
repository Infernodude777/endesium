# The Endesium Dragon Fight

The Endesium dragon is not the vanilla fight with more health. It is a staged,
escalating duel with its own attack grammar, a crystal-driven aegis system,
guaranteed scripted set-pieces, enrage waves, and a material hoard worth
looting. This document describes the fight as implemented, and how to test
each layer.

## The arena

The fight takes place on and around the central End island. The ten pillar
crystals matter more than in vanilla:

- **3+ crystals alive**: the dragon regenerates (1 HP per second, rising to
  2 HP per second at five or more crystals).
- **Each surviving crystal** grants the dragon **10% damage reduction** (capped
  at 60%). The `/dragonfight` readout shows the current percentage, and the
  pillars glow with the shield while it holds.
- **Destroying a crystal** detonates at its pillar and the dragon roars.
- **The last crystal falling breaks the aegis**: the dragon is staggered for
  five seconds, takes **+40% damage**, a wisp wave pours out, and the next
  scripted set-piece is forced immediately.

Clearing the pillars is therefore the real first phase of the fight - sniping
the dragon while the pillars stand is a losing race, and the moment the aegis
breaks is the payoff window.

## Attack repertoire

The fight controller drives the dragon through a rotating set of authored
attacks rather than vanilla's passive strafing loop. **This applies to the
first dragon and the respawned one alike** - the transformation is an
escalation (1.6x scale, resonance-exclusive attacks), not the unlock key.

| Attack | What it does | Counterplay |
|---|---|---|
| Impact dive | The dragon dives the target's position, dealing heavy damage and knockback in the impact zone | Watch for the climb, strafe late |
| Shockwave | A radial ground wave from the impact point | Get airborne or sprint through the telegraph |
| Screech | A disorienting cone that punishes clumping | Spread out |
| Fire barrage | Directed fireball volleys at the target | Keep moving laterally |
| Gale | A wind push that shoves players off the pillars | Hug the obsidian mid-animation |
| Sweep | A low wing sweep across the fountain platform | Get on top of the fountain |
| Catastrophe | The late-fight finisher: fissures and zone denial across the main island | Pre-position on the outer ring |
| **Resonance Collapse** (transformed only) | Drags every player toward the dragon for three seconds, then detonates at its position | Gain distance and use cover while the rings converge |
| **Abyssal Burrow** | The dragon plunges below the island into the void and vanishes; the ground rumbles for three seconds, then it erupts from directly beneath a player, dealing heavy damage and launching everything nearby | When the rumble starts, keep moving - never stand still |
| **Skyward Seize** | The dragon swoops a player, carries them aloft in its claws while spiraling upward, then hurls them across the sky | Burst the grab window with a ranged hit, or brace for the throw |
| **Oblivion Charge** | The dragon climbs to the sky, paints a straight lane through the nearest player, then dives it twice, leaving void-fire wakes in its path | Get off the line - the lane is telegraphed while it hovers |
| **Gravity Rifts** | Rifts tear open across the arena and drag players toward their cores | Fight the pull early; the cores hurt |

Attacks telegraph before they land. The controller alternates between ranged
pressure and commitment windows, so shield timing and repositioning both
matter.

## Special attacks

The special attacks are scripted set-pieces that puppet the dragon directly.
They unlock with enrage, and every stat below scales with the enrage level
(1 / 2 / 3):

| Move | Unlocks | Damage | Cooldown | Escalation |
|---|---|---|---|---|
| Abyssal Burrow | enrage 1 | 9 / 11 / 13 + launch | 26s / 22s / 18s | Bigger launch, shorter cooldown |
| Skyward Seize | enrage 2 | 5 / 8 / 10 across grab + throw | 30s / 25s / 20s | Higher throw; slow falling mercy (10s) at enrage 2 only - at enrage 3 the fall is yours |
| Oblivion Charge | enrage 2 | 4 per pass + wake fires | 28s / 24s / 20s | More wake fires, shorter cooldown |
| Gravity Rifts | enrage 3 | 3 per second in a rift core | 30s | Stronger pull, more rifts (5) |

Notes:

- **Set-pieces are guaranteed, not random.** One opens the fight about thirty
  seconds in, every enrage escalation forces the next within a second, and a
  broken crystal aegis queues one immediately. The random cadence only fills
  the gaps between.
- Only one special attack runs at a time, and each begins with a tell: the
  burrow opens with a portal howl, the seize with a wingbeat, the charge with
  a climb and painted lane, the rifts with portal shimmer at their feet.
- While puppeted the dragon is invulnerable (burrow / charge) or committed
  (seize); this is the window to reposition, not to burst.

## Enrage levels

As the dragon's health falls it escalates through three enrage levels. Each
escalation is announced with a title and a growl, immediately spawns a wave of
void wisps, and forces a scripted set-piece:

| Level | Health threshold | Effects |
|---|---|---|
| 1 | below 60% | Wave of 2 void wisps; periodic reinforcement waves |
| 2 | below 35% | Larger waves; dragon breath pools form beneath its flight path |
| 3 | below 15% | Maximum wave pressure (up to 8 wisps alive); shortest wave cooldown |

Void wisps cap at eight alive at once, so the fight stays readable. The wisps
are ordinary void wisps: killable, and their drops still apply.

## Victory rewards

When the dragon dies, an enhanced hoard drops at its last position:

- 1x Dragon Heart
- 3x Dragon Fang
- 8x Dragonbone
- 2x Resonant Dragon Scale

Vanilla experience is unchanged. The transformation event (`The End Answers`)
still fires on the first kill exactly as before.

## Testing

- `/dragonfight` prints a live readout: dragon HP, remaining crystals, current
  aegis reduction %, enrage level, and alive add count. Use it while fighting
  (or while spectating) to watch the escalation curve.
- To test the enrage ladder quickly: `/summon ender_dragon ~ ~10 ~` in the End,
  then damage the dragon with `/damage @e[type=ender_dragon,limit=1] 100` and
  watch levels 1-3 announce in sequence - each one forces a set-piece.
- To test the aegis: leave four crystals alive and check that the dragon's HP
  climbs between hits and that damage numbers shrink; then break the last
  crystal and confirm the stagger window (+40% damage taken, forced special).
- To test the specials: drop the dragon to the enrage threshold you want
  (burrow at 1, seize/charge at 2, rifts at 3) and wait - each is guaranteed
  within its cooldown once a player is in the arena.
- To test rewards: kill the dragon and confirm the hoard drops at its last
  position, then check the transformation event fires on a first kill.

## Implementation notes

The assault layer (`DragonAssaultHandler`) polls the End dimension on a server
tick rather than injecting into the dragon's AI. It never overrides the fight
controller's phase machine; it only observes health and crystal counts and adds
pressure around them. Crystal damage reduction is applied as a single gate in
the hurt mixin (`LivingEntityMixin`), fed by the assault layer's cached crystal
count. The special attacks (`DragonSpecialAttacks`) are the one deliberate
exception: each set-piece parks the dragon in the hover phase, disables its AI,
and puppets its position tick by tick for the duration, then hands control back
to the holding pattern. Rewards drop through `DragonRewards`, and the live
readout lives in `DragonFightCommand`.
