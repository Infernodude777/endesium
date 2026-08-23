# About Endesium

Endesium is a Fabric mod for Minecraft Java Edition 1.21.1 that grows the End into a real place: ten hand-authored regions, a discovery-driven progression system, ancient machinery you learn to read, and things that watch you from the dark before they commit.

## The pitch

Vanilla's End is a boss room with a parking lot. You arrive, you fight, you leave, and the outer islands sit there as generic stone with the occasional chorus plant and an end city nobody needed.

Endesium asks a different question: what if the End was old? Older than the dragon, older than the cities - a place where something built machines out of resonance and then went quiet. Where the ground itself remembers being important.

So the mod adds:

- **Ten continent-scale regions**, each with its own geology, ecology, weather of particles, and identity - from the mineral grave of the End Wastes to the drowned black water of the Void Marshes to the silent archives of the Umbral Reach.
- **Structures in tiers**: grand flagships like the Dust Cathedral and the Great Caldera, a commoner landmark tier (fossil arches, ember shrines, anchor ruins) so exploration always pays, and small ruins seeded through everything.
- **A resonance system** that treats information as progression. You craft a Lens, learn to read qualitative signals, wake dormant mechanisms, and earn tokens - not XP, not ore. Understanding is the gate.
- **Thirteen custom creatures** built on GeckoLib, each with its own body plan and combat identity: stalkers that flank, wraiths that ignite, constructs that guard, and two bosses worth building a character around.
- **Two real bosses**: the region-adaptive End Wardens guarding every flagship vault, and the End Golem - a three-phase colossus with a stagger window, arena tether, and rewards that permanently make you stronger.
- **A post-Dragon transformation**: killing the dragon changes the world state forever. New signals awaken. The archive unseals. Something older stirs.

## Philosophy

1. **Restraint over spectacle.** Desaturated palette, quiet effects, accents that mean something. If everything glows, nothing does.
2. **Observation over waypoints.** Signals are qualitative bands and broad directions, never coordinates. The compass exists only after you prove you can read the mechanism.
3. **Server authority.** Detection, rewards, and world state live server-side. Clients receive bounded answers.
4. **Rewards you keep.** Sigil hearts, absorbed cores, Golem's Resolve - the best prizes are permanent, and every one of them is guarded.
5. **Vanilla stays vanilla underneath.** The mixin extends the End biome source rather than replacing it. Your first dragon fight is exactly vanilla.

## How to play (short version)

1. Enter the End normally. Beat the dragon when ready.
2. Walk outward until the terrain stops looking vanilla.
3. Find a ruin or landmark. Craft the Resonance Lens (4 Void Shards around an Eye of Ender).
4. Right-click a dormant mechanism with the Lens. Take your token.
5. Follow the stronger signals inward. Gear up from the regions.
6. After the dragon dies: wardens in every vault, a golem at the arena, and ten regions of permanent power waiting to be taken.

The in-game Guidebook and Progression Guide cover everything else.

## Technical shape

- Fabric Loader + Fabric API for 1.21.1, Java 21, official mappings.
- GeckoLib 4.9.2 for entity models and animations.
- Data generation for recipes, loot tables, advancements, and language.
- Persistent world state via SavedData; per-player reward tracking via its own SavedData.
- No client-side cheating: the client asks, the server decides.

## Credits

Design, code, and assets by infernodude777, developed iteratively with heavy playtesting and an unreasonable number of bug hunts. Built on Fabric and GeckoLib - thanks to both teams.

Licensed CC0-1.0. Take it apart. That is what it is for.
