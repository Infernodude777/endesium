package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.block.InscribedSlateBlock;
import com.infernodude777.endesium.block.ResonantMechanismBlockEntity;
import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The ten Endesium biome-capitals. Each region holds exactly one hand-authored
 * flagship - a cathedral, a castle, a live volcano - placed rarely and built to
 * be seen from far across the void. Every flagship houses a resonant mechanism
 * whose {@link EndRuinVariant} carries that region's Lens signature and its
 * share of the discovery progression.
 */
public final class BiomeStructureFeature extends Feature<NoneFeatureConfiguration> {
    public BiomeStructureFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    /** One flagship attempt-cell per SPACING_GRID-chunk block: ~384 blocks between
     * same-region flagship attempts, scattered organically so no two flagships
     * ever line up into the straight rows the old fixed-mod lattice produced. */
    public static final int SPACING_GRID = 24;

    /**
     * The chunk (as [chunkX, chunkZ]) where this region's flagship would
     * generate inside the given grid cell. Pure math - no world access - so
     * commands can point players at landmarks cheaply. The slot inside each
     * cell is hashed from the cell itself, which scatters attempts organically
     * instead of locking every structure to the same mod-aligned column.
     */
    public static int[] flagshipChunk(long worldSeed, int region, int cellX, int cellZ) {
        return new int[]{
                cellX * SPACING_GRID + cellSlot(worldSeed, region, 0x51L, cellX, cellZ),
                cellZ * SPACING_GRID + cellSlot(worldSeed, region, 0x9D7L, cellX, cellZ)
        };
    }

    /** Hashes one slot inside a grid cell; varies per cell, region, and seed. */
    private static int cellSlot(long worldSeed, int region, long salt, int cellX, int cellZ) {
        long h = worldSeed ^ (region * 0x9E3779B97F4A7C15L) ^ (salt * 0xBF58476D1CE4E5B9L);
        h ^= cellX * 0x9E3779B97F4A7C15L;
        h ^= cellZ * 0xBF58476D1CE4E5B9L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        return (int) Math.floorMod(h, SPACING_GRID);
    }

	/**
	 * The structure-driven build path. Placement conditions (distance gate,
	 * lattice pick) are owned by the vanilla structure sets and the wrapper's
	 * region gate, so this path validates only the biome and dispatches
	 * straight to the hand-authored builders - the same code the legacy path
	 * runs, byte for byte.
	 */
	private static boolean buildForStructure(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		RandomSource random = ctx.random();
		BlockPos origin = ctx.origin();
		int region = EndBiomeProfiles.regionOf(level.getBiome(origin));
		if (region < 0) {
			return false;
		}
		try {
			switch (region) {
				case EndesiumRegions.END_WASTES -> dustCathedral(level, origin, random);
				case EndesiumRegions.CHORUS_WILDS -> elderwoodSanctum(level, origin, random);
				case EndesiumRegions.SHATTERED_HIGHLANDS -> skyrendKeep(level, origin, random);
				case EndesiumRegions.VOID_MARSHES -> drownedCathedral(level, origin, random);
				case EndesiumRegions.LUMINOUS_GROVES -> lumenCathedral(level, origin, random);
				case EndesiumRegions.ASHEN_EXPANSE -> greatCaldera(level, origin, random);
				case EndesiumRegions.CRYSTAL_BARRENS -> sunkenGeode(level, origin, random);
				case EndesiumRegions.VOID_SKIRTS -> voidSpire(level, origin, random);
				case EndesiumRegions.VOID_CROWN -> crownObservatory(level, origin, random);
				case EndesiumRegions.UMBRAL_REACH -> nullArchive(level, origin, random);
				default -> {
					return false;
				}
			}
		} catch (Exception e) {
			com.infernodude777.endesium.Endesium.LOGGER.error(
					"Endesium flagship structure build failed near [{}, {}]", origin.getX(), origin.getZ(), e);
			return false;
		}
		return true;
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		if (StructurePlacement.structureDriven) {
			return buildForStructure(ctx);
		}
		return false;
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

	/** Fills air beneath a feature's base so nothing floats over a dip. */
	private static void groundFill(WorldGenLevel l, BlockPos b, int dx, int dz, int fromY, Block block) {
		for (int y = fromY; y >= fromY - 24 && y >= l.getMinBuildHeight(); y--) {
			BlockPos p = off(b, dx, y, dz);
			if (!l.getBlockState(p).isAir()) {
				break;
			}
			setReplace(l, p, block);
		}
	}

	/** Places a block on the column's actual surface (first air block). */
	private static void surfacePlace(WorldGenLevel l, BlockPos b, int dx, int dz, Block block) {
		int sy = l.getHeight(Heightmap.Types.WORLD_SURFACE_WG, b.getX() + dx, b.getZ() + dz);
		setReplace(l, off(b, dx, sy, dz), block);
	}

	private static void setReplace(WorldGenLevel l, BlockPos p, Block block) {
		if (isProtected(l, p)) return;
		BlockState state = block.defaultBlockState();
		StructurePlacement.set(l, p, state, 3);
		// Fluids placed during worldgen stay static until ticked; schedule a
		// tick so lava and water settle and flow naturally once the world is
		// live instead of hanging in the air as dead sources.
		if (!state.getFluidState().isEmpty()) {
			l.scheduleTick(p, state.getFluidState().getType(), 0);
		}
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
        // Settle at the intended post (crypt floor, balcony ring, throne dais)
        // with real footing validation, never teleporting to the surface, then
        // refuse duplicates so a flagship never hosts two wardens at once.
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

    /** A hanging lamp: chain stub from above with a void lamp at the tip. */
    private static void hangLamp(WorldGenLevel l, BlockPos b, int dx, int dy, int dz) {
        col(l, b, dx, dz, dy + 1, dy + 2, Blocks.CHAIN);
        setReplace(l, off(b, dx, dy, dz), ModBlocks.VOID_LAMP);
    }

    // =====================================================================
    // END WASTES - the Dust Cathedral
    // =====================================================================

    private static void dustCathedral(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 19);

        // Cruciform platform.
        fill(l, b, -8, 0, -17, 8, 0, 17, ModBlocks.WASTES_STONE);
        disc(l, b, 0, 0, -19, 4, ModBlocks.WASTES_STONE);   // apse tongue
        fill(l, b, -5, 0, 18, 5, 0, 20, ModBlocks.WASTES_STONE); // approach court
        ring(l, b, 0, 0, 0, 12, Blocks.END_STONE_BRICKS);

        // Nave shell: soaring walls to y=13, hollow interior.
        fill(l, b, -7, 1, -16, 7, 13, 16, ModBlocks.WASTES_STONE);
        fill(l, b, -6, 1, -15, 6, 12, 15, Blocks.AIR);
        // Twin clerestory window bands running the full length.
        for (int z = -13; z <= 13; z += 4) {
            fill(l, b, -7, 4, z, -7, 5, z, ModBlocks.VOID_GLASS);
            fill(l, b, 7, 4, z, 7, 5, z, ModBlocks.VOID_GLASS);
            fill(l, b, -7, 9, z, -7, 10, z, ModBlocks.VOID_GLASS);
            fill(l, b, 7, 9, z, 7, 10, z, ModBlocks.VOID_GLASS);
        }
        // Transept arms give the cathedral its true cross silhouette.
        buildTranseptArm(l, b, -1, r);
        buildTranseptArm(l, b, 1, r);
        // Rose window set into the apse face: stained glass petals in cyan
        // and light blue radiating around a pale crystal heart.
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++) {
                if (dx * dx + dy * dy > 5 || (dx == 0 && dy == 0)) continue;
                Block petal = (dx + dy) % 2 == 0
                        ? Blocks.LIGHT_BLUE_STAINED_GLASS
                        : Blocks.CYAN_STAINED_GLASS;
                setReplace(l, off(b, dx, 8 + dy, -20), petal);
            }
        setReplace(l, off(b, 0, 8, -20), ModBlocks.PALE_CRYSTAL_BLOCK);
        // Nave chandelier: a gold hub with four hanging lamps.
        col(l, b, 0, 0, 10, 11, Blocks.CHAIN);
        setReplace(l, off(b, 0, 9, 0), Blocks.GOLD_BLOCK);
        hangLamp(l, b, -2, 8, 0);
        hangLamp(l, b, 2, 8, 0);
        hangLamp(l, b, 0, 8, -2);
        hangLamp(l, b, 0, 8, 2);
        // Buttresses along the outer flanks, arched at the top.
        for (int z = -12; z <= 14; z += 4) {
            fill(l, b, -9, 1, z, -8, 8, z, ModBlocks.CRACKED_SPIRE_STONE);
            setReplace(l, off(b, -10, 8, z), ModBlocks.CRACKED_SPIRE_STONE);
            fill(l, b, 8, 1, z, 9, 8, z, ModBlocks.CRACKED_SPIRE_STONE);
            setReplace(l, off(b, 10, 8, z), ModBlocks.CRACKED_SPIRE_STONE);
        }
        // Partial vaulted roof with collapsed gaps.
        for (int x = -6; x <= 6; x++)
            for (int z = -15; z <= 15; z++) {
                boolean beam = Math.abs(x) % 3 == 0 || z % 6 == 0;
                if (beam && r.nextInt(5) != 0)
                    setReplace(l, off(b, x, 13, z), ModBlocks.CRACKED_SPIRE_STONE);
            }
        // Grand south entrance arch with crystal keystone.
        fill(l, b, -1, 1, 16, 1, 4, 16, Blocks.AIR);
        col(l, b, -3, 16, 1, 5, Blocks.END_STONE_BRICKS);
        col(l, b, 3, 16, 1, 5, Blocks.END_STONE_BRICKS);
        fill(l, b, -2, 5, 16, 2, 5, 16, Blocks.END_STONE_BRICKS);
        setReplace(l, off(b, 0, 6, 16), ModBlocks.DORMANT_RESONANT_CRYSTAL);

        // Twin bell towers flanking the entrance court.
        buildBellTower(l, b, -11, 13, 22, r);
        buildBellTower(l, b, 11, 13, 22, r);

        // Crossing tower over the apse with tapering spire.
        fill(l, b, -3, 1, -20, 3, 15, -16, ModBlocks.WASTES_STONE);
        fill(l, b, -2, 1, -19, 2, 14, -17, Blocks.AIR);
        for (int level = 16; level <= 26; level++) {
            int half = Math.max(1, 3 - (level - 16) / 4);
            fill(l, b, -half, level, -22 + (26 - level), half, level, -14 - (26 - level), ModBlocks.CRACKED_SPIRE_STONE);
        }
        setReplace(l, off(b, 0, 27, -18), ModBlocks.DORMANT_RESONANT_CRYSTAL);

        // Interior colonnades crowned with alternating beacons.
        for (int z = -12; z <= 12; z += 4) {
            if (z >= -14 && z <= -10) continue; // crossing floor stays open
            col(l, b, -4, z, 1, 12, ModBlocks.CRACKED_SPIRE_STONE);
            col(l, b, 4, z, 1, 12, ModBlocks.CRACKED_SPIRE_STONE);
            Block cap = (z / 4) % 2 == 0 ? ModBlocks.VOID_LAMP : ModBlocks.DORMANT_RESONANT_CRYSTAL;
            setReplace(l, off(b, -4, 13, z), cap);
            setReplace(l, off(b, 4, 13, z), cap);
        }
        // Altar dais and mechanism.
        fill(l, b, -3, 1, -8, 3, 1, -4, ModBlocks.END_GRAY);
        fill(l, b, -2, 2, -7, 2, 2, -5, Blocks.END_STONE_BRICKS);
        setReplace(l, off(b, 0, 3, -6), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 4, -6), EndRuinVariant.INTACT);

        // Crypt beneath the crossing, reached by a side stair.
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
        // The crypt is a dust crawler nest; magma vents crack the floor
        // between the coffers so looters must watch their step.
        placeSpawner(l, off(b, -3, -4, -1), ModEntities.DUST_CRAWLER, r);
        setReplace(l, off(b, 0, -5, -3), Blocks.MAGMA_BLOCK);
        setReplace(l, off(b, -2, -5, -2), Blocks.MAGMA_BLOCK);
        setReplace(l, off(b, 2, -5, -2), Blocks.MAGMA_BLOCK);
        placeWarden(l, off(b, 1, -4, -3));

        // Scree field around the cathedral, snapped to the surface so no
        // piece ever hangs over a dip.
        for (int i = 0; i < 30; i++) {
            int dx = r.nextInt(35) - 17, dz = r.nextInt(43) - 21;
            if (Math.abs(dx) <= 9 && Math.abs(dz) <= 17) continue;
            surfacePlace(l, b, dx, dz, r.nextBoolean() ? ModBlocks.WASTES_GRAVEL : ModBlocks.CRACKED_SPIRE_STONE);
        }
        inscribe(l, off(b, -4, 1, 18), InscribedSlateBlock.SYMBOL_SPIRE);
        inscribe(l, off(b, 4, 1, 18), InscribedSlateBlock.SYMBOL_EYE);
    }

    /** A transept arm extending the cathedral into a true cross plan. */
    private static void buildTranseptArm(WorldGenLevel l, BlockPos b, int dir, RandomSource r) {
        int outer = dir * 17;
        int inner = dir * 8;
        int x0 = Math.min(outer, inner), x1 = Math.max(outer, inner);
        fill(l, b, x0, 0, -14, x1, 0, -8, Blocks.END_STONE_BRICKS);
        fill(l, b, x0, 1, -14, x1, 9, -14, ModBlocks.WASTES_STONE);   // far wall
        fill(l, b, x0, 1, -8, x1, 9, -8, ModBlocks.WASTES_STONE);     // near wall
        fill(l, b, x0, 1, -13, x0, 9, -9, ModBlocks.WASTES_STONE);    // outer end
        fill(l, b, x0 + (dir == -1 ? 1 : 0), 1, -12, x1 - (dir == 1 ? 1 : 0), 8, -10, Blocks.AIR);
        // Great window on the gable end.
        for (int dz = -12; dz <= -10; dz++)
            for (int dy = 4; dy <= 7; dy++)
                setReplace(l, off(b, outer, dy, dz), ModBlocks.VOID_GLASS);
        setReplace(l, off(b, outer, 6, -11), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        // Vault stub and side benches.
        if (r.nextBoolean()) fill(l, b, x0 + (dir == -1 ? 2 : 0), 9, -12, x1 - (dir == 1 ? 2 : 0), 9, -10, ModBlocks.CRACKED_SPIRE_STONE);
        lootBarrel(l, off(b, dir * 15, 1, -9), r, "chests/wastes_cathedral");
    }

    private static void buildBellTower(WorldGenLevel l, BlockPos b, int cx, int cz, int height, RandomSource r) {
        for (int y = 1; y <= height; y++) {
            fill(l, b, cx - 2, y, cz - 2, cx + 2, y, cz + 2,
                    y % 7 == 0 ? Blocks.END_STONE_BRICKS : ModBlocks.WASTES_STONE);
            fill(l, b, cx - 1, y, cz - 1, cx + 1, y, cz + 1, Blocks.AIR);
        }
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) != 2 && Math.abs(dz) != 2) continue;
                setReplace(l, off(b, cx + dx, height + 1, cz + dz), Blocks.END_STONE_BRICKS);
                if ((dx + dz) % 2 == 0) setReplace(l, off(b, cx + dx, height + 2, cz + dz), Blocks.END_STONE_BRICKS);
            }
        setReplace(l, off(b, cx, height + 3, cz), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        fill(l, b, cx - 2, 6, cz, cx - 2, 7, cz, ModBlocks.VOID_GLASS);
        fill(l, b, cx + 2, 6, cz, cx + 2, 7, cz, ModBlocks.VOID_GLASS);
        // The bell itself.
        col(l, b, cx, cz, height - 2, height - 1, Blocks.CHAIN);
        setReplace(l, off(b, cx, height - 3, cz), Blocks.GOLD_BLOCK);
    }

    // =====================================================================
    // CHORUS WILDS - the Elderwood Sanctum
    // =====================================================================

    private static void elderwoodSanctum(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 20);

        // Colossal hollow trunk: bark shell tapering 8 -> 4 over 28 blocks.
        for (int y = 0; y <= 26; y++) {
            int rad = Math.max(4, 8 - y / 7);
            for (int dx = -rad; dx <= rad; dx++)
                for (int dz = -rad; dz <= rad; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > rad * rad) continue;
                    if (y > 0 && d2 <= (rad - 2) * (rad - 2)) continue; // hollow core
                    Block bark = (dx + dz + y) % 4 == 0 ? ModBlocks.ELDER_CHORUS_WOOD : ModBlocks.ELDER_CHORUS_BARK;
                    setReplace(l, off(b, dx, y, dz), bark);
                }
        }
        // Moss altar floor at the heart of the trunk.
        disc(l, b, 0, 0, 0, 4, ModBlocks.CHORUS_MOSS);
        setReplace(l, off(b, 0, 1, 0), ModBlocks.ELDER_CHORUS_WOOD);
        landmarkMechanism(l, off(b, 0, 2, 0), EndRuinVariant.BLOOM_CONSERVATORY);

        // Root buttresses radiating out and down from the trunk base.
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4.0 + 0.39;
            double cos = Math.cos(ang), sin = Math.sin(ang);
            for (int k = 0; k <= 6; k++) {
                int dx = (int) Math.round(cos * (7 + k));
                int dz = (int) Math.round(sin * (7 + k));
                col(l, b, dx, dz, 0, Math.max(1, 8 - k), k % 2 == 0 ? ModBlocks.CHORUS_ROOT : ModBlocks.ELDER_CHORUS_BARK);
            }
        }
        // Spiral ledge climbing inside the trunk.
        for (int y = 2; y <= 22; y++) {
            double ang = y * 0.55D;
            int lx = (int) Math.round(Math.cos(ang) * 3);
            int lz = (int) Math.round(Math.sin(ang) * 3);
            setReplace(l, off(b, lx, y, lz), ModBlocks.CHORUS_ROOT);
            setReplace(l, off(b, lx, y - 1, lz), ModBlocks.CHORUS_ROOT);
        }
        // Canopy: layered discs with branch spokes reaching far out.
        disc(l, b, 0, 24, 0, 18, ModBlocks.CHORUS_ROOT);
        disc(l, b, 0, 25, 0, 16, ModBlocks.CHORUS_MOSS);
        disc(l, b, 0, 26, 0, 13, ModBlocks.CHORUS_MOSS);
        disc(l, b, 0, 27, 0, 9, ModBlocks.CHORUS_ROOT);
        disc(l, b, 0, 28, 0, 5, ModBlocks.CHORUS_MOSS);
        for (int i = 0; i < 10; i++) {
            double ang = i * Math.PI / 5.0;
            double cos = Math.cos(ang), sin = Math.sin(ang);
            for (int k = 10; k <= 19; k++) {
                int drop = k > 15 ? (k - 15) : 0;
                setReplace(l, off(b, (int) Math.round(cos * k), 23 - drop, (int) Math.round(sin * k)), ModBlocks.HOLLOW_CHORUS_WOOD);
            }
            int tx = (int) Math.round(cos * 20), tz = (int) Math.round(sin * 20);
            setReplace(l, off(b, tx, 21, tz), Blocks.CHORUS_PLANT);
            if (r.nextBoolean()) setReplace(l, off(b, tx, 22, tz), Blocks.CHORUS_FLOWER);
        }
        // Hanging tendrils with blooms beneath the canopy rim.
        for (int i = 0; i < 12; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0;
            int dx = (int) Math.round(Math.cos(ang) * (11 + r.nextInt(5)));
            int dz = (int) Math.round(Math.sin(ang) * (11 + r.nextInt(5)));
            int len = 5 + r.nextInt(5);
            for (int k = 0; k < len; k++) {
                BlockPos p = off(b, dx, 23 - k, dz);
                if (l.getBlockState(p).isAir()) setReplace(l, p, ModBlocks.CHORUS_ROOT);
                else break;
            }
            setReplace(l, off(b, dx, 23 - len, dz), ModBlocks.RESONANT_BLOOM);
        }
        // Mid-trunk gallery with loot, lit by blooms.
        fill(l, b, -5, 11, -5, 5, 14, 5, Blocks.AIR);
        fill(l, b, -5, 10, -5, 5, 10, 5, ModBlocks.CHORUS_ROOT);
        lootChest(l, off(b, -3, 11, 3), r, "chests/wilds_archive");
        lootBarrel(l, off(b, 3, 11, 3), r, "chests/wilds_archive");
        setReplace(l, off(b, 0, 15, -4), ModBlocks.RESONANT_BLOOM);
        setReplace(l, off(b, 4, 12, 0), ModBlocks.RESONANT_BLOOM);

        // Root vault below with the deeper cache.
        fill(l, b, -5, -5, -5, 5, -1, 5, Blocks.AIR);
        fill(l, b, -5, -6, -5, 5, -6, 5, ModBlocks.CHORUS_MOSS);
        col(l, b, 0, 0, -5, -1, ModBlocks.ELDER_CHORUS_WOOD);
        lootChest(l, off(b, -3, -5, -3), r, "chests/bloom_conservatory");
        lootBarrel(l, off(b, 3, -5, -3), r, "chests/bloom_conservatory");
        lootChest(l, off(b, 3, -5, 3), r, "chests/end_spire_treasure");
        // The root vault is a chorus stalker den: they blink between the
        // root pillars when intruders descend.
        placeSpawner(l, off(b, 0, -4, 4), ModEntities.CHORUS_STALKER, r);
        placeWarden(l, off(b, 0, -5, -2));
        inscribe(l, off(b, 8, 1, 0), InscribedSlateBlock.SYMBOL_RING);
        inscribe(l, off(b, -8, 1, 0), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // SHATTERED HIGHLANDS - Skyrend Keep
    // =====================================================================

    private static void skyrendKeep(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 20);

        // Curtain wall square, two thick, nine tall.
        fill(l, b, -16, 0, -16, 16, 0, 16, ModBlocks.HIGHLAND_SLATE);
        fill(l, b, -16, 1, -16, 16, 8, 16, ModBlocks.HIGHLAND_STONE);
        fill(l, b, -14, 1, -14, 14, 7, 14, Blocks.AIR);
        fill(l, b, -16, 4, -16, 16, 4, 16, ModBlocks.HIGHLAND_SLATE);
        // Crenellations.
        for (int i = -16; i <= 16; i += 2) {
            col(l, b, i, -16, 9, 9, ModBlocks.HIGHLAND_STONE);
            col(l, b, i, 16, 9, 9, ModBlocks.HIGHLAND_STONE);
            col(l, b, -16, i, 9, 9, ModBlocks.HIGHLAND_STONE);
            col(l, b, 16, i, 9, 9, ModBlocks.HIGHLAND_STONE);
        }

        // Gatehouse: south opening flanked by twin turrets, portcullis of
        // iron bars half-raised in the gateway.
        fill(l, b, -2, 1, 15, 2, 4, 16, Blocks.AIR);
        col(l, b, -2, 16, 3, 4, Blocks.IRON_BARS);
        col(l, b, 2, 16, 3, 4, Blocks.IRON_BARS);
        fill(l, b, -6, 1, 15, -3, 12, 17, ModBlocks.HIGHLAND_STONE);
        fill(l, b, 3, 1, 15, 6, 12, 17, ModBlocks.HIGHLAND_STONE);
        fill(l, b, -6, 1, 14, -3, 12, 18, ModBlocks.HIGHLAND_STONE);
        fill(l, b, 3, 1, 14, 6, 12, 18, ModBlocks.HIGHLAND_STONE);
        fill(l, b, -5, 1, 15, -4, 12, 16, Blocks.AIR);
        fill(l, b, 4, 1, 15, 5, 12, 16, Blocks.AIR);
        setReplace(l, off(b, 0, 13, 16), ModBlocks.HIGHLAND_LENSSTONE);

        // Four corner towers with lensstone crowns.
        buildWatchTower(l, b, -16, -16, 14, r);
        buildWatchTower(l, b, 16, -16, 14, r);
        buildWatchTower(l, b, -16, 16, 14, r);
        buildWatchTower(l, b, 16, 16, 14, r);
        for (int[] c : new int[][]{{-16, -16}, {16, -16}, {-16, 16}, {16, 16}}) {
            setReplace(l, off(b, c[0], 16, c[1]), ModBlocks.HIGHLAND_LENSSTONE);
        }

        // Inner keep: three storeys plus roof garden.
        fill(l, b, -7, 1, -7, 7, 18, 7, ModBlocks.HIGHLAND_STONE);
        fill(l, b, -6, 1, -6, 6, 17, 6, Blocks.AIR);
        fill(l, b, -7, 6, -7, 7, 6, 7, ModBlocks.HIGHLAND_SLATE);
        fill(l, b, -7, 12, -7, 7, 12, 7, ModBlocks.HIGHLAND_SLATE);
        fill(l, b, -1, 1, 7, 1, 3, 7, Blocks.AIR);
        for (int z = -4; z <= 4; z += 4) {
            fill(l, b, -7, 8, z, -7, 9, z, ModBlocks.VOID_GLASS);
            fill(l, b, 7, 8, z, 7, 9, z, ModBlocks.VOID_GLASS);
        }
        // Throne dais on the top floor: a gold seat flanked by iron-bar
        // armrests, with the keep's core burning behind it.
        fill(l, b, -3, 13, -6, 3, 13, -2, ModBlocks.HIGHLAND_SLATE);
        fill(l, b, -2, 14, -5, 2, 14, -3, ModBlocks.HIGHLAND_STONE);
        setReplace(l, off(b, 0, 14, -4), Blocks.GOLD_BLOCK);
        col(l, b, 0, -5, 15, 15, ModBlocks.WINDSCAR_BRACKET);
        setReplace(l, off(b, 0, 16, -5), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        col(l, b, -1, -6, 14, 14, Blocks.IRON_BARS);
        col(l, b, 1, -6, 14, 14, Blocks.IRON_BARS);
        setReplace(l, off(b, 0, 17, -4), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 18, -4), EndRuinVariant.RIFT_OBSERVATORY);
        // Roof parapet and crystal masts; a void ray roosts on the roof
        // garden, diving at anyone who reaches the throne floor.
        placeSpawner(l, off(b, 0, 14, 0), ModEntities.VOID_RAY, r);
        placeWarden(l, off(b, 4, 13, 4));
        for (int i = -7; i <= 7; i += 2) {
            col(l, b, i, -7, 19, 19, ModBlocks.HIGHLAND_STONE);
            col(l, b, i, 7, 19, 19, ModBlocks.HIGHLAND_STONE);
            col(l, b, -7, i, 19, 19, ModBlocks.HIGHLAND_STONE);
            col(l, b, 7, i, 19, 19, ModBlocks.HIGHLAND_STONE);
        }
        for (int[] mast : new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}}) {
            col(l, b, mast[0], mast[1], 13, 15, Blocks.CHAIN);
            setReplace(l, off(b, mast[0], 16, mast[1]), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        }

        // Courtyard dressing: banner masts, beacon posts, barracks ruin.
        for (int[] mast : new int[][]{{-12, 0}, {12, 0}, {0, -12}}) {
            col(l, b, mast[0], mast[1], 1, 6, ModBlocks.WINDSCAR_BRACKET);
            setReplace(l, off(b, mast[0], 7, mast[1]), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        }
        for (int[] post : new int[][]{{-11, 11}, {11, 11}}) {
            col(l, b, post[0], post[1], 1, 1, ModBlocks.HIGHLAND_STONE);
            setReplace(l, off(b, post[0], 2, post[1]), ModBlocks.HIGHLAND_LENSSTONE);
        }
        fill(l, b, 9, 1, -14, 14, 4, -10, ModBlocks.HIGHLAND_STONE);
        fill(l, b, 10, 1, -13, 13, 4, -11, Blocks.AIR);
        placeSpawner(l, off(b, 12, 2, -12), EntityType.PHANTOM, r);

        lootChest(l, off(b, -6, 2, 6), r, "chests/highland_observatory");
        lootBarrel(l, off(b, -6, 13, 5), r, "chests/highland_observatory");
        lootChest(l, off(b, 11, 2, -13), r, "chests/windscar_lift");
        inscribe(l, off(b, -4, 1, 13), InscribedSlateBlock.SYMBOL_RING);
        inscribe(l, off(b, 4, 1, 13), InscribedSlateBlock.SYMBOL_SPIRE);
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
        fill(l, b, cx - 2, 6, cz, cx - 2, 7, cz, ModBlocks.VOID_GLASS);
        fill(l, b, cx + 2, 6, cz, cx + 2, 7, cz, ModBlocks.VOID_GLASS);
    }

    // =====================================================================
    // VOID MARSHES - the Drowned Cathedral
    // =====================================================================

    private static void drownedCathedral(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 17);
        // Sunken nave: a two-step basin of mire, moss, and still black water.
        fill(l, b, -10, -1, -14, 10, -1, 12, Blocks.AIR);
        fill(l, b, -10, -2, -14, 10, -2, 12, ModBlocks.VOID_MARSH_SOIL);
        for (int i = 0; i < 40; i++) {
            int dx = r.nextInt(21) - 10, dz = r.nextInt(27) - 13;
            setReplace(l, off(b, dx, -2, dz), r.nextBoolean() ? ModBlocks.MARSH_MOSS : ModBlocks.VOID_MARSH_SOIL);
        }
        // Still pools of dark water gathering in the low places.
        for (int i = 0; i < 14; i++) {
            int dx = r.nextInt(19) - 9, dz = r.nextInt(25) - 12;
            if (Math.abs(dx) <= 3 && dz >= -4 && dz <= 0) continue; // keep the altar isle dry
            fill(l, b, dx - 1, -2, dz - 1, dx + 1, -2, dz + 1,
                    r.nextBoolean() ? Blocks.WATER : ModBlocks.MARSH_MOSS);
            setReplace(l, off(b, dx, -2, dz), Blocks.WATER);
        }
        // Rib skeleton: tide iron posts and arch beams marching down the nave.
        for (int z = -12; z <= 12; z += 6) {
            col(l, b, -11, z, -1, 9, ModBlocks.TIDE_IRON);
            col(l, b, 11, z, -1, 9, ModBlocks.TIDE_IRON);
            fill(l, b, -11, 10, z, 11, 10, z, ModBlocks.TIDE_IRON);
            fill(l, b, -8, 11, z, 8, 11, z, ModBlocks.TIDE_IRON);
            if (r.nextInt(3) == 0) fill(l, b, -5, 12, z, 5, 12, z, ModBlocks.TIDE_IRON);
            // Lantern chains swaying from the rib beams.
            col(l, b, -6, z + (z >= 0 ? 2 : -2), 8, 9, Blocks.CHAIN);
            setReplace(l, off(b, -6, 7, z + (z >= 0 ? 2 : -2)), ModBlocks.VOID_LAMP);
            col(l, b, 6, z + (z >= 0 ? 2 : -2), 8, 9, Blocks.CHAIN);
            setReplace(l, off(b, 6, 7, z + (z >= 0 ? 2 : -2)), ModBlocks.VOID_LAMP);
        }
        // A drowned sister-tower, snapped off at half height.
        for (int y = -1; y <= 7; y++) {
            int rad = y > 4 ? 2 : 3;
            ring(l, b, 15, y, -9 + (y / 5), rad, ModBlocks.TIDE_IRON);
        }
        disc(l, b, 15, -1, -9, 2, ModBlocks.MARSH_MOSS);
        // Ruined low perimeter courses with weathering gaps.
        for (int z = -14; z <= 12; z++) {
            if (r.nextInt(4) == 0) continue;
            col(l, b, -12, z, -1, 2, ModBlocks.TIDE_IRON);
            col(l, b, 12, z, -1, 2, ModBlocks.TIDE_IRON);
        }
        // Leaning bell tower rising out of the mire at the north-west flank.
        int lean = 0;
        for (int y = -1; y <= 16; y++) {
            lean = Math.max(0, y / 7);
            fill(l, b, -15 + lean, y, 10, -11 + lean, y, 14,
                    y % 6 == 0 ? ModBlocks.MARSH_MOSS : ModBlocks.TIDE_IRON);
            fill(l, b, -14 + lean, y, 11, -12 + lean, y, 13, Blocks.AIR);
        }
        col(l, b, -13 + 2, 12, 15, 16, Blocks.CHAIN);
        setReplace(l, off(b, -13 + 2, 14, 12), Blocks.GOLD_BLOCK);
        setReplace(l, off(b, -13 + 2, 17, 12), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        // Offerings drowned in the mire at the tower's base: gold and relics.
        setReplace(l, off(b, -11, -2, 15), Blocks.GOLD_BLOCK);
        setReplace(l, off(b, -10, -2, 15), ModBlocks.RESONANT_BLOOM);
        lootChest(l, off(b, -9, -2, 16), r, "chests/marsh_tide_bell");
        // Central altar island with the mechanism.
        fill(l, b, -3, -1, -4, 3, -1, 0, ModBlocks.TIDE_IRON);
        fill(l, b, -2, 0, -3, 2, 0, -1, ModBlocks.VOID_MARSH_SOIL);
        setReplace(l, off(b, 0, 1, -2), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 2, -2), EndRuinVariant.TIDE_BELL);
        placeWarden(l, off(b, -2, 1, -1));
        // Broken pew rows flanking the processional aisle.
        for (int z = 2; z <= 10; z += 3) {
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
            int dx = r.nextInt(21) - 10, dz = r.nextInt(25) - 12;
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
        // A floor that glows beneath your feet.
        fill(l, b, -11, 0, -20, 11, 0, 20, ModBlocks.LUMEN_STONE);
        // Glass curtain walls held by lumen pilasters every three blocks.
        fill(l, b, -10, 1, -19, 10, 13, 19, ModBlocks.VOID_GLASS);
        for (int z = -18; z <= 18; z += 3) {
            col(l, b, -10, z, 1, 14, ModBlocks.LUMEN_STONE);
            col(l, b, 10, z, 1, 14, ModBlocks.LUMEN_STONE);
        }
        for (int x = -9; x <= 9; x += 3) {
            col(l, b, x, -19, 1, 14, ModBlocks.LUMEN_STONE);
            col(l, b, x, 19, 1, 14, ModBlocks.LUMEN_STONE);
        }
        // Prism cornice and glass ceiling with prismatic ribs.
        fill(l, b, -10, 13, -19, 10, 13, 19, ModBlocks.PRISM_CANOPY_BLOCK);
        for (int x = -9; x <= 9; x++)
            for (int z = -18; z <= 18; z++)
                if ((Math.abs(x) % 3 == 0 || Math.abs(z) % 4 == 0) && r.nextInt(6) != 0)
                    setReplace(l, off(b, x, 14, z), ModBlocks.PRISM_CANOPY_BLOCK);
        // Hollow the hall.
        fill(l, b, -9, 1, -18, 9, 12, 18, Blocks.AIR);
        // Rose window on the south face with a crystal heart.
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = -3; dy <= 3; dy++)
                if (dx * dx + dy * dy <= 9)
                    setReplace(l, off(b, dx, 7 + dy, 19),
                            (dx == 0 && dy == 0) ? ModBlocks.DORMANT_RESONANT_CRYSTAL : ModBlocks.PALE_CRYSTAL_BLOCK);
        // Twin spires flanking the entrance.
        buildLumenSpire(l, b, -8, 17, 28);
        buildLumenSpire(l, b, 8, 17, 28);
        // Central lantern spire over the apse.
        for (int level = 14; level <= 30; level++) {
            int half = level < 20 ? 3 : level < 26 ? 2 : 1;
            fill(l, b, -half, level, -19 + (30 - level) / 2, half, level, -15 - (30 - level) / 2,
                    level % 4 == 0 ? ModBlocks.PRISM_CANOPY_BLOCK : ModBlocks.LUMEN_STONE);
        }
        setReplace(l, off(b, 0, 31, -17), ModBlocks.DORMANT_RESONANT_CRYSTAL);
        // Colonnade, chandeliers, and the aisle carpet.
        for (int z = -14; z <= 14; z += 4) {
            col(l, b, -5, z, 1, 12, ModBlocks.LUMEN_STONE);
            col(l, b, 5, z, 1, 12, ModBlocks.LUMEN_STONE);
        }
        for (int z = -12; z <= 12; z += 7) {
            col(l, b, -3, z, 12, 13, Blocks.CHAIN);
            setReplace(l, off(b, -3, 11, z), ModBlocks.LUMEN_GRAFT_BLOCK);
            col(l, b, 3, z, 12, 13, Blocks.CHAIN);
            setReplace(l, off(b, 3, 11, z), ModBlocks.LUMEN_GRAFT_BLOCK);
        }
        fill(l, b, -1, 0, -18, 1, 0, 18, ModBlocks.LUMEN_MOSS);
        // A prismatic inlay marks the crossing of the nave.
        fill(l, b, -2, 0, -3, 2, 0, 3, ModBlocks.PRISM_CANOPY_BLOCK);
        disc(l, b, 0, 0, 0, 1, ModBlocks.PALE_CRYSTAL_BLOCK);
        // Congregation pews: lumen-moss benches flanking the carpet.
        for (int z = -10; z <= 14; z += 3) {
            if (Math.abs(z) <= 3) continue; // keep the crossing clear
            setReplace(l, off(b, -4, 1, z), ModBlocks.LUMEN_MOSS);
            setReplace(l, off(b, -3, 1, z), ModBlocks.LUMEN_MOSS);
            setReplace(l, off(b, 3, 1, z), ModBlocks.LUMEN_MOSS);
            setReplace(l, off(b, 4, 1, z), ModBlocks.LUMEN_MOSS);
        }
        // Crystal gardens flanking the entrance court.
        for (int[] g : new int[][]{{-7, 22}, {7, 22}, {-10, 20}, {10, 20}}) {
            setReplace(l, off(b, g[0], 1, g[1]), ModBlocks.CRYSTAL_CLUSTER);
            setReplace(l, off(b, g[0] + (g[0] > 0 ? -1 : 1), 1, g[1]), ModBlocks.LUMEN_BLOOM);
        }
        // Apse dais and mechanism.
        fill(l, b, -4, 1, -18, 4, 1, -15, ModBlocks.PRISM_CANOPY_BLOCK);
        fill(l, b, -2, 2, -17, 2, 2, -16, Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 3, -16), EndRuinVariant.PRISM_CANOPY);

        lootChest(l, off(b, -8, 1, 16), r, "chests/bloom_conservatory");
        lootBarrel(l, off(b, 8, 1, 16), r, "chests/prism_canopy");
        lootChest(l, off(b, -8, 1, -17), r, "chests/luminous_lightwell");
        // A lumen moth swarm roosts in the open nave near the chandeliers.
        placeSpawner(l, off(b, 0, 10, 7), ModEntities.LUMEN_MOTH, r);
        placeWarden(l, off(b, 3, 1, -14));
        inscribe(l, off(b, -4, 1, 19), InscribedSlateBlock.SYMBOL_RING);
        inscribe(l, off(b, 4, 1, 19), InscribedSlateBlock.SYMBOL_EYE);
    }

    private static void buildLumenSpire(WorldGenLevel l, BlockPos b, int cx, int cz, int top) {
        for (int y = 14; y <= top; y++) {
            int half = y < 20 ? 1 : 0;
            fill(l, b, cx - half, y, cz - half, cx + half, y, cz + half,
                    y % 5 == 0 ? ModBlocks.PRISM_CANOPY_BLOCK : ModBlocks.LUMEN_STONE);
        }
        setReplace(l, off(b, cx, top + 1, cz), ModBlocks.DORMANT_RESONANT_CRYSTAL);
    }

    // =====================================================================
    // ASHEN EXPANSE - the Great Caldera
    // =====================================================================

    private static void greatCaldera(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 21);

        // Solid root massif up to the future crater floor.
        for (int y = 0; y <= 8; y++) {
            int rad = coneRadius(y);
            disc(l, b, 0, y, 0, rad, y % 5 == 0 ? ModBlocks.RESONANT_BASALT : ModBlocks.ASH_STONE);
        }
        // Cone shell above, hollowed into a crater bowl.
        for (int y = 9; y <= 24; y++) {
            int rad = coneRadius(y);
            for (int dx = -rad; dx <= rad; dx++)
                for (int dz = -rad; dz <= rad; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > rad * rad || d2 < (rad - 2) * (rad - 2)) {
                        if (d2 <= (rad - 2) * (rad - 2)) setReplace(l, off(b, dx, y, dz), Blocks.AIR);
                        continue;
                    }
                    Block blk = y % 5 == 0 ? ModBlocks.RESONANT_BASALT
                            : y >= 22 ? ModBlocks.ASHEN_SOIL : ModBlocks.ASH_STONE;
                    setReplace(l, off(b, dx, y, dz), blk);
                }
        }
        // Crater lava lake, hemmed by an obsidian shore so the edge reads as
        // cooled glass over molten rock.
        int rimY = 9;
        int lakeRad = coneRadius(rimY) - 2;
        disc(l, b, 0, rimY, 0, lakeRad + 1, Blocks.OBSIDIAN);
        disc(l, b, 0, rimY, 0, lakeRad, Blocks.LAVA);
        ring(l, b, 0, rimY + 1, 0, lakeRad + 2, Blocks.CRYING_OBSIDIAN);
        // Lava falls pouring from rim notches kept away from the southern
        // vault tunnel, so the hoard room stays dry.
        for (double ang : new double[]{0.4D, 2.6D, 3.5D, 5.2D}) {
            int nx = (int) Math.round(Math.cos(ang));
            int nz = (int) Math.round(Math.sin(ang));
            for (int y = 9; y >= 0; y--) {
                int rr = coneRadius(y) - 1;
                BlockPos p = off(b, nx * rr, y, nz * rr);
                setReplace(l, p, Blocks.LAVA);
                setReplace(l, off(b, (nx * rr) + nx, y, (nz * rr) + nz), Blocks.MAGMA_BLOCK);
            }
        }
        // Glowing magma crack veins wandering across the slopes.
        double veinAngle = r.nextDouble() * Math.PI * 2.0D;
        for (int y = 22; y >= 2; y--) {
            int rr = coneRadius(y);
            int vx = (int) Math.round(Math.cos(veinAngle) * (rr - 1));
            int vz = (int) Math.round(Math.sin(veinAngle) * (rr - 1));
            setReplace(l, off(b, vx, y, vz), Blocks.MAGMA_BLOCK);
            veinAngle += (r.nextDouble() - 0.5D) * 0.55D;
        }
        // Rivers of lava radiating from the root across the scorched plain.
        // Reach is capped so the height queries can never leave the region.
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
        for (int i = 0; i < 10; i++) {
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
        fill(l, b, -2, 1, 18, 2, 4, 22, Blocks.AIR);           // tunnel mouth
        fill(l, b, -2, 1, 10, 2, 4, 18, Blocks.AIR);           // corridor through shell
        col(l, b, -3, 14, 1, 4, Blocks.IRON_BARS);
        col(l, b, 3, 14, 1, 4, Blocks.IRON_BARS);
        fill(l, b, -4, 1, 2, 4, 6, 10, Blocks.AIR);            // vault chamber
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
        return Math.max(3, (int) Math.round(19.0D * Math.pow(1.0D - y / 27.0D, 0.9D)));
    }


    // =====================================================================
    // CRYSTAL BARRENS - the Geode of the Sunken Heart
    // =====================================================================

    private static void sunkenGeode(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 18);
        // Hemispherical geode shell: dark crystal outside, shard lining within.
        for (int y = 0; y <= 15; y++) {
            int rr = (int) Math.floor(Math.sqrt(Math.max(0, 16.0D * 16.0D - y * y)));
            for (int dx = -rr; dx <= rr; dx++)
                for (int dz = -rr; dz <= rr; dz++) {
                    double d = Math.sqrt(dx * dx + (double) dz * dz);
                    if (d > rr || d <= rr - 1.5D) continue;
                    setReplace(l, off(b, dx, y, dz),
                            r.nextInt(4) == 0 ? ModBlocks.DARK_CRYSTAL_BLOCK : ModBlocks.CRYSTAL_SHARD_BLOCK);
                }
        }
        // Hollow interior and a south entry arch.
        fill(l, b, -14, 1, -14, 14, 15, 14, Blocks.AIR);
        fill(l, b, -2, 1, 12, 2, 4, 16, Blocks.AIR);
        // Apex light shaft.
        fill(l, b, -1, 13, -1, 1, 15, 1, Blocks.AIR);
        // Sunken pit descending to the heart floor, rimmed in pale crystal.
        fill(l, b, -5, -4, -5, 5, -1, 5, Blocks.AIR);
        fill(l, b, -5, -5, -5, 5, -5, 5, ModBlocks.DARK_CRYSTAL_BLOCK);
        ring(l, b, 0, 0, 0, 6, ModBlocks.PALE_CRYSTAL_BLOCK);
        for (int k = 0; k < 5; k++) {
            fill(l, b, 5 + k, -k - 1, -2, 5 + k, 0 - k / 2, 2, Blocks.AIR);
            fill(l, b, 5 + k, -k - 2, -2, 5 + k, -k - 2, 2, ModBlocks.CRYSTAL_SHARD_BLOCK);
        }
        // The Heart: a luminous monolith of pale and dark crystal.
        for (int y = -4; y <= 5; y++) {
            Block layer = (y % 2 == 0) ? ModBlocks.PALE_CRYSTAL_BLOCK : ModBlocks.DARK_CRYSTAL_BLOCK;
            int rad = y > 2 ? 1 : 2;
            fill(l, b, -rad, y, -rad, rad, y, rad, layer);
        }
        setReplace(l, off(b, 0, 6, 0), ModBlocks.CRYSTAL_CLUSTER);
        // Cluster ring around the pit floor, each rooted in warm lumen light.
        for (int i = 0; i < 10; i++) {
            double ang = i * Math.PI / 5.0D;
            int dx = (int) Math.round(Math.cos(ang) * 4);
            int dz = (int) Math.round(Math.sin(ang) * 4);
            setReplace(l, off(b, dx, -4, dz), ModBlocks.CRYSTAL_CLUSTER);
            setReplace(l, off(b, dx, -5, dz), ModBlocks.LUMEN_STONE);
        }
        landmarkMechanism(l, off(b, 4, -4, 4), EndRuinVariant.SUNKEN);
        // A crystal burrower brood-guarding the Heart pit.
        placeSpawner(l, off(b, -4, -4, -4), ModEntities.CRYSTAL_BURROWER, r);
        placeWarden(l, off(b, -4, -4, 2));
        // Gallery pillars around the dome interior: alternating tall and low
        // crystal spires, lit from below.
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4.0D + 0.39D;
            int px = (int) Math.round(Math.cos(ang) * 10);
            int pz = (int) Math.round(Math.sin(ang) * 10);
            int top = i % 2 == 0 ? 10 : 6;
            col(l, b, px, pz, 1, top, ModBlocks.PALE_CRYSTAL_BLOCK);
            setReplace(l, off(b, px, top + 1, pz), ModBlocks.CRYSTAL_CLUSTER);
            setReplace(l, off(b, px, 0, pz), ModBlocks.LUMEN_STONE);
        }
        // Two grand spires flanking the Heart itself.
        col(l, b, -3, 0, -4, 8, ModBlocks.PALE_CRYSTAL_BLOCK);
        setReplace(l, off(b, -3, 9, 0), ModBlocks.CRYSTAL_CLUSTER);
        col(l, b, 3, 0, -4, 8, ModBlocks.PALE_CRYSTAL_BLOCK);
        setReplace(l, off(b, 3, 9, 0), ModBlocks.CRYSTAL_CLUSTER);
        // Soft growth catching the apex light.
        for (int i = 0; i < 12; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0D;
            int dx = (int) Math.round(Math.cos(ang) * (8 + r.nextInt(4)));
            int dz = (int) Math.round(Math.sin(ang) * (8 + r.nextInt(4)));
            BlockPos p = off(b, dx, 1, dz);
            if (!l.getBlockState(p).isAir()) continue;
            if (!l.getBlockState(p.below()).isSolidRender(l, p.below())) continue;
            setReplace(l, p, r.nextBoolean() ? ModBlocks.LUMEN_MOSS : ModBlocks.CRYSTAL_CLUSTER);
        }
        // Loot alcoves sunk into the shell.
        fill(l, b, -15, 1, -3, -13, 3, 3, Blocks.AIR);
        lootChest(l, off(b, -14, 1, 0), r, "chests/crystal_heart");
        lootBarrel(l, off(b, -14, 1, -2), r, "chests/crystal_heart");
        lootChest(l, off(b, 13, 1, 6), r, "chests/end_spire_treasure");
        inscribe(l, off(b, 0, 1, 15), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // VOID SKIRTS - the Void Spire
    // =====================================================================

    private static void voidSpire(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 19);

        // Plaza terrace with beacon posts marking the cardinal approaches.
        disc(l, b, 0, 0, 0, 17, ModBlocks.VOID_SLATE);
        for (int[] post : new int[][]{{0, -16}, {0, 16}, {-16, 0}, {16, 0}}) {
            col(l, b, post[0], post[1], 1, 3, ModBlocks.VOID_BRICK);
            setReplace(l, off(b, post[0], 4, post[1]), ModBlocks.VOID_LAMP);
        }
        for (int i = 0; i < 24; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0D;
            int dx = (int) Math.round(Math.cos(ang) * (8 + r.nextInt(7)));
            int dz = (int) Math.round(Math.sin(ang) * (8 + r.nextInt(7)));
            setReplace(l, off(b, dx, 0, dz), ModBlocks.UMBRAL_GRASS.defaultBlockState().getBlock());
        }
        // The spire itself: tapering voidstone shaft, forty blocks tall.
        for (int y = 1; y <= 36; y++) {
            int rad = Math.max(2, (int) Math.round(9.0D * Math.pow(1.0D - y / 40.0D, 1.05D)));
            for (int dx = -rad; dx <= rad; dx++)
                for (int dz = -rad; dz <= rad; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > rad * rad) continue;
                    if (y > 2 && d2 <= (rad - 1) * (rad - 1)) {
                        setReplace(l, off(b, dx, y, dz), Blocks.AIR); // climbable core
                        continue;
                    }
                    setReplace(l, off(b, dx, y, dz), y % 6 == 0 ? ModBlocks.VOID_BRICK : ModBlocks.VOIDSTONE);
                }
        }
        // Spiral ledges up the hollow core.
        for (int y = 3; y <= 33; y++) {
            double ang = y * 0.6D;
            int lx = (int) Math.round(Math.cos(ang) * 2);
            int lz = (int) Math.round(Math.sin(ang) * 2);
            setReplace(l, off(b, lx, y, lz), ModBlocks.VOID_SLATE);
        }
        // Four buttress fins that rotate as they rise.
        for (int y = 1; y <= 30; y++) {
            double ang = Math.floor(y / 8.0D) * Math.PI / 4.0D;
            int fx = (int) Math.round(Math.cos(ang) * (spireRadius(y) + 2));
            int fz = (int) Math.round(Math.sin(ang) * (spireRadius(y) + 2));
            fill(l, b, Math.min(0, fx), y, Math.min(0, fz), Math.max(0, fx), y, Math.max(0, fz), ModBlocks.VOID_BRICK);
        }
        // Lamp seams on the four faces.
        for (int y = 6; y <= 30; y += 8) {
            setReplace(l, off(b, spireRadius(y), y, 0), ModBlocks.VOID_LAMP);
            setReplace(l, off(b, -spireRadius(y), y, 0), ModBlocks.VOID_LAMP);
            setReplace(l, off(b, 0, y, spireRadius(y)), ModBlocks.VOID_LAMP);
            setReplace(l, off(b, 0, y, -spireRadius(y)), ModBlocks.VOID_LAMP);
        }
        // Summit chamber housing the Spire core.
        fill(l, b, -4, 37, -4, 4, 41, 4, ModBlocks.VOID_BRICK);
        fill(l, b, -3, 37, -3, 3, 41, 3, Blocks.AIR);
        for (int[] c : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
            col(l, b, c[0], c[1], 42, 43, ModBlocks.VOID_BRICK);
            setReplace(l, off(b, c[0], 44, c[1]), ModBlocks.VOID_LAMP);
        }
        setReplace(l, off(b, 0, 42, 0), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 43, 0), EndRuinVariant.SPIRE);
        // Jagged broken crown with shards sheared clean off, hovering where
        // they broke - the Spire's ancient wound.
        for (int i = 0; i < 6; i++) {
            double ang = i * Math.PI / 3.0D;
            int dx = (int) Math.round(Math.cos(ang) * 3);
            int dz = (int) Math.round(Math.sin(ang) * 3);
            col(l, b, dx, dz, 42, 43 + r.nextInt(3), ModBlocks.VOID_BRICK);
            int sx = (int) Math.round(Math.cos(ang) * (5 + r.nextInt(2)));
            int sz = (int) Math.round(Math.sin(ang) * (5 + r.nextInt(2)));
            setReplace(l, off(b, sx, 44 + r.nextInt(3), sz), ModBlocks.VOID_BRICK);
        }
        // Mid-shaft balcony: a ring walk with railing posts partway up.
        int balY = 20;
        ring(l, b, 0, balY, 0, spireRadius(balY) + 1, ModBlocks.VOID_BRICK);
        for (int i = 0; i < 12; i++) {
            double ang = i * Math.PI / 6.0D;
            int rx = (int) Math.round(Math.cos(ang) * (spireRadius(balY) + 1));
            int rz = (int) Math.round(Math.sin(ang) * (spireRadius(balY) + 1));
            col(l, b, rx, rz, balY + 1, balY + 1, ModBlocks.VOID_BRICK);
            if (i % 3 == 0) setReplace(l, off(b, rx, balY + 2, rz), ModBlocks.VOID_LAMP);
        }
        // Entry arch and markers; a void stalker lairs on the balcony ring,
        // ambushing climbers halfway up the shaft.
        fill(l, b, -2, 1, 8, 2, 4, 8, Blocks.AIR);
        col(l, b, -3, 8, 1, 5, ModBlocks.VOID_BRICK);
        col(l, b, 3, 8, 1, 5, ModBlocks.VOID_BRICK);
        placeSpawner(l, off(b, spireRadius(balY) - 1, balY + 1, 0), ModEntities.VOID_STALKER, r);
        placeWarden(l, off(b, 0, 38, 1));
        lootChest(l, off(b, 3, 38, 3), r, "chests/end_spire");
        lootBarrel(l, off(b, -3, 38, 3), r, "chests/end_spire");
        lootChest(l, off(b, 0, 38, -3), r, "chests/end_spire_treasure");
        inscribe(l, off(b, -4, 1, 9), InscribedSlateBlock.SYMBOL_SPIRE);
        inscribe(l, off(b, 4, 1, 9), InscribedSlateBlock.SYMBOL_RING);
    }

    private static int spireRadius(int y) {
        return Math.max(2, (int) Math.round(9.0D * Math.pow(1.0D - y / 40.0D, 1.05D)));
    }

    // =====================================================================
    // VOID CROWN - the Crown Observatory
    // =====================================================================

    private static void crownObservatory(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 19);
        // Three stepped tiers of the ziggurat.
        fill(l, b, -15, 0, -15, 15, 4, 15, ModBlocks.UMBRAL_STONE);
        fill(l, b, -15, 5, -15, 15, 5, 15, ModBlocks.VOID_SLATE);
        fill(l, b, -10, 6, -10, 10, 10, 10, ModBlocks.VOID_SLATE);
        fill(l, b, -10, 11, -10, 10, 11, 10, ModBlocks.UMBRAL_STONE);
        fill(l, b, -6, 12, -6, 6, 14, 6, ModBlocks.UMBRAL_STONE);
        fill(l, b, -6, 15, -6, 6, 15, 6, ModBlocks.VOID_SLATE);
        // Grand stair climbing the southern faces, inlaid with seal blocks.
        for (int s = 0; s < 5; s++) {
            fill(l, b, -2, 1 + s, 15 - s, 2, 1 + s, 16 - s, ModBlocks.VOID_SLATE);
            fill(l, b, -2, 6 + s, 10 - s, 2, 6 + s, 11 - s, ModBlocks.VOID_SLATE);
            fill(l, b, -2, 11 + s, 6 - s, 2, 11 + s, 7 - s, ModBlocks.VOID_SLATE);
            if (s % 2 == 0) setReplace(l, off(b, 0, 1 + s, 15 - s), ModBlocks.CROWN_SEAL_BLOCK);
            if (s % 2 == 0) setReplace(l, off(b, 0, 6 + s, 10 - s), ModBlocks.CROWN_SEAL_BLOCK);
        }
        // Corner needles crowned with crown-needle blocks.
        for (int[] c : new int[][]{{-14, -14}, {14, -14}, {-14, 14}, {14, 14}}) {
            col(l, b, c[0], c[1], 6, 16, ModBlocks.VOID_BRICK);
            setReplace(l, off(b, c[0], 17, c[1]), ModBlocks.CROWN_NEEDLE_BLOCK);
        }
        // Watch tower with a balcony ring.
        for (int y = 16; y <= 27; y++) {
            int rr = y > 24 ? 3 : 4;
            ring(l, b, 0, y, 0, rr, y % 5 == 0 ? ModBlocks.VOID_BRICK : ModBlocks.VOIDSTONE);
            if (y < 24) disc(l, b, 0, y, 0, rr - 1, Blocks.AIR);
        }
        disc(l, b, 0, 20, 0, 6, ModBlocks.VOID_SLATE);
        for (int i = 0; i < 12; i++) {
            double ang = i * Math.PI / 6.0D;
            int rx = (int) Math.round(Math.cos(ang) * 6);
            int rz = (int) Math.round(Math.sin(ang) * 6);
            col(l, b, rx, rz, 21, 22, ModBlocks.VOID_BRICK);
        }
        // The sealed observation orb: glass shell over a seal-lattice core.
        for (int dy = -3; dy <= 3; dy++) {
            int hr = (int) Math.floor(Math.sqrt(Math.max(0, 12.25D - dy * dy)));
            for (int dx = -hr; dx <= hr; dx++)
                for (int dz = -hr; dz <= hr; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > hr) continue;
                    if (d > hr - 1.2D) setReplace(l, off(b, dx, 28 + dy, dz), ModBlocks.VOID_GLASS);
                    else if ((dx + dy + dz) % 2 == 0) setReplace(l, off(b, dx, 28 + dy, dz), ModBlocks.CROWN_SEAL_BLOCK);
                }
        }
        col(l, b, 0, 0, 25, 27, ModBlocks.UMBRAL_STONE);
        setReplace(l, off(b, 0, 28, 0), Blocks.END_STONE_BRICKS);
        landmarkMechanism(l, off(b, 0, 29, 0), EndRuinVariant.CROWN_OBSERVATORY);
        setReplace(l, off(b, 0, 32, 0), ModBlocks.CROWN_SEAL_BLOCK);
        // Garden tufts along the tier rims.
        for (int i = 0; i < 20; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0D;
            int dx = (int) Math.round(Math.cos(ang) * (11 + r.nextInt(4)));
            int dz = (int) Math.round(Math.sin(ang) * (11 + r.nextInt(4)));
            if (Math.abs(dx) > 15 || Math.abs(dz) > 15) continue;
            setReplace(l, off(b, dx, 6, dz), ModBlocks.UMBRAL_GRASS);
        }
        lootChest(l, off(b, 12, 1, 12), r, "chests/crown_observatory");
        lootBarrel(l, off(b, -12, 1, 12), r, "chests/crownstep_procession");
        lootChest(l, off(b, 3, 13, -3), r, "chests/crownstep_procession");
        // A crown sentinel stands eternal watch on the second-tier walk.
        placeSpawner(l, off(b, 6, 12, 0), ModEntities.CROWN_SENTINEL, r);
        placeWarden(l, off(b, -8, 12, 8));
        inscribe(l, off(b, -3, 6, 14), InscribedSlateBlock.SYMBOL_SPIRE);
        inscribe(l, off(b, 3, 6, 14), InscribedSlateBlock.SYMBOL_EYE);
    }

    // =====================================================================
    // UMBRAL REACH - the Null Archive
    // =====================================================================

    private static void nullArchive(WorldGenLevel l, BlockPos b, RandomSource r) {
        b = flattenGround(l, b, 17);
        // Approach court of woven void.
        disc(l, b, 0, 0, 0, 15, ModBlocks.VOID_SOIL);
        fill(l, b, -1, 0, 6, 1, 0, 14, ModBlocks.VOID_WEAVE);
        // Blind facade: a windowless monolithic hall.
        fill(l, b, -13, 1, -13, 13, 16, 13, ModBlocks.VOIDSTONE);
        fill(l, b, -12, 1, -12, 12, 15, 12, Blocks.AIR);
        for (int i = -12; i <= 12; i += 4) {
            col(l, b, i, -13, 1, 16, ModBlocks.NULL_ARCHIVE_FRAME);
            col(l, b, i, 13, 1, 16, ModBlocks.NULL_ARCHIVE_FRAME);
            col(l, b, -13, i, 1, 16, ModBlocks.NULL_ARCHIVE_FRAME);
            col(l, b, 13, i, 1, 16, ModBlocks.NULL_ARCHIVE_FRAME);
        }
        // Corbelled roof closing to a threshold-core table.
        fill(l, b, -13, 17, -13, 13, 17, 13, ModBlocks.VOIDSTONE);
        fill(l, b, -11, 18, -11, 11, 18, 11, ModBlocks.VOIDSTONE);
        fill(l, b, -9, 19, -9, 9, 19, 9, ModBlocks.VOIDSTONE);
        for (int x = -8; x <= 8; x += 4)
            for (int z = -8; z <= 8; z += 4)
                setReplace(l, off(b, x, 20, z), ModBlocks.THRESHOLD_CORE_BLOCK);
        // Sole entrance: a recessed threshold portal facing south.
        fill(l, b, -1, 1, 13, 1, 5, 13, Blocks.AIR);
        col(l, b, -2, 13, 1, 6, ModBlocks.THRESHOLD_CORE_BLOCK);
        col(l, b, 2, 13, 1, 6, ModBlocks.THRESHOLD_CORE_BLOCK);
        fill(l, b, -2, 6, 13, 2, 6, 13, ModBlocks.THRESHOLD_CORE_BLOCK);
        // Silent stacks: rows of frame pillars in the dark.
        for (int x = -10; x <= 10; x += 4)
            for (int z = -10; z <= 10; z += 4) {
                if (Math.abs(x) <= 2 && Math.abs(z) <= 2) continue;
                col(l, b, x, z, 1, 2, ModBlocks.NULL_ARCHIVE_FRAME);
                col(l, b, x, z, 3, 4, ModBlocks.NULL_ARCHIVE_FRAME);
            }
        fill(l, b, -1, 1, -10, 1, 1, 10, ModBlocks.VOID_WEAVE);
        // The well shaft descending to the sealed core rotunda.
        fill(l, b, -2, 1, -2, 2, -5, 2, Blocks.AIR);
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
        // Alcove caches in the outer walls; a nullwalker stalks the rotunda.
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
