package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.block.InscribedSlateBlock;
import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The landmark tier of Endesium worldgen: medium, hand-authored builds placed
 * an order of magnitude more often than flagships (one attempt-cell per
 * 16-chunk block via the {@code endesium_landmarks} structure set, roughly
 * every 256 blocks per region) so a player always has something to find within
 * a few minutes of exploration.
 *
 * <p>Every landmark is small but characterful: biome-native materials, at
 * least one loot container, and exactly one mechanism or trap element.</p>
 *
 * <p>Generation entry: {@link com.infernodude777.endesium.world.structure.EndesiumLandmarkStructure}
 * (a registered vanilla Structure) calls {@link #generateInto} from its piece;
 * the old Feature lattice was retired in the structures migration.</p>
 */
public final class RegionLandmarkFeature {

    /** Biome-seam probe ring radius around the anchor. */
    public static final int PROBE_RADIUS = 10;
    /** Support ring radius for the slope-spread check. */
    public static final int SUPPORT_RING_RADIUS = 5;
    /** Largest height spread across the support ring the builder tolerates. */
    public static final int MAX_SLOPE_SPREAD = 6;
    /** Largest half-extent of any landmark build, including scatter. */
    public static final int MAX_BUILD_EXTENT = 14;

    /**
     * The exact site checks the piece applies, evaluated purely on noise so
     * {@code /locate} and structure-set placement can never report a candidate
     * that generation would refuse (mirrors the flagship check).
     *
     * @param surfaceY noise surface height at the anchor (the Y the piece sits on)
     */
    public static boolean siteValid(Structure.GenerationContext context, int centerX, int centerZ,
            int surfaceY, int region) {
        try {
            var source = context.chunkGenerator().getBiomeSource();
            var sampler = context.randomState().sampler();
            int minY = context.chunkGenerator().getMinY();
            if (surfaceY < minY + 8) return false; // anchor floats over the void

            // Biome seam ring at the height the structure will occupy.
            int qy = QuartPos.fromBlock(surfaceY);
            for (int i = 0; i < 8; i++) {
                double ang = Math.PI / 4.0D * i;
                int qx = QuartPos.fromBlock(centerX + (int) Math.round(Math.cos(ang) * PROBE_RADIUS));
                int qz = QuartPos.fromBlock(centerZ + (int) Math.round(Math.sin(ang) * PROBE_RADIUS));
                if (EndBiomeProfiles.regionOf(source.getNoiseBiome(qx, qy, qz, sampler)) != region) {
                    return false;
                }
            }
            // Slope spread across the support ring, matching the piece.
            int lowest = Integer.MAX_VALUE, highest = Integer.MIN_VALUE;
            for (int i = 0; i < 8; i++) {
                double ang = Math.PI / 4.0D * i;
                int sx = centerX + (int) Math.round(Math.cos(ang) * SUPPORT_RING_RADIUS);
                int sz = centerZ + (int) Math.round(Math.sin(ang) * SUPPORT_RING_RADIUS);
                int sy = context.chunkGenerator().getFirstOccupiedHeight(sx, sz,
                        Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                if (sy < lowest) lowest = sy;
                if (sy > highest) highest = sy;
            }
            return highest - lowest <= MAX_SLOPE_SPREAD;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates the site around {@code base} and builds this region's landmark.
     * Called from StructurePiece generation; every write is clipped to the
     * active piece box by {@link StructurePlacement}.
     */
    public static boolean generateInto(WorldGenLevel level, BlockPos base, int region, RandomSource random) {
        int bx = base.getX();
        int bz = base.getZ();

        // Landmarks keep compact footprints so they never leave the guarded
        // generation region; probe a modest ring for biome seams anyway.
        try {
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, bx, bz);
            // Per-column support: sample a ring around the anchor and reject
            // sites where the ground falls away, so compact builds never
            // float over a slope or straddle a cliff edge.
            int lowest = y;
            int highest = y;
            for (int i = 0; i < 8; i++) {
                double ang = Math.PI / 4.0D * i;
                int sx = bx + (int) Math.round(Math.cos(ang) * 5.0D);
                int sz = bz + (int) Math.round(Math.sin(ang) * 5.0D);
                int sy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sx, sz);
                if (sy < lowest) lowest = sy;
                if (sy > highest) highest = sy;
            }
            if (highest - lowest > 6) {
                Endesium.LOGGER.info("Landmark site [{}, {}] skipped: slope spread {} blocks",
                        bx, bz, highest - lowest);
                return false;
            }
            for (int i = 0; i < 8; i++) {
                double ang = Math.PI / 4.0D * i;
                BlockPos edge = base.offset((int) Math.round(Math.cos(ang) * 10),
                        0, (int) Math.round(Math.sin(ang) * 10));
                int edgeRegion = EndBiomeProfiles.regionOf(level.getBiome(edge));
                if (edgeRegion != region) {
                    Endesium.LOGGER.info(
                            "Landmark site [{}, {}] skipped: biome seam (edge region {} != {})",
                            bx, bz, edgeRegion, region);
                    return false;
                }
            }
            switch (region) {
                case EndesiumRegions.END_WASTES -> duneFossilArch(level, base, random);
                case EndesiumRegions.CHORUS_WILDS -> hollowStump(level, base, random);
                case EndesiumRegions.SHATTERED_HIGHLANDS -> windvaneWatchtower(level, base, random);
                case EndesiumRegions.VOID_MARSHES -> mireBellCairn(level, base, random);
                case EndesiumRegions.LUMINOUS_GROVES -> lightwellGazebo(level, base, random);
                case EndesiumRegions.ASHEN_EXPANSE -> emberShrine(level, base, random);
                case EndesiumRegions.CRYSTAL_BARRENS -> shardSpireCluster(level, base, random);
                case EndesiumRegions.VOID_SKIRTS -> anchorRuin(level, base, random);
                case EndesiumRegions.VOID_CROWN -> needleCircle(level, base, random);
                case EndesiumRegions.UMBRAL_REACH -> nullObelisk(level, base, random);
                default -> { return false; }
            }
        } catch (Exception e) {
            Endesium.LOGGER.error("Endesium landmark generation failed near [{}, {}]", bx, bz, e);
            return false;
        }
        return true;
    }

    // =====================================================================
    // Shared helpers (mirrors BiomeStructureFeature, kept local on purpose
    // until the planned structure-builder refactor consolidates them).
    // =====================================================================

    private static BlockPos off(BlockPos b, int dx, int dy, int dz) {
        return b.offset(dx, dy, dz);
    }

    private static void fill(WorldGenLevel l, BlockPos b, int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
        int nx0 = Math.min(x1, x2), nx1 = Math.max(x1, x2);
        int ny0 = Math.min(y1, y2), ny1 = Math.max(y1, y2);
        int nz0 = Math.min(z1, z2), nz1 = Math.max(z1, z2);
        for (int dx = nx0; dx <= nx1; dx++)
            for (int dy = ny0; dy <= ny1; dy++)
                for (int dz = nz0; dz <= nz1; dz++)
                    setReplace(l, off(b, dx, dy, dz), block);
    }

    private static void col(WorldGenLevel l, BlockPos b, int dx, int dz, int y1, int y2, Block block) {
        fill(l, b, dx, y1, dz, dx, y2, dz, block);
    }

    private static void disc(WorldGenLevel l, BlockPos b, int cx, int y, int cz, int r, Block block) {
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++)
                if (dx * dx + dz * dz <= r * r)
                    setReplace(l, off(b, cx + dx, y, cz + dz), block);
    }

    private static void ring(WorldGenLevel l, BlockPos b, int cx, int y, int cz, int r, Block block) {
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++) {
                int d = dx * dx + dz * dz;
                if (d <= r * r && d > (r - 1) * (r - 1))
                    setReplace(l, off(b, cx + dx, y, cz + dz), block);
            }
    }

    private static void setReplace(WorldGenLevel l, BlockPos p, Block block) {
        StructurePlacement.set(l, p, block.defaultBlockState(), 3);
    }

    private static void lootChest(WorldGenLevel l, BlockPos p, RandomSource rnd, String table) {
        setReplace(l, p, Blocks.CHEST);
        if (l.getBlockEntity(p) instanceof ChestBlockEntity c) c.setLootTable(lootKey(table), rnd.nextLong());
    }

    private static void lootBarrel(WorldGenLevel l, BlockPos p, RandomSource rnd, String table) {
        setReplace(l, p, Blocks.BARREL);
        if (l.getBlockEntity(p) instanceof BarrelBlockEntity barrel) barrel.setLootTable(lootKey(table), rnd.nextLong());
    }

    private static ResourceKey<LootTable> lootKey(String t) {
        return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id(t));
    }

    private static void inscribe(WorldGenLevel l, BlockPos p, int symbol) {
        if (!l.getBlockState(p).isAir()) return;
        StructurePlacement.set(l, p, ModBlocks.INSCRIBED_SLATE.defaultBlockState()
                .setValue(InscribedSlateBlock.SYMBOL, symbol), 3);
    }

    /**
     * A wakeable mini-mechanism: the landmark's own small resonance beacon.
     * Waking it pays out the curated biome_landmark cache.
     */
    private static void landmarkBeacon(WorldGenLevel l, BlockPos p) {
        if (!l.getBlockState(p).isAir()) return;
        StructurePlacement.set(l, p, ModBlocks.RESONANT_MECHANISM.defaultBlockState(), 3);
        if (l.getBlockEntity(p) instanceof com.infernodude777.endesium.block.ResonantMechanismBlockEntity m) {
            m.setVariant(EndRuinVariant.LANDMARK_BEACON);
        }
    }

    // =====================================================================
    // END WASTES - Dune Fossil Arch
    // =====================================================================

    private static void duneFossilArch(WorldGenLevel l, BlockPos b, RandomSource r) {
        // A colossal petrified ribcage arching out of the dust.
        for (int i = -3; i <= 3; i++) {
            if (i == 0) continue;
            int h = 11 - Math.abs(i) * 2;
            col(l, b, i * 3, 0, 1, h, ModBlocks.WASTES_STONE);
            setReplace(l, off(b, i * 3, h + 1, 0), ModBlocks.CRACKED_SPIRE_STONE);
            // Rib spurs reaching toward the spine.
            fill(l, b, Math.min(i * 3, i * 3 - Integer.signum(i)), 1, 0,
                    Math.max(i * 3, i * 3 - Integer.signum(i)), h / 2 + 2, 0,
                    ModBlocks.CRACKED_SPIRE_STONE);
        }
        // The skull, half-buried at the north end.
        fill(l, b, -2, 1, -8, 2, 4, -5, ModBlocks.CRACKED_SPIRE_STONE);
        fill(l, b, -1, 2, -8, 1, 3, -7, Blocks.AIR);
        setReplace(l, off(b, -1, 2, -9), ModBlocks.VOID_GLASS);
        setReplace(l, off(b, 1, 2, -9), ModBlocks.VOID_GLASS);
        // Scree drifts piled against the ribs.
        for (int i = 0; i < 18; i++) {
            int dx = r.nextInt(17) - 8, dz = r.nextInt(13) - 6;
            if (dz == 0 && Math.abs(dx) <= 10) continue;
            setReplace(l, off(b, dx, 1, dz), r.nextBoolean() ? ModBlocks.WASTES_GRAVEL : ModBlocks.WASTES_STONE);
        }
        lootBarrel(l, off(b, 0, 1, 4), r, "chests/landmark_end_wastes");
        landmarkBeacon(l, off(b, 0, 1, -3));
        inscribe(l, off(b, -5, 1, 3), InscribedSlateBlock.SYMBOL_SPIRE);
    }

    // =====================================================================
    // CHORUS WILDS - Hollow Stump
    // =====================================================================

    private static void hollowStump(WorldGenLevel l, BlockPos b, RandomSource r) {
        // A snapped chorus titan stump, hollow and roofed with moss.
        disc(l, b, 0, 0, 0, 4, ModBlocks.ELDER_CHORUS_BARK);
        for (int y = 1; y <= 6; y++) {
            for (int dx = -4; dx <= 4; dx++)
                for (int dz = -4; dz <= 4; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > 16) continue;
                    if (d2 <= 6) { setReplace(l, off(b, dx, y, dz), Blocks.AIR); continue; }
                    setReplace(l, off(b, dx, y, dz), (dx + dz + y) % 4 == 0
                            ? ModBlocks.ELDER_CHORUS_WOOD : ModBlocks.ELDER_CHORUS_BARK);
                }
        }
        disc(l, b, 0, 7, 0, 4, ModBlocks.CHORUS_MOSS);
        disc(l, b, 0, 7, 0, 2, ModBlocks.CHORUS_ROOT);
        // A bloom lantern hangs in the hollow.
        col(l, b, 0, 0, 5, 6, Blocks.CHAIN);
        setReplace(l, off(b, 0, 4, 0), ModBlocks.RESONANT_BLOOM);
        // Root steps up to a shelf entrance.
        fill(l, b, 4, 1, 0, 5, 2, 1, Blocks.AIR);
        col(l, b, 5, 0, 0, 2, ModBlocks.CHORUS_ROOT);
        lootChest(l, off(b, -2, 1, -2), r, "chests/landmark_chorus_wilds");
        lootBarrel(l, off(b, 2, 1, 2), r, "chests/wilds_archive");
        landmarkBeacon(l, off(b, 0, 1, -1));
        inscribe(l, off(b, 6, 1, 0), InscribedSlateBlock.SYMBOL_RING);
    }

    // =====================================================================
    // SHATTERED HIGHLANDS - Windvane Watchtower
    // =====================================================================

    private static void windvaneWatchtower(WorldGenLevel l, BlockPos b, RandomSource r) {
        disc(l, b, 0, 0, 0, 4, ModBlocks.HIGHLAND_SLATE);
        for (int y = 1; y <= 12; y++) {
            fill(l, b, -2, y, -2, 2, y, 2, y % 5 == 0 ? ModBlocks.HIGHLAND_SLATE : ModBlocks.HIGHLAND_STONE);
            fill(l, b, -1, y, -1, 1, y, 1, Blocks.AIR);
        }
        // Foothold spirals up one inner wall for climbing.
        for (int y = 1; y <= 11; y++) {
            double ang = y * 0.9D;
            int fx = (int) Math.round(Math.cos(ang));
            int fz = (int) Math.round(Math.sin(ang));
            setReplace(l, off(b, fx, y, fz), ModBlocks.HIGHLAND_SLATE);
        }
        // Crenellated crown.
        for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            col(l, b, c[0], c[1], 13, 14, ModBlocks.HIGHLAND_STONE);
        }
        // The windvane: a lensstone vane on a bracket mast that reads the gale.
        col(l, b, 0, 0, 13, 16, ModBlocks.WINDSCAR_BRACKET);
        setReplace(l, off(b, 0, 17, 0), ModBlocks.HIGHLAND_LENSSTONE);
        setReplace(l, off(b, 1, 16, 0), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        setReplace(l, off(b, -1, 16, 0), ModBlocks.VOID_GLASS);
        // Doorway and supply cache.
        fill(l, b, -1, 1, 2, 1, 3, 2, Blocks.AIR);
        lootBarrel(l, off(b, -1, 1, -1), r, "chests/landmark_shattered_highlands");
        lootBarrel(l, off(b, 1, 1, -1), r, "chests/highlands_summit");
        landmarkBeacon(l, off(b, 3, 1, 2));
        inscribe(l, off(b, 3, 1, 3), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // VOID MARSHES - Mire Bell Cairn
    // =====================================================================

    private static void mireBellCairn(WorldGenLevel l, BlockPos b, RandomSource r) {
        disc(l, b, 0, 0, 0, 3, ModBlocks.VOID_MARSH_SOIL);
        // Stacked cairn courses, each smaller than the last.
        fill(l, b, -2, 1, -2, 2, 2, 2, ModBlocks.TIDE_IRON);
        fill(l, b, -1, 3, -1, 1, 4, 1, ModBlocks.MARSH_MOSS);
        setReplace(l, off(b, 0, 5, 0), ModBlocks.TIDE_IRON);
        // The bell: gold clapper suspended from a chain gallows.
        col(l, b, 0, 3, 6, 8, Blocks.CHAIN);
        col(l, b, -2, 3, 6, 7, ModBlocks.TIDE_IRON);
        col(l, b, 2, 3, 6, 7, ModBlocks.TIDE_IRON);
        fill(l, b, -2, 8, 3, 2, 8, 3, ModBlocks.TIDE_IRON);
        setReplace(l, off(b, 0, 5, 3), Blocks.GOLD_BLOCK);
        // Drowned offerings ring the base; reeds take the rest.
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4.0D;
            int dx = (int) Math.round(Math.cos(ang) * 4);
            int dz = (int) Math.round(Math.sin(ang) * 4);
            setReplace(l, off(b, dx, 0, dz), i % 2 == 0 ? Blocks.GOLD_BLOCK : ModBlocks.MARSH_MOSS);
        }
        for (int i = 0; i < 6; i++) {
            int dx = r.nextInt(9) - 4, dz = r.nextInt(9) - 4;
            if (dx == 0 && dz == 0) continue;
            setReplace(l, off(b, dx, 1, dz), ModBlocks.VOID_REED);
        }
        lootBarrel(l, off(b, 2, 1, -2), r, "chests/landmark_void_marshes");
        landmarkBeacon(l, off(b, 3, 1, 0));
        inscribe(l, off(b, -3, 1, 3), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // LUMINOUS GROVES - Lightwell Gazebo
    // =====================================================================

    private static void lightwellGazebo(WorldGenLevel l, BlockPos b, RandomSource r) {
        disc(l, b, 0, 0, 0, 3, ModBlocks.LUMEN_STONE);
        // Six lumen pilasters carrying a prism dome.
        for (int i = 0; i < 6; i++) {
            double ang = i * Math.PI / 3.0D;
            int px = (int) Math.round(Math.cos(ang) * 3);
            int pz = (int) Math.round(Math.sin(ang) * 3);
            col(l, b, px, pz, 1, 4, ModBlocks.LUMEN_STONE);
            setReplace(l, off(b, px, 5, pz), ModBlocks.PRISM_CANOPY_BLOCK);
        }
        disc(l, b, 0, 6, 0, 2, ModBlocks.PRISM_CANOPY_BLOCK);
        // The lightwell itself: a glowing pool sunk into the floor.
        fill(l, b, -1, -1, -1, 1, -1, 1, ModBlocks.LUMEN_MOSS);
        setReplace(l, off(b, 0, -1, 0), ModBlocks.LUMEN_GRAFT_BLOCK);
        landmarkBeacon(l, off(b, 0, 1, 2));
        // Bloom planters at the entrances.
        for (int[] g : new int[][]{{0, 4}, {0, -4}, {4, 0}, {-4, 0}}) {
            setReplace(l, off(b, g[0], 1, g[1]), ModBlocks.LUMEN_BLOOM);
        }
        lootChest(l, off(b, -2, 1, 2), r, "chests/landmark_luminous_groves");
        lootBarrel(l, off(b, 2, 1, -2), r, "chests/prism_canopy");
        inscribe(l, off(b, 3, 1, 3), InscribedSlateBlock.SYMBOL_RING);
    }

    // =====================================================================
    // ASHEN EXPANSE - Ember Shrine
    // =====================================================================

    private static void emberShrine(WorldGenLevel l, BlockPos b, RandomSource r) {
        disc(l, b, 0, 0, 0, 4, ModBlocks.ASH_STONE);
        // Raised basalt shrine platform with a magma channel moat.
        ring(l, b, 0, 1, 0, 4, Blocks.MAGMA_BLOCK);
        fill(l, b, -3, 1, -3, 3, 1, 3, ModBlocks.RESONANT_BASALT);
        fill(l, b, -1, 2, -1, 1, 2, 1, ModBlocks.ASHEN_CRUST != null ? ModBlocks.ASHEN_CRUST : ModBlocks.ASH_STONE);
        // Altar hood: two pillars and a lintel over the ember heart.
        col(l, b, -2, -2, 2, 5, ModBlocks.RESONANT_BASALT);
        col(l, b, 2, -2, 2, 5, ModBlocks.RESONANT_BASALT);
        fill(l, b, -2, 6, -2, 2, 6, -2, ModBlocks.RESONANT_BASALT);
        setReplace(l, off(b, 0, 5, -2), Blocks.MAGMA_BLOCK);
        setReplace(l, off(b, 0, 4, -2), Blocks.LAVA);
        // Ember vents hissing around the approach - step carefully.
        for (int[] v : new int[][]{{-4, 2}, {4, 2}, {0, 5}}) {
            setReplace(l, off(b, v[0], 1, v[1]), Blocks.MAGMA_BLOCK);
            setReplace(l, off(b, v[0], 2, v[1]), Blocks.FIRE);
        }
        lootChest(l, off(b, 0, 2, 2), r, "chests/landmark_ashen_expanse");
        lootBarrel(l, off(b, -3, 2, 3), r, "chests/ashen_volcano");
        landmarkBeacon(l, off(b, 2, 2, 2));
        inscribe(l, off(b, 4, 1, 4), InscribedSlateBlock.SYMBOL_SPIRE);
    }

    // =====================================================================
    // CRYSTAL BARRENS - Shard Spire Cluster
    // =====================================================================

    private static void shardSpireCluster(WorldGenLevel l, BlockPos b, RandomSource r) {
        disc(l, b, 0, 0, 0, 5, ModBlocks.CRYSTAL_SHARD_BLOCK);
        // Three faceted spires: tall center, twin flanks.
        buildShardSpire(l, b, 0, 0, 11, true);
        buildShardSpire(l, b, -3, 2, 7, false);
        buildShardSpire(l, b, 3, -2, 8, false);
        // Shallow dig pit between them hiding a cache under a crystal cap.
        fill(l, b, -1, -1, -1, 1, -1, 1, Blocks.AIR);
        lootChest(l, off(b, 0, -2, 0), r, "chests/landmark_crystal_barrens");
        setReplace(l, off(b, 0, -1, 0), ModBlocks.PALE_CRYSTAL_BLOCK);
        // Scatter growth catching the light.
        for (int i = 0; i < 8; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0D;
            int dx = (int) Math.round(Math.cos(ang) * (4 + r.nextInt(3)));
            int dz = (int) Math.round(Math.sin(ang) * (4 + r.nextInt(3)));
            setReplace(l, off(b, dx, 1, dz), ModBlocks.CRYSTAL_CLUSTER);
        }
        inscribe(l, off(b, -4, 1, -4), InscribedSlateBlock.SYMBOL_EYE);
    }

    private static void buildShardSpire(WorldGenLevel l, BlockPos b, int cx, int cz, int height, boolean core) {
        for (int y = 1; y <= height; y++) {
            int half = Math.max(0, (height - y) / 5);
            fill(l, b, cx - half, y, cz - half, cx + half, y, cz + half,
                    (y + cx + cz) % 3 == 0 ? ModBlocks.DARK_CRYSTAL_BLOCK : ModBlocks.PALE_CRYSTAL_BLOCK);
        }
        setReplace(l, off(b, cx, height + 1, cz), core ? ModBlocks.DORMANT_RESONANT_CRYSTAL : ModBlocks.CRYSTAL_CLUSTER);
        if (core) landmarkBeacon(l, off(b, cx, 1, cz + 4));
    }

    // =====================================================================
    // VOID SKIRTS - Anchor Ruin
    // =====================================================================

    private static void anchorRuin(WorldGenLevel l, BlockPos b, RandomSource r) {
        disc(l, b, 0, 0, 0, 5, ModBlocks.VOID_SLATE);
        // A fallen sky-anchor: tilted monolith pinned by chain tethers.
        for (int k = 0; k <= 8; k++) {
            int px = k - 4;
            int py = 1 + Math.abs(k - 4) / 2;
            fill(l, b, px, py, -1, px, py + 1, 1, k % 3 == 0 ? ModBlocks.VOID_BRICK : ModBlocks.VOIDSTONE);
        }
        // Tether chains running down to bedplate stubs.
        for (int[] t : new int[][]{{-5, -3}, {5, 3}}) {
            col(l, b, t[0] / 2, t[1] / 2, 1, 3, Blocks.CHAIN);
            setReplace(l, off(b, t[0] / 2, 1, t[1] / 2), ModBlocks.VOID_BRICK);
        }
        // Umbral tufts reclaiming the plate.
        for (int i = 0; i < 6; i++) {
            int dx = r.nextInt(9) - 4, dz = r.nextInt(9) - 4;
            setReplace(l, off(b, dx, 1, dz), ModBlocks.UMBRAL_GRASS);
        }
        lootBarrel(l, off(b, 3, 1, -3), r, "chests/landmark_void_skirts");
        lootBarrel(l, off(b, -3, 1, 3), r, "chests/end_spire");
        landmarkBeacon(l, off(b, 0, 1, 4));
        inscribe(l, off(b, 0, 1, 5), InscribedSlateBlock.SYMBOL_SPIRE);
    }

    // =====================================================================
    // VOID CROWN - Needle Circle
    // =====================================================================

    private static void needleCircle(WorldGenLevel l, BlockPos b, RandomSource r) {
        disc(l, b, 0, 0, 0, 4, ModBlocks.VOID_SLATE);
        // Five crown needles ringing a sealed center.
        for (int i = 0; i < 5; i++) {
            double ang = i * 2.0D * Math.PI / 5.0D;
            int px = (int) Math.round(Math.cos(ang) * 3);
            int pz = (int) Math.round(Math.sin(ang) * 3);
            int h = 6 + (i % 2) * 2;
            col(l, b, px, pz, 1, h, ModBlocks.VOID_BRICK);
            setReplace(l, off(b, px, h + 1, pz), ModBlocks.CROWN_NEEDLE_BLOCK);
        }
        // Sealed altar at the center: seal cap over a hidden cache.
        setReplace(l, off(b, 0, 1, 0), ModBlocks.CROWN_SEAL_BLOCK);
        fill(l, b, -1, 0, -1, 1, 0, 1, ModBlocks.VOIDSTONE);
        fill(l, b, -1, -1, -1, 1, -1, 1, Blocks.AIR);
        lootChest(l, off(b, 0, -2, 0), r, "chests/landmark_void_crown");
        setReplace(l, off(b, 0, -1, 0), ModBlocks.CROWN_SEAL_BLOCK);
        lootBarrel(l, off(b, -3, 1, 3), r, "chests/crown_observatory");
        landmarkBeacon(l, off(b, 2, 1, -2));
        inscribe(l, off(b, 4, 1, 0), InscribedSlateBlock.SYMBOL_RING);
    }

    // =====================================================================
    // UMBRAL REACH - Null Obelisk
    // =====================================================================

    private static void nullObelisk(WorldGenLevel l, BlockPos b, RandomSource r) {
        disc(l, b, 0, 0, 0, 3, ModBlocks.VOID_SOIL);
        fill(l, b, -2, 1, -2, 2, 1, 2, ModBlocks.THRESHOLD_CORE_BLOCK);
        // A windowless obelisk rising nine blocks, frame-ribbed.
        for (int y = 2; y <= 10; y++) {
            int half = y > 8 ? 0 : 1;
            fill(l, b, -half, y, -half, half, y, half, ModBlocks.NULL_ARCHIVE_FRAME);
            if (y % 3 == 0) fill(l, b, -1, y, -1, 1, y, 1, ModBlocks.VOID_WEAVE);
        }
        setReplace(l, off(b, 0, 11, 0), ModBlocks.THRESHOLD_CORE_BLOCK);
        // No lamps here: the archive's silence is the point. The cache hides
        // behind the obelisk's shadow side.
        lootChest(l, off(b, 0, 2, 3), r, "chests/landmark_umbral_reach");
        lootBarrel(l, off(b, 2, 1, -2), r, "chests/hollow_threshold");
        landmarkBeacon(l, off(b, 2, 2, -2));
        inscribe(l, off(b, -3, 1, 0), InscribedSlateBlock.SYMBOL_EYE);
    }
}
