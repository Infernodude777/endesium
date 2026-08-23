# Advancements & Loot

## Advancements

All advancements live under `data/endesium/advancement`. They trigger on
validated actions, never merely on receiving an item.

| ID | Title | Trigger |
|---|---|---|
| `first_resonance` | First Resonance | activate a dormant mechanism with the Lens |
| `echo_sight` | Echo Sight | craft an Echo Compass |
| `fractured_station` | Whispers in the Wastes | activate a fractured station |
| `sunken_archive` | The Sunken Archive | unearth and activate a buried station |
| `the_long_resonance` | The Long Resonance | follow resonance from ruin to tower |
| `what_remains` | What Remains | reach the Shattered Spire and wake its core |
| `dragon_transformation` | The End Answers | defeat the Ender Dragon (world transformation) |
| `archive_awakened` | Archive Awakened | wake the Resonant Archive core after the transformation |

The root advancement is granted once; repeated activation does not re-grant it.
The Dragon transformation advancement fires exactly once per world (guarded by
persistent world state) and survives server restart and Dragon respawn.

## Loot

Loot tables are under `data/endesium/loot_table`.

- `entities/void_stalker.json` — guaranteed Resonance Lens plus an independent
  uncommon Void Shard roll, so the instrument is reliable while shards remain
  scarce and the stalker is not a shard farm.
- `chests/end_ruin.json` — ruin chest loot: Void Shards and modest discovery
  materials.
- `chests/end_archive.json` — the Resonant Archive's discovery loot.
- Block loot (`dropSelf`) for the building/vegetation blocks.

## Design rules

- No impossible rewards and no duplicate rewards.
- Void Shards are intentionally not renewable via a farm.
- Some discoveries reward knowledge (an advancement) rather than loot.
