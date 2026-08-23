# Roadmap

## Done

- Technical foundation and visual design.
- First vertical slice (End Wastes, Resonance, Resonance Lens, End Ruin, Void
  Stalker, First Resonance).
- Geography expansion (End Wastes + Chorus Wilds, transitions, ecology).
- Exploration/structure expansion (ruin variants, Shattered Spire, resonance
  chain, environmental storytelling).
- QA stabilization pass.
- 10x overhaul: landmarks, bosses, regional ecology, post-Dragon transformation.
- Polish pass: mob AI/boss combat bug fixes (flying move controls, warden
  cooldowns that never ticked down, golem self-stagger loop, beam-sweep spam),
  all 16 particle types rendering with region tints, hurt animations wired on
  every creature, mechanical tooltips on all utility items, canonical creative
  tab, dev-only gating for state-mutating commands, resonance cache pruning.
- **Structures migration:** flagships and landmarks are now registered vanilla
  Structures (`endesium:flagship` / `endesium:landmark` types + twenty JSON
  entries in two random_spread structure sets). Chunk ownership, bounding-box
  clipping through `StructurePlacement` piece mode, native `/locate structure`
  support, and anchor-chunk generation with per-site seam/support diagnostics.
  The old Feature lattice (`biome_structure`/`biome_landmark`) is retired from
  all biome JSONs.
- Dragon visual arc: stage-assembled regalia rig (horns, neck bands, dorsal
  plates, mantle, braces, tail crown) plus an emissive chest core model with a
  dedicated glow texture; visibility driven per combat phase via the renderer
  mixin.
- Pure-logic test suite (JUnit): resonance direction buckets, void-anchor
  expiry, dragon phase thresholds, arena fracture-distance math.

## Not started (future milestones, planned separately)

- Deep End.
- Additional major biomes.
- Full custom dragon *body* replacement (the staged regalia overlay is in;
  replacing vanilla geometry outright should wait for an in-game bone-alignment
  pass against a live client).
- True 3D hero item models (wings/sword/lens/compass) - sprite polish is
  complete; geometry authoring is an art milestone.

## Verification status after the structures migration

Verified headlessly: registry load, `/locate structure` resolving flagship and
landmark ids in the End, chunk forceload without errors, clean logs. The final
block-level confirmation (structure blocks present at a located site) needs one
more `gradlew runServer` session; generation diagnostics now log exactly why a
site is skipped if it is ever rejected.

## Scope discipline

The final stop condition for every milestone is: when the current system is
stable, visually distinct, performant, and integrated with the vertical slice,
stop. Do not start the next major feature until it is planned separately.
