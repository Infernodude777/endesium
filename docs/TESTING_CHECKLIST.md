# Endesium — In-Game Test Checklist

This is the manual verification checklist for everything that a headless build
environment cannot prove: the *feel* of the mechanics in a real client. Run this
after any milestone that touches world generation, entities, the Dragon, or the
Resonance systems.

Build-only gates (run first, must be green before playtesting):

```bash
./gradlew build
./gradlew runDatagen
node tools/validate_resources.mjs
./tools/qa_post_dragon.sh
```

---

## A. Dragon state & transformation

- [ ] Kill the Ender Dragon in a fresh survival world; the transformation fires **exactly once** (deep sound, particle surge, "The End answers." action-bar line, `dragon_transformation` advancement).
- [ ] It does **not** repeat on: world reload, another player joining, re-entering the End, or server restart.
- [ ] Save → quit → restart → re-enter End: transformation still active, no duplicate event, no duplicate advancement.
- [ ] Respawn the Dragon and kill it again: transformation stays **permanent**, nothing resets, no duplicate advancement.

## B. Event timing / multi-location

- [ ] Kill the Dragon while a second player is in the Overworld, Nether, or offline — they get **no** End-only announcement, but encounter the transformed world when they later enter the End.

## C. Resonant Archive

- [ ] **Pre-Dragon:** the Archive exists, looks intact, core is sealed; the Lens reads it as an ordinary dormant signal (NOT the 512/1.8 awakened source).
- [ ] Attempt activation pre-Dragon → exact message "The archive is sealed. It waits for the End to wake." — **no reward, no advancement, no state change**.
- [ ] **Post-Dragon:** the Lens reads it as the strongest signal (deep resonance, radius 512, strength 1.8), no phantom signals.
- [ ] Wake the core → Archive Sigil + 2 Void Shards + Archive Fragment; `archive_awakened` triggers once.
- [ ] **Reward duplication:** spam right-click, leave/return, unload/reload chunk, restart server, two players — reward is **never** duplicated.
- [ ] Generation: ~1 per 5×5 chunk cell, both biomes, no floating/buried/overlapping pieces, no interference with End Cities or gateways.

## D. Resonance awakening

- [ ] Record a dormant ruin's Lens reading pre-Dragon vs post-Dragon — dormant sources gain radius/strength (1.5×/1.3×); active mechanisms unchanged.
- [ ] Edge cases: unloaded chunk, destroyed source, teleport, dimension change, server restart — no phantom signals, server stays authoritative.

## E. Command audit

- [ ] `/endesium dragonstate get` returns the current state.
- [ ] `/endesium dragonstate set true` / `set false` works, requires op level 2 (survival players rejected).
- [ ] Invalid input (bad boolean, missing/extra args, wrong syntax) → clean Brigadier error, no crash, no state corruption.
- [ ] `set true` does **not** grant the transformation advancement or Archive rewards; `set false` does **not** delete rewards, reset structures, or duplicate content.

## F. Regression (before & after Dragon)

- [ ] End Wastes, Chorus Wilds, End Ruins (all variants), Shattered Spire, Void Stalker (guaranteed Lens drop, AI, attack/reposition), Resonance Lens, Resonance Token, Echo Compass, First Resonance, and all prior advancements still work.
- [ ] Vanilla intact: Dragon fight, End crystals, exit portal, gateways, End Cities, chorus plants, Endermen, outer islands.

## G. World compatibility & seeds

- [ ] Fresh world (pre-Dragon), existing explored world (pre-Dragon), existing world (post-Dragon) with new chunk generation all behave; **no retroactive terrain rewrite, player builds untouched**.
- [ ] 2–3 different seeds: Archive generation, biomes, ruins, spire, transformation all present; no seed-specific failures.

## H. Performance & logs

- [ ] Dragon death, SavedData lookup, Archive generation/activation, Lens scanning — no per-tick global scans, no chunk-load storms, no particle storms.
- [ ] Client + server logs: classify every Endesium ERROR/WARN as real bug vs harmless vs unrelated.

## I. Full progression playthrough (no commands)

- [ ] Overworld → Nether → End → End Wastes → Chorus Wilds → End Ruins → Resonance → Echo Compass → Shattered Spire → Dragon → transformation → awakened Resonance → Resonant Archive → Archive Sigil. Confirm the Dragon's death genuinely changes the world.
