# Devlog 22 - The End Fight Got Real

hey guys.

this one's a two-day entry. yesterday was the sky getting alive and the gear
lines finally clicking together into actual fighting systems. today was the
dragon. the end fight has been building layers for a while - phases, enrage,
scripted moves - but it still had one big problem: **the first dragon barely
used any of it**. that's fixed now. the first kill is the fight now. the
respawned one is the nightmare.

## yesterday - 8/26

### the sky got three new residents

sky jelly, galefin, and deep lurker went in (that one got its own devlog - 21).
the sky half of `AmbientSkyManager` fills the air with biome motes when you
fly high, and the deep half keeps the under-island dark honest: lurkers
spawned into the black pockets below the heightmap, capped so the deep end is
ominous without ever becoming a wall.

the error pass that came with them mattered as much as the mobs - a renderer
with two stray closing braces, 19 registered items that never reached the
creative tab, three spawn eggs with no models and no home in the vanilla tab.
all of it restored. the Endesium tab is the canonical listing again.

### the sets became systems

the luminous, ash, and null sets each got a cross-set layer in
`EndgearSetBonuses` - wear the **full set** and hold that line's **tool** and
armor + weapon merge into one fighting identity:

- **luminous + prism grip** - speed II and haste II while channelling; every
  hit blinds with a flash (3 true damage + glowing) and sparks light shards.
- **ash + ember core** - fire resistance while channelling; hits stoke the
  burn for 2 extra seconds and throw weight behind the strike.
- **null + erased edge** - strength I and haste I; every hit shreds 4 armor
  durability off the target and drags them into the blade.

and the void set finally got its signature: **hover**. full void set, sneak in
the air, and you stop falling - a gentle drift at -0.12 blocks/tick instead of
a tumble, with void motes trailing under you. it pairs with the sword's
singularity for total sky control, and it's the boots' tooltip promise kept.

also: the ash/luminous/null armor and dragon wings recipes were quietly
**broken** - the ingredient syntax didn't parse, so the gear you grinded for
couldn't be crafted. every recipe fixed, verified.

## today - 8/27

### the plan that makes everything deliberate

locked in a ten-hour plan to retype every single Endesium source file - the
final "every line is deliberate" pass across the whole mod. the plan jsons are
in the repo root alongside the earlier session plans.

### the dragon actually fights now

the biggest change is the one you'd never see from a commit name: the
four-phase fight controller - impact dives, shockwaves, screeches, gales,
storms, collapses, the final roar, catastrophe - **now runs on the first
dragon too**. before, the first kill was vanilla strafing plus harassment, and
all the authored grammar was locked behind the respawned dragon. that gate is
gone. the first dragon opens with the full attack schedule, escalates through
all four phases, and the transformation still makes things worse: 1.6x scale
and the resonance-exclusive moves are still the awakened dragon's alone.

### the pillars became a real mechanic

crystals were just a heal before. now the aegis is a shield:

- every surviving pillar grants the dragon **10% damage reduction** (capped at
  60%), so clearing the pillars is a real first phase, not an optional chore.
- destroying a crystal detonates at its pillar with a roar.
- **the last crystal falling breaks the aegis**: the dragon is staggered for
  five seconds and takes **+40% damage**, a wisp wave pours out, and the next
  scripted set-piece fires immediately. that's your payoff window.

### set-pieces are guaranteed now

scripted specials used to roll a dice every tick. now they're scheduled:
**one opens the fight about thirty seconds in**, every enrage escalation
forces the next one within a second, and a broken aegis queues one on the
spot. the random cadence only fills the gaps. you will see these moves, every
fight, on purpose.

### two new attacks

- **Oblivion Charge** (enrage 2+, set-piece) - the dragon climbs high, paints
  a straight lane through the nearest player with end-rod markers, then dives
  it twice, tearing through anyone on the line and leaving void-fire wakes in
  its path. get off the lane.
- **Resonance Collapse** (transformed dragon only) - drags every player
  toward the dragon for three seconds with converging rings, then detonates
  at its position. gain distance and find cover; standing still loses.

## numbers

- 2 new attacks (1 scripted set-piece, 1 controller move)
- crystal aegis: 10% reduction per pillar (max 60%), +40% damage for 5s when broken
- set-pieces: guaranteed at 30s in, at every enrage, and on aegis break
- first dragon now uses the full 4-phase controller - no more vanilla first kill
- `gradlew build` green; `docs/DRAGON_FIGHT.md` rewritten for the new fight

## what's next

the respawned dragon is still the peak - the resonance attacks, the scale, the
fury. the fight has the grammar now; the next pass is making the *world* around
it react - the island cracking, the sky answering, the hoard becoming a
location instead of a pile. and the ten-hour file pass waits for nobody.

go break the pillars. it matters now.
