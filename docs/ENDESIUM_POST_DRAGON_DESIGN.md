# Endesium: Post-Dragon Transformation

Status: design and implementation for the "Post-Dragon Transformation"
milestone. This document defines the first meaningful world-state milestone in
Endesium: the moment the Ender Dragon's defeat changes the End itself. It builds
on the completed geography, ecology, and exploration systems and deliberately
stops before the Deep End and the final Endgame.

The Deep End, the final Endgame, bosses, armor, Void Grapple, and Resonance
Storms remain future milestones and are not designed here.

---

## 1. Core design principle

The Ender Dragon remains a vanilla milestone. Endesium does not replace the
Dragon fight. Instead the Dragon's defeat is a **world event**:

```
DRAGON DEFEATED
        ↓
WORLD STATE CHANGES
        ↓
RESONANCE AWAKENS
        ↓
NEW DISCOVERIES BECOME POSSIBLE
        ↓
A NEW LAYER OF THE END OPENS
```

The player should feel: "I didn't just beat the boss. I changed the End."

## 2. What already exists (the foundation this builds on)

- End Wastes and Chorus Wilds biomes with deterministic, vanilla-compatible
  generation.
- End Ruins (three variants) and the Shattered Spire landmark, generated as
  worldgen features.
- The Resonance system: server-authoritative sources, the Resonance Lens with
  qualitative readings, and persistent mechanism block entities.
- The Echo Compass (early progression capability), Resonance Token, Void
  Shard, Resonant materials, and the Resonant Bloom.
- The Void Stalker with readable, client-synchronized AI.
- First Resonance and the exploration advancement chain.
- Zero custom commands in production; the only Endesium command is the
  development-only `/endesium dragonstate` added by this milestone.

## 3. Dragon defeat detection

- Hook: a mixin on `EndDragonFight.setDragonKilled(EnderDragon)` — the vanilla
  method that fires only on an actual kill. World-load state restore goes
  through `scanState` and never calls this method, so reloading a world cannot
  false-trigger the transformation.
- The transformation fires **exactly once** per world, guarded by the
  persisted world state.
- Respawned and re-killed Dragons do not reset or re-trigger Endesium state.
  The transformation is permanent once earned.

## 4. Persistent world state

`PostDragonState` (a world-level `SavedData`, key `endesium_post_dragon`, held
in the overworld's `DimensionDataStorage` so it is always loaded regardless of
which dimension any player occupies):

- `dragonDefeated` — the vanilla fight has been completed at least once.
- `transformationActive` — the Endesium transformation has fired.
- `version` — transformation version for future migrations.

Requirements met: saves, loads, survives server restart and player logout,
works on dedicated servers, and is world-level (never player-owned).

## 5. The transformation event

Restrained by design — no fireworks. When the Dragon dies, players in the End
experience:

1. A deep resonance surge sound (`endesium:event.dragon_transformation`).
2. A ring of Resonance Active particles sweeping outward.
3. A quiet system message: "The End answers."
4. The `dragon_transformation` advancement.

Players elsewhere simply encounter the changed world later. There is no global
announcement to other dimensions.

## 6. Resonance awakening

After the transformation:

- **Dormant mechanisms awaken.** All non-active mechanism sources radiate at
  1.5x radius and 1.3x strength; active mechanisms stay as-is.
- **The Resonant Archive core becomes a detectable source** (`AWAKENED_ARCHIVE`,
  radius 512, strength 1.8) — the strongest signal in the mod, readable by the
  Lens across an entire region.
- The Lens reads the Archive as "the archive resonates — the End is awake",
  giving players a reason to follow the strongest signal they have ever seen.
- Before the transformation the Archive core is **sealed**: it reads as an
  ordinary dormant relic (radius 96, strength 1.0) so the Lens does not treat
  it as an awakened source until the Dragon is actually defeated.

## 7. The Resonant Archive (first post-Dragon discovery)

- One new landmark: the **Resonant Archive** — a sealed domed hall of Resonant
  Slate and End Stone Bricks, older and more intact than an End Ruin.
- It generates deterministically (one per 5x5 chunk cell in Endesium biomes)
  in **all** worlds, but its core is inert until the Dragon is defeated.
  Trying to wake it early returns: "The archive is sealed. It waits for the
  End to wake."
- Because nothing about generation depends on runtime post-Dragon state,
  already-generated worlds are safe: the archive simply wakes in place. No
  terrain is regenerated, no player builds are touched.
- After the transformation, waking the Archive Core grants the Archive Sigil
  (epic, the first post-Dragon token), Void Shards, and an Archive Fragment
  lore note. The `archive_awakened` advancement confirms the discovery.

## 8. Progression gates

```
ENTER END → explore → find ruins → learn Resonance (Lens)
    → wake a mechanism → Resonance Token
    → craft Echo Compass → track the Shattered Spire
    → defeat the Ender Dragon
    → TRANSFORMATION
    → dormant signals awaken; the Archive becomes the loudest signal
    → wake the Archive Core → Archive Sigil
    → the next layer is now implied (designed later, not implemented here)
```

The gate is knowledge + a world event, not a mining tier. No armor, no stats,
no damage changes.

## 9. Development command

`/endesium dragonstate get|set <true|false>` — development-only, permission
level 2, for testing the transformation without fighting the Dragon. It does
not bypass any production reward (the Archive still requires a real Lens
activation, which requires the post-Dragon state to be active). Documented in
`docs/COMMANDS.md`.

## 10. Multiplayer and save/load

- The state is world-level and server-authoritative. Player A killing the
  Dragon transforms the world for everyone; Player B offline or in another
  dimension encounters the awakened End when they return.
- Save/load: the transformation persists across restart; the event does not
  repeat; rewards do not duplicate (reward claims are stored in the mechanism
  block entity).
- Dragon respawn: vanilla respawning still works; Endesium progression is
  unaffected and never resets.

## 11. Performance

- The Dragon-death hook is event-driven — no per-tick checks.
- World state lookup is a single `SavedData` fetch, cached per level.
- The Archive generates like the other features (chunk-cell gated, bounded),
  so generation cost is negligible and consistent with existing landmarks.

## 12. Scope boundaries

This milestone deliberately does NOT implement: the Deep End, final Endgame,
bosses, Endesium or Voidsteel armor, Void Grapple, Momentum combat, Resonance
Storms, Meteor Showers, a new dimension, or a complete End replacement. The
Archive Sigil exists to make the next milestone concrete, but the next layer is
designed separately.

## 13. Future compatibility

- The `PostDragonState` version field allows the Deep End milestone to extend
  the same SavedData with new phases without breaking existing worlds.
- The Archive is the natural anchor for the next layer: it is rare, sealed,
  post-Dragon, and already implies "something beneath".
