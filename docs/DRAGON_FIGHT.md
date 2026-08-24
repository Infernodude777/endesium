# The Endesium Dragon Fight

The Endesium dragon is not the vanilla fight with more health. It is a staged,
escalating duel with its own attack grammar, a crystal-driven aegis system,
enrage waves, and a material hoard worth looting. This document describes the
fight as implemented, and how to test each layer.

## The arena

The fight takes place on and around the central End island. The ten pillar
crystals matter more than in vanilla: while **three or more crystals survive**,
the dragon slowly regenerates (1 HP per second, rising to 2 HP per second at
five or more crystals). Clearing the pillars is therefore the real first phase
of the fight - sniping the dragon while the pillars stand is a losing race.

## Attack repertoire

The fight controller drives the dragon through a rotating set of authored
attacks rather than vanilla's passive strafing loop:

| Attack | What it does | Counterplay |
|---|---|---|
| Impact dive | The dragon dives the target's position, dealing heavy damage and knockback in the impact zone | Watch for the climb, strafe late |
| Shockwave | A radial ground wave from the impact point | Get airborne or sprint through the telegraph |
| Screech | A disorienting cone that punishes clumping | Spread out |
| Fire barrage | Directed fireball volleys at the target | Keep moving laterally |
| Gale | A wind push that shoves players off the pillars | Hug the obsidian mid-animation |
| Sweep | A low wing sweep across the fountain platform | Get on top of the fountain |
| Catastrophe | The late-fight finisher: fissures and zone denial across the main island | Pre-position on the outer ring |
| **Abyssal Burrow** | The dragon plunges below the island into the void and vanishes; the ground rumbles for three seconds, then it erupts from directly beneath a player, dealing heavy damage and launching everything nearby | When the rumble starts, keep moving - never stand still |
| **Skyward Seize** | The dragon swoops a player, carries them aloft in its claws while spiraling upward, then hurls them across the sky | Burst the grab window with a ranged hit, or brace for the throw |
| **Gravity Rifts** | Four rifts tear open across the arena and drag players toward their cores | Fight the pull early; the cores hurt |

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
| Gravity Rifts | enrage 3 | 3 per second in a rift core | 30s | Stronger pull, more rifts (5) |

Notes:

- Only one special attack runs at a time, and each begins with a tell: the
  burrow opens with a portal howl, the seize with a wingbeat, the rifts with
  portal shimmer at their feet.
- While puppeted the dragon is invulnerable (burrow) or committed (seize);
  this is the window to reposition, not to burst.

## Crystal aegis

- 3+ crystals alive: the dragon regenerates 1 HP per second.
- 5+ crystals alive: regeneration rises to 2 HP per second.
- 0 crystals: the aegis is gone; damage taken is permanent.

## Enrage levels

As the dragon's health falls it escalates through three enrage levels. Each
escalation is announced with a title and a growl, and immediately spawns a
wave of void wisps:

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
  enrage level, and alive add count. Use it while fighting (or while spectating)
  to watch the escalation curve.
- To test the enrage ladder quickly: `/summon ender_dragon ~ ~10 ~` in the End,
  then damage the dragon with `/damage @e[type=ender_dragon,limit=1] 100` and
  watch levels 1-3 announce in sequence.
- To test the aegis: leave four crystals alive and check that the dragon's HP
  climbs between hits.
- To test the specials: drop the dragon to the enrage threshold you want
  (burrow at 1, seize at 2, rifts at 3) and wait - each fires within seconds
  once its cooldown is clear and a player is in the arena.
- To test rewards: kill the dragon and confirm the hoard drops at its last
  position, then check the transformation event fires on a first kill.

## Implementation notes

The assault layer (`DragonAssaultHandler`) polls the End dimension on a server
tick rather than injecting into the dragon's AI. It never overrides the fight
controller's phase machine; it only observes health and crystal counts and adds
pressure around them. The special attacks (`DragonSpecialAttacks`) are the one
deliberate exception: each set-piece parks the dragon in the hover phase,
disables its AI, and puppets its position tick by tick for the duration, then
hands control back to the holding pattern. Rewards drop through `DragonRewards`,
and the live readout lives in `DragonFightCommand`.
