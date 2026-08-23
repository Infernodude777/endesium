# Endesium Critical Audit

**Audit date:** 2026-08-18  
**Scope:** current source tree, data/resources, automated QA scripts, and the
implemented Endesium gameplay claims.  
**Standard:** judge the mod as a player would experience it, not as a list of
classes that happen to compile.

## Executive verdict

Endesium has a strong identity and an unusually clear visual thesis: a quiet,
weathered End with discovery-driven progression. The project also has a solid
server-authoritative foundation and good automated resource hygiene.

But it is not yet a finished, confident mod. It currently reads like several
ambitious milestones stacked on top of one another: the original resonance
slice, a ten-biome ecology pass, a large item tranche, enlarged landmarks, and a
post-Dragon boss system. The result is breadth without enough consolidation.
The largest risks are not Java compilation failures; they are **content that
claims to exist but does not behave as advertised, worldgen that is more fragile
than its documentation admits, and a lack of live client/playtest evidence**.

The honest rating is:

- **Identity:** promising and distinctive.
- **Technical baseline:** good, with several important fixes now applied.
- **Player-facing cohesion:** inconsistent.
- **Worldgen confidence:** medium; deterministic, but not yet stress-proven.
- **Visual confidence:** unknown until a real client pass is performed.
- **Release readiness:** not ready for a public 1.0 release.

---

## Priority table

| Priority | Finding | Player impact | Status |
|---|---|---|---|
| P0 | Several advertised systems were not connected to the actual game loop | Content appears absent or fake | Fixed in this audit pass |
| P0 | Enlarged landmark features rejected Endesium-custom terrain as unsupported | Spires, Archives, and Monoliths could silently never generate | Fixed in this audit pass |
| P0 | Wilds Sanctum rotation recursively called its own offset helper | Worldgen could stack-overflow while generating Chorus Wilds | Fixed in this audit pass |
| P0 | First Dragon fight was receiving Endesium's custom attack scheduler | Vanilla milestone became unexpectedly harder | Fixed in this audit pass |
| P0 | Death hook marked the transformation before `setDragonKilled` | The Dragon could die without firing the world-awakening announcement | Fixed in deep-improvement pass |
| P1 | Dragon heavy-attack cooldowns were written but never decremented | Storm, gale, dive, and awakened attacks could silently stop recurring | Fixed in deep-improvement pass |
| P1 | Renderer stage transforms ran before vanilla's first pose push | A client could retain a Dragon transform on the caller's pose stack | Fixed in deep-improvement pass |
| P1 | Void Ray and Lumen Moth were typed as hostile Monsters despite being wildlife | Passive ecology could behave like hostile mob content | Fixed in this audit pass |
| P1 | Resonant Wings recipe ownership was unclear despite the book and docs promising one | Easy to misdiagnose as broken and risk duplicate resources | Fixed/verified in this audit pass |
| P1 | Ash Sifter created renewable Void Shards and Dragonbone | Contradicted scarcity and bypassed progression | Fixed in this audit pass |
| P1 | Archive Key could directly activate every mechanism | It bypassed the Lens and early discovery loop | Fixed in this audit pass |
| P1 | Wastes Compass directions were rotated | The navigation tool lied | Fixed in this audit pass |
| P1 | Nullwalker was listed as a natural spawn in nearly every biome | The “deepest rare creature” lost its identity | Fixed in this audit pass |
| P1 | End Cartographer still said seven regions and called three regions unknown | Exploration feedback was visibly wrong | Fixed in this audit pass |
| P1 | Sonic Boom did not consume Elytra durability | Its stated cost and actual balance disagreed | Fixed in this audit pass |
| P2 | Enlarged landmarks write across chunk boundaries as ordinary Features | Possible generation-order, overlap, and save-boundary risk | Mitigated by spacing/support checks; stress test remains open |
| P2 | Many “landmarks” have no mechanism despite the design language implying they do | Exploration rewards are inconsistent | Open; needs a deliberate landmark contract |
| P2 | Endesium uses Features instead of registered Structures | No `/locate structure`, structure bounding boxes, or structure-aware protections | Intentional, but a real usability tradeoff |
| P2 | Most item models remain 2D sprites with transform polish only | Inventory looks cleaner, but not genuinely bespoke or 3D | Open art pass |
| P2 | The dedicated creative tab duplicates much of the vanilla inventory | Creative browsing is noisy and repetitive | Open UX cleanup |
| P2 | Automated checks cannot validate visuals, audio, multiplayer, or feel | “PASS” can still hide ugly/broken client behavior | Open manual test requirement |
| P2 | There are no meaningful unit tests for progression, item behavior, or worldgen geometry | Regressions are easy to reintroduce | Open test investment |
| P3 | Custom entities have expected “No data fixer registered” warnings | Harmless today, weak migration story | Open hardening |
| P3 | Documentation has accumulated contradictory milestone-era claims | Contributors and players cannot know the truth | Partially addressed here; broader cleanup needed |

---

## Findings in detail

### 1. The mod had breadth-before-contract problems

The source tree contains ten regions, many utility items, nine ecology entities,
multiple landmark families, a transformed Dragon, an Archive, an Elytra ability,
and a large guidebook. That is a lot of surface area for one release. A player
can reasonably ask:

- Which systems are actually required for progression?
- Which items are useful versus decorative or unfinished?
- Which landmarks contain mechanisms and which are scenery?
- Is the Dragon fight vanilla, Endesium-enhanced, or post-Dragon only?
- Is the Archive opened with a Lens, a Key, a Sigil, or all three?

Previously, the answer differed between code, guidebook text, and design docs.
That is a product problem, not merely a documentation problem. A feature is not
done when its class exists; it is done when its acquisition, use, reward,
feedback, and next step agree.

**Countermeasure:** this pass fixed the most damaging contract breaks and records
the remaining contracts below. Every future item or structure should enter the
same acceptance table before being called complete.

### 2. Natural ecology needed a truthful type audit

The biome JSONs already contain natural spawn lists for the ecology species; an
initial code-only audit missed that data-driven path. Adding another set of
Fabric spawn registrations would have duplicated weights, so that tempting fix
was rejected.

The real defect was that `VoidRayEntity` and `LumenMothEntity` were `Monster`
subclasses despite being described as wildlife/peaceful gliders. Their spawn
placement also used the hostile monster predicate.

**Countermeasure applied:**

- Changed Void Ray and Lumen Moth to `PathfinderMob` rather than `Monster`.
- Changed their placement predicate to the general mob predicate.
- Kept the existing biome JSON spawn lists as the single source of spawn
  weights and region assignment.

**Still required:** playtest actual population density and ensure passive
creatures do not crowd out vanilla End ambience.

### 3. Wilds Sanctum had a fatal rotation bug

The rotation helper in `WildsSanctumBuilder` called `offset(...)` recursively
instead of calling `base.offset(...)`. Any non-default rotation could recurse
until a `StackOverflowError`, which is a catastrophic worldgen failure rather
than a cosmetic issue. The headless archive-generation run exposed this exact
failure while force-generating the Wilds.

**Countermeasure applied:** corrected all four rotation branches to call
`BlockPos.offset` directly. The feature gate was also updated to recognize the
custom Endesium terrain created before landmark placement.

### 4. Enlarged landmarks rejected the terrain they were designed for

The terrain pass runs before landmark placement and replaces the vanilla End
surface with Endesium geology. The Shattered Spire and Resonant Monolith
features still required the block immediately below their origin to be vanilla
End Stone, while the Archive's broad support check only partially recognized
custom geology. On a generated Endesium island, the named landmark could
therefore reject its candidate and disappear. This is exactly the kind of bug
that a successful compile and a locate-biome test will miss.

**Countermeasure applied:** all three flagship feature gates now accept the
shared `ModBlocks.isPlantGround` family, End Stone Bricks, and the relevant
Endesium stone states in their origin/support checks. Their stricter footprint
thresholds remain in place to avoid floating structures.

### 5. The main post-Dragon craft was easy to misdiagnose

The guidebook and progression docs promise that Dragon materials lead to
Resonant Wings. The recipe does exist, but it is generated under
`src/main/generated` by `EndesiumRecipeProvider`, not checked in as a manually
written source-data recipe. A naive audit can miss it and accidentally add a
duplicate resource; Gradle caught exactly that duplicate during this pass.

**Countermeasure applied:** removed the duplicate source resource, kept the
canonical generated recipe, and verified `runDatagen` plus the final build. The
recipe uses a vanilla Elytra, Resonant Dragon Scales, Ender Essence, and
Dragonbone, preserving the rare post-Dragon gate. The process rule is now clear:
do not hand-add a source recipe when the datagen provider owns it.

### 6. The Ash Sifter violated the shard economy

The Ash Sifter converted renewable Ashen Soil into a 20% Void Shard chance and a
Dragonbone chance. That made both a supposedly scarce progression material and a
Dragon reward renewable through a basic biome tool. This contradicted the README
and the balance notes.

**Countermeasure applied:** the Sifter now returns Ashen Embers, Magma Cores, or a
rare Echo Shard instead. It no longer produces Void Shards or Dragonbone.

**Still required:** test whether Ashen Soil itself is too renewable and whether
the remaining drops are worth the durability cost.

### 7. Archive Key progression bypass

`ArchiveKeyItem` previously activated any Resonant Mechanism directly. A player
could use a key on an ordinary ruin and skip the Lens-reading step, even though
the entire early game is supposed to teach observation and interpretation.

**Countermeasure applied:** the key now only interacts with `ARCHIVE` mechanisms.
The normal ruins still require the Lens. The Archive itself still enforces the
post-Dragon state.

The key is intentionally an optional direct Archive activation route rather than
a replacement for the Lens. The guidebook now says so explicitly.

### 8. Navigation feedback was incorrect

`WastesCompassItem` used `atan2(dz, dx)` but mapped zero degrees to north even
though zero in Minecraft's X/Z plane points east. The item therefore reported a
systematic 90-degree error. `EndCartographerItem` also had stale “seven regions”
text and omitted names for Void Skirts, Void Crown, and Umbral Reach.

**Countermeasure applied:** corrected the eight-way compass table, removed the
unused detection-range constant, changed seven to ten, and added all region
names.

### 9. The first Dragon fight was over-customized

`DragonFightController` was ticking for every Ender Dragon and could schedule
Endesium attacks during the first vanilla fight. That conflicted with the
post-Dragon promise that the first kill remains the familiar gateway and that
the awakened fight is the later challenge.

**Countermeasure applied:** the custom controller now returns immediately until
`PostDragonState` is active. The initial Dragon remains vanilla-compatible; a
respawned Dragon after the transformation receives the Endesium phase system.

### 10. Sonic Boom cost did not match its documentation

The Resonant Wings ability had an 864-durability item but the Sonic Boom handler
never damaged it. That made durability a decorative number and removed the main
resource decision from a powerful 40-block, armor-ignoring attack.

**Countermeasure applied:** every accepted Sonic Boom request now consumes one
Wings durability in addition to the persisted 15-second cooldown.

### 11. Landmark scale is now visually stronger, but the Feature architecture is
still a risk

The Shattered Spire and Resonant Archive are now 23x23 footprints, and several
biome landmarks were enlarged. Their candidates are widely separated and their
support checks are stricter. However, these are still hand-written `Feature`
placements that can write outside their owning chunk. They do not have the
lifecycle guarantees of a registered Minecraft `Structure`.

Risks:

- neighbor chunks may not be in the expected generation state;
- future features can overwrite or be overwritten by cross-chunk writes;
- save/reload edge cases are harder to reason about;
- there are no structure bounding boxes or `/locate structure` integration;
- large features are difficult to inspect and regenerate safely.

**Countermeasure already present:** candidate spacing, centered placement,
solid-footprint checks, and deterministic rotation. These reduce the risk but do
not prove it absent.

**Required next step:** run multi-seed stress generation over a large End grid,
parse all touched region files, and either formalize the cross-chunk feature
contract or migrate the flagship landmarks to registered Structures.

### 12. The biome landmark promise is inconsistent

The design language says biome discoveries lead to meaningful mechanisms and
rewards. Several `BiomeStructureFeature` archetypes instead place a small chest,
barrel, or decorative center without a Resonant Mechanism. That makes the
resonance loop central in some regions and irrelevant in others.

**Required countermeasure:** define one of two explicit contracts and enforce it:

1. every named landmark gets a mechanism, persistent activation state, and a
   region-specific reward; or
2. landmarks are explicitly scenery/loot sites and are not described as
   resonance stations.

The recommended choice is (1) for flagship landmarks and (2) for small scenery.
Do not leave the distinction implicit.

### 13. Feature versus Structure is an intentional but costly tradeoff

The mod deliberately uses generation Features, so Endesium landmarks do not
appear in `/locate structure`. That supports the “discover, do not waypoint”
philosophy, but it also removes a standard debugging and accessibility tool.

This is acceptable only if the Lens, Compass, guidebook, and visual landmarks
are reliable enough to replace it. Until the live game proves that, the choice
is a usability risk rather than an unqualified virtue.

**Recommended countermeasure:** keep production discovery organic, but add a
permission-gated development locate command or debug-only structure labels for
QA. Do not expose coordinates to ordinary players.

### 14. Item visuals are cleaner, not finished

The shared GUI and handheld parents improve scale and transforms, but most item
models are still generated 2D sprites. Armor inventory models are still sprite
planes, and the guidebook uses a vanilla book base with an overlay. This is a
presentation improvement, not a full custom-model solution.

**Required countermeasure:** create a small number of genuine hero models first:

- Void armor set with a consistent silhouette and emissive seam;
- Resonant Wings icon/model with readable scale-and-thread construction;
- Void sword and pickaxe with distinct silhouettes;
- guidebook cover with an actual Endesium texture rather than a vanilla book
  fallback.

Do not make every material a complex model. Spend art budget on the items the
player holds, wears, or sees in progression screens.

### 15. Creative inventory organization is noisy

The dedicated Endesium tab contains the full catalog, while `ModItems.register`
also injects many of the same entries into vanilla Ingredients, Tools,
Combat, and Building Blocks tabs. This creates duplicate browsing paths and makes
the mod feel larger and less curated than it is.

**Recommended countermeasure:** keep progression essentials in the relevant
vanilla tabs only if discoverability is a goal; otherwise use the dedicated tab
as the canonical catalog and remove duplicate bulk entries. Never duplicate the
same tool in multiple vanilla tabs unless vanilla convention strongly supports
it.

### 16. Automated QA is necessary but cannot certify the player experience

Current gates cover build, datagen, resource references, and a headless server.
They do not prove:

- the guidebook opens and pages do not clip;
- armor, tools, entities, particles, and sounds look good in a real client;
- passive and hostile spawn populations feel correct;
- two players receive fair server-authoritative feedback;
- the first and transformed Dragon fights are readable rather than exhausting;
- enlarged structures look intentional from a distance;
- audio is not repetitive or too loud.

**Countermeasure:** treat manual client, multiplayer, and two-seed visual passes
as release gates, not optional polish. The audit is intentionally not calling
those areas “PASS” based on headless logs.

### 17. Documentation drift is a real bug

The repository contains many milestone-era documents. Some still describe two
biomes, one entity, no commands, or the old vertical slice, while current code
contains ten biomes, nine entities, and a development command. A contributor
following an old report can make a correct change that reintroduces a retired
architecture.

**Countermeasure:** this audit is the source of truth for the current gap list.
The next documentation pass should mark historical reports as historical and
make `README.md`, `FEATURE_REFERENCE.md`, `WORLDGEN.md`, `TESTING.md`, and the
progression guide agree on one current contract.

---

## Expansion pass audit update — 2026-08-18

The structure/content expansion adds ten named landmarks to the five regions that
previously had the weakest landmark identity: Shattered Highlands, Void Marshes,
Luminous Groves, Void Crown, and Umbral Reach. The other five regions already had
at least two named archetypes and were not inflated with redundant structures.
This is the correct scope for the requirement, but it creates new risks:

- **P1 — Texture aliases are not final art.** The new block models reuse existing
  biome textures to preserve palette discipline and keep the first implementation
  resource-safe. This is cleaner than placeholder gray cubes, but the blocks are
  not visually distinct enough for a final release. Bespoke hero textures remain
  required.
- **P1 — Structure contracts are still feature-based.** The new builders perform
  support checks before writing, but they still share the cross-chunk risk of the
  existing `BiomeStructureFeature` architecture. A large-seed scan is still a
  release gate.
- **P1 — Dragon stage renderer is compile-verified, not play-verified.** The new
  client mixin scales and pitches the vanilla model by health stage and awakened
  state. It deliberately avoids replacing vanilla geometry, but packaged-client
  rendering, shader interaction, and visual readability still require a live
  client pass.
- **P1 — First-fight balance changed intentionally.** The first Dragon now gets
  readable base choreography instead of being completely vanilla; awakened-only
  rifts, howls, and meteors remain locked behind transformation. Damage, timing,
  and safe-zone clarity need two-player testing before release.
- **P2 — Relic behavior is bounded but not yet deep.** Winch, Bell Clapper, Lumen
  Graft, Crown Needle, and Null Quill use a shared server-side relic behavior.
  That prevents client spoofing and cooldown bypasses, but the final design should
  give the strongest relics more structure-specific interactions instead of only
  short effects/messages.
- **P2 — Generic feature rarity does not guarantee world coverage.** Every biome
  now has at least two named archetypes in the code inventory, but a single seed
  can still fail support checks or roll the same family repeatedly. Density and
  archetype distribution need measurement rather than assumption.

**Countermeasures applied:** support-safe builders, named focal blocks, dedicated
loot tables, datagen recipes/translations, shared clean item transforms,
server-authoritative relic activation, stored Dragon zones with bounded lifetimes,
exactly-once meteor impacts, storm lifetime correction, talon telegraph timing,
and zone cleanup on Dragon death.

## Deep-improvement pass — 2026-08-18

This pass targeted defects that would survive a green compile and make the live
fight or world feel unreliable.

### Fixed

- **Transformation event ordering:** the death-animation hook now grants drops
  without marking `PostDragonState`. Vanilla's `EndDragonFight.setDragonKilled`
  remains the sole state transition and can therefore fire the one-time
  transformation announcement. This closes a silent failure where the state was
  already active by the time the event mixin ran.
- **Dragon scheduler liveness:** every heavy-attack cooldown now decrements on
  every server tick. Dive cooldowns are also actually assigned when dives end;
  previously those fields existed but were never advanced or populated, making
  the cooldown contract misleading and some attacks one-shot per Dragon.
- **Dragon shutdown safety:** the controller exits during the death animation,
  clears delayed zones when a target dies or disconnects, and expires rift and
  meteor markers even if their normal impact path is interrupted.
- **Stage agreement:** server and client now use the same 75/45/20 health bands,
  preventing the attack pool and the visible stage treatment from disagreeing.
- **Fracture performance:** arena fracture geometry is cached per Dragon state
  instead of recomputed once per player during every fissure pulse.
- **Renderer pose hygiene:** stage scaling is injected after vanilla's first
  `pushPose`, so the matching vanilla `popPose` removes the transform rather than
  leaking it into later render calls.
- **Feature protection:** expanded biome builders no longer replace protected
  portal/bedrock/obsidian/dragon-egg blocks or existing block entities when a
  cross-chunk Feature overlaps a generated area.
- **Legacy-builder protection:** introduced `StructurePlacement` and routed the
  old Wilds Sanctum, End Ruin, Spire, Archive, Monolith, and biome-dispatch
  builders through it. The hand-authored Feature path now has one consistent
  protected-write boundary instead of relying on each builder remembering its
  own guard.
- **Relic contracts:** the Windscar Winch, Mire Bell Clapper, Lumen Graft, Crown
  Needle, and Null Quill now have distinct server-side actions. The Bell marks
  nearby solid footing, the Needle tunes to the strongest loaded signal, and
  the Quill recalls a nearby inscription instead of merely applying a generic
  effect. All reject use outside the End and retain bounded cooldown/durability
  costs.
- **Automated contract coverage:** `check_resources_complete.py` now verifies
  all ten biome dispatches, at least two named landmark builders per biome,
  biome feature wiring, Dragon death/stage safety tokens, renderer pose safety,
  and the absence of direct writes in legacy landmark builders.
- **Build regression:** removed an invalid `TooltipContext` import from the
  shared relic class; the project now compiles both server and client sources.

### Remaining blockers after this pass

These are deliberately not marked solved without evidence:

- The renderer injection still requires a packaged-client smoke test because
  mixin target stability cannot be proven by Java compilation alone.
- The terrain, vegetation, and central Dragon arena generators intentionally
  retain direct writes because they sculpt the authored terrain rather than
  place optional landmarks. They have separate pillar/portal exclusion rules;
  migrating those terrain writers to the landmark guard would be incorrect.
- The relic actions are now distinct, but their balance and readability still
  need live playtesting. The Null Quill reports a symbol rather than modifying
  the world, which is deliberate but should be validated against the intended
  clue loop.
- Multi-seed density, cross-chunk ownership, two-player Dragon balance, and
  visual inventory/armor review remain live acceptance work.

## Acceptance plan

### Phase A — completed in this audit pass

- [x] Verify the data-driven biome spawn lists and avoid duplicate code-based registrations.
- [x] Make wildlife entity classes and spawn predicates match their design.
- [x] Allow landmark support checks to recognize Endesium-custom terrain.
- [x] Fix the Wilds Sanctum recursive rotation crash exposed by headless generation.
- [x] Add the missing Resonant Wings recipe.
- [x] Stop Ash Sifter progression-material farming.
- [x] Restrict Archive Key use to Archive mechanisms.
- [x] Correct Wastes Compass directions.
- [x] Correct End Cartographer's ten-region feedback.
- [x] Keep the first Dragon fight vanilla-compatible.
- [x] Make Sonic Boom consume Wings durability.

### Phase B — next engineering pass

- [ ] Add a landmark contract test: every flagship landmark must have the
  expected mechanism, variant, loot table, and activation reward.
- [ ] Add a large multi-seed region-file scan for cross-chunk landmark writes.
- [ ] Measure chunk generation time before and after enlarged footprint checks.
- [ ] Add multiplayer test coverage for Lens, Compass, Archive, Sonic Boom,
  reward duplication, and reconnect/reload behavior.
- [ ] Decide whether flagship landmarks should migrate to registered Structures.
- [ ] Add a permission-gated development locator for QA only.

### Phase C — player-facing consolidation

- [ ] Make the ten-region progression table explicit and truthful.
- [ ] Give each flagship biome landmark either a mechanism/reward contract or a
  clearly scenery-only label.
- [ ] Remove duplicate creative-tab clutter.
- [ ] Add tooltips that explain each utility item's actual scope, cooldown, and
  durability cost.
- [ ] Update historical QA/design reports so they are clearly marked archival.

### Phase D — art and feel gate

- [ ] Perform a live client inventory pass at GUI, ground, first-person, and
  third-person scales.
- [ ] Perform a live armor-worn and Elytra pass with all animations loaded.
- [ ] Inspect every flagship landmark from 32, 64, and 128 blocks away.
- [ ] Listen to a complete 20-minute End session for sound repetition and volume.
- [ ] Play the first Dragon, transformed Dragon, and multiplayer resonance loop
  with real players before calling the milestone release-ready.

---

## Current verification

Verification completed on 2026-08-18:

| Gate | Result | Notes |
|---|---|---|
| `./gradlew build` | PASS | Server and client compile; no test sources are currently present. |
| `./gradlew runDatagen` | PASS | Providers completed successfully; the expected missing Geckolib refmap warning remains development-only. |
| `node tools/validate_resources.mjs` | PASS | 222 model/geometry files checked. A trailing-comma defect in nine biome JSON files was caught and fixed during the previous final pass. |
| `python tools/check_resources_complete.py` | PASS | 63 blocks and 84 items have the expected resource families. |
| `tools/qa_run.sh structure_rotation` | PASS | No new worldgen failure, loot parse failure, or stack overflow; the log retains expected data-fixer warnings and the vanilla End Highlands locate warning. |
| `tools/qa_post_dragon.sh dragon2` | PASS | Transformation is idempotent across two phases; no new Dragon exception appeared. |
| `git diff --check` | PASS | No whitespace errors. |

The headless log still contains Minecraft's `No data fixer registered` messages
for Endesium entities and an expected `end_highlands` locate warning. These are
known non-blocking warnings, not evidence of a clean player-facing release.

A green result means the project is mechanically loadable. It does **not** mean
that the remaining visual, balance, cross-chunk, or multiplayer risks are
solved. Those require the acceptance work above.

## Final hardening pass — 2026-08-18

This follow-up closed two player-facing defects found while reviewing the
remaining smoke-test evidence:

- **Vanilla sound path compatibility:** `sounds.json` no longer references the
  retired `mob/enderdragon`, `mob/warden`, `random/explode`, or legacy
  `block/amethyst` resource layouts. Dragon, Warden, explosion, and amethyst
  references now use the 1.21 asset paths. `tools/validate_resources.mjs`
  rejects those retired prefixes so the regression cannot silently return.
- **Guidebook use safety and truthfulness:** the client guidebook callback now
  returns the held stack rather than an empty stack, and the Sonic Boom page
  correctly states that each activation consumes one Resonant Wings durability.
  This removes a potential predicted-inventory flicker/loss and a direct
  contradiction between the guidebook and server behavior.

The final dedicated-server hardening run completed without worldgen exceptions,
stack overflows, server crashes, or Dragon-state failures. The only remaining
log noise is the known development-time Geckolib refmap warning, Minecraft's
standard “No data fixer registered” messages for custom entities, the expected
failure to locate vanilla `end_highlands` in a seed whose eligible ring is fully
assigned to Endesium regions, and temporary “Can't keep up” spikes during the
large forced-chunk scan. Those are recorded as limits rather than hidden.

The project still cannot honestly claim visual perfection without a human client
pass for the guidebook, inventory transforms, armor, landmark silhouettes,
audio mix, and both single-player and multiplayer Dragon balance. The automated
baseline is now stronger, but those are acceptance gates, not compile problems.
