package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.block.InscribedSlateBlock;
import com.infernodude777.endesium.block.ResonantMechanismBlockEntity;
import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The ten Endesium biome-capitals. Each region holds exactly one hand-authored
 * flagship built on the titan silhouette: splayed roots, an irregular boulder
 * body, a narrow neck, and a vast radial-spoke canopy with a fringed, hanging
 * underside - open sky shows between the canopy ribs. Every flagship houses a
 * resonant mechanism whose {@link EndRuinVariant} carries that region's Lens
 * signature.
 *
 * <p>Generation entry: {@link com.infernodude777.endesium.world.structure.EndesiumFlagshipStructure}
 * (a registered vanilla Structure) validates the site per generating chunk and
 * calls {@link #generateInto}.</p>
 *
 * <p>Build geometry is capped at a 21 block half-extent from the anchor so the
 * single-pass anchor-chunk build always stays inside the primed 3x3 chunk
 * region that worldgen allows writes into.</p>
 */
public final class BiomeStructureFeature {

    /** Largest half-footprint across all ten flagships, including skirts. */
    public static final int MAX_FOOTPRINT_RADIUS = 16;

    /** Largest half-extent of any single flagship build, including scatter. */
    public static final int MAX_BUILD_EXTENT = 21;

    /** Height spread across a footprint that the terrace builder can dress. */
    public static final int MAX_TERRAIN_SPREAD = 14;

    /** Half-footprint of each region's flagship, including terrace skirts. */
    public static int footprintRadius(int region) {
        return switch (region) {
            case EndesiumRegions.SHATTERED_HIGHLANDS, EndesiumRegions.ASHEN_EXPANSE -> 16;
            case EndesiumRegions.VOID_SKIRTS -> 15;
            default -> 13;
        };
    }

    /**
     * The exact site checks the piece applies, evaluated purely on noise so
     * {@code /locate} and structure-set placement can never report a candidate
     * that generation would refuse. Vanilla runs {@code findGenerationPoint}
     * for both paths, so agreement here is what keeps locate truthful.
     *
     * @param surfaceY noise surface height at the anchor (the Y the piece sits on)
     */
    public static boolean siteValid(Structure.GenerationContext context, int centerX, int centerZ,
            int surfaceY, int region) {
        try {
            var source = context.chunkGenerator().getBiomeSource();
            var sampler = context.randomState().sampler();
            int radius = footprintRadius(region);

            // Never straddle a biome border: sample the full probe ring at the
            // height the structure will actually occupy.
            int probeDist = radius + 2;
            int qy = QuartPos.fromBlock(surfaceY);
            for (int i = 0; i < 8; i++) {
                double ang = Math.PI / 4.0D * i;
                int qx = QuartPos.fromBlock(centerX + (int) Math.round(Math.cos(ang) * probeDist));
                int qz = QuartPos.fromBlock(centerZ + (int) Math.round(Math.sin(ang) * probeDist));
                if (EndBiomeProfiles.regionOf(source.getNoiseBiome(qx, qy, qz, sampler)) != region) {
                    return false;
                }
            }

            // Support: the footprint must rest on real island terrain (not
            // void) for the same 58% of sampled columns the block-level check
            // demands, bailing early once too many void columns are seen.
            int minY = context.chunkGenerator().getMinY();
            int lowest = Integer.MAX_VALUE, highest = Integer.MIN_VALUE;
            int samples = radius + 1;
            int total = samples * samples;
            int allowedFailures = total - (total * 58 + 99) / 100;
            int failed = 0;
            for (int dx = -radius; dx <= radius; dx += 2) {
                for (int dz = -radius; dz <= radius; dz += 2) {
                    int h = context.chunkGenerator().getFirstOccupiedHeight(centerX + dx, centerZ + dz,
                            Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                    if (h >= minY + 24) {
                        if (h < lowest) lowest = h;
                        if (h > highest) highest = h;
                    } else if (++failed > allowedFailures) {
                        return false;
                    }
                }
            }
            return highest - lowest <= MAX_TERRAIN_SPREAD;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates the site around {@code base} and builds this region's flagship.
     * Called from StructurePiece generation; every write is clipped to the
     * active piece box by {@link StructurePlacement}.
     */
    public static boolean generateInto(WorldGenLevel level, BlockPos base, int region, RandomSource random) {
        int bx = base.getX();
        int bz = base.getZ();

        int footprintRadius = footprintRadius(region);

        try {
            // Never straddle a biome border: sample a full ring around the
            // entire footprint. If any sample falls in a different region,
            // skip rather than build half a flagship against a geology seam.
            int probeDist = footprintRadius + 2;
            for (int i = 0; i < 8; i++) {
                double ang = Math.PI / 4.0D * i;
                int px = (int) Math.round(Math.cos(ang) * probeDist);
                int pz = (int) Math.round(Math.sin(ang) * probeDist);
                BlockPos edge = base.offset(px, 0, pz);
                int edgeRegion = EndBiomeProfiles.regionOf(level.getBiome(edge));
                if (edgeRegion != region) {
                    Endesium.LOGGER.info(
                            "Flagship site [{}, {}] skipped: biome seam (edge region {} != {})",
                            bx, bz, edgeRegion, region);
                    return false;
                }
            }

            if (!hasSolidFootprint(level, base, footprintRadius)) {
                Endesium.LOGGER.info("Flagship site [{}, {}] skipped: unsupportive footprint", bx, bz);
                return false;
            }
            switch (region) {
                case EndesiumRegions.END_WASTES -> dustCathedral(level, base, random);
                case EndesiumRegions.CHORUS_WILDS -> elderwoodSanctum(level, base, random);
                case EndesiumRegions.SHATTERED_HIGHLANDS -> skyrendKeep(level, base, random);
                case EndesiumRegions.VOID_MARSHES -> drownedCathedral(level, base, random);
                case EndesiumRegions.LUMINOUS_GROVES -> lumenCathedral(level, base, random);
                case EndesiumRegions.ASHEN_EXPANSE -> greatCaldera(level, base, random);
                case EndesiumRegions.CRYSTAL_BARRENS -> sunkenGeode(level, base, random);
                case EndesiumRegions.VOID_SKIRTS -> voidSpire(level, base, random);
                case EndesiumRegions.VOID_CROWN -> crownObservatory(level, base, random);
                case EndesiumRegions.UMBRAL_REACH -> nullArchive(level, base, random);
                default -> { return false; }
            }
        } catch (Exception e) {
            Endesium.LOGGER.error("Endesium flagship generation failed near [{}, {}]", bx, bz, e);
            return false;
        }
        return true;
    }

    // =====================================================================
    // Shared helpers
    // =====================================================================

    private static BlockPos off(BlockPos b, int dx, int dy, int dz) {
        return b.offset(dx, dy, dz);
    }

    /** Inclusive box fill through the protected writer. */
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

    /** Hanging column that stops at the first obstruction, for tendrils. */
    private static void hangColumn(WorldGenLevel l, BlockPos b, int dx, int yTop, int dz, int len, Block block) {
        for (int k = 0; k < len; k++) {
            BlockPos p = off(b, dx, yTop - k, dz);
            if (!l.getBlockState(p).isAir()) break;
            setReplace(l, p, block);
        }
    }

    /**
     * Splayed tapering roots: thick legs leaking out of the body's lower half
     * and stepping down to the ground, with open air between them.
     */
    private static void titanRoots(WorldGenLevel l, BlockPos b, Block body, Block accent) {
        for (int i = 0; i < 7; i++) {
            double ang = i * 2.0D * Math.PI / 7.0D + 0.25D;
            double cos = Math.cos(ang), sin = Math.sin(ang);
            int reach = 12 + (i % 3) * 2;
            int yStart = 9 - (i % 2) * 2;
            int steps = Math.max(1, reach - 7);
            for (int k = 0; k <= steps; k++) {
                double t = (double) k / steps;
                int dx = (int) Math.round(cos * (6 + t * (reach - 6)));
                int dz = (int) Math.round(sin * (6 + t * (reach - 6)));
                int y = (int) Math.round(yStart - t * (yStart - 1));
                int th = k < steps / 2 ? 1 : 0;
                for (int px = -th; px <= th; px++)
                    for (int pz = -th; pz <= th; pz++) {
                        setReplace(l, off(b, dx + px, y, dz + pz),
                                (k % 3 == 0 && accent != null) ? accent : body);
                    }
            }
        }
    }

    /**
     * The boulder body: a lobed, rough-shelled mass that bulges at its middle
     * and is hollow above knee height so it can be entered and climbed.
     */
    private static void titanBody(WorldGenLevel l, BlockPos b, Block body, Block accent) {
        for (int y = 3; y <= 15; y++) {
            double t = (y - 3) / 12.0D;
            int baseRad = (int) Math.round(8.0D + 3.0D * Math.sin(t * Math.PI));
            for (int dx = -13; dx <= 13; dx++)
                for (int dz = -13; dz <= 13; dz++) {
                    double d = Math.sqrt(dx * dx + (double) dz * dz);
                    double ang = Math.atan2(dz, dx);
                    double edge = baseRad + 1.6D * Math.sin(ang * 3.0D + y * 0.9D);
                    if (d > edge) continue;
                    Block blk = accent != null && Math.floorMod(dx * 3 + dz * 5 + y, 9) < 2 ? accent : body;
                    if (d > edge - 1.3D || y <= 5) {
                        setReplace(l, off(b, dx, y, dz), blk);
                    } else {
                        setReplace(l, off(b, dx, y, dz), Blocks.AIR);
                    }
                }
        }
    }

    /** Tapering ribbed column with a hollow climbable core. */
    private static void ribbedColumn(WorldGenLevel l, BlockPos b, int y0, int rBase, int rTop, int height,
            Block block, Block accent) {
        for (int y = 0; y <= height; y++) {
            int rad = rBase + (rTop - rBase) * y / Math.max(1, height);
            for (int dx = -rad; dx <= rad; dx++)
                for (int dz = -rad; dz <= rad; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > rad * rad) continue;
                    if (rad > 3 && y > 0 && d2 <= (rad - 2) * (rad - 2)) {
                        setReplace(l, off(b, dx, y0 + y, dz), Blocks.AIR);
                        continue;
                    }
                    Block blk = accent != null && Math.floorMod(dx * 3 + dz * 5, 7) < 2 ? accent : block;
                    setReplace(l, off(b, dx, y0 + y, dz), blk);
                }
        }
    }

    /**
     * The signature canopy: radial spokes with open sky between them, tied by
     * short arc segments, jagged at the rim, with a raised center cap.
     */
    private static void titanCanopy(WorldGenLevel l, BlockPos b, int y, int spokes,
            Block canopy, Block arc, Block cap, Block tipCap) {
        for (int i = 0; i < spokes; i++) {
            double ang = i * 2.0D * Math.PI / spokes;
            double cos = Math.cos(ang), sin = Math.sin(ang);
            int tip = 17 + (i % 3);
            for (int k = 4; k <= tip; k++) {
                int dx = (int) Math.round(cos * k);
                int dz = (int) Math.round(sin * k);
                setReplace(l, off(b, dx, y, dz), canopy);
                if (k % 2 == 0) {
                    int px = (int) Math.round(-sin);
                    int pz = (int) Math.round(cos);
                    setReplace(l, off(b, dx + px, y, dz + pz), canopy);
                }
            }
            for (int rr : new int[]{9, 13, 17}) {
                if (rr > tip) continue;
                for (int s = -2; s <= 2; s++) {
                    double a2 = ang + s * (Math.PI * 2.0D / spokes) / 5.0D;
                    setReplace(l, off(b, (int) Math.round(Math.cos(a2) * rr), y,
                            (int) Math.round(Math.sin(a2) * rr)), arc);
                }
            }
        }
        disc(l, b, 0, y + 1, 0, 6, cap);
        disc(l, b, 0, y + 2, 0, 3, canopy);
        setReplace(l, off(b, 0, y + 3, 0), tipCap);
    }

    /** Chains and tip blocks hanging beneath a canopy rim. */
    private static void canopyHangers(WorldGenLevel l, BlockPos b, int yTop, int rInner, int rOuter, int count,
            RandomSource r, Block chain, Block tip, int minLen, int maxLen) {
        for (int i = 0; i < count; i++) {
            double ang = i * 2.0D * Math.PI / count + 0.3D;
            int dist = (i % 2 == 0) ? rOuter : rInner;
            int dx = (int) Math.round(Math.cos(ang) * dist);
            int dz = (int) Math.round(Math.sin(ang) * dist);
            int len = minLen + (i % Math.max(1, maxLen - minLen + 1));
            hangColumn(l, b, dx, yTop, dz, len, chain);
            setReplace(l, off(b, dx, yTop - len, dz), tip);
        }
    }

    /** Scattered debris ringing the base so the monument sits in a field. */
    private static void scatterDebris(WorldGenLevel l, BlockPos b, RandomSource r, int count, int minR, int maxR,
            Block a, Block b2) {
        for (int i = 0; i < count; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0D;
            int dist = minR + r.nextInt(Math.max(1, maxR - minR + 1));
            int dx = (int) Math.round(Math.cos(ang) * dist);
            int dz = (int) Math.round(Math.sin(ang) * dist);
            setReplace(l, off(b, dx, 1, dz), r.nextBoolean() ? a : b2);
        }
    }

    private static void setReplace(WorldGenLevel l, BlockPos p, Block block) {
        if (isProtected(l, p)) return;
        StructurePlacement.set(l, p, block.defaultBlockState(), 3);
    }

    private static boolean isProtected(WorldGenLevel l, BlockPos p) {
        BlockState s = l.getBlockState(p);
        return s.is(Blocks.BEDROCK) || s.is(Blocks.OBSIDIAN) || s.is(Blocks.END_PORTAL)
                || s.is(Blocks.END_PORTAL_FRAME) || s.is(Blocks.END_GATEWAY)
                || s.is(Blocks.CHORUS_PLANT) || s.is(Blocks.CHORUS_FLOWER)
                || s.is(Blocks.BEACON) || s.is(Blocks.SPAWNER) || s.is(Blocks.DRAGON_EGG)
                || l.getBlockEntity(p) != null;
    }

    private static boolean hasSolidFootprint(WorldGenLevel l, BlockPos b, int r) {
        int total = 0, solid = 0;
        for (int dx = -r; dx <= r; dx += 2)
            for (int dz = -r; dz <= r; dz += 2) {
                total++;
                BlockState st = l.getBlockState(b.below().offset(dx, 0, dz));
                if (st.is(Blocks.END_STONE) || st.is(Blocks.END_STONE_BRICKS) || ModBlocks.isPlantGround(st)) solid++;
            }
        return solid * 100 >= total * 58;
    }

    private static BlockPos flattenGround(WorldGenLevel l, BlockPos b, int r) {
        int maxY = b.getY(), bx = b.getX(), bz = b.getZ();
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++) {
                int yy = l.getHeight(Heightmap.Types.WORLD_SURFACE_WG, bx + dx, bz + dz);
                if (yy > maxY) maxY = yy;
            }
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++) {
                int yy = l.getHeight(Heightmap.Types.WORLD_SURFACE_WG, bx + dx, bz + dz);
                if (yy >= maxY) continue;
                Block g = l.getBlockState(new BlockPos(bx + dx, yy - 1, bz + dz)).getBlock();
                if (!ModBlocks.isPlantGround(g.defaultBlockState())) g = Blocks.END_STONE;
                for (int k = yy; k < maxY; k++) {
                    BlockPos p = new BlockPos(bx + dx, k, bz + dz);
                    if (l.getBlockState(p).isAir()) StructurePlacement.set(l, p, g.defaultBlockState(), 3);
                }
            }
        for (int d = 0; d < r; d++) {
            int band = r - d;
            if (band <= 0) break;
            int drop = Math.min(2, 1 + d / 4);
            for (int i = -band; i <= band; i++) {
                int[][] sides = {{i, -band}, {i, band}, {-band, i}, {band, i}};
                for (int[] s : sides) {
                    BlockPos top = new BlockPos(bx + s[0], maxY - drop, bz + s[1]);
                    if (l.getBlockState(top).isAir()) StructurePlacement.set(l, top, Blocks.END_STONE.defaultBlockState(), 3);
                    for (int k = 1; k <= 2; k++) {
                        BlockPos under = top.below(k);
                        if (l.getBlockState(under).isAir()) StructurePlacement.set(l, under, Blocks.END_STONE.defaultBlockState(), 3);
                    }
                }
            }
        }
        return new BlockPos(bx, maxY, bz);
    }

    private static void landmarkMechanism(WorldGenLevel l, BlockPos p, EndRuinVariant v) {
        if (!l.getBlockState(p).isAir()) return;
        StructurePlacement.set(l, p, ModBlocks.RESONANT_MECHANISM.defaultBlockState(), 3);
        if (l.getBlockEntity(p) instanceof ResonantMechanismBlockEntity m) m.setVariant(v);
    }

    private static void lootChest(WorldGenLevel l, BlockPos p, RandomSource rnd, String table) {
        setReplace(l, p, Blocks.CHEST);
        if (l.getBlockEntity(p) instanceof ChestBlockEntity c) c.setLootTable(lootKey(table), rnd.nextLong());
    }

    private static void lootBarrel(WorldGenLevel l, BlockPos p, RandomSource rnd, String table) {
        setReplace(l, p, Blocks.BARREL);
        if (l.getBlockEntity(p) instanceof BarrelBlockEntity barrel) barrel.setLootTable(lootKey(table), rnd.nextLong());
    }

    private static void placeSpawner(WorldGenLevel l, BlockPos p, EntityType<?> type, RandomSource rnd) {
        if (isProtected(l, p)) return;
        StructurePlacement.set(l, p, Blocks.SPAWNER.defaultBlockState(), 3);
        if (l.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity s)
            s.setEntityId(type, rnd);
    }

    /** Stands a live regional warden at the given post; it attunes to the biome. */
    private static void placeWarden(WorldGenLevel l, BlockPos p) {
        if (isProtected(l, p)) return;
        var warden = ModEntities.END_WARDEN.create(l.getLevel());
        if (warden == null) return;
        boolean spawned = com.infernodude777.endesium.entity.BossPlacement.spawnBoss(
                warden, l, p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D, 128.0D);
        if (!spawned) {
            Endesium.LOGGER.warn("Endesium warden skipped near [{}, {}]: no safe footing",
                    p.getX(), p.getZ());
        }
    }

    private static ResourceKey<LootTable> lootKey(String t) {
        return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id(t));
    }

    private static void inscribe(WorldGenLevel l, BlockPos p, int symbol) {
        if (!l.getBlockState(p).isAir()) return;
        StructurePlacement.set(l, p, ModBlocks.INSCRIBED_SLATE.defaultBlockState()
                .setValue(InscribedSlateBlock.SYMBOL, symbol), 3);
    }

    // =====================================================================
    // END WASTES - the Dust Cathedral
    // =====================================================================

    private static void dustCathedral(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 19);

        // The titan: roots, boulder body, neck, and a ribbed spoke canopy.
        titanRoots(l, b, ModBlocks.WASTES_STONE, ModBlocks.CRACKED_SPIRE_STONE);
        titanBody(l, b, ModBlocks.WASTES_STONE, ModBlocks.CRACKED_SPIRE_STONE);
        titanNeckAndCanopy(l, b, ModBlocks.WASTES_STONE, ModBlocks.CRACKED_SPIRE_STONE,
                ModBlocks.CRACKED_SPIRE_STONE, ModBlocks.WASTES_STONE,
                Blocks.END_STONE_BRICKS, ModBlocks.DORMANT_RESONANT_CRYSTAL);
        // Golden bells swinging from the canopy's fringed rim.
        canopyHangers(l, b, 24, 14, 18, 14, r, Blocks.CHAIN, Blocks.GOLD_BLOCK, 3, 6);
        for (int i = 0; i < 6; i++) {
            double ang = i * Math.PI / 3.0D + 0.5D;
            int dx = (int) Math.round(Math.cos(ang) * 16);
            int dz = (int) Math.round(Math.sin(ang) * 16);
            hangColumn(l, b, dx, 24, dz, 2, Blocks.CHAIN);
            setReplace(l, off(b, dx, 22, dz), ModBlocks.VOID_LAMP);
        }

        // Rose window set into the body's south face.
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++) {
                if (dx * dx + dy * dy > 5 || (dx == 0 && dy == 0)) continue;
                Block petal = (dx + dy) % 2 == 0
                        ? Blocks.LIGHT_BLUE_STAINED_GLASS
                        : Blocks.CYAN_STAINED_GLASS;
                setReplace(l, off(b, dx, 10 + dy, 10), petal);
            }
        setReplace(l, off(b, 0, 10, 10), ModBlocks.PALE_CRYSTAL_BLOCK);

        // Altar dais and mechanism in the body's hollow heart.
        fill(l, b, -3, 4, -3, 3, 4, 3, ModBlocks.END_GRAY);
        fill(l, b, -2, 5, -2, 2, 5, 2, Blocks.END_STONE_BRICKS);
        setReplace(l, off(b, 0, 6, 0), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 7, 0), EndRuinVariant.INTACT);
        lootBarrel(l, off(b, 3, 6, 3), r, "chests/wastes_cathedral");

        // Crypt beneath the body, reached by a side stair.
        fill(l, b, -3, -4, -6, 3, -1, 0, Blocks.AIR);
        fill(l, b, -3, -5, -6, 3, -5, 0, ModBlocks.END_GRAY);
        fill(l, b, 5, -4, 0, 6, -1, 2, Blocks.AIR);
        for (int k = 0; k < 6; k++) {
            fill(l, b, 5 - k / 3, -k / 2 - 1, 3 + k, 5 - k / 3, -k / 2 - 1, 3 + k, Blocks.END_STONE_BRICKS);
            fill(l, b, 5 - k / 3, -k / 2, 3 + k, 5 - k / 3, -k / 2 + 2, 3 + k, Blocks.AIR);
        }
        lootChest(l, off(b, -2, -4, -5), r, "chests/wastes_cathedral");
        lootBarrel(l, off(b, 2, -4, -5), r, "chests/wastes_cathedral");
        lootChest(l, off(b, 0, -4, -1), r, "chests/end_spire_treasure");
        placeSpawner(l, off(b, -3, -4, -1), ModEntities.DUST_CRAWLER, r);
        setReplace(l, off(b, 0, -5, -3), Blocks.MAGMA_BLOCK);
        setReplace(l, off(b, -2, -5, -2), Blocks.MAGMA_BLOCK);
        setReplace(l, off(b, 2, -5, -2), Blocks.MAGMA_BLOCK);
        placeWarden(l, off(b, 1, -4, -3));

        // Scree field and fallen columns ringing the roots.
        scatterDebris(l, b, r, 34, 11, 21, ModBlocks.WASTES_GRAVEL, ModBlocks.CRACKED_SPIRE_STONE);
        fill(l, b, 12, 1, 5, 17, 1, 5, ModBlocks.CRACKED_SPIRE_STONE);
        setReplace(l, off(b, 18, 1, 5), ModBlocks.WASTES_GRAVEL);
        fill(l, b, -17, 1, -3, -12, 1, -3, ModBlocks.CRACKED_SPIRE_STONE);
        col(l, b, 14, -8, 1, 4, ModBlocks.CRACKED_SPIRE_STONE);
        setReplace(l, off(b, 14, 5, -8), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        inscribe(l, off(b, -4, 1, 12), InscribedSlateBlock.SYMBOL_SPIRE);
        inscribe(l, off(b, 4, 1, 12), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // CHORUS WILDS - the Elderwood Sanctum
    // =====================================================================

    private static void elderwoodSanctum(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 20);

        // The titan: bark body on splayed chorus roots.
        titanRoots(l, b, ModBlocks.CHORUS_ROOT, ModBlocks.ELDER_CHORUS_BARK);
        titanBody(l, b, ModBlocks.ELDER_CHORUS_BARK, ModBlocks.ELDER_CHORUS_WOOD);
        titanNeckAndCanopy(l, b, ModBlocks.ELDER_CHORUS_BARK, ModBlocks.ELDER_CHORUS_WOOD,
                ModBlocks.CHORUS_ROOT, ModBlocks.CHORUS_MOSS,
                ModBlocks.CHORUS_MOSS, ModBlocks.RESONANT_BLOOM);
        // Bloom tendrils fringing the canopy rim.
        canopyHangers(l, b, 24, 14, 18, 16, r, ModBlocks.CHORUS_ROOT, ModBlocks.RESONANT_BLOOM, 4, 9);
        // Branch tips reaching past the rim, crowned with chorus flowers.
        for (int i = 0; i < 7; i++) {
            double ang = i * 2.0D * Math.PI / 7.0D + 0.45D;
            int tx = (int) Math.round(Math.cos(ang) * 20);
            int tz = (int) Math.round(Math.sin(ang) * 20);
            setReplace(l, off(b, tx, 25, tz), ModBlocks.HOLLOW_CHORUS_WOOD);
            if (r.nextBoolean()) setReplace(l, off(b, tx, 26, tz), Blocks.CHORUS_FLOWER);
        }
        // Canopy-top blooms catching the light.
        for (int i = 0; i < 12; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0D;
            int dist = r.nextInt(14);
            int dx = (int) Math.round(Math.cos(ang) * dist);
            int dz = (int) Math.round(Math.sin(ang) * dist);
            if (!l.getBlockState(off(b, dx, 26, dz)).isAir()) continue;
            if (l.getBlockState(off(b, dx, 25, dz)).isAir()) continue;
            setReplace(l, off(b, dx, 26, dz), ModBlocks.RESONANT_BLOOM);
        }

        // Moss altar and mechanism in the body's hollow heart.
        disc(l, b, 0, 4, 0, 5, ModBlocks.CHORUS_MOSS);
        setReplace(l, off(b, 0, 5, 0), ModBlocks.ELDER_CHORUS_WOOD);
        landmarkMechanism(l, off(b, 0, 6, 0), EndRuinVariant.BLOOM_CONSERVATORY);
        lootChest(l, off(b, -4, 6, 4), r, "chests/wilds_archive");
        lootBarrel(l, off(b, 4, 6, 4), r, "chests/wilds_archive");
        setReplace(l, off(b, 0, 8, -4), ModBlocks.RESONANT_BLOOM);

        // Root buttresses between the main roots.
        for (int i = 0; i < 10; i++) {
            double ang = i * Math.PI / 5.0D + 0.7D;
            double cos = Math.cos(ang), sin = Math.sin(ang);
            for (int k = 0; k <= 6; k++) {
                int dx = (int) Math.round(cos * (9 + k));
                int dz = (int) Math.round(sin * (9 + k));
                col(l, b, dx, dz, 0, Math.max(1, 6 - k),
                        k % 2 == 0 ? ModBlocks.CHORUS_ROOT : ModBlocks.ELDER_CHORUS_BARK);
            }
        }
        // Spiral ledge climbing body and neck.
        for (int y = 7; y <= 24; y++) {
            double ang = y * 0.55D;
            int lx = (int) Math.round(Math.cos(ang) * 3);
            int lz = (int) Math.round(Math.sin(ang) * 3);
            setReplace(l, off(b, lx, y, lz), ModBlocks.CHORUS_ROOT);
        }

        // Root vault below with the deeper cache.
        fill(l, b, -5, -5, -5, 5, -1, 5, Blocks.AIR);
        fill(l, b, -5, -6, -5, 5, -6, 5, ModBlocks.CHORUS_MOSS);
        col(l, b, 0, 0, -5, -1, ModBlocks.ELDER_CHORUS_WOOD);
        lootChest(l, off(b, -3, -5, -3), r, "chests/bloom_conservatory");
        lootBarrel(l, off(b, 3, -5, -3), r, "chests/bloom_conservatory");
        lootChest(l, off(b, 3, -5, 3), r, "chests/end_spire_treasure");
        placeSpawner(l, off(b, 0, -4, 4), ModEntities.CHORUS_STALKER, r);
        placeWarden(l, off(b, 0, -5, -2));
        inscribe(l, off(b, 9, 1, 0), InscribedSlateBlock.SYMBOL_RING);
        inscribe(l, off(b, -9, 1, 0), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // SHATTERED HIGHLANDS - Skyrend Keep
    // =====================================================================

    private static void skyrendKeep(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 20);

        // Low curtain wall square around the titan's feet.
        fill(l, b, -13, 0, -13, 13, 0, 13, ModBlocks.HIGHLAND_SLATE);
        fill(l, b, -13, 1, -13, 13, 4, 13, ModBlocks.HIGHLAND_STONE);
        fill(l, b, -11, 1, -11, 11, 3, 11, Blocks.AIR);
        for (int i = -13; i <= 13; i += 2) {
            col(l, b, i, -13, 5, 5, ModBlocks.HIGHLAND_STONE);
            col(l, b, i, 13, 5, 5, ModBlocks.HIGHLAND_STONE);
            col(l, b, -13, i, 5, 5, ModBlocks.HIGHLAND_STONE);
            col(l, b, 13, i, 5, 5, ModBlocks.HIGHLAND_STONE);
        }
        fill(l, b, -2, 1, 13, 2, 3, 13, Blocks.AIR);
        col(l, b, -2, 13, 2, 3, Blocks.IRON_BARS);
        col(l, b, 2, 13, 2, 3, Blocks.IRON_BARS);
        buildWatchTower(l, b, -13, -13, 9, r);
        buildWatchTower(l, b, 13, -13, 9, r);
        buildWatchTower(l, b, -13, 13, 9, r);
        buildWatchTower(l, b, 13, 13, 9, r);
        for (int[] c : new int[][]{{-13, -13}, {13, -13}, {-13, 13}, {13, 13}}) {
            setReplace(l, off(b, c[0], 11, c[1]), ModBlocks.HIGHLAND_LENSSTONE);
        }

        // The titan: a keep grown into a stone colossus on splayed roots.
        titanRoots(l, b, ModBlocks.HIGHLAND_STONE, ModBlocks.HIGHLAND_SLATE);
        titanBody(l, b, ModBlocks.HIGHLAND_STONE, ModBlocks.HIGHLAND_SLATE);
        titanNeckAndCanopy(l, b, ModBlocks.HIGHLAND_STONE, ModBlocks.HIGHLAND_SLATE,
                ModBlocks.HIGHLAND_SLATE, ModBlocks.HIGHLAND_STONE,
                ModBlocks.HIGHLAND_LENSSTONE, ModBlocks.DORMANT_RESONANT_CRYSTAL);
        canopyHangers(l, b, 24, 14, 18, 12, r, Blocks.CHAIN, ModBlocks.VOID_LAMP, 3, 6);
        // The shattering: broken keep fragments hang suspended mid-air.
        for (int i = 0; i < 7; i++) {
            double ang = i * 2.0D * Math.PI / 7.0D + 0.35D;
            int dist = 12 + (i % 3) * 2;
            int fx = (int) Math.round(Math.cos(ang) * dist);
            int fz = (int) Math.round(Math.sin(ang) * dist);
            int fy = 28 + (i % 4) * 2;
            fill(l, b, fx - 1, fy, fz - 1, fx + 1, fy, fz + 1, ModBlocks.HIGHLAND_STONE);
            setReplace(l, off(b, fx, fy + 1, fz), i % 2 == 0 ? ModBlocks.HIGHLAND_SLATE : ModBlocks.HIGHLAND_STONE);
            int tether = 2 + (i % 3);
            for (int k = 1; k <= tether; k++) {
                BlockPos cp = off(b, fx, fy - k, fz);
                if (l.getBlockState(cp).isAir()) setReplace(l, cp, Blocks.CHAIN);
                else break;
            }
            if (i % 2 == 0) setReplace(l, off(b, fx, fy + 2, fz), ModBlocks.HIGHLAND_LENSSTONE);
        }

        // Open-air throne floor on the body's crown, beneath the canopy.
        fill(l, b, -4, 16, -4, 4, 16, 4, ModBlocks.HIGHLAND_SLATE);
        setReplace(l, off(b, 0, 17, -3), Blocks.GOLD_BLOCK);
        col(l, b, -1, -4, 17, 17, Blocks.IRON_BARS);
        col(l, b, 1, -4, 17, 17, Blocks.IRON_BARS);
        setReplace(l, off(b, 0, 17, -1), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 18, -1), EndRuinVariant.RIFT_OBSERVATORY);
        placeWarden(l, off(b, 3, 17, 3));
        lootBarrel(l, off(b, -3, 17, 3), r, "chests/highland_observatory");
        placeSpawner(l, off(b, 0, 28, 0), ModEntities.VOID_RAY, r);

        // Courtyard dressing: banner masts and a barracks ruin.
        for (int[] mast : new int[][]{{-9, 0}, {9, 0}, {0, -9}}) {
            col(l, b, mast[0], mast[1], 1, 5, ModBlocks.WINDSCAR_BRACKET);
            setReplace(l, off(b, mast[0], 6, mast[1]), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        }
        fill(l, b, 6, 1, -11, 11, 3, -7, ModBlocks.HIGHLAND_STONE);
        fill(l, b, 7, 1, -10, 10, 3, -8, Blocks.AIR);
        placeSpawner(l, off(b, 9, 2, -9), EntityType.PHANTOM, r);

        lootChest(l, off(b, -9, 1, 9), r, "chests/highland_observatory");
        lootChest(l, off(b, 9, 1, -10), r, "chests/windscar_lift");
        inscribe(l, off(b, -4, 1, 11), InscribedSlateBlock.SYMBOL_RING);
        inscribe(l, off(b, 4, 1, 11), InscribedSlateBlock.SYMBOL_SPIRE);
    }

    private static void buildWatchTower(WorldGenLevel l, BlockPos b, int cx, int cz, int height, RandomSource r) {
        for (int y = 1; y <= height; y++) {
            fill(l, b, cx - 2, y, cz - 2, cx + 2, y, cz + 2,
                    y % 6 == 0 ? ModBlocks.HIGHLAND_SLATE : ModBlocks.HIGHLAND_STONE);
            fill(l, b, cx - 1, y, cz - 1, cx + 1, y, cz + 1, Blocks.AIR);
        }
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) != 2 && Math.abs(dz) != 2) continue;
                setReplace(l, off(b, cx + dx, height + 1, cz + dz), ModBlocks.HIGHLAND_STONE);
                if ((dx + dz) % 2 == 0) setReplace(l, off(b, cx + dx, height + 2, cz + dz), ModBlocks.HIGHLAND_STONE);
            }
        fill(l, b, cx - 2, 5, cz, cx - 2, 6, cz, ModBlocks.VOID_GLASS);
        fill(l, b, cx + 2, 5, cz, cx + 2, 6, cz, ModBlocks.VOID_GLASS);
    }

    /** Neck and canopy in one call: the titan's head. */
    private static void titanNeckAndCanopy(WorldGenLevel l, BlockPos b, Block body, Block accent,
            Block canopy, Block arc, Block cap, Block tipCap) {
        ribbedColumn(l, b, 16, 5, 4, 9, body, accent);
        titanCanopy(l, b, 25, 14, canopy, arc, cap, tipCap);
    }

    // =====================================================================
    // VOID MARSHES - the Drowned Cathedral
    // =====================================================================

    private static void drownedCathedral(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 17);
        // Sunken nave: a two-step basin of mire, moss, and still black water.
        fill(l, b, -10, -1, -14, 10, -1, 14, Blocks.AIR);
        fill(l, b, -10, -2, -14, 10, -2, 14, ModBlocks.VOID_MARSH_SOIL);
        for (int i = 0; i < 40; i++) {
            int dx = r.nextInt(21) - 10, dz = r.nextInt(29) - 14;
            setReplace(l, off(b, dx, -2, dz), r.nextBoolean() ? ModBlocks.MARSH_MOSS : ModBlocks.VOID_MARSH_SOIL);
        }
        for (int i = 0; i < 14; i++) {
            int dx = r.nextInt(19) - 9, dz = r.nextInt(27) - 13;
            if (Math.abs(dx) <= 6 && Math.abs(dz) <= 6) continue; // keep the titan's feet firm
            fill(l, b, dx - 1, -2, dz - 1, dx + 1, -2, dz + 1,
                    r.nextBoolean() ? Blocks.WATER : ModBlocks.MARSH_MOSS);
            setReplace(l, off(b, dx, -2, dz), Blocks.WATER);
        }
        // Rib skeleton: tide iron posts and pointed arches ringing the basin.
        for (int z = -12; z <= 12; z += 8) {
            if (Math.abs(z) <= 6) continue;
            col(l, b, -11, z, -1, 7, ModBlocks.TIDE_IRON);
            col(l, b, 11, z, -1, 7, ModBlocks.TIDE_IRON);
            fill(l, b, -11, 8, z, 11, 8, z, ModBlocks.TIDE_IRON);
            fill(l, b, -7, 9, z, 7, 9, z, ModBlocks.TIDE_IRON);
            fill(l, b, -3, 10, z, 3, 10, z, ModBlocks.TIDE_IRON);
        }
        // West facade gable with a broken rose window.
        for (int x = -8; x <= 8; x++) {
            for (int y = 1; y <= 7; y++) {
                if (r.nextInt(5) == 0) continue;
                setReplace(l, off(b, x, y, -14), ModBlocks.TIDE_IRON);
            }
        }
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++) {
                int d = dx * dx + dy * dy;
                if (d <= 4) setReplace(l, off(b, dx, 4 + dy, -14), Blocks.AIR);
                else if (d <= 8) setReplace(l, off(b, dx, 4 + dy, -14), ModBlocks.VOID_GLASS);
            }
        setReplace(l, off(b, 0, 4, -14), ModBlocks.DORMANT_RESONANT_CRYSTAL);

        // The drowned titan: tide iron body furred with marsh moss.
        titanRoots(l, b, ModBlocks.TIDE_IRON, ModBlocks.MARSH_MOSS);
        titanBody(l, b, ModBlocks.TIDE_IRON, ModBlocks.MARSH_MOSS);
        titanNeckAndCanopy(l, b, ModBlocks.TIDE_IRON, ModBlocks.MARSH_MOSS,
                ModBlocks.MARSH_MOSS, ModBlocks.TIDE_IRON,
                ModBlocks.MIREGLASS, ModBlocks.DORMANT_RESONANT_CRYSTAL);
        canopyHangers(l, b, 24, 14, 18, 14, r, Blocks.CHAIN, ModBlocks.VOID_LAMP, 3, 6);
        // The gold bell hangs in the neck's hollow throat.
        col(l, b, 0, 0, 20, 21, Blocks.CHAIN);
        setReplace(l, off(b, 0, 19, 0), Blocks.GOLD_BLOCK);
        // Altar and mechanism at the titan's foot.
        fill(l, b, -3, -1, -4, 3, -1, 0, ModBlocks.TIDE_IRON);
        fill(l, b, -2, 0, -3, 2, 0, -1, ModBlocks.VOID_MARSH_SOIL);
        setReplace(l, off(b, 0, 1, -2), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 2, -2), EndRuinVariant.TIDE_BELL);
        placeWarden(l, off(b, -2, 0, -1));
        // A drowned sister-tower, snapped off at half height.
        for (int y = -1; y <= 6; y++) {
            int rad = y > 3 ? 2 : 3;
            ring(l, b, 14, y, -9, rad, ModBlocks.TIDE_IRON);
        }
        disc(l, b, 14, -1, -9, 2, ModBlocks.MARSH_MOSS);
        // Leaning bell tower at the north-west flank.
        int lean = 0;
        for (int y = -1; y <= 16; y++) {
            lean = Math.max(0, y / 6);
            fill(l, b, -15 + lean, y, 10, -11 + lean, y, 14,
                    y % 6 == 0 ? ModBlocks.MARSH_MOSS : ModBlocks.TIDE_IRON);
            fill(l, b, -14 + lean, y, 11, -12 + lean, y, 13, Blocks.AIR);
        }
        col(l, b, -10, 12, 12, 14, Blocks.CHAIN);
        setReplace(l, off(b, -10, 11, 12), Blocks.GOLD_BLOCK);
        setReplace(l, off(b, -10, 15, 12), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        // Offerings drowned in the mire at the tower's base.
        setReplace(l, off(b, -11, -2, 15), Blocks.GOLD_BLOCK);
        setReplace(l, off(b, -10, -2, 15), ModBlocks.RESONANT_BLOOM);
        lootChest(l, off(b, -9, -2, 16), r, "chests/marsh_tide_bell");
        // Broken pew rows flanking the aisle.
        for (int z = 5; z <= 12; z += 3) {
            fill(l, b, -6, -1, z, -4, -1, z, ModBlocks.TIDE_IRON);
            fill(l, b, 4, -1, z, 6, -1, z, ModBlocks.TIDE_IRON);
        }
        // Crypt alcoves let into the flanks; one is a marsh crawler lair.
        fill(l, b, -14, -2, -8, -11, 0, -5, Blocks.AIR);
        lootChest(l, off(b, -13, -2, -6), r, "chests/marsh_temple");
        fill(l, b, 11, -2, -8, 14, 0, -5, Blocks.AIR);
        lootChest(l, off(b, 13, -2, -6), r, "chests/marsh_tide_bell");
        lootBarrel(l, off(b, 13, -2, -5), r, "chests/marsh_tide_bell");
        placeSpawner(l, off(b, 11, -1, -7), ModEntities.MARSH_CRAWLER, r);
        // Reeds taking the nave back.
        for (int i = 0; i < 16; i++) {
            int dx = r.nextInt(21) - 10, dz = r.nextInt(27) - 13;
            if (r.nextBoolean()) setReplace(l, off(b, dx, -1, dz), ModBlocks.VOID_REED);
        }
        inscribe(l, off(b, 0, 0, 15), InscribedSlateBlock.SYMBOL_EYE);
        inscribe(l, off(b, -3, 0, 15), InscribedSlateBlock.SYMBOL_RING);
    }

    // =====================================================================
    // LUMINOUS GROVES - the Lumen Cathedral
    // =====================================================================

    private static void lumenCathedral(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 18);
        // A court floor that glows beneath your feet.
        fill(l, b, -12, 0, -16, 12, 0, 16, ModBlocks.LUMEN_STONE);
        // Low colonnade walls framing the court.
        for (int z = -14; z <= 14; z += 4) {
            col(l, b, -10, z, 1, 4, ModBlocks.LUMEN_STONE);
            col(l, b, 10, z, 1, 4, ModBlocks.LUMEN_STONE);
        }
        for (int x = -6; x <= 6; x += 4) {
            col(l, b, x, -14, 1, 4, ModBlocks.LUMEN_STONE);
            col(l, b, x, 14, 1, 4, ModBlocks.LUMEN_STONE);
        }
        // Aisle carpet, prismatic crossing inlay, and pews.
        fill(l, b, -1, 0, -14, 1, 0, 14, ModBlocks.LUMEN_MOSS);
        fill(l, b, -2, 0, -3, 2, 0, 3, ModBlocks.PRISM_CANOPY_BLOCK);
        disc(l, b, 0, 0, 0, 1, ModBlocks.PALE_CRYSTAL_BLOCK);
        for (int z = -10; z <= 12; z += 3) {
            if (Math.abs(z) <= 3) continue;
            setReplace(l, off(b, -4, 1, z), ModBlocks.LUMEN_MOSS);
            setReplace(l, off(b, -3, 1, z), ModBlocks.LUMEN_MOSS);
            setReplace(l, off(b, 3, 1, z), ModBlocks.LUMEN_MOSS);
            setReplace(l, off(b, 4, 1, z), ModBlocks.LUMEN_MOSS);
        }

        // The radiant titan: lumen body under a glowing prism canopy.
        titanRoots(l, b, ModBlocks.LUMEN_STONE, ModBlocks.PRISM_CANOPY_BLOCK);
        titanBody(l, b, ModBlocks.LUMEN_STONE, ModBlocks.PRISM_CANOPY_BLOCK);
        titanNeckAndCanopy(l, b, ModBlocks.LUMEN_STONE, ModBlocks.PRISM_CANOPY_BLOCK,
                ModBlocks.PRISM_CANOPY_BLOCK, ModBlocks.LUMEN_STONE,
                ModBlocks.PALE_CRYSTAL_BLOCK, ModBlocks.LUMEN_GRAFT_BLOCK);
        canopyHangers(l, b, 24, 14, 18, 14, r, Blocks.CHAIN, ModBlocks.LUMEN_GRAFT_BLOCK, 3, 6);
        // Rose window set into the body's south face.
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++) {
                if (dx * dx + dy * dy > 5 || (dx == 0 && dy == 0)) continue;
                setReplace(l, off(b, dx, 10 + dy, 10),
                        (dx + dy) % 2 == 0 ? ModBlocks.PALE_CRYSTAL_BLOCK : ModBlocks.VOID_GLASS);
            }
        setReplace(l, off(b, 0, 10, 10), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        // Apse dais and mechanism in the body's hollow heart.
        fill(l, b, -3, 4, -3, 3, 4, 3, ModBlocks.PRISM_CANOPY_BLOCK);
        setReplace(l, off(b, 0, 5, 0), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 6, 0), EndRuinVariant.PRISM_CANOPY);
        // Light pylons at the court corners.
        for (int[] p : new int[][]{{-12, -14}, {12, -14}, {-12, 14}, {12, 14}}) {
            col(l, b, p[0], p[1], 1, 6, ModBlocks.LUMEN_STONE);
            setReplace(l, off(b, p[0], 7, p[1]), ModBlocks.LUMEN_GRAFT_BLOCK);
        }
        // Crystal gardens flanking the approach.
        for (int[] g : new int[][]{{-7, 18}, {7, 18}, {-10, 16}, {10, 16}}) {
            setReplace(l, off(b, g[0], 1, g[1]), ModBlocks.CRYSTAL_CLUSTER);
            setReplace(l, off(b, g[0] + (g[0] > 0 ? -1 : 1), 1, g[1]), ModBlocks.LUMEN_BLOOM);
        }
        lootChest(l, off(b, -8, 1, 12), r, "chests/bloom_conservatory");
        lootBarrel(l, off(b, 8, 1, 12), r, "chests/prism_canopy");
        lootChest(l, off(b, 8, 1, -12), r, "chests/luminous_lightwell");
        placeSpawner(l, off(b, 0, 5, 8), ModEntities.LUMEN_MOTH, r);
        placeWarden(l, off(b, 3, 4, -5));
        inscribe(l, off(b, -4, 1, 16), InscribedSlateBlock.SYMBOL_RING);
        inscribe(l, off(b, 4, 1, 16), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // ASHEN EXPANSE - the Great Caldera
    // =====================================================================

    private static void greatCaldera(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 21);

        // Solid root massif up to the future crater floor.
        for (int y = 0; y <= 9; y++) {
            int rad = coneRadius(y);
            disc(l, b, 0, y, 0, rad, y % 5 == 0 ? ModBlocks.RESONANT_BASALT : ModBlocks.ASH_STONE);
        }
        // Cone shell above, hollowed into a crater bowl.
        for (int y = 10; y <= 28; y++) {
            int rad = coneRadius(y);
            for (int dx = -rad; dx <= rad; dx++)
                for (int dz = -rad; dz <= rad; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > rad * rad || d2 < (rad - 2) * (rad - 2)) {
                        if (d2 <= (rad - 2) * (rad - 2)) setReplace(l, off(b, dx, y, dz), Blocks.AIR);
                        continue;
                    }
                    Block blk = y % 5 == 0 ? ModBlocks.RESONANT_BASALT
                            : y >= 25 ? ModBlocks.ASHEN_SOIL : ModBlocks.ASH_STONE;
                    setReplace(l, off(b, dx, y, dz), blk);
                }
        }
        // Crater lava lake hemmed by an obsidian shore.
        int rimY = 10;
        int lakeRad = coneRadius(rimY) - 2;
        disc(l, b, 0, rimY, 0, lakeRad + 1, Blocks.OBSIDIAN);
        disc(l, b, 0, rimY, 0, lakeRad, Blocks.LAVA);
        ring(l, b, 0, rimY + 1, 0, lakeRad + 2, Blocks.CRYING_OBSIDIAN);
        // Lava falls pouring from four rim notches.
        for (double ang : new double[]{0.4D, 2.0D, 3.5D, 5.0D}) {
            int nx = (int) Math.round(Math.cos(ang));
            int nz = (int) Math.round(Math.sin(ang));
            for (int y = 10; y >= 0; y--) {
                int rr = coneRadius(y) - 1;
                BlockPos p = off(b, nx * rr, y, nz * rr);
                setReplace(l, p, Blocks.LAVA);
                setReplace(l, off(b, (nx * rr) + nx, y, (nz * rr) + nz), Blocks.MAGMA_BLOCK);
            }
        }
        // Glowing magma crack veins wandering across the slopes.
        double veinAngle = r.nextDouble() * Math.PI * 2.0D;
        for (int y = 26; y >= 2; y--) {
            int rr = coneRadius(y);
            int vx = (int) Math.round(Math.cos(veinAngle) * (rr - 1));
            int vz = (int) Math.round(Math.sin(veinAngle) * (rr - 1));
            setReplace(l, off(b, vx, y, vz), Blocks.MAGMA_BLOCK);
            veinAngle += (r.nextDouble() - 0.5D) * 0.55D;
        }
        // An obsidian spine ridging one flank of the cone.
        for (int y = 24; y >= 12; y--) {
            int rr = coneRadius(y);
            setReplace(l, off(b, rr, y, 0), Blocks.OBSIDIAN);
            setReplace(l, off(b, rr - 1, y + 1, 1), Blocks.OBSIDIAN);
        }
        // Rivers of lava radiating from the root across the scorched plain.
        for (double ang : new double[]{0.9D, 2.2D, 3.6D, 5.3D}) {
            double cos = Math.cos(ang), sin = Math.sin(ang);
            int sx = (int) Math.round(cos * 16), sz = (int) Math.round(sin * 16);
            int ex = (int) Math.round(cos * 21), ez = (int) Math.round(sin * 21);
            int steps = 12;
            for (int k = 0; k <= steps; k++) {
                int px = sx + (ex - sx) * k / steps;
                int pz = sz + (ez - sz) * k / steps;
                BlockPos gp = off(b, px, 0, pz);
                int py = l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
                        gp.getX(), gp.getZ()) - 1;
                if (py < b.getY() - 3) continue;
                setReplace(l, new BlockPos(gp.getX(), py, gp.getZ()), Blocks.LAVA);
                setReplace(l, new BlockPos(gp.getX(), py - 1, gp.getZ()), Blocks.MAGMA_BLOCK);
                setReplace(l, new BlockPos(gp.getX() + 1, py + 1, gp.getZ()), Blocks.AIR);
                setReplace(l, new BlockPos(gp.getX() - 1, py + 1, gp.getZ()), Blocks.AIR);
                setReplace(l, new BlockPos(gp.getX(), py + 1, gp.getZ() + 1), Blocks.AIR);
            }
        }
        // Bubbling pools pocking the ash plain around the cone.
        for (int i = 0; i < 12; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0D;
            int dist = 17 + r.nextInt(5);
            int px = (int) Math.round(Math.cos(ang) * dist);
            int pz = (int) Math.round(Math.sin(ang) * dist);
            BlockPos gp = off(b, px, 0, pz);
            int py = l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
                    gp.getX(), gp.getZ()) - 1;
            if (py < b.getY() - 3) continue;
            fill(l, b, px - 1, py, pz - 1, px + 1, py, pz + 1, Blocks.MAGMA_BLOCK);
            setReplace(l, new BlockPos(gp.getX(), py, gp.getZ()), Blocks.LAVA);
        }
        // Hidden vault buried in the southern root, reached by a barred tunnel.
        fill(l, b, -2, 1, 18, 2, 4, 22, Blocks.AIR);
        fill(l, b, -2, 1, 10, 2, 4, 18, Blocks.AIR);
        col(l, b, -3, 14, 1, 4, Blocks.IRON_BARS);
        col(l, b, 3, 14, 1, 4, Blocks.IRON_BARS);
        fill(l, b, -4, 1, 2, 4, 6, 10, Blocks.AIR);
        fill(l, b, -4, 0, 2, 4, 0, 10, Blocks.MAGMA_BLOCK);
        fill(l, b, -4, 7, 2, 4, 7, 10, ModBlocks.RESONANT_BASALT);
        fill(l, b, -4, 1, 2, -4, 6, 10, ModBlocks.RESONANT_BASALT);
        fill(l, b, 4, 1, 2, 4, 6, 10, ModBlocks.RESONANT_BASALT);
        fill(l, b, -4, 1, 10, 4, 6, 10, ModBlocks.RESONANT_BASALT);
        lootChest(l, off(b, -3, 2, 3), r, "chests/ashen_volcano");
        lootChest(l, off(b, 3, 2, 3), r, "chests/ashen_citadel");
        lootBarrel(l, off(b, 0, 2, 3), r, "chests/ashen_volcano");
        placeSpawner(l, off(b, 0, 2, 9), EntityType.BLAZE, r);
        placeSpawner(l, off(b, -3, 2, 9), ModEntities.ASH_WRAITH, r);
        placeWarden(l, off(b, 0, 1, 6));
        // Mechanism on the crater rim, overlooking the lake.
        fill(l, b, -1, rimY + 1, -coneRadius(rimY) + 1, 1, rimY + 1, -coneRadius(rimY) + 3, ModBlocks.RESONANT_BASALT);
        landmarkMechanism(l, off(b, 0, rimY + 2, -coneRadius(rimY) + 2), EndRuinVariant.FRACTURED);
        // Burnt shrine ruins at the foot of the mountain.
        for (int[] c : new int[][]{{-17, -8}, {17, 8}}) {
            fill(l, b, c[0] - 2, 1, c[1] - 2, c[0] + 2, 3, c[1] + 2, ModBlocks.ASH_STONE);
            fill(l, b, c[0] - 1, 1, c[1] - 1, c[0] + 1, 3, c[1] + 1, Blocks.AIR);
        }
        inscribe(l, off(b, 0, 1, 23), InscribedSlateBlock.SYMBOL_EYE);
    }

    private static int coneRadius(int y) {
        return Math.max(3, (int) Math.round(20.0D * Math.pow(1.0D - y / 30.0D, 0.85D)));
    }

    // =====================================================================
    // CRYSTAL BARRENS - the Geode of the Sunken Heart
    // =====================================================================

    private static void sunkenGeode(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 18);
        // The crystal titan: a shard-strewn colossus on splayed roots.
        titanRoots(l, b, ModBlocks.CRYSTAL_SHARD_BLOCK, ModBlocks.DARK_CRYSTAL_BLOCK);
        titanBody(l, b, ModBlocks.CRYSTAL_SHARD_BLOCK, ModBlocks.DARK_CRYSTAL_BLOCK);
        titanNeckAndCanopy(l, b, ModBlocks.DARK_CRYSTAL_BLOCK, ModBlocks.PALE_CRYSTAL_BLOCK,
                ModBlocks.DARK_CRYSTAL_BLOCK, ModBlocks.CRYSTAL_SHARD_BLOCK,
                ModBlocks.PALE_CRYSTAL_BLOCK, ModBlocks.CRYSTAL_CLUSTER);
        canopyHangers(l, b, 24, 14, 18, 12, r, Blocks.CHAIN, ModBlocks.CRYSTAL_CLUSTER, 3, 6);
        // A hovering halo of shards circles the canopy.
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4.0D;
            int hx = (int) Math.round(Math.cos(ang) * (10 + (i % 3) * 2));
            int hz = (int) Math.round(Math.sin(ang) * (10 + (i % 3) * 2));
            setReplace(l, off(b, hx, 28 + (i % 3), hz), ModBlocks.CRYSTAL_SHARD_BLOCK);
            setReplace(l, off(b, hx, 29 + (i % 3), hz), i % 2 == 0 ? ModBlocks.PALE_CRYSTAL_BLOCK : ModBlocks.DARK_CRYSTAL_BLOCK);
        }
        // Shard monoliths standing between the roots.
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4.0D + 0.2D;
            int mx = (int) Math.round(Math.cos(ang) * 12);
            int mz = (int) Math.round(Math.sin(ang) * 12);
            int h = 4 + (i % 3) * 2;
            col(l, b, mx, mz, 1, h, i % 2 == 0 ? ModBlocks.DARK_CRYSTAL_BLOCK : ModBlocks.PALE_CRYSTAL_BLOCK);
            setReplace(l, off(b, mx, h + 1, mz), ModBlocks.CRYSTAL_CLUSTER);
        }
        // Gallery pillars around the body, lit from below.
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4.0D + 0.39D;
            int px = (int) Math.round(Math.cos(ang) * 9);
            int pz = (int) Math.round(Math.sin(ang) * 9);
            int top = i % 2 == 0 ? 7 : 5;
            col(l, b, px, pz, 1, top, ModBlocks.PALE_CRYSTAL_BLOCK);
            setReplace(l, off(b, px, top + 1, pz), ModBlocks.CRYSTAL_CLUSTER);
        }
        // Sunken pit descending to the heart floor, rimmed in pale crystal.
        fill(l, b, -5, -1, -5, 5, 2, 5, Blocks.AIR);
        ring(l, b, 0, 3, 0, 6, ModBlocks.PALE_CRYSTAL_BLOCK);
        fill(l, b, -5, -4, -5, 5, -2, 5, Blocks.AIR);
        fill(l, b, -5, -5, -5, 5, -5, 5, ModBlocks.DARK_CRYSTAL_BLOCK);
        for (int k = 0; k < 5; k++) {
            fill(l, b, 5 + k, -k - 1, -2, 5 + k, 0 - k / 2, 2, Blocks.AIR);
            fill(l, b, 5 + k, -k - 2, -2, 5 + k, -k - 2, 2, ModBlocks.CRYSTAL_SHARD_BLOCK);
        }
        // The Heart: a luminous monolith of pale and dark crystal.
        for (int y = -4; y <= 4; y++) {
            Block layer = (y % 2 == 0) ? ModBlocks.PALE_CRYSTAL_BLOCK : ModBlocks.DARK_CRYSTAL_BLOCK;
            int rad = y > 2 ? 1 : 2;
            fill(l, b, -rad, y, -rad, rad, y, rad, layer);
        }
        setReplace(l, off(b, 0, 5, 0), ModBlocks.CRYSTAL_CLUSTER);
        for (int i = 0; i < 10; i++) {
            double ang = i * Math.PI / 5.0D;
            int dx = (int) Math.round(Math.cos(ang) * 4);
            int dz = (int) Math.round(Math.sin(ang) * 4);
            setReplace(l, off(b, dx, -4, dz), ModBlocks.CRYSTAL_CLUSTER);
            setReplace(l, off(b, dx, -5, dz), ModBlocks.LUMEN_STONE);
        }
        landmarkMechanism(l, off(b, 4, -4, 4), EndRuinVariant.SUNKEN);
        placeSpawner(l, off(b, -4, -4, -4), ModEntities.CRYSTAL_BURROWER, r);
        placeWarden(l, off(b, -4, -4, 2));
        // Loot alcoves sunk into the body flanks.
        fill(l, b, -13, 1, -3, -11, 3, 3, Blocks.AIR);
        lootChest(l, off(b, -12, 1, 0), r, "chests/crystal_heart");
        lootBarrel(l, off(b, -12, 1, -2), r, "chests/crystal_heart");
        fill(l, b, 11, 1, 3, 13, 3, 6, Blocks.AIR);
        lootChest(l, off(b, 12, 1, 5), r, "chests/end_spire_treasure");
        inscribe(l, off(b, 0, 1, 14), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // VOID SKIRTS - the Void Spire
    // =====================================================================

    private static void voidSpire(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 19);

        // Plaza terrace with beacon posts and standing monoliths.
        disc(l, b, 0, 0, 0, 17, ModBlocks.VOID_SLATE);
        for (int[] post : new int[][]{{0, -16}, {0, 16}, {-16, 0}, {16, 0}}) {
            col(l, b, post[0], post[1], 1, 3, ModBlocks.VOID_BRICK);
            setReplace(l, off(b, post[0], 4, post[1]), ModBlocks.VOID_LAMP);
        }
        for (int i = 0; i < 6; i++) {
            double ang = i * Math.PI / 3.0D + 0.5D;
            int mx = (int) Math.round(Math.cos(ang) * 13);
            int mz = (int) Math.round(Math.sin(ang) * 13);
            col(l, b, mx, mz, 1, 3 + (i % 2) * 2, ModBlocks.VOIDSTONE);
            setReplace(l, off(b, mx, 4 + (i % 2) * 2, mz), ModBlocks.VOID_BRICK);
        }
        for (int i = 0; i < 24; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0D;
            int dx = (int) Math.round(Math.cos(ang) * (8 + r.nextInt(7)));
            int dz = (int) Math.round(Math.sin(ang) * (8 + r.nextInt(7)));
            setReplace(l, off(b, dx, 0, dz), ModBlocks.UMBRAL_GRASS.defaultBlockState().getBlock());
        }

        // The titan: a voidstone colossus on splayed roots.
        titanRoots(l, b, ModBlocks.VOIDSTONE, ModBlocks.VOID_BRICK);
        titanBody(l, b, ModBlocks.VOIDSTONE, ModBlocks.VOID_BRICK);
        titanNeckAndCanopy(l, b, ModBlocks.VOIDSTONE, ModBlocks.VOID_BRICK,
                ModBlocks.VOID_BRICK, ModBlocks.VOIDSTONE,
                ModBlocks.VOID_SLATE, ModBlocks.VOID_LAMP);
        // Lamp tendrils fringing the canopy rim.
        canopyHangers(l, b, 24, 14, 18, 14, r, Blocks.CHAIN, ModBlocks.VOID_LAMP, 3, 6);
        // Balcony ring partway up the neck.
        ring(l, b, 0, 20, 0, 7, ModBlocks.VOID_BRICK);
        for (int i = 0; i < 12; i++) {
            double ang = i * Math.PI / 6.0D;
            int rx = (int) Math.round(Math.cos(ang) * 7);
            int rz = (int) Math.round(Math.sin(ang) * 7);
            col(l, b, rx, rz, 21, 21, ModBlocks.VOID_BRICK);
            if (i % 3 == 0) setReplace(l, off(b, rx, 22, rz), ModBlocks.VOID_LAMP);
        }
        placeSpawner(l, off(b, 6, 21, 0), ModEntities.VOID_STALKER, r);
        // Entry arch into the body's hollow.
        fill(l, b, -1, 4, 10, 1, 7, 11, Blocks.AIR);
        col(l, b, -2, 10, 4, 8, ModBlocks.VOID_BRICK);
        col(l, b, 2, 10, 4, 8, ModBlocks.VOID_BRICK);
        // Summit chamber on the canopy cap, housing the Spire core.
        fill(l, b, -2, 28, -2, 2, 31, 2, ModBlocks.VOID_BRICK);
        fill(l, b, -1, 28, -1, 1, 31, 1, Blocks.AIR);
        for (int[] c : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            setReplace(l, off(b, c[0], 32, c[1]), ModBlocks.VOID_BRICK);
            setReplace(l, off(b, c[0], 33, c[1]), ModBlocks.VOID_LAMP);
        }
        setReplace(l, off(b, 0, 28, 0), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 29, 0), EndRuinVariant.SPIRE);
        placeWarden(l, off(b, 0, 29, 1));
        // Jagged broken crown shards hovering where they sheared off.
        for (int i = 0; i < 6; i++) {
            double ang = i * Math.PI / 3.0D;
            int sx = (int) Math.round(Math.cos(ang) * (4 + r.nextInt(2)));
            int sz = (int) Math.round(Math.sin(ang) * (4 + r.nextInt(2)));
            setReplace(l, off(b, sx, 33 + r.nextInt(3), sz), ModBlocks.VOID_BRICK);
        }
        lootChest(l, off(b, 1, 29, 1), r, "chests/end_spire");
        lootBarrel(l, off(b, -1, 29, 1), r, "chests/end_spire");
        lootChest(l, off(b, 0, 29, -1), r, "chests/end_spire_treasure");
        inscribe(l, off(b, -4, 1, 9), InscribedSlateBlock.SYMBOL_SPIRE);
        inscribe(l, off(b, 4, 1, 9), InscribedSlateBlock.SYMBOL_RING);
    }

    // =====================================================================
    // VOID CROWN - the Crown Observatory
    // =====================================================================

    private static void crownObservatory(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 19);
        // Two stepped ziggurat tiers as the monument's footprint.
        fill(l, b, -14, 0, -14, 14, 2, 14, ModBlocks.UMBRAL_STONE);
        fill(l, b, -14, 3, -14, 14, 3, 14, ModBlocks.VOID_SLATE);
        fill(l, b, -10, 4, -10, 10, 5, 10, ModBlocks.UMBRAL_STONE);
        // Grand stair climbing the southern faces, inlaid with seal blocks.
        for (int s = 0; s < 3; s++) {
            fill(l, b, -2, 1 + s, 14 - s, 2, 1 + s, 15 - s, ModBlocks.VOID_SLATE);
            if (s % 2 == 0) setReplace(l, off(b, 0, 1 + s, 14 - s), ModBlocks.CROWN_SEAL_BLOCK);
        }
        for (int s = 0; s < 2; s++) {
            fill(l, b, -2, 4 + s, 10 - s, 2, 4 + s, 11 - s, ModBlocks.VOID_SLATE);
            if (s % 2 == 0) setReplace(l, off(b, 0, 4 + s, 10 - s), ModBlocks.CROWN_SEAL_BLOCK);
        }
        // Corner needles crowned with crown-needle blocks.
        for (int[] c : new int[][]{{-13, -13}, {13, -13}, {-13, 13}, {13, 13}}) {
            col(l, b, c[0], c[1], 6, 13, ModBlocks.VOID_BRICK);
            setReplace(l, off(b, c[0], 14, c[1]), ModBlocks.CROWN_NEEDLE_BLOCK);
        }

        // The titan: an umbral colossus bearing the observatory.
        titanRoots(l, b, ModBlocks.UMBRAL_STONE, ModBlocks.VOID_SLATE);
        titanBody(l, b, ModBlocks.UMBRAL_STONE, ModBlocks.VOID_SLATE);
        titanNeckAndCanopy(l, b, ModBlocks.UMBRAL_STONE, ModBlocks.VOID_SLATE,
                ModBlocks.VOID_SLATE, ModBlocks.UMBRAL_STONE,
                ModBlocks.CROWN_SEAL_BLOCK, ModBlocks.CROWN_NEEDLE_BLOCK);
        canopyHangers(l, b, 24, 14, 18, 12, r, Blocks.CHAIN, ModBlocks.VOID_LAMP, 3, 6);

        // The sealed observation orb hangs beneath the canopy on chains:
        // a glass shell over a seal-lattice heart.
        for (int dy = -3; dy <= 3; dy++) {
            int hr = (int) Math.floor(Math.sqrt(Math.max(0, 12.25D - dy * dy)));
            for (int dx = -hr; dx <= hr; dx++)
                for (int dz = -hr; dz <= hr; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > hr) continue;
                    if (d > hr - 1.2D) setReplace(l, off(b, dx, 20 + dy, dz), ModBlocks.VOID_GLASS);
                    else if ((dx + dy + dz) % 2 == 0) setReplace(l, off(b, dx, 20 + dy, dz), ModBlocks.CROWN_SEAL_BLOCK);
                }
        }
        col(l, b, 0, 0, 24, 25, Blocks.CHAIN);
        // Orbiting seal fragments sheared from the orb, hovering in place.
        for (int[] o : new int[][]{{6, 0}, {-6, 0}, {0, 6}, {0, -6}}) {
            setReplace(l, off(b, o[0], 20, o[1]), ModBlocks.CROWN_SEAL_BLOCK);
            setReplace(l, off(b, o[0], 21, o[1]), ModBlocks.VOID_GLASS);
        }
        // Mechanism on the body's crown beside the hanging orb.
        setReplace(l, off(b, 3, 16, 0), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 3, 17, 0), EndRuinVariant.CROWN_OBSERVATORY);
        // Garden tufts along the tier rims.
        for (int i = 0; i < 20; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0D;
            int dx = (int) Math.round(Math.cos(ang) * (11 + r.nextInt(4)));
            int dz = (int) Math.round(Math.sin(ang) * (11 + r.nextInt(4)));
            if (Math.abs(dx) > 13 || Math.abs(dz) > 13) continue;
            setReplace(l, off(b, dx, 4, dz), ModBlocks.UMBRAL_GRASS);
        }
        lootChest(l, off(b, 12, 1, 12), r, "chests/crown_observatory");
        lootBarrel(l, off(b, -12, 1, 12), r, "chests/crownstep_procession");
        lootChest(l, off(b, -4, 5, -4), r, "chests/crownstep_procession");
        placeSpawner(l, off(b, 6, 6, 0), ModEntities.CROWN_SENTINEL, r);
        placeWarden(l, off(b, -8, 6, 8));
        inscribe(l, off(b, -3, 4, 13), InscribedSlateBlock.SYMBOL_SPIRE);
        inscribe(l, off(b, 3, 4, 13), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // UMBRAL REACH - the Null Archive
    // =====================================================================

    private static void nullArchive(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 17);
        // Approach court of woven void.
        disc(l, b, 0, 0, 0, 13, ModBlocks.VOID_SOIL);
        fill(l, b, -1, 0, 6, 1, 0, 13, ModBlocks.VOID_WEAVE);
        // Silent stacks: rows of frame pillars ringing the court.
        for (int x = -10; x <= 10; x += 5)
            for (int z = -10; z <= 10; z += 5) {
                if (Math.abs(x) <= 3 && Math.abs(z) <= 3) continue;
                col(l, b, x, z, 1, 2, ModBlocks.NULL_ARCHIVE_FRAME);
                col(l, b, x, z, 3, 5, ModBlocks.NULL_ARCHIVE_FRAME);
            }

        // The titan: a blind voidstone colossus, frame-ribbed.
        titanRoots(l, b, ModBlocks.VOIDSTONE, ModBlocks.NULL_ARCHIVE_FRAME);
        titanBody(l, b, ModBlocks.VOIDSTONE, ModBlocks.NULL_ARCHIVE_FRAME);
        titanNeckAndCanopy(l, b, ModBlocks.VOIDSTONE, ModBlocks.NULL_ARCHIVE_FRAME,
                ModBlocks.VOIDSTONE, ModBlocks.NULL_ARCHIVE_FRAME,
                ModBlocks.THRESHOLD_CORE_BLOCK, ModBlocks.NULL_ARCHIVE_FRAME);
        // The severed crown: frame rings hover above the canopy, the spire
        // still assembling itself in the dark.
        ring(l, b, 0, 31, 0, 5, ModBlocks.NULL_ARCHIVE_FRAME);
        ring(l, b, 0, 33, 0, 3, ModBlocks.NULL_ARCHIVE_FRAME);
        setReplace(l, off(b, 0, 33, 0), ModBlocks.THRESHOLD_CORE_BLOCK);
        // Sole entrance: a recessed threshold portal facing south.
        fill(l, b, -1, 4, 10, 1, 8, 11, Blocks.AIR);
        col(l, b, -2, 10, 4, 9, ModBlocks.THRESHOLD_CORE_BLOCK);
        col(l, b, 2, 10, 4, 9, ModBlocks.THRESHOLD_CORE_BLOCK);
        fill(l, b, -2, 9, 10, 2, 9, 10, ModBlocks.THRESHOLD_CORE_BLOCK);
        // The well shaft descending to the sealed core rotunda.
        fill(l, b, -2, 4, -2, 2, -5, 2, Blocks.AIR);
        for (int k = 0; k < 6; k++) col(l, b, 2, 2 + k, -k - 1, -k - 1, ModBlocks.VOID_BRICK);
        fill(l, b, -5, -7, -5, 5, -1, 5, Blocks.AIR);
        disc(l, b, 0, -7, 0, 5, ModBlocks.VOID_BRICK);
        ring(l, b, 0, -2, 0, 4, ModBlocks.NULL_ARCHIVE_FRAME);
        for (int[] lp : new int[][]{{-4, 0}, {4, 0}, {0, -4}, {0, 4}}) {
            setReplace(l, off(b, lp[0], -6, lp[1]), ModBlocks.VOID_LAMP);
        }
        col(l, b, 0, 0, -6, -4, ModBlocks.UMBRAL_STONE);
        setReplace(l, off(b, 0, -3, 0), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, -2, 0), EndRuinVariant.ARCHIVE);
        // Alcove caches sunk into the court's edge.
        fill(l, b, -13, 1, -6, -11, 3, -3, Blocks.AIR);
        lootChest(l, off(b, -12, 1, -5), r, "chests/null_archive");
        fill(l, b, 11, 1, 3, 13, 3, 6, Blocks.AIR);
        lootChest(l, off(b, 12, 1, 5), r, "chests/null_archive");
        lootBarrel(l, off(b, 12, 1, 3), r, "chests/hollow_threshold");
        placeSpawner(l, off(b, 0, -6, 4), ModEntities.NULLWALKER, r);
        placeWarden(l, off(b, -3, -6, -3));
        inscribe(l, off(b, 0, 1, 15), InscribedSlateBlock.SYMBOL_EYE);
    }
}
