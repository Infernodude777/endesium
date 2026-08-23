package com.infernodude777.endesium.client.screen;

import java.util.List;

/**
 * The written body of the Progression Guide. Every page tells you exactly how
 * to get something, how to craft something, or where something lives. The
 * screen renders these pages verbatim; categories become the tab bar.
 */
public final class ProgressionGuideContent {
    private ProgressionGuideContent() {
    }

    public record Entry(String category, String title, List<String> body) {
    }

    public static final List<String> CATEGORIES =
            List.of("Start", "Progression", "Items", "Crafting", "Landmarks", "Endgame");

    public static final List<Entry> PAGES = List.of(
        new Entry("Start", "Read Me First", List.of(
            "This guide covers everything: how to obtain every item,",
            "craft every recipe, and find every flagship structure.",
            "Tabs above split the journey into six stages.",
            "Golden rule: nothing here needs luck - every gate is",
            "knowledge, and this book is the knowledge.")),
        new Entry("Start", "Reaching the End", List.of(
            "Craft Eyes of Ender (blaze powder + ender pearl), follow",
            "them to a stronghold, fill the portal frame sockets, and",
            "jump through.",
            "Bring iron or better gear, plenty of blocks, and patience:",
            "the outer End is very far from the central island.")),
        new Entry("Start", "Reading the Land", List.of(
            "Beyond the center lie ten Endesium regions: Wastes, Wilds,",
            "Highlands, Marshes, Groves, Ashen, Barrens, Skirts, Crown,",
            "and Umbral Reach.",
            "Each has unique geology, growth, mobs, and exactly ONE",
            "grand flagship structure.",
            "Cheat sheet: /locate biome endesium:<region_name>")),
        new Entry("Progression", "The Core Loop", List.of(
            "1. EXPLORE outward until the terrain looks strange.",
            "2. NOTICE dormant ruins and flagship structures.",
            "3. Hold the RESONANCE LENS and right-click a mechanism",
            "   block (dark metal, cyan seam) to wake it.",
            "4. Collect your TOKEN and clue fragment.",
            "5. Tokens unlock everything that comes next.")),
        new Entry("Progression", "Crafting the Lens", List.of(
            "RESONANCE LENS - shaped recipe:",
            "    S . S",
            "    . E .      S = Void Shard",
            "    S . S      E = Eye of Ender",
            "Void Shards come from Void Stalker drops, ruin and flagship",
            "loot, and waking mechanisms. Never farmable - on purpose.")),
        new Entry("Progression", "Waking Mechanisms", List.of(
            "Mechanisms are dark metal blocks with a cyan seam, found at",
            "the heart of every ruin and flagship.",
            "Use the Lens ON the block: particles surge, loot unlocks,",
            "and you receive a Resonance Token plus a clue fragment.",
            "Your first wake grants First Resonance.")),
        new Entry("Progression", "Echo Compass", List.of(
            "Shape 4 Void Shards around a Resonance Token to craft an",
            "ECHO COMPASS.",
            "Using it reports a cardinal heading and rough distance to",
            "the strongest loaded resonance source, with a short white",
            "particle trail pointing the way.",
            "It never shows coordinates - you learn to read it.")),
        new Entry("Items", "Void Gear Chain", List.of(
            "VOID ORE grows in Void Skirts stone. It drops itself and",
            "smelts into VOID INGOTS; deep seams also yield gems.",
            "Ingots repair every Void tool and armor piece and craft",
            "the entire gear family listed on the next pages.")),
        new Entry("Items", "Void Tools", List.of(
            "SWORD: Slowness on hit. HOLD USE in full set to charge a",
            "black hole singularity (watch the bar above your hotbar).",
            "PICKAXE: right-click resonance blast (needs Leggings).",
            "AXE: right-click dash forward (needs Boots).",
            "SHOVEL: Speed I while held. HOE: Jump Boost while held.",
            "All repair with Void Ingots.")),
        new Entry("Items", "Void Armor Set", List.of(
            "HELMET: Night Vision + Water Breathing.",
            "CHESTPLATE: Resistance I, emergency Absorption below half.",
            "LEGGINGS: Haste II. BOOTS: Knockback resistance bonus.",
            "FULL SET also grants immunity to your own black hole and",
            "is the key that lets the Void Sword charge at all.")),
        new Entry("Items", "Region Tools", List.of(
            "Wastes Compass: heading to the island heart.",
            "Highland Grappler: launch up cliffs. Lumen Lantern: sight.",
            "Void Filter: purge debuffs. Crystal Resonator: mineral ping.",
            "Ash Sifter: sift ash into embers and cores.",
            "Chorus Pruner: sustainable chorus harvest.",
            "Void Flare: light beacons. End Cartographer: charts regions.")),
        new Entry("Items", "Biome Relics", List.of(
            "Windscar Winch: wind lift (Highlands).",
            "Mire Bell Clapper: breathing pulse (Marshes).",
            "Lumen Graft: dark sight (Groves).",
            "Crown Needle: points to the strongest signal (Crown).",
            "Null Quill: recalls erased clues (Umbral Reach).",
            "Found near each region's flagship caches.")),
        new Entry("Crafting", "Lens & Compass", List.of(
            "RESONANCE LENS: 4 Void Shards around an Eye of Ender.",
            "ECHO COMPASS: 4 Void Shards around a Resonance Token.",
            "Both are one-time crafts - they never break or expire.")),
        new Entry("Crafting", "Resonant Wings", List.of(
            "Shaped:  E S E",
            "         S W S      E = Ender Essence",
            "         B S B      S = Resonant Dragon Scale",
            "                    W = Vanilla Elytra,  B = Dragonbone",
            "Repairs with Phantom Membranes. Abilities under Endgame.")),
        new Entry("Crafting", "Void Toolset", List.of(
            "Standard vanilla shapes using Void Ingots + Sticks:",
            "Sword: 2 ingots + stick. Pickaxe: 3 ingots + 2 sticks.",
            "Axe: 3 ingots + 2 sticks offset. Shovel: 1 ingot + 2 sticks.",
            "Hoe: 2 ingots + 2 sticks offset.",
            "Netherite-tier stats before any enchantments.")),
        new Entry("Crafting", "Void Armor", List.of(
            "Classic shapes in Void Ingots:",
            "Helmet 5, Chestplate 8, Leggings 7, Boots 4.",
            "Netherite-tier protection plus the piece bonuses listed",
            "under Items > Void Armor Set.")),
        new Entry("Crafting", "Utility Crafts", List.of(
            "VOID ANCHOR: sneak-use binds a point, use recalls (60s).",
            "VOID DASH: pure mobility. VOID LANTERN: plants void lamps.",
            "ASH SIFTER works on ashen soil; CHORUS PRUNER on chorus wood.",
            "ARCHIVE KEY: Archive Fragment + Resonance Core + Eye of Ender.",
            "THIS GUIDE: book + 2 paper + ender pearl, shapeless!")),
        new Entry("Landmarks", "Flagship Rules", List.of(
            "Ten biomes, ten flagships. They spawn far apart and never",
            "straddle a biome border.",
            "Between them, every region also scatters smaller LANDMARKS -",
            "fossil arches, watchtowers, bell cairns, shard spires and more,",
            "roughly one every few hundred blocks. Each holds loot and a",
            "wakeable mini-mechanism.",
            "Lost? /endesium locate structure <name> gives an anchor.",
            "Names: dust_cathedral, elderwood_sanctum, skyrend_keep,",
            "drowned_cathedral, lumen_cathedral, great_caldera,",
            "sunken_geode, void_spire, crown_observatory, null_archive.")),
        new Entry("Landmarks", "Dust Cathedral (Wastes)", List.of(
            "Cruciform nave, twin bell towers, rose window over the apse.",
            "Mechanism on the altar dais; the REAL treasure is the crypt:",
            "descend the side stair beneath the crossing for vaults and",
            "a spire-treasure chest.")),
        new Entry("Landmarks", "Sanctum & Skyrend", List.of(
            "ELDERWOOD SANCTUM (Wilds): climb INSIDE the mother tree on",
            "spiral ledges; mid-gallery loot and a root vault below.",
            "SKYREND KEEP (Highlands): full castle with throne core,",
            "a barracks phantom spawner, and courtyard cache chests.")),
        new Entry("Landmarks", "Drowned & Lumen", List.of(
            "DROWNED CATHEDRAL (Marshes): sunken nave, black water pools,",
            "leaning gold bell tower; crypt alcoves flank the altar isle.",
            "LUMEN CATHEDRAL (Groves): glowing glass walls, prism gardens",
            "outside, three loot stations within.")),
        new Entry("Landmarks", "Caldera & Geode", List.of(
            "GREAT CALDERA (Ashen): lava lake, lava rivers, magma veins.",
            "A barred tunnel in the south root leads to the blaze-guarded",
            "VAULT - the best loot in the region.",
            "GEODE (Barrens): descend inside the crystal dome to the Heart",
            "pit; clusters glow around a pale monolith.")),
        new Entry("Landmarks", "Spire, Crown, Archive", List.of(
            "VOID SPIRE (Skirts): tallest thing in the End; climb the",
            "core ledges to the summit chamber and richest cache.",
            "CROWN OBSERVATORY (Crown): sealed glass orb atop a ziggurat.",
            "NULL ARCHIVE (Umbral): SEALED until the Dragon dies.")),
        new Entry("Endgame", "The Dragon", List.of(
            "The first fight stays vanilla - prepare like classic End.",
            "FIRST KILL drops: guidebook, 4 Dragon Scales, Dragonbone,",
            "Ender Essence, Resonance Echo, Archive Fragment:",
            "the entire Resonant Wings kit plus more.")),
        new Entry("Endgame", "The Transformation", List.of(
            "When the Dragon dies, the End answers permanently: dormant",
            "mechanisms reach farther and the respawned Dragon fights in",
            "four escalating phases.",
            "It survives restarts and respawning never resets it.")),
        new Entry("Endgame", "Null Archive & Sigil", List.of(
            "After the transformation, the Null Archive unseals.",
            "Wake its well-shaft core for the ARCHIVE SIGIL: proof the",
            "End was never finished.",
            "Its alcoves hold the deepest caches in the mod.")),
        new Entry("Endgame", "Resonant Wings", List.of(
            "While worn: glide about TWICE as fast, +4 armor, three extra",
            "hearts, halved fall damage, higher step, less knockback.",
            "SONIC BOOM bound to R (configurable): 40-block ray, 8 armor-",
            "ignoring damage, 15s cooldown persisted server-side.",
            "This is the power spike everything above builds toward.")),
        new Entry("Endgame", "The Wardens", List.of(
            "Every flagship vault is guarded by an END WARDEN wearing its",
            "region's colors: crests in the Wastes, horns at the Caldera,",
            "halos in the Groves. It telegraphs a signature attack, then",
            "periodically RAISES GUARD - frontal hits do almost nothing.",
            "Flank it, or bait the guard and punish the recovery.",
            "At two-thirds health it calls local kin; below half it enrages.",
            "Kill it for its region-keyed Warden Sigil.")),
        new Entry("Endgame", "Sigils & Attunement", List.of(
            "CARRY a sigil: slow regeneration anywhere in the End.",
            "USE a sigil to attune it: PERMANENT +1 heart, up to +10 hearts.",
            "Attune all TEN regions and become WARDEN ASCENDANT: a hidden",
            "challenge, a visible aura, and a regeneration pulse forever.",
            "Ten wardens stand between you and that.")),
        new Entry("Endgame", "The End Golem", List.of(
            "Where the dragon falls, something older wakes: a 300-HP engine",
            "with three phases - slams, homing resonance barrages with minion",
            "summons, then burning ground and faster shockwaves.",
            "KEY MECHANIC: deal 60+ damage within 8 seconds to STAGGER it -",
            "it kneels for five seconds and takes DOUBLE damage. Build your",
            "burst around the window. Sidestep the beam sweep; blasts barely",
            "hurt it, so bring your sword.")),
        new Entry("Endgame", "Golem Cores & Effigy", List.of(
            "The Golem drops GOLEM CORES (3-5 per kill). Carry them for",
            "Resistance in the End, or absorb each for PERMANENTLY +1 heart",
            "AND +0.25 attack damage (max +10 / +4).",
            "Absorb ten cores to unlock GOLEM'S RESOLVE: once per day, death",
            "refuses you. Craft a GOLEM EFFIGY (dragonbone + void bricks + a",
            "core) to summon another golem whenever one is ready to fall.")),
        new Entry("Endgame", "Advancement Checklist", List.of(
            "Order: Into the Wastes/Wilds -> First Resonance -> Whispers ->",
            "Sunken Archive -> Echo Sight -> What Remains (Spire) ->",
            "Long Resonance -> per-biome chain -> The End Answers ->",
            "Archive Awakened -> Warden's Bane -> Sigil Attuned ->",
            "The Engine Falls Silent -> Heavier Than Stone -> hidden:",
            "Warden Ascendant, Golem's Resolve, Effigy Ignited.",
            "If stuck: reread Landmarks. The answer is always out there.")));
}
