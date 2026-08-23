# Endesium: End Ecology and Early Progression

Status: design for the "End Ecology and Early Progression" milestone. This
document is deliberately small and scoped. It builds on the completed vertical
slice (End Wastes, Chorus Wilds, End Ruins, the Shattered Spire, Resonance, the
Resonance Lens, and the Void Stalker) and adds the first layer of ecological and
progression depth on top of those systems.

Nothing here invents the Endesium endgame. The Deep End, post-Dragon
progression, bosses, armor, Void Grapple, and the final Endgame remain future
milestones.

---

## 1. Ecological principles

1. **The End is sparse by design.** Density is a tool, not a target. Every
   addition must earn its density by communicating something about the world.
2. **Everything means something.** Flora, terrain, particles, and creatures
   exist to be interpreted. Nothing is decoration with no reading.
3. **Regions differ in kind, not just color.** End Wastes and Chorus Wilds must
   reward different behaviors and contain different discoveries.
4. **Resources stay discovery-based.** Rare Endesium materials are not ores.
   Their value comes from where they are found and what they unlock.
5. **The player's knowledge is progression.** The most important unlocks are
   understanding how the End works, not stat increases.
6. **Server-authoritative, bounded, quiet.** No per-tick world-wide scans, no
   global state, no client-invented positions. The End stays quiet.

## 2. The two biomes as ecosystems

### End Wastes — the mineral grave

- Identity: dead, fractured, sparse, ancient. A place where mechanisms were
  built and then abandoned.
- Ecology: resonant outcrops, fractured spines, quiet fissures, dead chorus
  remnants, and rare surface traces of dormant crystal.
- Life: the Void Stalker is the only creature that belongs here, and it is rare
  enough that seeing one is an event.
- Resource character: **decay and salvage.** Ruins here are the source of Void
  Shards and the first Resonance Tokens. The Wastes give the player the means
  to understand what the Wilds hides.
- Rhythm: quiet, slow, watchful. Discovery arrives as a change in the terrain:
  a shelf, a seam, a ruin.

### Chorus Wilds — the living spread

- Identity: alive, strange, organic, spreading. Something has adapted to the
  emptiness and is slowly reclaiming it.
- Ecology: rolling terrain, dense chorus, low sprouts, tall tendrils, and the
  new Resonant Bloom — a pale flower that carries a faint charge and grows
  wherever chorus spreads.
- Life: chorus flora is the dense life; the Void Stalker is rarer here, which
  makes Wilds exploration feel safer but stranger.
- Resource character: **growth and charge.** The Wilds hold Resonant Blooms,
  renewable in the same loose sense chorus is, and the strongest environmental
  resonance signals. The Wilds give the player the materials that make
  resonance useful.
- Rhythm: denser, brighter, more active, but never noisy. The Wilds are where
  the player notices that something is *responding*.

## 3. Resource distribution

| Resource | Where | Rarity | Renewable? | Role |
|---|---|---|---|---|
| Void Shard | ruin loot, Void Stalker drop, mechanism rewards | rare | no | Lens and Compass crafting |
| Resonance Token | mechanism activation (guaranteed) | uncommon | no | progression gate for the Echo Compass |
| Resonant Bloom | Chorus Wilds vegetation | common in Wilds, absent in Wastes | yes (grows like chorus) | regional signature; future crafting relevance |
| Dormant Resonant Crystal | Wastes outcrops, ruins | uncommon | no | decoration and signal |
| Resonant materials (Slate, End Gray) | ruins and Wastes terrain | common | no | building language |

Rules:

- **No Endesium ore.** No vein, no "mine better ore" tier.
- **Void Shards stay scarce.** The Lens recipe already needs four; the Compass
  needs two more. Shards are the resource the player is always slightly short
  of, which keeps ruins and Stalkers meaningful.
- **Tokens are the first real gate.** A token can only be earned by activating
  a mechanism, so the Echo Compass can only be crafted by someone who has
  already done the first discovery. This is progression through understanding,
  not through mining.

## 4. Creature relationships

### The Void Stalker

- Belongs to the Wastes. It is rarer in the Wilds (weight 1 vs 4), so players
  learn to read the biome by what they expect to meet.
- It is not a structure guardian. It patrols and observes; its presence near a
  ruin is a hint that the ruin matters, never a guarantee it is dangerous.
- It now drops a guaranteed Resonance Lens, giving a non-crafting path to the
  instrument while keeping shards uncommon.

### Ambient life

No new hostile creature is added this milestone. Chorus Wilds reads as alive
through flora (sprouts, tendrils, blooms), spore particles, and terrain rather
than through mob count. If a passive environmental organism is ever needed, it
must have a reading (e.g., drift toward resonance) and be rare — not a
background mob.

## 5. Resonance progression

Resonance is not a detection toy. It is the End's underlying system, and the
player learns this in stages:

1. **Quiet.** The Lens is held, the world says nothing. (No signal.)
2. **Listening.** A band and a broad direction appear. (The Lens — existing.)
3. **Waking.** Activating a mechanism proves understanding and earns a Token.
4. **Tracking.** The Echo Compass turns a recognized signal into a heading and
   a distance. This is the first capability upgrade.
5. **Connecting.** Waking more mechanisms strengthens the net signal
   (active sources emit stronger and wider than dormant ones), and the Spire
   core can be felt from across the wastes.

The player never receives coordinates. The Compass reports direction and
distance in tens of blocks — precise enough to navigate by, never precise
enough to feel like a map marker.

## 6. Early progression

The first progression layer is a chain of *understanding*:

```
EXPLORE the outer End
  -> NOTICE a ruin (terrain changes, walls, mechanisms)
  -> INVESTIGATE with the Lens (band + direction)
  -> UNDERSTAND by activating the mechanism (Token + fragment)
  -> ACQUIRE a Resonance Token (guaranteed on first activation)
  -> CRAFT the Echo Compass (Token + Void Shards)
  -> USE it to track stronger signals (Spire, other ruins)
  -> DISCOVER that the End is connected
```

Gates:

- **Gate 1 — the Lens.** Requires Void Shards (ruin/stalker) and an Ender Eye.
- **Gate 2 — the Token.** Requires activating any dormant mechanism.
- **Gate 3 — the Echo Compass.** Requires a Token and two Void Shards.

No gate is a level, a stat, or a boss. Every gate is a discovery the player
made.

## 7. New item and capability

### Resonance Token (existing item, now obtainable)

A small ancient-gold medallion with a cyan ring and eye. It is the physical
proof that a mechanism was woken. It is granted exactly once per mechanism and
is the crafting gate for the Echo Compass. Future milestones may give tokens
more uses; today one use is enough.

### Echo Compass (new)

A dark, ringed instrument with a pale needle. Right-click in the End:

- No signal: "The echo compass is still."
- Signal: reports the exact cardinal direction and the distance rounded to the
  nearest ten blocks ("The needle pulls northwest, about 120 blocks away.").
- A short white particle line and a restrained pulse sound accompany the
  reading so the feedback is physical, not just text.
- 3-second cooldown; server-authoritative; only reads loaded sources in the
  End dimension.

Why this capability: it turns the Lens's qualitative feeling into navigable
information, but only for signals the player has already proven they can
interpret (they had to wake a mechanism to earn the Token). It expands
exploration without adding damage, movement tech, or armor.

## 8. Discovery loop

```
EXPLORE  -> wander until the terrain changes
NOTICE   -> a shape, a seam, a bloom, a ruin wall
INVESTIGATE -> Lens band, Compass heading, physical search
UNDERSTAND  -> the mechanism wakes; the fragment explains
ACQUIRE     -> Token, shard, fragment
USE         -> craft the Compass; track the next signal
DISCOVER    -> the Spire answers from across the wastes
```

The loop is repeatable because ruins vary (Intact/Fractured/Sunken) and the
Spire is rare. It is non-repetitive because each step is physical and
environmental rather than a quest checklist.

## 9. Regional reasons to explore

- **End Wastes** — ruins, Void Shards, Stalkers, Tokens. The Wastes teach the
  player how to read mechanisms.
- **Chorus Wilds** — Resonant Blooms, dense flora, stronger environmental
  resonance, rarer Stalkers. The Wilds reward the player who learned to read
  the Wastes.

Neither biome is "better." The Wastes give tools; the Wilds give materials and
signals. A complete first run visits both.

## 10. Reward philosophy

- Rewards are knowledge and access first, items second.
- The strongest reward this milestone is the Echo Compass — a capability, not a
  weapon.
- No power creep: no armor, no damage increase, no mining tier.
- Mechanism rewards stay modest: Void Shards, a Token, a lore fragment.

## 11. Balance philosophy

- Balance by *feel* in an actual world, not by tables. The acceptance test is
  whether a player naturally enters the discovery loop without a guide.
- Rare resources stay rare; the Loop should never feel like a grind.
- The Compass should make exploration *more* interesting, never trivialize it:
  it only tracks loaded, recognized sources.

## 12. Future compatibility

- Tokens are a natural future currency/component for post-Dragon progression.
- The Echo Compass's tracking contract (bounded, server-authoritative,
  no-coordinates) is the same contract future Resonance devices must keep.
- Resonant Blooms can later feed alchemy/incense-style systems without changing
  their ecological role.
- Nothing in this milestone changes vanilla generation, the Dragon, End Cities,
  gateways, or the central island.

## 13. Explicitly out of scope

Deep End, post-Dragon progression, bosses, End Golem, End Serpent, Endesium or
Voidsteel armor, Void Grapple, momentum combat, Resonance Storms, Meteor
Showers, new dimensions, complete End replacement.
