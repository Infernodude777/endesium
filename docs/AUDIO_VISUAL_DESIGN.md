# Audio & Visual Design

Endesium's atmosphere is quiet. Every signal is sparse and means something. This
documents the audio and particle language so future additions stay consistent.

## Audio

Endesium registers a small sound event set. Many events reuse restrained vanilla
amethyst sounds so the mod stays quiet without shipping large audio files.

| Event | Purpose |
|---|---|
| `ambient.end_wastes_low` | sparse Wastes ambience |
| `ambient.chorus_wilds_low` | soft Wilds ambience |
| `item.resonance_lens.activate` | lens activation |
| `item.resonance_lens.pulse_low` / `pulse_high` | lens pulse |
| `block.end_ruin_mechanism.activate` | mechanism activation |
| `entity.void_stalker.idle/attack/reposition/hurt/death` | stalker states |

No sound should become irritating through normal play. Ambience stays low and
intermittent.

## Particles

Particles are client-side and carry no gameplay state. They exist to reinforce a
reading, not to flood the screen.

| Particle | Meaning |
|---|---|
| `end_wastes_mote` | slow drifting motes in the Wastes |
| `chorus_spore` | sparse spores in the Wilds |
| `resonance_pulse` | lens pulse at use |
| `resonance_active` | an active mechanism |
| `ruin_gold_contact` | mechanism interaction contact |
| `void_stalker_trace` | stalker reposition trace |

## Rules

- The entire body of a creature never glows. One cyan signal appears only during
  attack commitment.
- The End stays sparse; do not fill every chunk with particles.
- Palette: End stone, muted violet, deep purple, desaturated cyan, pale organic
  accents. Stronger colors used sparingly.
