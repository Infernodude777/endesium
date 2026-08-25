# Boss Balance - The Hard Mode Pass

Every Endesium boss was audited for bugs and weaknesses, then rebuilt around
one rule: the player should never feel safe.

## Bugs and weaknesses found

- **End Warden had 80 health** - less than an iron golem. Five critical hits
  ended the fight.
- **Attack damage was 9** - weaker than a vanilla warden's casual swipe.
- **Aggro range was 32 blocks** - bow users could snipe from outside the
  warden's awareness and it would never respond.
- **No armor toughness, no attack knockback** - stun-locked and knocked around
  by any player swinging fast.
- **Special attacks almost never fired**: the melee goal outranked the special
  goal, and the special cooldown was 9 seconds (4.5 enraged) on top of that.
- **One minion wave per fight, two minions total** - the call-for-allies was a
  footnote.
- **Enrage was cosmetic** - a sound and some particles, no stat change.
- **35 experience for a boss kill** - less than a stack of ore.
- **Crown Sentinel had 60 health, 8 damage, and a 24-block aggro range** - it
  could be shot from a hilltop while it wandered, unaware.
- **The dragon had vanilla 200 health** - the final boss of the mod died
  faster than its own crystal pillars.

## The new baseline

### End Warden (regional mini-boss)

- 300 health (was 80), 16 base damage (was 9), 14 armor + 4 toughness.
- 64-block aggro range (was 32) - no more free sniper shots.
- Faster base speed, and attack knockback so its hits shove you back.
- **Enrage is real now**: at half health the warden's damage jumps to 20 and
  its speed to 0.36 - permanently, for the rest of the fight.
- Special cooldown nearly halved (110s / 55s enraged), and every special hits
  harder: gale slam 7-10, blink strike 10, suppression 10, ember nova burns
  for 8 seconds.
- **A second minion wave** spawns below one-third health.
- 150 experience (was 35).

### Crown Sentinel

- 160 health, 14 damage, 12 armor + 3 toughness, 48-block aggro range,
  faster pursuit.

### The dragon (final boss)

- **600 health** (was 200) - the fight is three times longer, and the enrage
  ladder at 60% / 35% / 15% now has real room to breathe.
- Enrage waves spawn 2 + enrage wisps per wave (up to 10 alive), with a
  faster cadence at high enrage.
- Breath pools from enrage two come every 10 seconds instead of 12.
- The crystal aegis, breath pools, and hoard rewards are unchanged - the
  pillars still matter, the phases still escalate, and the kill still pays.

## Testing

- Spawn a warden (`/summon endesium:end_warden`) and check the bar: 300 health,
  enrage at 150, damage 20 after the roar.
- Check the second wave: drop the warden below 1/3 health and count minions.
- Spawn the dragon (`/summon ender_dragon`) and read `/dragonfight`: the bar
  should open at 600.
