# Design Decisions

Recorded rationale for non-obvious choices. Each entry names the decision, the
alternatives considered, and why the chosen path was taken.

## ADR-001: Extend vanilla End rather than replace it

- **Decision:** layer Endesium biomes on top of the vanilla End biome source
  instead of replacing the dimension generator.
- **Alternatives:** a separate dimension, or a full End generator replacement.
- **Why:** the vertical slice must keep the Dragon fight, gateways, End Cities,
  chorus, and Endermen intact. A mixin overwrite of `getNoiseBiome` is the
  smallest change that adds Wastes/Wilds without touching vanilla systems.

## ADR-002: Overwrite `getNoiseBiome` instead of injecting its return

- **Decision:** overwrite `TheEndBiomeSource.getNoiseBiome`.
- **Alternatives:** a `RETURN` injection.
- **Why:** the return injection fired intermittently between fresh server
  starts, so the modded biomes sometimes never entered the memoized
  possible-biome set. An overwrite runs the same code path deterministically,
  regardless of which constructor built the source.

## ADR-003: Capture the seed, not per-chunk randomness

- **Decision:** capture the world seed once via a mixin and derive biome
  assignment from it.
- **Why:** deterministic, seed-dependent generation that is identical on client
  and server, with no per-tick or per-chunk random state.

## ADR-004: Structures are features, not registered structures

- **Decision:** implement ruins and the spire as configured/placed features.
- **Why:** features place per-chunk deterministically and keep the mod's
  structure count small. This also means `/locate structure` does not list them,
  which preserves the discovery feel.

## ADR-005: Resonance is server-authoritative and qualitative

- **Decision:** the server computes a bounded qualitative reading; the client
  never receives coordinates.
- **Why:** prevents coordinate leaks between players and keeps discovery
  server-authoritative.

## ADR-006: Prototype assets are retained, not deleted

- **Decision:** keep `void_stalker_v1_reference` and `foundation_test_*`
  textures as documented references.
- **Why:** they are useful for troubleshooting the Blockbench export history and
  are explicitly documented as non-production. Deleting them risks losing
  reference material without a strong reason.

## ADR-007: Minimal custom commands

- **Decision:** register zero custom commands.
- **Why:** no command surface means no input-validation, permission, or cheat
  surface to maintain. Vanilla commands (`/locate`, `/give`, `/summon`) suffice
  for testing.
