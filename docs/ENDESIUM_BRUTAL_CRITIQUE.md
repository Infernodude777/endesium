# Endesium — brutal release critique and remediation plan

**Audit date:** 2026-08-18  
**Scope:** current Java source, biome/worldgen JSON, generated assets, recipes,
loot, client entrypoints, Dragon hooks, existing QA reports, and local build
validation.  
**Standard:** judge the shipped player experience, not the number of classes or
successful Gradle tasks.

## Executive verdict

Endesium is ambitious, distinctive, and technically more organized than most
first-pass Minecraft mods. Its strongest qualities are the restrained End
identity, server-side resonance model, persistent post-Dragon state, and clear
material language.

It is **not release-ready as a polished public 1.0**.

The central problem is not that there is too little content. There is too much
content whose player-facing contracts are not yet proven together. Ten biomes,
multiple structure families, dozens of items, nine ecology entities, a guidebook,
custom Elytra behavior, and a staged Dragon system create a large regression
surface. The project has stronger static validation than interactive evidence.

### Honest scorecard

| Area | Score | Critic's reading |
|---|---:|---|
| Identity | 8/10 | Clear, unusual, and coherent in documents and palette. |
| Code organization | 6/10 | Good separation, but too many milestone layers remain alive together. |
| Worldgen confidence | 5/10 | Deterministic and guarded, but Feature-based cross-chunk writes are risky. |
| Player progression | 5/10 | The intended loop is good; density, discoverability, and reward pacing are unproven. |
| Dragon fight | 5/10 | Ambitious and now better gated, but still mostly untested choreography. |
| Visual finish | 4/10 | Resource-complete does not mean visually bespoke or attractive. |
| Multiplayer confidence | 3/10 | Server-authoritative by design, not meaningfully live-tested. |
| Release readiness | 4/10 | A strong development milestone, not a finished mod. |

## Severity table

| ID | Severity | Finding | Status |
|---|---|---|---|
| B-01 | P0 | No live client, multiplayer, or audio evidence can certify the advertised experience | Open; cannot be honestly solved by static code alone |
| B-02 | P0 | Large landmarks are hand-written Features that write across chunk boundaries | Mitigated with spacing and protected writes; not proven safe |
| B-03 | P1 | Structure coverage is possible, not guaranteed; support rejection can erase a biome's intended content | Countered with deterministic per-biome landmark slots; generation scan still open |
| B-04 | P1 | The project still contains multiple overlapping worldgen generations and legacy feature paths | Countered in active biome JSON; retired Java registrations remain |
| B-05 | P1 | First-fight behavior and staged visual behavior previously contradicted the post-Dragon contract | Countered in this pass: first Dragon now skips custom scheduler and stage transform |
| B-06 | P1 | Biome mechanisms reused ordinary End Ruin variants, which could award Wastes-specific progression for non-Wastes landmarks | Countered in this pass by using `INTACT` for generic biome landmark interactions |
| B-07 | P1 | Void Anchor comments promised a temporary point, but implementation had no expiry or End-only boundary | Countered in this pass with End-only use and a 60-second expiry |
| B-08 | P1 | Many item actions have no user-facing tooltip explaining scope, cooldown, or failure behavior | Partially countered for shared relics; broad item UX remains open |
| B-09 | P1 | “Custom models” are mostly 2D item sprites with clean transforms, not bespoke 3D assets | Open art work |
| B-10 | P1 | Dragon “model stages” are renderer transforms over vanilla geometry, not genuinely different models | Countered with a vanilla-textured 3D armor/crown overlay; full Blockbench replacement still open |
| B-11 | P2 | The ResonanceManager is in-memory and source registration is tick-driven | Countered with immediate first-tick registration; multiplayer lifecycle tests remain |
| B-12 | P2 | Development commands are shipped in the production mod surface | Op-gated and documented, but should be isolated or explicitly marked dev-only |
| B-13 | P2 | Creative inventory injection duplicates large parts of the dedicated tab and vanilla tabs | Open UX cleanup |
| B-14 | P2 | Custom entities emit “No data fixer registered” errors on startup | Functionally harmless today; migration hardening is absent |
| B-15 | P2 | There are no meaningful automated tests for geometry, rewards, item cooldowns, or attack timing | Open test investment |
| B-16 | P2 | Documentation claims are distributed across many milestone-era files | Partially countered by this report; full cleanup remains |
| B-17 | P3 | Rare item/resource balance is not measured, only described | Open playtest/balance work |
| B-18 | P3 | Accessibility and discoverability are intentionally sparse but not yet compensated by reliable feedback | Open client/playtest work |

## Findings and countermeasures

### B-01 — Static validation is being mistaken for player validation

The project can compile, generate data, validate model references, and boot a
server while still having broken first-person transforms, unreadable structures,
poor particle density, clipped guidebook pages, bad audio mixing, or an
exhausting Dragon encounter. The existing reports are transparent about this,
but the volume of “PASS” language makes the project look more finished than it
is.

**Countermeasure:** keep headless gates, but make these explicit release gates:

1. fresh client inventory and first-person item pass;
2. armor and Elytra third-person pass;
3. 32/64/128-block structure silhouette pass;
4. 20-minute audio repetition/volume pass;
5. two-player Lens, reward, Archive, and Dragon pass;
6. reconnect, death, chunk unload, and server restart pass.

This cannot be fully countered by more static assertions. The honest status is
**open until a real client and two players exercise it**.

### B-02 — Feature-based large structures are architecturally fragile

`ShatteredSpireFeature`, `ResonantArchiveFeature`, `ResonantMonolithFeature`, and
`BiomeStructureFeature` place sizeable buildings through ordinary Features.
Several builders write beyond the generating chunk. `StructurePlacement` protects
important blocks, but it is not a bounding-box arbiter and cannot establish
ownership or ordering between neighboring features.

Possible failures include:

- a neighboring chunk being written before its terrain pass finishes;
- two features competing for the same surface;
- a save/reload boundary preserving only part of a large footprint;
- a later feature replacing a non-protected Endesium block;
- generation time spikes caused by repeated height/support scans;
- no standard `/locate structure` or structure bounding box for debugging.

**Countermeasure applied:** deterministic cell spacing, centered candidates,
support thresholds, protected writes, and the existing `StructurePlacement` guard.
This reduces damage but does not prove correctness.

**Required countermeasure:** either migrate flagship landmarks to registered
Minecraft Structures with proper placement, or add a formal cross-chunk feature
contract and a multi-seed region-file scanner that records every block touched by
each candidate. Do not call this solved until that stress test exists.

### B-03 — “Two structures per biome” is an inventory guarantee, not a world guarantee

The source contains at least two named archetypes per custom biome, but one
placed feature chooses a random archetype and can reject the candidate when the
support footprint is unsuitable. A seed can therefore show a biome for a long
time without showing either intended landmark, or can repeatedly roll a small
variant instead of a flagship structure.

This is especially dangerous for progression items hidden in structure loot.
The class inventory satisfies the requirement while the player experience can
still fail it.

**Countermeasure applied:** `BiomeStructureFeature` now assigns a deterministic
four-slot budget from the candidate cell and reserves slots 0 and 1 for the two
named landmarks in every biome. The rarity filter and support check remain, so a
failed candidate can still leave a gap; a multi-seed generation scan is still
required before calling the world guarantee proven.

### B-04 — Worldgen has too many overlapping generations

The current tree contains shared terrain, shared vegetation, shared structure
dispatch, older Wastes formations, older Wilds terrain/vegetation classes,
separate End Ruin generation, and multiple landmark features. Some paths are no
longer wired in the biome JSON; others still run beside the shared pass.

That is expensive cognitive load and makes ordering bugs likely. A future
contributor cannot safely remove a “legacy” class without checking data files,
registration, generated reports, and documentation.

**Countermeasure applied:** the active End Wastes and Chorus Wilds biome JSON
now uses the shared terrain/vegetation/structure dispatch only; their duplicate
legacy placements were removed. The old Wastes scenic feature was also corrected
so it does not silently reject the new surface blocks.

**Remaining cleanup:** the retired feature classes and registry entries still
exist for compatibility with older generated data. They should be quarantined or
removed in a dedicated save-compatibility release rather than deleted casually.

**Required countermeasure:** choose one authoritative path for each concern:

- one terrain sculpt pass;
- one vegetation pass per biome family;
- one ordinary ruin path;
- one flagship landmark path;
- one central arena path.

Then remove or clearly quarantine unused feature classes and update the data
contract. Dead registrations are not free: they preserve ambiguity.

### B-05 — First Dragon contract was inconsistent

The Dragon controller previously ticked on every Ender Dragon while the design
said the transformed fight should be the special challenge. The renderer also
applied stage transforms to the first fight. That made the first milestone
harder and visually different before the player had earned the awakening.

**Countermeasure applied now:**

- `DragonFightController` returns for non-transformed Dragons after clearing
  stale custom zones;
- `EnderDragonRendererMixin` leaves the first Dragon visually vanilla;
- custom staged combat and presentation begin only when `PostDragonState` is
  active.

**Remaining risk:** the awakened fight still needs real timing and balance tests.

### B-06 — Biome landmark progression could award the wrong advancement

The mechanism advancement code treats `FRACTURED` as the Wastes-specific
“fractured station” route. Reusing that variant for biome landmarks such as the
Fallen Spire, Elder Shrine, Skybridge, Crater, and Crystal Landmark would tell a
player they had found a Wastes station in another biome.

**Countermeasure applied now:** the added biome landmark interactions use the
neutral `INTACT` variant unless they have a dedicated named variant. This avoids
false Wastes progression until region-specific advancement contracts exist.

**Required countermeasure:** add a dedicated `BIOME_LANDMARK` contract or
region-specific advancement triggers rather than overloading early ruin variants.

### B-07 — Void Anchor had no expiry despite promising one

The source comments described a temporary 60-second anchor, but the item stored
only coordinates and a bound flag. It also did not restrict use to the End. That
made the item a permanent coordinate bookmark and allowed behavior outside its
intended biome/dimension.

**Countermeasure applied now:**

- binding and recall are End-only;
- bound game time is persisted in item data;
- the point expires after 1,200 ticks;
- expired data is cleared with player feedback.

**Remaining risk:** this behavior needs a save/reload test and a test for old
anchors created before the new timestamp field.

### B-08 — Utility item UX is too implicit

The mod has many tools with cooldowns, durability costs, dimension restrictions,
loaded-signal limitations, or failure conditions. Several individual items do
not explain those constraints in their tooltips. Players will interpret a quiet
result as a bug rather than a deliberate limit.

**Countermeasure:** shared biome relic tooltips now explain action and cooldown.
The remaining tools need concise tooltip contracts:

- dimension/scope;
- cooldown;
- durability or consumption cost;
- what “no signal” means;
- whether it tracks loaded content only.

Do not add lore paragraphs to every item. Add one useful mechanical line.

### B-09 — Resource-complete is not art-complete

The automated audit verifies 63 blocks, 84 items, and 222 model/geometry files,
but most item models are flat sprites using shared transform parents. That is
acceptable for ingredients, not for hero items the player holds or wears.

**Required art countermeasure:** prioritize:

1. Resonant Wings;
2. Void armor silhouette;
3. Void sword and pickaxe;
4. Resonance Lens and Echo Compass;
5. Guidebook cover;
6. one signature item from each of the ten biome families.

Use bespoke 3D or layered models only where the silhouette matters. Do not waste
art time making every block a complex model.

### B-10 — Dragon stages are not genuinely different models

The previous renderer changed scale, pitch, and a subtle pulse while retaining
vanilla Dragon geometry. That was safe and readable, but it did not meet a strict
interpretation of “improve the model in different stages.”

**Countermeasure applied:** the client now registers a named, Blockbench-compatible
`ender_dragon_armor` geometry and renders it as a vanilla-textured 3D overlay. It
adds a fractured crown, dorsal plates, shoulder mantle, wing-root braces, and a
tapered tail crown. The first fight receives a restrained version; awakened
stages scale and animate it more aggressively. Hitboxes and vanilla wing/death
animation remain untouched.

The exposed MCP registry did not provide a Blockbench server, so this pass could
not honestly claim an MCP-generated export. The `.geo.json` is deliberately
structured for direct Blockbench import/refinement once that integration is
available.

**Remaining countermeasure:** replace the overlay with a fully authored custom
Dragon model only after a client-side visual pass confirms bone alignment and
keeps the vanilla fight readable.

- Stage 1: vanilla body and small dormant seam;
- Stage 2: visible wing-edge fractures and core glow;
- Stage 3: controlled seam overlay and broken horn/crown silhouette;
- Stage 4: short-lived resonance crown and stronger chest core;
- Awakened respawn: larger silhouette plus unique overlay.

Keep hitbox and vanilla death rendering intact. Add a packaged client smoke test
before relying on mixin target stability.

### B-11 — Resonance registry lifecycle is runtime-only

`ResonanceManager` stores loaded sources in a per-`ServerLevel` weak map. Block
entities re-register periodically and unregister on removal. This is a sensible
server-authoritative cache, but it is not persisted state. Between chunk load,
first tick, dimension changes, and player use, a valid mechanism can briefly be
silent.

**Countermeasure applied:** the block entity now registers its source on the
first server tick after load, then refreshes on a twenty-tick heartbeat; reward
and activation state remain persistent in the block entity and removal still
unregisters the cache entry. Add tests for chunk unload/reload, server restart,
and immediate Lens use after loading a chunk.

### B-12 — Debug commands are in the production command tree

`/endesium dragonstate set` is op-gated, which is better than an unprotected
cheat, but it is still a production surface that can invalidate a survival
world's progression. It also creates a divergence between normal Dragon death
and test activation.

**Countermeasure:** retain it only if QA needs it, explicitly label it
`development-only`, and consider a build flag or separate dev entrypoint for
release jars. At minimum, log every use and make the command unavailable to
non-development builds.

### B-13 — Creative inventory is overstuffed

The dedicated Endesium tab contains the full catalog, while many of those items
are also inserted into vanilla Ingredients, Tools, Combat, and Building Blocks.
This is useful for discovery during development but noisy for players.

**Countermeasure:** make the dedicated tab canonical. Keep only progression
essentials in vanilla tabs, or remove duplicate injection after the first public
release. Add a development-only “all content” tab if needed.

### B-14 — Entity data-fixer errors are harmless now but still technical debt

Dedicated-server and datagen boots log `No data fixer registered for
endesium:<entity>`. This does not currently crash or prevent saves, but it means
there is no migration path for entity NBT if the entity schema changes.

**Countermeasure:** document the warning as intentional for the current release,
then add entity datafix registrations before a long-lived public world format is
promised. Do not hide the log without providing migration behavior.

### B-15 — Test coverage is mostly scripts and source contracts

There are no meaningful unit tests for:

- structure footprint geometry;
- variant-to-loot mapping;
- one-time reward behavior;
- item expiry and cooldowns;
- Dragon attack timing;
- two-player damage/target selection;
- client rendering mixin compatibility.

**Countermeasure:** add small pure tests first: region direction math, structure
variant matrix, anchor expiry, reward idempotence, and Dragon phase thresholds.
Then use headless integration tests for persistence and a real client for render
and input paths.

### B-16 — Documentation drift is a product bug

The repository contains historical reports describing earlier two-biome and
prototype states alongside current ten-biome and post-Dragon claims. A new
contributor can follow an obsolete document and reintroduce deleted systems.

**Countermeasure:** mark every old report as historical, keep one current
`FEATURE_REFERENCE.md`, one current `WORLDGEN.md`, one current progression guide,
and link all archival documents back to the current contract. This critique is
not a replacement for that consolidation.

### B-17 — Balance is described, not measured

Scarcity, structure rarity, utility durability, Dragon drop rates, and mob
weights are mostly numbers chosen in source. The mod has no recorded median
exploration distance to first Lens/ruin, time to Echo Compass, or expected time
to Archive.

**Countermeasure:** measure at least these seed/player metrics:

- distance to first qualifying ruin;
- distance between the two biome landmarks;
- Void Shards per hour without farming exploits;
- time and deaths for first Dragon;
- awakened Dragon completion rate;
- utility uses per biome visit.

Balance changes should be tied to those measurements, not intuition.

### B-18 — Discovery-first design can become “nothing happened”

The mod intentionally avoids map markers, coordinates, and loud effects. If a
signal source is not loaded, a structure fails support checks, or an item has no
nearby target, the player may receive only silence. Restraint becomes frustration
when the feedback is not explicit.

**Countermeasure:** preserve quiet presentation but distinguish failure states:

- “No loaded signal”;
- “Signal blocked by terrain”;
- “No qualifying structure nearby”;
- “Archive still sealed”;
- “This item only answers in the End.”

The feedback must remain qualitative and must not leak coordinates.

## Remediation roadmap

### Release blocker pass

- [x] Gate custom Dragon scheduler behind persistent post-Dragon state.
- [x] Gate first-fight renderer stage transforms behind awakening.
- [x] Prevent biome landmarks from awarding Wastes-specific progression variants.
- [x] Make Void Anchor End-only and time-limited.
- [x] Keep resource and build validation green.
- [ ] Complete a fresh-world, multi-seed End generation scan (the deterministic
  landmark slots reduce risk but do not replace this gate).
- [ ] Complete a real client smoke test.
- [ ] Complete a two-player server test.

### Engineering consolidation pass

- [ ] Decide whether flagship landmarks migrate to registered Structures.
- [ ] Remove or quarantine unused legacy worldgen registrations.
- [ ] Add deterministic per-biome structure budgets and coverage metrics.
- [ ] Add pure tests for phase, direction, expiry, rewards, and variant mapping.
- [x] Register/validate sources on the first server tick after block entity load;
  unload/removal unregisters the source.
- [ ] Isolate development-only commands from the production release path.

### Player-facing polish pass

- [ ] Add mechanical tooltips for all utility items.
- [ ] Reduce creative-tab duplication.
- [ ] Add hero item models and replace the overlay with a fully authored Dragon
  model after Blockbench/MCP access and an in-game alignment pass.
- [ ] Tune particle/audio density in a 20-minute session.
- [ ] Add explicit qualitative failure feedback.
- [ ] Measure and rebalance exploration and Dragon pacing.

## What is actually verified right now

The following gates have been run locally during this audit series:

- `./gradlew build` — passed after the fixes in this pass;
- deterministic landmark-slot dispatch and active-biome JSON consolidation were
  reviewed in source and the project rebuilt successfully;
- `./gradlew runDatagen` — passed;
- `python tools/check_resources_complete.py` — passed for 63 blocks and 84 items;
- `node tools/validate_resources.mjs` — passed with 222 model/geometry files;
- dedicated-server boot and clean shutdown — passed;
- `/endesium dragonstate get` — executed successfully.

These results prove mechanical loadability and resource integrity. They do **not**
prove that the mod is beautiful, balanced, multiplayer-safe, accessible, or
pleasant for a new player. Calling those areas “done” would be dishonest.
