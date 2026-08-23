# Feature Reference

A current inventory of the production Endesium content. Historical milestone reports may intentionally list smaller slices.

## Blocks

| ID | Notes |
|---|---|
| `resonant_slate` | ruin building material |
| `end_gray` | ruin building material |
| `dormant_resonant_crystal` | decorative deposit / mechanism component |
| `resonant_mechanism` | block entity; activates via Lens |
| `inscribed_slate` | 4 symbol variants (blockstate 0-3) |
| `cracked_spire_stone` | spire landmark material |
| `resonant_pillar` | spire landmark material |
| `chorus_sprout` | Chorus Wilds vegetation |
| `wild_tendril` | Chorus Wilds vegetation |
| `resonant_bloom` | Chorus Wilds signature flora; faintly luminous |

## Core progression items

| ID | Notes |
|---|---|
| `void_shard` | uncommon remnant; ruin loot + independent stalker drop |
| `resonance_lens` | crafted or guaranteed Void Stalker drop; qualitative resonance reader |
| `resonance_token` | guaranteed mechanism activation reward; gates the Echo Compass |
| `echo_compass` | tracks recognized signals with heading + distance |
| `archive_sigil` | epic; first post-Dragon token from the Resonant Archive |
| `void_stalker_spawn_egg` | spawn egg |

## Entity

| ID | Notes |
|---|---|
| `void_stalker` | GeckoLib biped predator |
| `dust_crawler` | Wastes scavenger |
| `chorus_stalker` | Chorus Wilds ambusher |
| `void_ray` | Highland glider |
| `marsh_crawler` | Marsh hunter |
| `lumen_moth` | Peaceful Grove glider |
| `ash_wraith` | Ashen ranged hostile |
| `crystal_burrower` | Crystal Barrens burrower |
| `nullwalker` | Rare Umbral Reach hostile |

## Particles

| ID | Notes |
|---|---|
| `end_wastes_mote` | Wastes ambient |
| `chorus_spore` | Wilds ambient |
| `resonance_pulse` | lens pulse |
| `resonance_active` | active mechanism |
| `ruin_gold_contact` | mechanism interaction |
| `void_stalker_trace` | stalker reposition trace |
| `resonance_beam` | short white Lens-to-mechanism activation line |

## Sounds

| ID | Notes |
|---|---|
| `ambient.end_wastes_low` | Wastes ambience |
| `ambient.chorus_wilds_low` | Wilds ambience |
| `item.resonance_lens.activate` | lens |
| `item.resonance_lens.pulse_low` / `pulse_high` | lens; also the Archive Sigil chime |
| `block.end_ruin_mechanism.activate` | mechanism |
| `entity.void_stalker.idle/attack/reposition/hurt/death` | stalker |
| `item.echo_compass.use` | compass read |
| `event.dragon_transformation` | post-Dragon transformation surge |

## Advancements

| ID | Title |
|---|---|
| `first_resonance` | First Resonance |
| `echo_sight` | Echo Sight |
| `fractured_station` | Whispers in the Wastes |
| `sunken_archive` | The Sunken Archive |
| `the_long_resonance` | The Long Resonance |
| `what_remains` | What Remains |
| `dragon_transformation` | The End Answers |
| `archive_awakened` | Archive Awakened |

## Biomes

| ID | Identity |
|---|---|
| `end_wastes` | dead, fractured, sparse |
| `chorus_wilds` | alive, organic, spreading |
| `shattered_highlands` | broken peaks and wind |
| `void_marshes` | low, waterlogged ground |
| `luminous_groves` | cyan-lit living growth |
| `ashen_expanse` | heat, ash, and ember |
| `crystal_barrens` | exposed mineral formations |
| `void_skirts` | dark outer frontier |
| `void_crown` | high void geology |
| `umbral_reach` | deepest void region |

## Structures (generation features)

| ID | Notes |
|---|---|
| `end_ruin` | three weighted variants (Intact / Fractured / Sunken) |
| `shattered_spire` | rare landmark tower |
| `resonant_archive` | post-Dragon landmark; inert until the transformation |
| `resonant_monolith` | uncommon tall way-marker |
| `biome_structure` | region-specific landmark archetypes |
| `wilds_sanctum` | Chorus Wilds landmark |

## World state

`PostDragonState` (SavedData, End dimension): `dragonDefeated`,
`transformationActive`, `version`. Set by the Dragon-kill mixin; readable via
the dev command `/endesium dragonstate get`.
