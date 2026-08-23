# Endesium First Vertical Slice Design Specification

**Status:** Design locked before gameplay implementation  
**Scope:** End Wastes, Void Shard, Resonance Lens, End Ruin, Void Stalker, basic Resonance, and one discovery advancement  
**Out of scope:** Deep End, new bosses, post-dragon progression, complete End replacement, custom End terrain generator, and additional mobs

This document describes the intended player experience and asset requirements. It is not an implementation plan disguised as code: no gameplay behavior should be added until the design and visual assets are approved.

## Design goals

The first slice should feel like a quiet, deliberate intrusion into the vanilla End. The player should recognize vanilla End stone, Endermen, void, and the Dragon's domain, then notice a small number of things that do not belong. The slice should reward observation and interpretation rather than map markers, noisy particles, or a linear quest arrow.

The visual and mechanical hierarchy is:

1. Vanilla End emptiness and scale.
2. A rare, readable environmental anomaly.
3. A handcrafted ruin with evidence of intent.
4. A restrained resonance signal.
5. A predator that observes the player instead of immediately becoming a target dummy.
6. One discovery and reward that creates a reason to continue exploring.

---

## 1. End Wastes

### Identity

End Wastes is not a purple forest and not a replacement for the End. It is a sparse transition region where familiar End islands become thinner, more weathered, and subtly resonant. The biome should initially make the player question whether they are seeing a new biome or simply an unusual part of the vanilla End.

### Terrain appearance

- Vanilla End island terrain remains the foundation.
- End stone is exposed in broad pale shelves with more visible void gaps between formations.
- Island edges become fractured and stair-stepped rather than smooth.
- Occasional shallow charcoal-colored seams appear in the ground, never as a full black surface.
- Small shelves and split columns create sightline breaks for the End Ruin and Void Stalker.
- No large mountains, custom floating continents, or dramatic terrain replacement in the first slice.

### Terrain formations

The first generation pass should use three readable formations:

- **Broken Shelf:** A vanilla End stone shelf with a missing underside and a narrow bridge-like connection.
- **Quiet Fissure:** A 1–2 block-wide dark seam that terminates in the island rather than becoming a glowing chasm.
- **Resonant Outcrop:** A small cluster of End stone and muted violet mineral faces that acts as a visual hint, not a waypoint.

These formations should be added around vanilla terrain rather than replacing the vanilla End biome source or noise generator.

### Vegetation and surface life

End Wastes should be nearly barren:

- No trees, grass fields, or generic alien forest.
- One future surface motif, **hollow lichen**, may cling to vertical End stone faces in small 1–3 block clusters.
- Hollow lichen is gray-lavender, matte, and cup-shaped; it does not glow continuously.
- Chorus plants remain a vanilla feature where vanilla generation places them. End Wastes should not be carpeted with a replacement plant.

### Blocks

First-slice environmental blocks:

- Vanilla End Stone: foundation and visual anchor.
- Vanilla End Stone Bricks: ruin construction and repaired edges.
- End Gray: weathered structural accent.
- Resonant Slate: muted charcoal block with a subtle violet seam; reserved for the ruin mechanism and rare outcrops.
- Dormant Resonant Crystal: pale lavender/desaturated cyan inclusion used sparingly, not as a full bright block.

The actual first implementation should introduce only the blocks required by the approved structure and material test. No block should exist only to add visual noise.

### Particles and ambient effects

- Ambient particles: one small gray-violet mote every few seconds near resonant outcrops.
- Resonance particles: desaturated cyan pixels appear only near an active source or when the Lens is being used.
- No constant purple fog, full-screen tint, or particle storm.
- End Wastes ambience has longer quiet intervals than vanilla End ambience, with occasional low, distant tonal movement.

### Colors, lighting, and atmosphere

- Base: End Stone Cream, End Gray, Charcoal.
- Depth: Deep Violet and Void Black only in recesses.
- Signal: Desaturated Cyan for dormant clues; Resonance Cyan only for active events.
- Rare history: Ancient Gold only on the ruin mechanism or one relic detail.
- Lighting remains compatible with vanilla End lighting. Any custom light should be low-level and localized.
- Fog should preserve silhouettes and maintain the sensation of open void.

### Generation frequency and vanilla relationship

- End Wastes should occupy approximately 8–12% of eligible outer End biome samples in the first tuning pass.
- The central End island and the immediate Dragon arena remain vanilla End.
- The End Wastes biome source is an extension of vanilla selection, not a complete biome-source replacement.
- End terrain noise, island height logic, Dragon behavior, Endermen, and vanilla End structures remain unchanged.
- End Ruins should be much rarer than the biome: approximately one candidate per 6–10 End Wastes chunks, subject to spacing and terrain checks.

---

## 2. Void Shard

### Appearance and material identity

Void Shard is a fractured End material: dense, dark, and mineral rather than a glowing purple crystal. It looks like a sliver broken from a deeper resonant deposit. Its most important visual feature is the silhouette, followed by one internal seam that catches cold light.

### Lore and function

Void Shards are inert fragments of a material that can hold a resonance pattern. They are not fuel and are not ordinary ore. A shard is a physical remnant of an older End mechanism and becomes useful only when paired with an interpretive instrument such as the Resonance Lens.

### Rarity and acquisition

- Rare End Ruin loot, with a small chance for a second shard in a hidden compartment.
- Very rare drop from an activated End Ruin mechanism.
- Not obtained from ordinary End stone mining.
- Not available in the first slice from a renewable farm.

### Crafting uses

- Primary use: crafting the Resonance Lens.
- Secondary future use: resonant blocks and progression artifacts.
- The first slice should not consume every shard automatically; the player should retain the feeling that the material is scarce.

### Visual effects and relationship to Resonance

- In inventory: completely static and matte except for a readable pale facet.
- When held near an active source: one internal cyan seam briefly brightens.
- When dropped: no permanent glow; an occasional tiny desaturated mote is acceptable near an active source only.
- The shard itself is dormant. Active Resonance comes from context, not from a permanently neon item.

### Exact 16x16 texture specification

**Canvas:** 16x16 pixels, RGBA PNG.  
**Background:** Every pixel outside the silhouette is alpha 0.  
**Edge alpha:** All visible pixels alpha 255. No semi-transparent pixels.  
**Scaling:** Nearest-neighbor only.

**Silhouette:**

- An asymmetric, tapered shard angled from upper-right toward lower-left.
- Bounding box approximately 9x13 pixels, leaving intentional transparent breathing room.
- One pointed tip, one wider fractured shoulder, and one uneven lower break.
- No rectangular backing, circular gem shape, or full black outline around empty space.

**Pixel placement philosophy:**

- The outer contour uses clustered charcoal pixels and stepped diagonals.
- The interior is divided into two or three broad planes, not a noisy mosaic.
- The left/lower plane is darker and slightly cooler.
- The upper/inner plane is lighter and connected to vanilla End stone through a cream facet.
- One short desaturated-cyan seam is placed inside the material, never on the complete edge.
- One Ancient Gold pixel or two-pixel inclusion is allowed only if it reads as an embedded relic trace; it must not look like random decoration.

**Palette:**

- Charcoal `#26232B`: silhouette and deepest material plane.
- Deep Violet `#312A3D`: shadow plane.
- Muted Violet `#5E526E`: body plane.
- End Stone Cream `#D8D0B4`: fractured mineral facet.
- End Gray `#77747D`: weathered transition.
- Desaturated Cyan `#7EA7A6`: dormant resonance seam.
- Optional Ancient Gold `#C6A85A`: one embedded historical inclusion.
- Optional Pale Lavender `#C4BBCD`: one controlled reflected highlight.

**Intended read:** At 16x16 it should read first as a sharp rare mineral fragment, second as an Endesium material, and only then reveal its resonance seam. If the cyan or gold is noticed before the silhouette, the texture is too bright.

---

## 3. Resonance Lens

### Appearance

The Lens is a small, dark-framed instrument with a pale mineral aperture. It should read as a tool for interpretation, not as a compass or weapon. Its center contains a muted violet lens with a single cyan glint when active.

### Crafting recipe

A shaped recipe using:

- 4 Void Shards at the cardinal points.
- 1 Ender Eye in the center.
- Empty corners and center-adjacent spaces.

The recipe communicates that the Lens is built from scarce End material around a vanilla End navigation component, but it does not make the item a coordinate device.

### Interaction

- Use the Lens while holding it in either hand.
- The server evaluates the nearest active ResonanceSource in the End.
- The client receives only a bounded signal state, not exact source coordinates.
- A 20-tick base activation cooldown prevents rapid polling.
- The Lens can be used repeatedly and is not consumed.
- It does not function in the Overworld or Nether except to display a quiet inactive response.

### Detection radius and strength

- Maximum useful detection radius: 96 blocks.
- Preferred signal range: 16–64 blocks.
- Occlusion reduces strength but does not create exact wall-penetrating direction data.
- Strength is calculated from distance and source state:

`strength = clamp((96 - distance) / 80, 0, 1) * sourceStrength * visibilityFactor`

- `sourceStrength` for the first End Ruin is 1.0 while its mechanism is dormant but discoverable, and 1.35 during activation.
- `visibilityFactor` is between 0.35 and 1.0 and is calculated server-side from nearby obstruction and source state.

### Signal behavior

| Distance | Signal | Player interpretation |
|---|---|---|
| More than 96 blocks | No pulse, or one very quiet pulse every 8 seconds if the player is facing the correct broad sector | Something may exist far away, but the Lens is not a locator yet. |
| 64–96 blocks | One muted pulse every 4 seconds; slight pitch variation based on broad horizontal bearing | The player can choose a direction and test again, but receives no arrow. |
| 32–64 blocks | Two pulses separated by a short pause every 3 seconds; desaturated cyan mote near the Lens | The player knows the signal is becoming meaningful and should search the landscape. |
| 16–32 blocks | Three-pulse cadence every 2 seconds; stronger hand shimmer and directional stereo bias | The player can triangulate by turning and comparing cadence, not by reading coordinates. |
| 0–16 blocks | Rapid but restrained pulse; a brief cyan seam appears on the Lens and nearby mechanism | The source is close enough to find by looking for the ruin or mechanism. |

The Lens never displays a compass needle, waypoint, coordinates, or destination distance. During the brief, server-validated mechanism activation only, a white particle line may travel from the Lens to the mechanism as a confirmation effect; it is not a locator beam and does not persist.

### Player feedback

- Hand animation: a small lift and settle, not a dramatic weapon swing.
- Particles: 1–4 square cyan motes at high strength, never a cloud.
- Sound: low resonant click plus a pitched pulse; the pitch rises with strength.
- Action bar: no numerical distance. At most, a qualitative message after repeated use: `The lens is quiet`, `The lens is listening`, or `The lens is answering`.

### Multiplayer behavior

- Detection and source state are server authoritative.
- Each player receives their own signal based on position and line of sight.
- One player's activation does not reveal coordinates to other players.
- Source cooldowns are tracked per player for Lens use, while the End Ruin activation state is shared.
- Client packets contain bounded signal strength, pulse phase, and broad direction bucket only.

---

## 4. End Ruin

### First structure

The End Ruin is a small, hand-authored observation-and-mechanism site, not a castle, temple, or fantasy shrine. It is a broken instrument station built into an End island shelf.

### Dimensions and approach

- Footprint: 13 blocks wide x 9 blocks deep.
- Maximum height: 7 blocks above the local island surface.
- One side is open to the void, exposing the broken foundation.
- The approach presents a partial silhouette first: two leaning End stone-brick pillars, a low broken wall, and a single muted cyan seam visible only from certain angles.
- There is no giant beacon or vertical marker.

### Architectural language and palette

- Foundation: vanilla End stone and End stone bricks.
- Weathering: End Gray and Charcoal recesses.
- Inset material: Deep Violet and Muted Violet.
- Quiet inscription: Pale Lavender in a sparse three-mark pattern.
- Mechanism: Resonant Slate, one Resonance Cyan focal seam, and one controlled Ancient Gold contact.
- Broken edges use stepped missing blocks and asymmetry, not random rubble piles.

### Rooms and spaces

1. **Broken approach shelf:** a 3-block-wide entry path with a missing side and a view into the void.
2. **Outer ring:** a roughly rectangular chamber with two intact corners and two collapsed corners.
3. **Mechanism chamber:** a 5x5 interior with a low central resonant plate.
4. **Hidden crawlspace:** a one-block-wide side cavity behind a damaged panel, visible through an unusual pale-lavender mark.
5. **Open observation side:** the ruin deliberately faces empty End space, implying that it was built to watch or listen rather than defend.

### Resonant mechanism

The mechanism is a low circular or square plate set into the floor, not a chest pedestal. It has three inactive sockets and one central seam. The Lens produces its strongest signal within 16 blocks. The player activates it by using the Lens on the plate after finding the correct approach, not by pressing a quest button.

### Loot

- Guaranteed: 1 Void Shard.
- Common: vanilla End stone-related material or a small amount of Ender Pearl equivalent.
- Uncommon: second Void Shard, paper-like observation fragment, or an Ender Eye component.
- Hidden crawlspace: one Ancient Gold fragment or rare lore item, with a low chance.
- Loot should be modest; the discovery, signal, and mechanism are the primary reward.

### Environmental storytelling

The arrangement implies that the builders were measuring the void and recording resonance, not worshipping it. The observation side faces outward. The mechanism is partially repaired, suggesting the ruin was revisited after its original construction. The hidden crawlspace contains evidence that someone tried to preserve a fragment after the station failed.

### Frequency and variations

- Rare placement in End Wastes only.
- Minimum spacing prevents neighboring ruins from forming a settlement.
- Three approved variations: clockwise rotation, mirrored broken corner, and alternate mechanism inset.
- All variations retain the same readable silhouette and one hidden discovery space.

### Player experience inside

Entering the ruin should make the End quieter for a moment. The player sees a low floor plate, hears a faint pulse, and notices that one wall mark is not decoration: its spacing matches the Lens pulse cadence. The player is rewarded for looking around, not for following a visible marker.

---

## 5. Void Stalker

### Appearance

The Void Stalker is a tall, slender, non-human biped. It may create the same unease as a distant slender humanoid silhouette, but it must not be an Enderman, Shulker, Phantom, or a humanoid reskin.

- Height: approximately 3.0–3.5 blocks.
- Width: approximately 0.75–1.0 blocks across the torso and shoulders.
- Body: narrow vertical torso with a slightly hunched forward posture, long center of mass, and a restrained rear resonance structure.
- Legs: two long, jointed, digitigrade legs with narrow lower sections and clear grounded feet.
- Arms: two unusually long, jointed forelimbs that hang below the torso and end in compact hooked claws; they must remain visibly non-human in proportion and joint direction.
- Head: compact wedge or faceted mask integrated into a short neck, not a featureless humanoid cube.
- Eyes: two small pale-lavender points that become cyan only during attack commitment.
- Mouth: a narrow horizontal split visible during attack anticipation, not a permanent open monster mouth.
- Secondary structure: one short rear sensing filament or split dorsal appendage that reacts to resonance; it must not resemble a phantom wing.
- Silhouette: tall, narrow, angular, asymmetrical, and predatory. The player should recognize the head, torso, hanging arms, long legs, and forward direction at a distance.
- Model texture: 32x32 RGBA creature texture, with no full-body glow.

This is an intentional revision of the original low quadruped direction. The creature remains distinctly Endesium through mineral anatomy, non-human joints, sparse resonance signals, and a non-Enderman head and posture.

### Creature palette

- Charcoal and Deep Violet for the body.
- End Gray for worn plates.
- Muted Violet for joint recesses.
- Pale Lavender for eyes at rest.
- Resonance Cyan only in the eye/core during attack or repositioning.
- No purple aura and no bright outline.

### Animation list

| Animation | Duration | Movement and intent |
|---|---:|---|
| Idle listen | 2.4 s loop | Narrow torso suspends and shifts one pixel, head makes a small scan, rear filament follows late. |
| Observe | 1.2 s | Head locks onto target, long arms lower asymmetrically, body becomes still. |
| Walk | 0.8 s loop | Long alternating leg stride with a delayed arm swing; posture remains eerie and controlled. |
| Run/pursuit | 0.55 s loop | Narrow torso leans forward, long legs extend, arms trail with readable acceleration. |
| Attack anticipation | 0.45 s | Mouth seam opens, shoulders draw back, cyan eye signal appears. |
| Attack impact | 0.25 s | One elongated claw thrusts forward; damage occurs at the impact frame. |
| Attack recovery | 0.5 s | Claw retracts, spine settles, short vulnerability window. |
| Hurt | 0.3 s | Side recoil and stagger; cyan signal extinguishes. |
| Reposition | 0.7 s | Tall body compresses into a narrow dark-violet silhouette, two cyan pixels remain, then it unfolds at the new position. |
| Death | 1.1 s | Long limbs release and fold inward; particles fall toward the torso rather than exploding outward. |

### AI behavior state machine

```text
IDLE_LISTEN
  -> AWARE when a player enters 32 blocks or makes a loud interaction
  -> OBSERVE when line of sight is acquired
  -> SEARCH when line of sight is lost

OBSERVE
  -> POSITION if target is outside preferred range
  -> ATTACK_PREP if target is within attack range
  -> REPOSITION if target is too close for more than 2 seconds
  -> SEARCH if line of sight is lost for 3 seconds

POSITION
  -> OBSERVE when 6–10 blocks away
  -> REPOSITION if target blocks or corners repeatedly
  -> SEARCH when target is lost

ATTACK_PREP
  -> ATTACK if anticipation completes and target remains in range
  -> POSITION if target leaves range

ATTACK
  -> RECOVER after impact
  -> REPOSITION if the attack misses and target backpedals

RECOVER
  -> OBSERVE after 0.5 seconds
  -> RETREAT if health is below 30%

REPOSITION
  -> OBSERVE after a short side/rear blink
  -> RETREAT if the destination is unsafe

RETREAT
  -> OBSERVE after reaching 12–16 blocks
  -> SEARCH if target is lost
```

### AI values

- Detection range: 32 blocks.
- Preferred combat distance: 6–10 blocks.
- Attack range: 2.6 blocks.
- Health: 24 points.
- Contact damage: 4 points before difficulty scaling.
- Movement speed: 0.28 base; pursuit speed approximately 0.34.
- Attack cooldown: 24 ticks, with a readable anticipation phase.
- Knockback resistance: 0.35, enough to avoid trivial stun-locking but not enough to ignore shields or knockback tools.
- Reposition cooldown: 160 ticks, with a minimum of 80 ticks after a failed reposition.
- Reposition distance: 8–14 blocks to a side or rear position, never through solid blocks and never directly onto the player.
- Reposition requires a valid destination and line-of-sight check; it is not unconditional teleport spam.
- Target selection: nearest survival-mode player who enters detection range, with existing target retained until lost or dead.
- Despawn: normal hostile-mob distance rules; persistent only if named or manually placed.
- Spawn: End Wastes only, low weight, on valid solid End terrain below the normal hostile light threshold. No central End island spawning in the first slice.
- Drops: one Resonance Lens, plus a low chance of Void Shard. The creature must not become the primary shard farm.

The reposition behavior exists specifically to stop continuous backward walking from being a complete strategy, while retaining readable cooldowns and counterplay.

---

## 6. Resonance system

### ResonanceSource

A ResonanceSource is a small, server-owned description of a discoverable signal:

- Stable source ID.
- Dimension.
- Position or bounded area.
- ResonanceType.
- Base strength.
- Detection radius.
- Active/dormant state.
- Activation cooldown.
- Optional source-specific signal profile.

The first source is the End Ruin mechanism. Future sources can be structures, blocks, artifacts, portals, mobs, or world events without changing Lens behavior.

### ResonanceType

Initial types:

- `DORMANT_RELIC`: quiet, intermittent signal from an inactive ruin.
- `ACTIVE_MECHANISM`: stronger pulses during activation.
- `CREATURE_TRACE`: reserved for future Void Stalker behavior and not required for the first implementation.

### ResonanceManager

The manager is a server-side service scoped to a server level/dimension. It should:

- Register and unregister source records.
- Find the strongest eligible source for a player.
- Calculate bounded signal strength and direction bucket.
- Enforce per-player Lens cooldowns.
- Enforce shared source activation cooldowns.
- Provide a small serializable state for persistent ruin activation.
- Never send exact coordinates to the client.

This is intentionally smaller than a general quest system. It is a source registry plus server-authoritative signal calculation.

### Detection and activation

- The manager checks sources within 96 blocks of the player.
- Distance, source state, obstruction, and source strength determine the result.
- The Lens receives a qualitative pulse packet, not a world position.
- A source can be dormant but detectable before activation.
- Activation requires the player to be close and use the Lens on the mechanism.
- Once activated, the ruin source changes state for all players in that End Ruin.

### Client effects

The client renders only the response:

- Pulse timing.
- A small number of square particles.
- Lens seam brightness.
- Sound pitch and volume.
- Optional action-bar phrase.

The client never decides whether a source exists and never grants the reward.

---

## 7. First Resonance discovery sequence

1. **Find the End Wastes.** The player travels beyond the central island and notices sparse broken shelves, altered silence, and one unusual mineral outcrop.
2. **Notice the ruin.** The End Ruin is visible only as a low broken station; there is no beacon, map marker, or floating text.
3. **Recognize the anomaly.** A floor plate and wall marks form a repeated spacing pattern. The Lens recipe becomes meaningful because the player has found a Void Shard in the ruin or carried one from earlier exploration.
4. **Use the Lens.** At first the response is quiet. The player experiments with distance and facing rather than receiving a coordinate.
5. **Interpret the signal.** Pulse cadence increases near the mechanism. The player notices that the strongest signal is not necessarily at the tallest ruin block; it is at the low resonant plate.
6. **Locate the mechanism.** Within 16 blocks, the Lens seam flashes cyan and the plate emits a restrained pulse.
7. **Activate it.** The player uses the Lens on the plate. The server validates the source, position, item, cooldown, and activation state.
8. **Trigger the response.** A short cyan pulse travels through the ruin's seams, one Ancient Gold contact lights for less than two seconds, and a low chord resolves into silence. No explosion or permanent neon beacon occurs.
9. **Receive the reward.** The mechanism opens the hidden crawlspace or reveals a small compartment containing a second Void Shard or a lore fragment. The reward is physical and discoverable.
10. **Earn the advancement.** The advancement is granted when the player first enters End Wastes or completes the first validated resonance discovery, with the selected trigger fixed during implementation. The preferred final trigger is successful mechanism activation because it rewards the full discovery rather than biome contact alone.
11. **Decide what to do next.** The player is left with a material, a partial message, and evidence that the End contains more listening stations, without being sent to a new dimension or a quest marker.

---

## 8. Unified visual identity

### Primary colors

End Stone Cream, End Gray, Charcoal, and Deep Violet establish the material world. They should occupy most pixels and most block faces.

### Secondary colors

Muted Violet, Pale Lavender, Desaturated Cyan, and Muted Magenta distinguish alien material, weathering, and dormant resonance without making every object bright.

### Accent colors

Resonance Cyan means active energy. Ancient Gold means rare historical significance. Pale White is a one-pixel discovery highlight. Accents are sparse and must have a gameplay or lore meaning.

### Shadow colors

Void Black is reserved for deep recesses and silhouette separation. Charcoal is the normal structural shadow. A black fill is never a substitute for transparent alpha.

### Creature colors

Charcoal, Deep Violet, End Gray, and Muted Violet form the Void Stalker body. Pale Lavender is its resting eye signal. Resonance Cyan appears only in attack/reposition states.

### Architectural colors

End Stone Cream foundations, End Gray weathering, Charcoal recesses, Deep Violet insets, Pale Lavender inscriptions, and one Ancient Gold mechanism detail.

---

## 9. Audio identity

Audio should preserve silence and distance as active parts of the End atmosphere.

| Cue | Design |
|---|---|
| End Wastes ambient | Sparse low wind-like void tone with long silence; no constant pad. |
| Void Shard pickup | Short dry mineral click with a low tail. |
| Lens activation | Two-part glass/stone click followed by a muted pitched pulse. |
| Resonance pulse | Low fundamental plus a pale upper harmonic; pitch rises with strength. |
| Ruin activation | One short chord resolving downward into silence, with a single metallic contact sound. |
| Void Stalker idle | Very quiet plate movement and distant dry scrape, infrequent and positional. |
| Void Stalker attack | Short inhalation-like scrape during anticipation, then a sharp impact click. |
| Void Stalker reposition | Compressed low pulse with a brief reversed tail; never a loud magical teleport. |
| Void Stalker hurt/death | Material-specific crack and collapse, not a generic fantasy roar. |

No constant music is required. Silence before and after a resonance event is part of the reward.

---

## 10. Pixel-art and asset rules

- Item textures: exactly 16x16 RGBA PNG.
- Mob textures: 32x32 RGBA unless a later Blockbench test proves another size is clearer.
- Actual alpha 0 outside silhouettes.
- Opaque alpha 255 for ordinary pixels.
- Semi-transparency is prohibited for the first item set.
- Nearest-neighbor scaling only.
- No anti-aliasing, gradients, noisy dithering, AI-generated raster imagery, or random accent pixels.
- Every texture must pass a dimension, alpha, palette-count, and semi-transparency check.
- Every item must be reviewed at inventory, dropped-item, and first-person scales.
- Textures that are unreadable at one scale return to DogSprite for revision before implementation.

### Block transparency rule

Any future block texture containing transparent pixels must use an appropriate cutout render layer. Opaque block rendering must never be used to infer transparency from item rendering. Fully opaque blocks should remain on the default solid layer.

---

## 11. Player experience

The player enters the familiar End and initially sees exactly the kind of emptiness they expect. After traveling outward, the island geometry becomes more fractured and the atmosphere becomes quieter. Pale End stone shelves lead toward a low silhouette that could almost be ordinary ruin debris.

As the player approaches, two broken pillars and a partial wall frame the void. There is no glowing marker. A small muted mineral seam catches the eye only when the player turns. Inside, the ruin is compact and practical: a broken observation station with a low plate, unusual wall spacing, and one blocked cavity.

The Void Stalker is first seen crouched on a separated shelf rather than spawning directly in front of the player. It watches, moves to a preferred distance, and only attacks after the player has acknowledged it. Its side reposition prevents effortless backpedaling, but its cooldown and anticipation remain readable.

The player uses the Resonance Lens and initially hears almost nothing. Repeated tests reveal a pulse cadence. The player follows the changing rhythm across the ruin instead of following an arrow. Near the plate, the Lens seam brightens, the room answers with a few cyan motes, and the mechanism accepts the Lens.

The activation response is brief: seams illuminate, a single ancient contact flashes gold, a chord resolves, and a hidden compartment opens. The reward is a Void Shard and a fragment of evidence that this was one of several stations. The advancement confirms the discovery without explaining everything. The player leaves with a material, a creature memory, and a question rather than a completed checklist.

---

# Exact implementation asset plan

## Asset list

- End Wastes biome definition and biome palette references.
- Resonant Slate block texture and model.
- Dormant Resonant Crystal block texture and model.
- Void Shard 16x16 item texture.
- Resonance Lens 16x16 item texture.
- End Ruin structure templates for three approved variations.
- End Ruin mechanism block/model.
- Void Stalker 32x32 texture.
- Void Stalker GeckoLib geometry model.
- Void Stalker GeckoLib animation file.
- End Wastes ambient sound.
- Void Shard pickup sound.
- Lens pulse and activation sounds.
- Ruin activation sound.
- Void Stalker idle, attack, reposition, hurt, and death sounds.
- Dormant mote particle.
- Resonance pulse particle.
- Activation response particle.
- Advancement icon and translations.

## Exact item texture list

1. `void_shard.png` — 16x16, asymmetric fractured mineral sliver.
2. `resonance_lens.png` — 16x16, dark frame with pale aperture and one restrained cyan glint.

Each must be generated or refined with DogSprite MCP and pass the alpha and palette validation before being added to Minecraft resources.

## Exact Blockbench model list

1. `void_stalker_v2.geo.json` — approved tall, slender, non-human biped production model. The retained low quadruped export is reference-only.
2. `end_ruin_mechanism.geo.json` — low floor mechanism with sockets and central seam.
3. Optional `resonant_crystal.geo.json` — only if a block model cannot communicate the crystal silhouette clearly.

Vanilla-style cube blocks should not use Blockbench geometry when a normal block model is clearer.

## Animation list

- `animation.void_stalker.idle_listen`
- `animation.void_stalker.observe`
- `animation.void_stalker.walk`
- `animation.void_stalker.run`
- `animation.void_stalker.attack_anticipation`
- `animation.void_stalker.attack_impact`
- `animation.void_stalker.attack_recovery`
- `animation.void_stalker.hurt`
- `animation.void_stalker.reposition`
- `animation.void_stalker.death`

## Sound list

- `ambient.end_wastes_low`
- `item.void_shard.pickup`
- `item.resonance_lens.activate`
- `item.resonance_lens.pulse_low`
- `item.resonance_lens.pulse_high`
- `block.end_ruin_mechanism.activate`
- `entity.void_stalker.idle`
- `entity.void_stalker.attack`
- `entity.void_stalker.reposition`
- `entity.void_stalker.hurt`
- `entity.void_stalker.death`

## Particle list

- `end_wastes_mote` — gray-violet dormant mote.
- `resonance_pulse` — small desaturated-cyan square pulse.
- `resonance_active` — restrained cyan seam response.
- `ruin_gold_contact` — one-second Ancient Gold mechanism spark.
- `void_stalker_trace` — rare dark-violet reposition residue.

## Gameplay state machine

### End Ruin source

```text
DORMANT_HIDDEN
  -> DORMANT_DETECTABLE when the player enters the source radius
  -> DISCOVERED when the player observes or enters the ruin

DORMANT_DETECTABLE
  -> SIGNALING when the Lens is used within 96 blocks
  -> ACTIVATION_READY when the player reaches the mechanism

ACTIVATION_READY
  -> ACTIVATING when the Lens is used on the mechanism
  -> DORMANT_DETECTABLE when the player leaves the area

ACTIVATING
  -> ACTIVE after server validation and response sequence

ACTIVE
  -> ACTIVE permanently for the first slice, with future support for cooldown/reset states
```

### Void Stalker

```text
IDLE_LISTEN -> AWARE -> OBSERVE -> POSITION -> ATTACK_PREP -> ATTACK -> RECOVER
                    |          |                    |             |
                  SEARCH <-----+                 REPOSITION <-----+
                    ^                               |
                    +-------------------------------+
                         RETREAT when badly injured
```

## Implementation dependencies

- Minecraft Java Edition 1.21.1.
- Fabric Loader and Fabric API versions already locked by the foundation.
- Java 21.
- Fabric Loom and official Mojang mappings already locked by the foundation.
- GeckoLib 4.9.2 for Void Stalker animation and rendering.
- DogSprite MCP 4.0.0 for item pixel art.
- Blockbench MCP for geometry and animation authoring.
- A server-authoritative source registry and bounded client signal packet.
- No new cloud service, API key, paid art application, or external gameplay framework.

## Implementation order

1. Approve this specification and the visual palette.
2. Approve the Void Shard and Resonance Lens concept textures at all three Minecraft viewing scales.
3. Author and validate the first material blocks.
4. Author the End Ruin structure and its three controlled variations.
5. Implement the single End Ruin ResonanceSource and persistent activation state.
6. Implement the Lens signal behavior and server-authoritative networking.
7. Implement Void Shard and Lens item behavior.
8. Implement the End Wastes extension using the approved terrain relationship.
9. Author the Void Stalker silhouette in Blockbench.
10. Author, export, and validate Void Stalker animations and sounds.
11. Implement the Void Stalker AI state machine and GeckoLib renderer.
12. Add the first Resonance advancement and reward.
13. Run dedicated-server, client, resource, multiplayer, and fresh-world verification.
14. Only after the complete slice passes review should further End content be designed.

No implementation should begin before the asset and visual review gates are approved.
