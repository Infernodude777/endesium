package com.infernodude777.endesium.world.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Optional;

/**
 * The Endesium stronghold: a full replacement for the vanilla stronghold -
 * the structure players reach the End through - rebuilt as a clean, grand,
 * End-touched stronghold. Dozens of pieces span roughly three times the
 * vanilla footprint: a monumental switchback descent hall, a resonance
 * atrium, a portal room that still hosts the twelve frames, a library,
 * treasury, barracks, sanctum, guard posts with phantom roosts, a dining
 * hall, scriptorium, arboretum, and a crypt.
 *
 * <p>Hard rules:
 * <ul>
 *   <li>The End portal frames ALWAYS generate with no eyes; progression is intact.</li>
 *   <li>Every chest uses a vanilla loot table. No Endesium gear - no Void Armor,
 *       no tools, no relics - can ever drop here. The stronghold previews the
 *       End but never spoils it.</li>
 *   <li>Mobs are vanilla-flavored guards: vindicator squads, phantom roosts,
 *       endermen, silverfish, skeletons, a witch, bats.</li>
 *   <li>Everything is built in absolute world coordinates as one {@link Piece}
 *       class whose NBT round-trips cleanly, matching the mod's other
 *       code-built structures.</li>
 * </ul>
 */
public final class EndStrongholdStructure extends Structure {
	public static final MapCodec<EndStrongholdStructure> CODEC = simpleCodec(EndStrongholdStructure::new);
	private static final class EndesiumLootTables {
		private static final ResourceKey<LootTable> STRONGHOLD = ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("endesium", "chests/stronghold"));
	}

	public EndStrongholdStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		int cx = chunkPos.getMiddleBlockX();
		int cz = chunkPos.getMiddleBlockZ();
		int minY = context.chunkGenerator().getMinY();
		int surface = context.chunkGenerator().getFirstFreeHeight(cx, cz,
				Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
		// Deliberately deep underground: 64-92 blocks below the terrain surface.
		// Keep the complete citadel above the world floor while leaving a substantial stone roof.
		int depth = Math.max(minY + 24, Math.min(surface - 58, surface - 78 - context.random().nextInt(16)));
		return Optional.of(new GenerationStub(new BlockPos(cx, depth, cz),
				builder -> buildPieces(builder, context, cx, cz, surface, depth)));
	}

	private void buildPieces(StructurePiecesBuilder builder, GenerationContext context,
			int cx, int cz, int surface, int depth) {
		RandomSource random = context.random();

		// --- Great Descent Hall: the monumental buried entrance ---
		int descent = Math.max(1, surface - depth);
		int hallSouthZ = cz - 16;
		int hallNorthZ = hallSouthZ - descent - 7;
		builder.addPiece(new Piece(bbox(cx - 7, depth - 8, hallNorthZ - 2, cx + 7, surface + 5, hallSouthZ),
				Kind.HALL, 0, descent, surface, 0));

		// --- Guaranteed-connected citadel spine ---
		// The locate position is a sealed chamber with one south-facing door.
		builder.addPiece(new Piece(bbox(cx - 9, depth - 1, cz - 9, cx + 9, depth + 9, cz + 9),
				Kind.ARRIVAL_VAULT, 0, 0, 0, 0));
		// The hub begins directly beyond that door; the shared overlap is intentional.
		builder.addPiece(new Piece(bbox(cx - 16, depth - 1, cz + 8, cx + 16, depth + 20, cz + 40),
				Kind.HUB, 0, 0, 0, 0));
		builder.addPiece(new Piece(bbox(cx - 6, depth - 19, cz + 12, cx + 6, depth + 21, cz + 24),
				Kind.STARWELL, 0, 0, 0, 0));
		builder.addPiece(new Piece(bbox(cx - 18, depth + 2, cz - 12, cx + 18, depth + 9, cz + 11),
				Kind.JUNCTION, 0, 1, 0, 1));
		// Door-to-hub threshold and a broad route through the hub.
		builder.addPiece(new Piece(bbox(cx - 3, depth - 1, cz + 7, cx + 3, depth + 4, cz + 14),
				Kind.CORRIDOR, 1, 7, 0, 1));

		// --- Grand district rooms ---
		builder.addPiece(new Piece(bbox(cx - 13, depth + 4, cz - 57, cx + 13, depth + 18, cz - 27),
				Kind.LIBRARY, 0, 0, 0, 0));    // archive galleries
		builder.addPiece(new Piece(bbox(cx - 14, depth + 5, cz - 28, cx + 14, depth + 17, cz - 14),
				Kind.OBSERVATORY, 0, 0, 0, 0)); // void observatory
		builder.addPiece(new Piece(bbox(cx - 16, depth - 1, cz - 15, cx - 8, depth + 4, cz - 7),
				Kind.TREASURY, 0, 0, 0, 0));   // treasury, west arm
		builder.addPiece(new Piece(bbox(cx + 8, depth - 1, cz - 15, cx + 16, depth + 4, cz - 7),
				Kind.GUARD_POST, 0, 0, 0, 0)); // guard post + phantom roost, east arm
		builder.addPiece(new Piece(bbox(cx + 17, depth - 1, cz + 8, cx + 31, depth + 8, cz + 22),
				Kind.SANCTUM, 0, 0, 0, 0));    // end-touched sanctum
		builder.addPiece(new Piece(bbox(cx + 34, depth - 1, cz - 7, cx + 48, depth + 8, cz + 7),
				Kind.RESONANCE_ENGINE, 0, 0, 0, 0)); // resonance engineering wing
		builder.addPiece(new Piece(bbox(cx + 15, depth - 1, cz - 19, cx + 29, depth + 8, cz - 5),
				Kind.BARRACKS, 0, 0, 0, 0));   // barracks
		builder.addPiece(new Piece(bbox(cx + 32, depth - 1, cz - 19, cx + 48, depth + 8, cz - 3),
				Kind.BASTION, 0, 0, 0, 0));    // warden bastion
		builder.addPiece(new Piece(bbox(cx - 37, depth - 1, cz - 4, cx - 29, depth + 4, cz + 4),
				Kind.SCRIPTORIUM, 0, 0, 0, 0)); // scriptorium, west arm
		builder.addPiece(new Piece(bbox(cx - 28, depth - 1, cz - 18, cx - 14, depth + 8, cz - 4),
				Kind.ARBORETUM, 0, 0, 0, 0));   // original conservatory
		builder.addPiece(new Piece(bbox(cx - 48, depth - 1, cz - 9, cx - 32, depth + 8, cz + 9),
				Kind.CONSERVATORY, 0, 0, 0, 0)); // end-touched conservatory
		builder.addPiece(new Piece(bbox(cx - 14, depth - 1, cz + 27, cx + 14, depth + 15, cz + 53),
				Kind.PORTAL, 0, 0, 0, 0));     // portal cathedral
		builder.addPiece(new Piece(bbox(cx - 25, depth - 1, cz + 27, cx - 13, depth + 8, cz + 39),
				Kind.RELIQUARY, 0, 0, 0, 0)); // reliquary

		// --- Crypt below the hub, reached by a ladder shaft ---
		builder.addPiece(new Piece(bbox(cx - 7, depth - 18, cz - 7, cx + 7, depth - 8, cz + 7),
				Kind.CRYPT, 0, 0, 0, 0));
		builder.addPiece(new Piece(bbox(cx + 11, depth - 18, cz - 7, cx + 25, depth - 8, cz + 7),
				Kind.CATACOMBS, 0, 0, 0, 0));

		// --- Corridors (added after rooms so their air punches the doorways) ---
		// Great Descent Hall -> hub (also serves the north crossroads)
		builder.addPiece(new Piece(bbox(cx - 3, depth - 1, cz - 17, cx + 3, depth + 4, cz - 6),
				Kind.CORRIDOR, 1, 11, 0, 1));
		// North crossroads -> observatory -> archive (continuous ascending route)
		builder.addPiece(new Piece(bbox(cx - 5, depth + 8, cz - 16, cx + 5, depth + 12, cz - 8),
				Kind.CORRIDOR, 1, 8, 0, 4));
		builder.addPiece(new Piece(bbox(cx - 5, depth + 8, cz - 28, cx + 5, depth + 12, cz - 14),
				Kind.CORRIDOR, 1, 14, 0, 4));
		// North crossroads -> treasury
		builder.addPiece(new Piece(bbox(cx - 9, depth - 1, cz - 12, cx - 3, depth + 4, cz - 8),
				Kind.CORRIDOR, 2, 6, 0, 1));
		// North crossroads -> guard post
		builder.addPiece(new Piece(bbox(cx + 3, depth - 1, cz - 12, cx + 9, depth + 4, cz - 8),
				Kind.CORRIDOR, 2, 6, 0, 0));
		// Hub -> east crossroads -> engineering wing
		builder.addPiece(new Piece(bbox(cx + 6, depth - 1, cz - 3, cx + 18, depth + 4, cz + 3),
				Kind.CORRIDOR, 2, 12, 0, 2));
		// East crossroads -> sanctum
		builder.addPiece(new Piece(bbox(cx + 17, depth - 1, cz + 3, cx + 23, depth + 4, cz + 9),
				Kind.CORRIDOR, 1, 6, 0, 0));
		// East crossroads -> barracks
		builder.addPiece(new Piece(bbox(cx + 17, depth - 1, cz - 7, cx + 23, depth + 4, cz - 3),
				Kind.CORRIDOR, 1, 4, 0, 0));
		// Hub -> west crossroads
		builder.addPiece(new Piece(bbox(cx - 17, depth - 1, cz - 3, cx - 6, depth + 4, cz + 3),
				Kind.CORRIDOR, 2, 11, 0, 3));
		// West crossroads -> scriptorium
		builder.addPiece(new Piece(bbox(cx - 29, depth - 1, cz - 3, cx - 23, depth + 4, cz + 3),
				Kind.CORRIDOR, 2, 6, 0, 1));
		// West crossroads -> arboretum
		builder.addPiece(new Piece(bbox(cx - 23, depth - 1, cz - 8, cx - 17, depth + 4, cz - 3),
				Kind.CORRIDOR, 1, 5, 0, 0));
		// Hub -> portal cathedral: a continuous ceremonial nave with explicit thresholds.
		builder.addPiece(new Piece(bbox(cx - 5, depth + 15, cz + 38, cx + 5, depth + 19, cz + 57),
				Kind.CORRIDOR, 1, 19, 0, 4));
		builder.addPiece(new Piece(bbox(cx - 18, depth + 14, cz + 49, cx + 18, depth + 20, cz + 64),
				Kind.GALLERY, 0, 0, 0, 0));
		// Side rooms have explicit thresholds from the main spine.
		builder.addPiece(new Piece(bbox(cx - 16, depth + 2, cz + 4, cx - 9, depth + 7, cz + 8),
				Kind.CORRIDOR, 2, 7, 0, 1));
		builder.addPiece(new Piece(bbox(cx + 16, depth + 2, cz + 4, cx + 23, depth + 7, cz + 8),
				Kind.CORRIDOR, 2, 7, 0, 1));
		// Major elevated galleries and engineering approaches.
		builder.addPiece(new Piece(bbox(cx + 11, depth + 7, cz - 4, cx + 34, depth + 11, cz + 4),
				Kind.BRIDGE, 0, 0, 0, 0));
		builder.addPiece(new Piece(bbox(cx - 11, depth + 7, cz - 4, cx - 31, depth + 11, cz + 4),
				Kind.GALLERY, 0, 0, 0, 0));
		builder.addPiece(new Piece(bbox(cx + 25, depth - 1, cz - 3, cx + 35, depth + 4, cz + 3),
				Kind.CORRIDOR, 2, 10, 0, 4));
		builder.addPiece(new Piece(bbox(cx - 31, depth - 1, cz - 3, cx - 48, depth + 4, cz + 3),
				Kind.CORRIDOR, 2, 17, 0, 3));
	}

	private static BoundingBox bbox(int x0, int y0, int z0, int x1, int y1, int z1) {
		return new BoundingBox(x0, y0, z0, x1, y1, z1);
	}

	@Override
	public StructureType<?> type() {
		return EndesiumStructureTypes.END_STRONGHOLD;
	}

	public static final class Type implements StructureType<EndStrongholdStructure> {
		@Override
		public MapCodec<EndStrongholdStructure> codec() {
			return CODEC;
		}
	}

	// ------------------------------------------------------------------
	//  Piece
	// ------------------------------------------------------------------

	enum Kind {
		HALL, ARRIVAL_VAULT, HUB, JUNCTION, LIBRARY, TREASURY, GUARD_POST, SANCTUM, BARRACKS,
		SCRIPTORIUM, ARBORETUM, PORTAL, CRYPT, CORRIDOR, STARWELL, OBSERVATORY,
		RESONANCE_ENGINE, BASTION, CONSERVATORY, RELIQUARY, CATACOMBS, BRIDGE,
		GALLERY
	}

	public static final class Piece extends net.minecraft.world.level.levelgen.structure.StructurePiece {
		private final Kind kind;
		/** Corridor direction hint (mirrors used for decor symmetry); unused elsewhere. */
		private final int dir;
		/** Hall stair count for HALL pieces, otherwise 0. */
		private final int aux;
		/** Hall surface (walk Y of the entrance) for HALL pieces, otherwise 0. */
		private final int aux2;
		/** Corridor decor variant. */
		private final int variant;

		private Piece(BoundingBox box, Kind kind, int dir, int aux, int aux2, int variant) {
			super(EndesiumStructureTypes.END_STRONGHOLD_PIECE, 0, box);
			this.kind = kind;
			this.dir = dir;
			this.aux = aux;
			this.aux2 = aux2;
			this.variant = variant;
		}

		/** Load path: the base constructor parses "BB"/"GD", we read the rest. */
		private Piece(CompoundTag tag) {
			super(EndesiumStructureTypes.END_STRONGHOLD_PIECE, tag);
			int kindOrd = tag.getInt("Kind");
			this.kind = kindOrd >= 0 && kindOrd < Kind.values().length ? Kind.values()[kindOrd] : Kind.HUB;
			this.dir = tag.getInt("Dir");
			this.aux = tag.getInt("Aux");
			this.aux2 = tag.getInt("Aux2");
			this.variant = tag.getInt("Variant");
		}

		public static Piece load(StructurePieceSerializationContext context, CompoundTag tag) {
			return new Piece(tag);
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
			tag.putInt("Kind", kind.ordinal());
			tag.putInt("Dir", dir);
			tag.putInt("Aux", aux);
			tag.putInt("Aux2", aux2);
			tag.putInt("Variant", variant);
		}

		@Override
		public void postProcess(WorldGenLevel level, StructureManager structureManager,
				ChunkGenerator generator, RandomSource random, BoundingBox box,
				ChunkPos chunkPos, BlockPos pos) {
			switch (kind) {
				case HALL -> buildHall(level, box, random);
				case ARRIVAL_VAULT -> buildArrivalVault(level, box, random);
				case HUB -> buildHub(level, box, random);
				case JUNCTION -> buildJunction(level, box, random);
				case LIBRARY -> buildLibrary(level, box, random);
				case TREASURY -> buildTreasury(level, box, random);
				case GUARD_POST -> buildGuardPost(level, box, random);
				case SANCTUM -> buildSanctum(level, box, random);
				case BARRACKS -> buildBarracks(level, box, random);
				case SCRIPTORIUM -> buildScriptorium(level, box, random);
				case ARBORETUM -> buildArboretum(level, box, random);
				case PORTAL -> buildPortalRoom(level, box, random);
				case CRYPT -> buildCrypt(level, box, random);
				case CORRIDOR -> buildCorridor(level, box, random);
			case STARWELL -> buildStarwell(level, box, random);
			case OBSERVATORY -> buildObservatory(level, box, random);
			case RESONANCE_ENGINE -> buildResonanceEngine(level, box, random);
			case BASTION -> buildBastion(level, box, random);
			case CONSERVATORY -> buildConservatory(level, box, random);
			case RELIQUARY -> buildReliquary(level, box, random);
			case CATACOMBS -> buildCatacombs(level, box, random);
			case BRIDGE -> buildBridge(level, box, random);
			case GALLERY -> buildGallery(level, box, random);
			}
		}

		// ---- low-level helpers (all world coordinates, clipped to chunk) ----

		private void place(WorldGenLevel level, BoundingBox clip, int x, int y, int z, BlockState state) {
			if (!clip.isInside(new BlockPos(x, y, z))) return;
			level.setBlock(new BlockPos(x, y, z), state, 2);
		}

		private void door(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction facing) {
			BlockState lower = Blocks.DARK_OAK_DOOR.defaultBlockState()
					.setValue(net.minecraft.world.level.block.DoorBlock.FACING, facing)
					.setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
			BlockState upper = lower.setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
			place(level, clip, x, y, z, lower);
			place(level, clip, x, y + 1, z, upper);
		}

		private void fill(WorldGenLevel level, BoundingBox clip, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
			if (clip.minX() > x1 || clip.maxX() < x0 || clip.minY() > y1 || clip.maxY() < y0 || clip.minZ() > z1 || clip.maxZ() < z0) {
				return;
			}
			for (BlockPos p : BlockPos.betweenClosed(x0, y0, z0, x1, y1, z1)) {
				if (!clip.isInside(p)) continue;
				level.setBlock(p, state, 2);
			}
		}

		private void air(WorldGenLevel level, BoundingBox clip, int x0, int y0, int z0, int x1, int y1, int z1) {
			fill(level, clip, x0, y0, z0, x1, y1, z1, Blocks.CAVE_AIR.defaultBlockState());
		}

		private BlockState brick(RandomSource random) {
			int roll = random.nextInt(24);
			if (roll == 0) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
			if (roll == 1) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
			return Blocks.STONE_BRICKS.defaultBlockState();
		}

		private BlockState darkBrick(RandomSource random) {
			int roll = random.nextInt(18);
			if (roll == 0) return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
			if (roll == 1) return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
			return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
		}

		private void arch(WorldGenLevel level, BoundingBox clip, int x0, int y0, int z0,
				int x1, int y1, int z1, Direction direction, BlockState state) {
			if (direction.getAxis() == Direction.Axis.Z) {
				for (int x = x0; x <= x1; x++) {
					place(level, clip, x, y0, z0, state);
					place(level, clip, x, y0, z1, state);
				}
				for (int y = y0; y <= y1; y++) {
					place(level, clip, x0, y, z0, state);
					place(level, clip, x1, y, z0, state);
					place(level, clip, x0, y, z1, state);
					place(level, clip, x1, y, z1, state);
				}
			} else {
				for (int z = z0; z <= z1; z++) {
					place(level, clip, x0, y0, z, state);
					place(level, clip, x1, y0, z, state);
				}
				for (int y = y0; y <= y1; y++) {
					place(level, clip, x0, y, z0, state);
					place(level, clip, x0, y, z1, state);
					place(level, clip, x1, y, z0, state);
					place(level, clip, x1, y, z1, state);
				}
			}
		}

		private void pillar(WorldGenLevel level, BoundingBox clip, int x, int y0, int z, int y1, BlockState state) {
			fill(level, clip, x, y0, z, x, y1, z, state);
			// Single-block cap slab only at the top for a clean column silhouette.
			place(level, clip, x, y1, z, state);
		}

		private void inlay(WorldGenLevel level, BoundingBox clip, int x, int y, int z, BlockState accent) {
			if (!clip.isInside(new BlockPos(x, y, z))) return;
			place(level, clip, x, y, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			floorDecor(level, clip, x, y + 1, z, accent);
		}

		/** Places a decoration only when it has a solid floor beneath it. */
		private void floorDecor(WorldGenLevel level, BoundingBox clip, int x, int y, int z, BlockState state) {
			if (!clip.isInside(new BlockPos(x, y, z))) return;
			if (!level.getBlockState(new BlockPos(x, y - 1, z)).isAir()) place(level, clip, x, y, z, state);
		}

		/** Places a wall decoration only when the requested wall anchor exists. */
		private void wallDecor(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction wall, BlockState state) {
			BlockPos anchor = new BlockPos(x, y, z).relative(wall);
			if (clip.isInside(anchor) && !level.getBlockState(anchor).isAir()) place(level, clip, x, y, z, state);
		}

		private void beam(WorldGenLevel level, BoundingBox clip, int x0, int y, int z0, int x1, int z1, BlockState state) {
			fill(level, clip, Math.min(x0, x1), y, Math.min(z0, z1), Math.max(x0, x1), y, Math.max(z0, z1), state);
		}

		private void supportedLight(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction wall) {
			wallDecor(level, clip, x, y, z, wall, Blocks.END_ROD.defaultBlockState());
		}

		private void supportedCluster(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction wall) {
			wallDecor(level, clip, x, y, z, wall, Blocks.AMETHYST_CLUSTER.defaultBlockState());
		}

		private void groundedPillar(WorldGenLevel level, BoundingBox clip, int x, int floorY, int z, int height, BlockState state) {
			fill(level, clip, x, floorY, z, x, floorY, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			fill(level, clip, x, floorY + 1, z, x, floorY + height, z, state);
		}

		private void radialSigil(WorldGenLevel level, BoundingBox clip, int cx, int y, int cz, int radius, BlockState accent) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					int distance = Math.abs(dx) + Math.abs(dz);
					if (distance == radius || (Math.abs(dx) == Math.abs(dz) && distance <= radius)) {
						inlay(level, clip, cx + dx, y, cz + dz, accent);
					}
				}
			}
		}

		/** The room shell: floor, walls, ceiling, hollow interior with dense built-in wall detail. */
		private void shell(WorldGenLevel level, BoundingBox clip, RandomSource random,
				int x0, int z0, int x1, int z1, int floorY, int topY) {
			fill(level, clip, x0, floorY, z0, x1, floorY, z1, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			fill(level, clip, x0, topY, z0, x1, topY, z1, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
			// Baseboard accent around the room perimeter.
			for (int x = x0; x <= x1; x++) {
				place(level, clip, x, floorY + 1, z0, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
				place(level, clip, x, floorY + 1, z1, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
			}
			for (int z = z0 + 1; z <= z1 - 1; z++) {
				place(level, clip, x0, floorY + 1, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
				place(level, clip, x1, floorY + 1, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
			}
			for (int y = floorY + 2; y <= topY - 1; y++) {
				for (int x = x0; x <= x1; x++) {
					place(level, clip, x, y, z0, brick(random));
					place(level, clip, x, y, z1, brick(random));
				}
				for (int z = z0 + 1; z <= z1 - 1; z++) {
					place(level, clip, x0, y, z, brick(random));
					place(level, clip, x1, y, z, brick(random));
				}
			}
			air(level, clip, x0 + 1, floorY + 1, z0 + 1, x1 - 1, topY - 1, z1 - 1);
			// Dense wall detail: alternating shelves and barrels with a candle row
			// above, so every wall face reads lived-in rather than bare brick.
			for (int x = x0 + 2; x <= x1 - 2; x += 2) {
				wallDecor(level, clip, x, floorY + 2, z0, Direction.NORTH, x % 4 == 0 ? Blocks.BARREL.defaultBlockState() : Blocks.BOOKSHELF.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, z1, Direction.SOUTH, x % 4 == 0 ? Blocks.BOOKSHELF.defaultBlockState() : Blocks.BARREL.defaultBlockState());
				wallDecor(level, clip, x, floorY + 3, z0, Direction.NORTH, x % 4 == 2 ? Blocks.CANDLE.defaultBlockState() : Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
				wallDecor(level, clip, x, floorY + 3, z1, Direction.SOUTH, x % 4 == 2 ? Blocks.CANDLE.defaultBlockState() : Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
			}
			for (int z = z0 + 2; z <= z1 - 2; z += 2) {
				wallDecor(level, clip, x0, floorY + 2, z, Direction.WEST, z % 4 == 0 ? Blocks.BARREL.defaultBlockState() : Blocks.BOOKSHELF.defaultBlockState());
				wallDecor(level, clip, x1, floorY + 2, z, Direction.EAST, z % 4 == 0 ? Blocks.BOOKSHELF.defaultBlockState() : Blocks.BARREL.defaultBlockState());
				wallDecor(level, clip, x0, floorY + 3, z, Direction.WEST, z % 4 == 2 ? Blocks.CANDLE.defaultBlockState() : Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
				wallDecor(level, clip, x1, floorY + 3, z, Direction.EAST, z % 4 == 2 ? Blocks.CANDLE.defaultBlockState() : Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
			}
			// Structural ribs anchored into opposing walls, with hanging lanterns.
			for (int x = x0 + 2; x <= x1 - 2; x += 4) {
				beam(level, clip, x, topY - 1, z0 + 1, x, z1 - 1, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
				if (topY - floorY >= 5) hangingLantern(level, clip, x, topY - 1, (z0 + z1) / 2);
			}
			// Wall-mounted lights along all four walls, every few blocks.
			for (int x = x0 + 2; x <= x1 - 2; x += 3) {
				supportedLight(level, clip, x, topY - 2, z0 + 1, Direction.NORTH);
				supportedLight(level, clip, x, topY - 2, z1 - 1, Direction.SOUTH);
			}
			for (int z = z0 + 2; z <= z1 - 2; z += 3) {
				supportedLight(level, clip, x0 + 1, topY - 2, z, Direction.WEST);
				supportedLight(level, clip, x1 - 1, topY - 2, z, Direction.EAST);
			}
		}

		private void carpet(WorldGenLevel level, BoundingBox clip, int x0, int z0, int x1, int z1, int y, BlockState state) {
			for (int x = x0; x <= x1; x++) {
				for (int z = z0; z <= z1; z++) floorDecor(level, clip, x, y, z, state);
			}
		}

		private void bench(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction facing) {
			BlockState stair = Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing);
			floorDecor(level, clip, x, y, z, stair);
			floorDecor(level, clip, x + (facing.getAxis() == Direction.Axis.Z ? 1 : 0), y, z + (facing.getAxis() == Direction.Axis.X ? 1 : 0), stair);
		}

		private void shelf(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction wall) {
			wallDecor(level, clip, x, y, z, wall, Blocks.BARREL.defaultBlockState());
			wallDecor(level, clip, x, y + 1, z, wall, Blocks.BARREL.defaultBlockState());
		}

		private void wallBay(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction wall, BlockState accent) {
			wallDecor(level, clip, x, y, z, wall, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
			wallDecor(level, clip, x, y + 1, z, wall, accent);
			wallDecor(level, clip, x, y + 2, z, wall, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
		}

		private void table(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction facing, BlockState top) {
			floorDecor(level, clip, x, y, z, top);
			int legX = x + (facing.getAxis() == Direction.Axis.X ? 0 : 1);
			int legZ = z + (facing.getAxis() == Direction.Axis.Z ? 0 : 1);
			floorDecor(level, clip, legX, y, legZ, Blocks.OAK_FENCE.defaultBlockState());
		}

		private void hangingLantern(WorldGenLevel level, BoundingBox clip, int x, int y, int z) {
			// The chain and lantern terminate at the generated ceiling, so this is never a loose prop.
			if (!clip.isInside(new BlockPos(x, y, z))) return;
			if (level.getBlockState(new BlockPos(x, y + 1, z)).isAir()) return;
			place(level, clip, x, y, z, Blocks.CHAIN.defaultBlockState());
			if (clip.isInside(new BlockPos(x, y - 1, z))) place(level, clip, x, y - 1, z, Blocks.LANTERN.defaultBlockState());
		}

		private void supportedFurniture(WorldGenLevel level, BoundingBox clip, int x, int y, int z, BlockState state) {
			// Furniture is only emitted on a generated floor and never in the route's two-block aisle.
			floorDecor(level, clip, x, y, z, state);
		}

		private void alcove(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction wall, BlockState accent) {
			wallBay(level, clip, x, y, z, wall, accent);
			wallDecor(level, clip, x, y + 1, z, wall, Blocks.LANTERN.defaultBlockState());
		}

		private void chandelier(WorldGenLevel level, BoundingBox clip, int x, int y, int z) {
			// Triple lantern cluster hanging from a generated ceiling.
			if (!clip.isInside(new BlockPos(x, y, z))) return;
			if (level.getBlockState(new BlockPos(x, y + 1, z)).isAir()) return;
			place(level, clip, x, y, z, Blocks.CHAIN.defaultBlockState());
			place(level, clip, x, y - 1, z, Blocks.LANTERN.defaultBlockState());
			place(level, clip, x - 1, y - 1, z, Blocks.LANTERN.defaultBlockState());
			place(level, clip, x + 1, y - 1, z, Blocks.LANTERN.defaultBlockState());
		}

		private void sconce(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction wall) {
			// End-rod bracket with a candle above it, set into the wall face.
			wallDecor(level, clip, x, y, z, wall, Blocks.END_ROD.defaultBlockState());
			wallDecor(level, clip, x, y + 1, z, wall, Blocks.CANDLE.defaultBlockState());
		}

		private void banner(WorldGenLevel level, BoundingBox clip, int x, int y, int z, Direction facing) {
			// Wall banner facing into the room, anchored to the block behind it.
			place(level, clip, x, y, z, Blocks.PURPLE_WALL_BANNER.defaultBlockState()
					.setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, facing));
		}

		private void coffin(WorldGenLevel level, BoundingBox clip, int x, int y, int z) {
			// A stone-slab tomb on a bone plinth, with a candle at the head.
			place(level, clip, x, y, z, Blocks.BONE_BLOCK.defaultBlockState());
			place(level, clip, x, y + 1, z, Blocks.STONE_BRICK_SLAB.defaultBlockState());
			place(level, clip, x, y + 1, z + 1, Blocks.STONE_BRICK_SLAB.defaultBlockState());
			place(level, clip, x, y + 1, z - 1, Blocks.CANDLE.defaultBlockState());
		}

		private void plant(WorldGenLevel level, BoundingBox clip, int x, int y, int z) {
			// A single-block garden plant chosen deterministically from position.
			int roll = Math.floorMod(x * 31 + z * 17, 6);
			BlockState flower = switch (roll) {
				case 0 -> Blocks.POPPY.defaultBlockState();
				case 1 -> Blocks.DANDELION.defaultBlockState();
				case 2 -> Blocks.AZURE_BLUET.defaultBlockState();
				case 3 -> Blocks.CORNFLOWER.defaultBlockState();
				case 4 -> Blocks.ALLIUM.defaultBlockState();
				default -> Blocks.FERN.defaultBlockState();
			};
			supportedFurniture(level, clip, x, y, z, flower);
		}

		private void armorStand(WorldGenLevel level, BoundingBox clip, int x, int y, int z, float yaw) {
			BlockPos at = new BlockPos(x, y, z);
			if (!clip.isInside(at)) return;
			net.minecraft.world.entity.decoration.ArmorStand stand =
					new net.minecraft.world.entity.decoration.ArmorStand(level.getLevel(), x + 0.5D, y, z + 0.5D);
			stand.setYRot(yaw);
			level.addFreshEntity(stand);
		}

		private void roomDressing(WorldGenLevel level, BoundingBox clip, RandomSource random,
				int x0, int z0, int x1, int z1, int floorY, int topY, int density) {
			int minX = Math.min(x0, x1) + 2, maxX = Math.max(x0, x1) - 2;
			int minZ = Math.min(z0, z1) + 2, maxZ = Math.max(z0, z1) - 2;
			if (minX > maxX || minZ > maxZ) return;
			int aisleX = (minX + maxX) / 2;
			int aisleZ = (minZ + maxZ) / 2;
			int step = density <= 4 ? 2 : 3;
			// Wall sconces down all four walls, offset from the shell's shelf rows.
			for (int x = minX; x <= maxX; x += step) {
				supportedLight(level, clip, x, topY - 2, z0 + 1, Direction.NORTH);
				supportedLight(level, clip, x, topY - 2, z1 - 1, Direction.SOUTH);
			}
			for (int z = minZ; z <= maxZ; z += step) {
				supportedLight(level, clip, x0 + 1, topY - 2, z, Direction.WEST);
				supportedLight(level, clip, x1 - 1, topY - 2, z, Direction.EAST);
			}
			// Floor tiles: a two-tone grid between the clusters, so no slab is bare.
			for (int x = minX + 1; x <= maxX - 1; x += 2) {
				for (int z = minZ + 1; z <= maxZ - 1; z += 2) {
					if (Math.abs(x - aisleX) <= 1 && Math.abs(z - aisleZ) <= 1) continue;
					place(level, clip, x, floorY, z, (x + z) % 4 == 0 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.POLISHED_ANDESITE.defaultBlockState());
				}
			}
			// Clustered floor noise: every open cell gets a purpose - a worktable,
			// a storage nook, a reading corner, or a lit display - never empty.
			for (int x = minX; x <= maxX; x += step) {
				for (int z = minZ; z <= maxZ; z += step) {
					if (Math.abs(x - aisleX) <= 1 && Math.abs(z - aisleZ) <= 1) continue;
					int roll = random.nextInt(10);
					if (roll == 0) {
						table(level, clip, x, floorY + 1, z, Direction.SOUTH, Blocks.DARK_OAK_PLANKS.defaultBlockState());
						supportedFurniture(level, clip, x, floorY + 2, z, Blocks.CANDLE.defaultBlockState());
					} else if (roll == 1) {
						supportedFurniture(level, clip, x, floorY + 1, z, Blocks.BARREL.defaultBlockState());
						if (topY - floorY >= 4) supportedFurniture(level, clip, x, floorY + 2, z, Blocks.BARREL.defaultBlockState());
					} else if (roll == 2) {
						supportedFurniture(level, clip, x, floorY + 1, z, Blocks.CHEST.defaultBlockState());
						supportedFurniture(level, clip, x + 1, floorY + 1, z, Blocks.BARREL.defaultBlockState());
					} else if (roll == 3) {
						supportedFurniture(level, clip, x, floorY + 1, z, Blocks.FLOWER_POT.defaultBlockState());
						if (topY - floorY >= 4) supportedFurniture(level, clip, x, floorY + 2, z, Blocks.CANDLE.defaultBlockState());
					} else if (roll == 4) {
						supportedFurniture(level, clip, x, floorY + 1, z, Blocks.BOOKSHELF.defaultBlockState());
						supportedFurniture(level, clip, x, floorY + 2, z, Blocks.CANDLE.defaultBlockState());
					} else if (roll == 5) {
						supportedFurniture(level, clip, x, floorY + 1, z, Blocks.CRAFTING_TABLE.defaultBlockState());
						supportedFurniture(level, clip, x + 1, floorY + 1, z, Blocks.BARREL.defaultBlockState());
					} else if (roll == 6) {
						bench(level, clip, x, floorY + 1, z, Direction.NORTH);
						supportedFurniture(level, clip, x, floorY + 1, z + 1, Blocks.CANDLE.defaultBlockState());
					} else if (roll == 7) {
						carpet(level, clip, x, z, x + 1, z + 1, floorY + 1, Blocks.RED_CARPET.defaultBlockState());
						supportedFurniture(level, clip, x, floorY + 1, z, Blocks.CANDLE.defaultBlockState());
					} else if (roll == 8) {
						supportedFurniture(level, clip, x, floorY + 1, z, Blocks.CANDLE.defaultBlockState());
						supportedFurniture(level, clip, x + 1, floorY + 1, z, Blocks.CANDLE.defaultBlockState());
						supportedFurniture(level, clip, x, floorY + 1, z + 1, Blocks.CANDLE.defaultBlockState());
					} else {
						supportedFurniture(level, clip, x, floorY + 1, z, Blocks.BARREL.defaultBlockState());
						supportedFurniture(level, clip, x, floorY + 2, z, Blocks.LANTERN.defaultBlockState());
					}
				}
			}
			// Ceiling light: hanging lanterns down the aisle, chandeliers over the clusters.
			if (topY - floorY >= 5) {
				for (int x = minX + 2; x <= maxX - 2; x += 6) hangingLantern(level, clip, x, topY - 1, aisleZ);
			}
			if (topY - floorY >= 8) {
				for (int x = minX + 3; x <= maxX - 3; x += 8) chandelier(level, clip, x, topY - 1, aisleZ);
			}
		}

		private void decorateMainAisle(WorldGenLevel level, BoundingBox clip, RandomSource random,
				int x0, int z0, int x1, int z1, int y) {
			boolean alongZ = x1 - x0 <= z1 - z0;
			int minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
			int minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);
			if (alongZ) {
				for (int z = minZ + 2; z <= maxZ - 2; z += 5) {
					floorDecor(level, clip, (minX + maxX) / 2 - 1, y, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
					floorDecor(level, clip, (minX + maxX) / 2 + 1, y, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				}
			} else {
				for (int x = minX + 2; x <= maxX - 2; x += 5) {
					floorDecor(level, clip, x, y, (minZ + maxZ) / 2 - 1, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
					floorDecor(level, clip, x, y, (minZ + maxZ) / 2 + 1, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				}
			}
		}

		private void chest(WorldGenLevel level, BoundingBox clip, RandomSource random,
				int x, int y, int z, Direction facing, ResourceKey<LootTable> table) {
			BlockPos p = new BlockPos(x, y, z);
			if (!clip.isInside(p)) return;
			level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
			if (level.getBlockEntity(p) instanceof ChestBlockEntity chest) {
				chest.setLootTable(table, random.nextLong());
			}
		}

		private void spawner(WorldGenLevel level, BoundingBox clip, int x, int y, int z, EntityType<?> type, RandomSource random) {
			BlockPos p = new BlockPos(x, y, z);
			if (!clip.isInside(p)) return;
			level.setBlock(p, Blocks.SPAWNER.defaultBlockState(), 2);
			if (level.getBlockEntity(p) instanceof SpawnerBlockEntity s) {
				s.setEntityId(type, random);
			}
		}

		private void spawnMob(WorldGenLevel level, BoundingBox clip, EntityType<? extends Mob> type, int x, int y, int z, float yaw) {
			BlockPos at = new BlockPos(x, y, z);
			if (!clip.isInside(at)) return;
			Mob mob = type.create(level.getLevel());
			if (mob == null) return;
			mob.moveTo(x + 0.5D, y, z + 0.5D, yaw, 0.0F);
			mob.setPersistenceRequired();
			level.addFreshEntity(mob);
		}

		private void spawnGuard(WorldGenLevel level, BoundingBox clip, int x, int y, int z, float yaw, boolean captain) {
			BlockPos at = new BlockPos(x, y, z);
			if (!clip.isInside(at)) return;
			Mob guard = EntityType.VINDICATOR.create(level.getLevel());
			if (guard == null) return;
			guard.moveTo(x + 0.5D, y, z + 0.5D, yaw, 0.0F);
			guard.setPersistenceRequired();
			guard.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
			guard.setDropChance(EquipmentSlot.MAINHAND, 0.5F);
			if (captain) {
				guard.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
				guard.setDropChance(EquipmentSlot.CHEST, 0.5F);
				guard.setCustomName(net.minecraft.network.chat.Component.literal("Stronghold Captain"));
				guard.setCustomNameVisible(true);
				var hp = guard.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
				if (hp != null) {
					hp.setBaseValue(40.0D);
					guard.setHealth(guard.getMaxHealth());
				}
			}
			level.addFreshEntity(guard);
		}

		// ---- Great Descent Hall ----

		private void buildHall(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int x0 = bb.minX() + 1;
			int x1 = bb.maxX() - 1;
			int zNorth = bb.minZ();
			int zSouth = bb.maxZ();
			int surface = aux2;
			int steps = aux;
			int bottom = surface - steps;
			int cx = (x0 + x1) / 2;

			// Porch platform outside the entrance.
			fill(level, clip, x0 - 1, surface - 2, zNorth, x1 + 1, surface - 1, zNorth, Blocks.STONE_BRICKS.defaultBlockState());
			place(level, clip, cx - 2, surface, zNorth, Blocks.END_ROD.defaultBlockState());
			place(level, clip, cx + 2, surface, zNorth, Blocks.END_ROD.defaultBlockState());

			for (int z = zNorth + 1; z <= zSouth - 1; z++) {
				int walk;
				boolean stairRow = false;
				int i = z - (zNorth + 4);
				if (z <= zNorth + 3) {
					walk = surface;             // top vestibule
				} else if (z <= zNorth + 3 + steps) {
					walk = surface - 1 - i;     // stairs descend one block per step
					stairRow = true;
				} else {
					walk = bottom;              // bottom vestibule
				}

				// Foundation mass below the walk.
				fill(level, clip, x0, bottom - 8, z, x1, walk - 2, z, Blocks.STONE_BRICKS.defaultBlockState());
				// Side walls.
				for (int y = bottom - 8; y <= walk + 5; y++) {
					place(level, clip, x0 - 1, y, z, brick(random));
					place(level, clip, x1 + 1, y, z, brick(random));
				}
				// End walls (north entrance wall, south terminal wall).
				if (z == zNorth + 1 || z == zSouth - 1) {
					for (int x = x0 - 1; x <= x1 + 1; x++) {
						for (int y = bottom - 8; y <= walk + 5; y++) {
							place(level, clip, x, y, z, brick(random));
						}
					}
				}
				if (z == zNorth + 1) {
					// Carve the entrance opening into the north wall.
					air(level, clip, cx - 2, surface, z, cx + 2, surface + 3, z);
					place(level, clip, cx - 2, surface + 3, z, Blocks.END_ROD.defaultBlockState());
					place(level, clip, cx + 2, surface + 3, z, Blocks.END_ROD.defaultBlockState());
				}

				if (stairRow) {
					// Two stair lanes either side of the central balustrade.
					BlockState stair = Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
					for (int x = x0; x <= cx - 1; x++) place(level, clip, x, walk - 1, z, stair);
					for (int x = cx + 1; x <= x1; x++) place(level, clip, x, walk - 1, z, stair);
					place(level, clip, cx, walk - 1, z, Blocks.STONE_BRICKS.defaultBlockState());
					place(level, clip, cx, walk, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
					if (i % 4 == 0) place(level, clip, cx, walk + 1, z, Blocks.END_ROD.defaultBlockState());
				} else {
					fill(level, clip, x0, walk - 1, z, x1, walk - 1, z, Blocks.STONE_BRICKS.defaultBlockState());
					if (z <= zNorth + 3) {
						// Red runner down the entrance aisle.
						place(level, clip, cx, walk, z, Blocks.RED_CARPET.defaultBlockState());
					}
				}
				// Air above the walk, ceiling five up.
				air(level, clip, x0, walk + 1, z, x1, walk + 4, z);
				fill(level, clip, x0, walk + 5, z, x1, walk + 5, z, Blocks.STONE_BRICKS.defaultBlockState());
			}

			// Two sentinels greet at the top of the descent.
			spawnGuard(level, clip, cx - 2, surface, zNorth + 2, 180.0F, false);
			spawnGuard(level, clip, cx + 2, surface, zNorth + 2, 180.0F, false);
			// Dense descent dressing: candles on the balustrade, sconces and banners
			// down both walls, and chiseled inlays on the level landings.
			for (int z = zNorth + 1; z <= zSouth - 1; z++) {
				int i = z - (zNorth + 4);
				int walk = z <= zNorth + 3 ? surface : (z <= zNorth + 3 + steps ? surface - 1 - i : bottom);
				if (i > 0 && i % 5 == 0) place(level, clip, cx, walk + 1, z, Blocks.CANDLE.defaultBlockState());
				if ((z - zNorth) % 6 == 0) {
					place(level, clip, x0 - 1, walk + 3, z, Blocks.END_ROD.defaultBlockState());
					place(level, clip, x1 + 1, walk + 3, z, Blocks.END_ROD.defaultBlockState());
				}
				if ((z - zNorth) % 12 == 6) {
					banner(level, clip, x0 - 1, walk + 4, z, Direction.EAST);
					banner(level, clip, x1 + 1, walk + 4, z, Direction.WEST);
				}
			}
			for (int x = x0; x <= x1; x += 2) {
				place(level, clip, x, surface, zNorth + 3, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
				place(level, clip, x, bottom, zSouth - 2, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
			}
		}

		private void buildArrivalVault(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			// A sealed, quiet arrival chamber. The single south gate is the intentional route onward.
			fill(level, clip, cx - 4, floorY + 1, cz - 4, cx + 4, floorY + 1, cz + 4, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			radialSigil(level, clip, cx, floorY, cz, 4, Blocks.PURPLE_CARPET.defaultBlockState());
			for (int[] corner : new int[][]{{bb.minX() + 2, bb.minZ() + 2}, {bb.minX() + 2, bb.maxZ() - 2}, {bb.maxX() - 2, bb.minZ() + 2}, {bb.maxX() - 2, bb.maxZ() - 2}}) {
				groundedPillar(level, clip, corner[0], floorY, corner[1], 6, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
			}
			// South gate: frame, threshold, and a fully grounded door. The rest of the vault stays sealed.
			for (int x : new int[]{cx - 2, cx + 2}) groundedPillar(level, clip, x, floorY, bb.maxZ(), 5, Blocks.PURPUR_PILLAR.defaultBlockState());
			beam(level, clip, cx - 2, floorY + 5, bb.maxZ(), cx + 2, bb.maxZ(), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
			door(level, clip, cx, floorY + 1, bb.maxZ(), Direction.SOUTH);
			fill(level, clip, bb.minX(), floorY + 1, bb.minZ(), bb.maxX(), floorY + 3, bb.minZ(), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
			fill(level, clip, bb.minX(), floorY + 1, bb.maxX(), bb.maxX(), floorY + 3, bb.maxX(), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
			fill(level, clip, bb.minX(), floorY + 1, bb.minZ(), bb.minX(), floorY + 3, bb.maxZ(), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
			chest(level, clip, random, cx - 3, floorY + 1, cz, Direction.EAST, EndesiumLootTables.STRONGHOLD);
			chest(level, clip, random, cx + 3, floorY + 1, cz, Direction.WEST, EndesiumLootTables.STRONGHOLD);
			// Dense arrival chamber detail: flower pots, candles, barrels along the sealed walls.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.NORTH, Blocks.BARREL.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.SOUTH, Blocks.FLOWER_POT.defaultBlockState());
			}
			for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 3) {
				wallDecor(level, clip, bb.minX() + 1, floorY + 2, z, Direction.WEST, Blocks.CANDLE.defaultBlockState());
				wallDecor(level, clip, bb.maxX() - 1, floorY + 2, z, Direction.EAST, Blocks.CANDLE.defaultBlockState());
			}
			// A second grounded threshold marks the only route out of the locate chamber.
			for (int x = cx - 3; x <= cx + 3; x++) floorDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Blocks.PURPLE_CARPET.defaultBlockState());
			// Dense arrival dressing: candle sconces on the sealed walls, corner
			// storage clusters, and a chandelier over the sigil.
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x += 2) {
				sconce(level, clip, x, floorY + 3, bb.minZ(), Direction.NORTH);
				sconce(level, clip, x, floorY + 3, bb.maxZ(), Direction.SOUTH);
			}
			for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 2) {
				sconce(level, clip, bb.minX(), floorY + 3, z, Direction.WEST);
				sconce(level, clip, bb.maxX(), floorY + 3, z, Direction.EAST);
			}
			for (int[] corner : new int[][]{{bb.minX() + 1, bb.minZ() + 1}, {bb.minX() + 1, bb.maxZ() - 1},
					{bb.maxX() - 1, bb.minZ() + 1}, {bb.maxX() - 1, bb.maxZ() - 1}}) {
				supportedFurniture(level, clip, corner[0], floorY + 1, corner[1], Blocks.FLOWER_POT.defaultBlockState());
				supportedFurniture(level, clip, corner[0], floorY + 1, corner[1] + 1, Blocks.BARREL.defaultBlockState());
			}
			if (bb.maxY() - floorY >= 7) chandelier(level, clip, cx, bb.maxY() - 1, cz);
			place(level, clip, cx - 2, floorY + 2, bb.maxZ() - 1, Blocks.END_ROD.defaultBlockState());
			place(level, clip, cx + 2, floorY + 2, bb.maxZ() - 1, Blocks.END_ROD.defaultBlockState());
		}

		// ---- Atrium hub ----

		private void buildHub(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());

			// Floor treatment: polished stone with a purple carpet border and central sigil.
			carpet(level, clip, bb.minX() + 1, bb.minZ() + 1, bb.maxX() - 1, bb.maxZ() - 1, floorY, Blocks.POLISHED_ANDESITE.defaultBlockState());
			radialSigil(level, clip, cx, floorY, cz, Math.min(10, Math.max(5, bb.getXSpan() / 3)), Blocks.PURPLE_CARPET.defaultBlockState());
			// Central resonance dais: low, grounded, clear visual compass.
			fill(level, clip, cx - 2, floorY + 1, cz - 2, cx + 2, floorY + 1, cz + 2,
					Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			fill(level, clip, cx - 1, floorY + 2, cz - 1, cx + 1, floorY + 2, cz + 1,
					Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
			place(level, clip, cx, floorY + 2, cz, Blocks.AMETHYST_BLOCK.defaultBlockState());
			place(level, clip, cx, floorY + 3, cz, Blocks.END_ROD.defaultBlockState());
			// Four slim structural columns at the room's cardinal axes for rhythm.
			groundedPillar(level, clip, cx, floorY, bb.minZ() + 2, bb.getYSpan() - 2, Blocks.PURPUR_PILLAR.defaultBlockState());
			groundedPillar(level, clip, cx, floorY, bb.maxZ() - 2, bb.getYSpan() - 2, Blocks.PURPUR_PILLAR.defaultBlockState());
			groundedPillar(level, clip, bb.minX() + 2, floorY, cz, bb.getYSpan() - 2, Blocks.PURPUR_PILLAR.defaultBlockState());
			groundedPillar(level, clip, bb.maxX() - 2, floorY, cz, bb.getYSpan() - 2, Blocks.PURPUR_PILLAR.defaultBlockState());
			// Carpet ring around the dais.
			carpet(level, clip, cx - 2, cz - 2, cx + 2, cz + 2, floorY + 1, Blocks.RED_CARPET.defaultBlockState());
			// Dense perimeter furniture: desks, shelves, barrels, bookshelves along all four walls.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				// North wall: bookshelves and barrels.
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.NORTH, Blocks.BOOKSHELF.defaultBlockState());
				wallDecor(level, clip, x, floorY + 3, bb.minZ() + 1, Direction.NORTH, Blocks.BOOKSHELF.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.SOUTH, Blocks.BARREL.defaultBlockState());
				// South wall: barrels and flower pots.
				wallDecor(level, clip, x, floorY + 2, bb.maxZ() - 1, Direction.SOUTH, Blocks.BARREL.defaultBlockState());
				wallDecor(level, clip, x, floorY + 3, bb.maxZ() - 1, Direction.SOUTH, Blocks.BOOKSHELF.defaultBlockState());
			}
			for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 3) {
				// West wall: shelves and flower pots.
				wallDecor(level, clip, bb.minX() + 1, floorY + 2, z, Direction.WEST, Blocks.BARREL.defaultBlockState());
				wallDecor(level, clip, bb.minX() + 1, floorY + 3, z, Direction.WEST, Blocks.BOOKSHELF.defaultBlockState());
				// East wall: bookshelves and barrels.
				wallDecor(level, clip, bb.maxX() - 1, floorY + 2, z, Direction.EAST, Blocks.BOOKSHELF.defaultBlockState());
				wallDecor(level, clip, bb.maxX() - 1, floorY + 3, z, Direction.EAST, Blocks.BARREL.defaultBlockState());
			}
			// Corner desk clusters: fence-post tables with flower pots and candles.
			for (int[] corner : new int[][]{{bb.minX() + 3, bb.minZ() + 3}, {bb.minX() + 3, bb.maxZ() - 3},
					{bb.maxX() - 3, bb.minZ() + 3}, {bb.maxX() - 3, bb.maxZ() - 3}}) {
				supportedFurniture(level, clip, corner[0], floorY + 1, corner[1], Blocks.FLOWER_POT.defaultBlockState());
				supportedFurniture(level, clip, corner[0] + 1, floorY + 1, corner[1], Blocks.OAK_FENCE.defaultBlockState());
				supportedFurniture(level, clip, corner[0] + 1, floorY + 2, corner[1], Blocks.CANDLE.defaultBlockState());
			}
			// Wall-mounted lanterns every 5 blocks for warm ambient light.
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 5) {
				supportedLight(level, clip, x, bb.maxY() - 2, bb.minZ() + 1, Direction.SOUTH);
				supportedLight(level, clip, x, bb.maxY() - 2, bb.maxZ() - 1, Direction.NORTH);
			}
			for (int z = bb.minZ() + 3; z <= bb.maxZ() - 3; z += 5) {
				supportedLight(level, clip, bb.minX() + 1, bb.maxY() - 2, z, Direction.EAST);
				supportedLight(level, clip, bb.maxX() - 1, bb.maxY() - 2, z, Direction.WEST);
			}
			// Hanging lanterns from ceiling beams for central lighting.
			for (int x = bb.minX() + 4; x <= bb.maxX() - 4; x += 6) {
				hangingLantern(level, clip, x, bb.maxY() - 1, cz - 4);
				hangingLantern(level, clip, x, bb.maxY() - 1, cz + 4);
			}
			spawnMob(level, clip, EntityType.BAT, cx - 3, bb.maxY() - 2, cz - 3, 0);
			spawnMob(level, clip, EntityType.BAT, cx + 3, bb.maxY() - 2, cz + 3, 120);
			spawnMob(level, clip, EntityType.BAT, cx, bb.maxY() - 2, cz, 240);
			// Upper gallery access is a real, grounded staircase rather than a visual-only platform.
			for (int step = 0; step < 6; step++) {
				fill(level, clip, bb.minX() + 3 + step, floorY + 1 + step, bb.minZ() + 3, bb.minX() + 4 + step, floorY + 1 + step, bb.minZ() + 4, Blocks.STONE_BRICK_STAIRS.defaultBlockState());
			}
			// Dense hub dressing: banners between the columns, chandeliers over the
			// dais, benches and barrel rows breaking up the open floor.
			banner(level, clip, bb.minX() + 1, floorY + 5, (bb.minZ() + bb.maxZ()) / 2, Direction.EAST);
			banner(level, clip, bb.maxX() - 1, floorY + 5, (bb.minZ() + bb.maxZ()) / 2, Direction.WEST);
			banner(level, clip, (bb.minX() + bb.maxX()) / 2, floorY + 5, bb.minZ() + 1, Direction.SOUTH);
			banner(level, clip, (bb.minX() + bb.maxX()) / 2, floorY + 5, bb.maxZ() - 1, Direction.NORTH);
			if (bb.maxY() - floorY >= 8) {
				chandelier(level, clip, cx, bb.maxY() - 1, cz - 8);
				chandelier(level, clip, cx, bb.maxY() - 1, cz + 8);
			}
			for (int x = bb.minX() + 6; x <= bb.maxX() - 6; x += 5) {
				bench(level, clip, x, floorY + 1, bb.minZ() + 2, Direction.NORTH);
				bench(level, clip, x, floorY + 1, bb.maxZ() - 3, Direction.SOUTH);
			}
			for (int z = bb.minZ() + 6; z <= bb.maxZ() - 6; z += 5) {
				supportedFurniture(level, clip, bb.minX() + 2, floorY + 1, z, Blocks.BARREL.defaultBlockState());
				supportedFurniture(level, clip, bb.maxX() - 2, floorY + 1, z, Blocks.BARREL.defaultBlockState());
			}
			// Floor tile seams radiating from the dais so the open floor reads as patterned.
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 4) {
				place(level, clip, x, floorY, bb.minZ() + 1, Blocks.STONE_BRICKS.defaultBlockState());
				place(level, clip, x, floorY, bb.maxZ() - 1, Blocks.STONE_BRICKS.defaultBlockState());
			}
		}

		private void buildJunction(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			for (int[] corner : new int[][]{{bb.minX() + 1, bb.minZ() + 1}, {bb.minX() + 1, bb.maxZ() - 1},
					{bb.maxX() - 1, bb.minZ() + 1}, {bb.maxX() - 1, bb.maxZ() - 1}}) {
				place(level, clip, corner[0], floorY + 1, corner[1], Blocks.END_ROD.defaultBlockState());
			}
			floorDecor(level, clip, cx, floorY + 1, cz, Blocks.RED_CARPET.defaultBlockState());
			// Four grounded route markers make junctions readable without relying on signs.
			for (int[] marker : new int[][]{{cx - 2, cz}, {cx + 2, cz}, {cx, cz - 2}, {cx, cz + 2}}) {
				floorDecor(level, clip, marker[0], floorY + 1, marker[1], Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			}
			if (variant == 0) {
				chest(level, clip, random, cx, floorY + 1, cz + 1, Direction.NORTH, EndesiumLootTables.STRONGHOLD);
			}
			// Junction detail: flower pots and barrels in the corners.
			for (int[] corner : new int[][]{{bb.minX() + 2, bb.minZ() + 2}, {bb.minX() + 2, bb.maxZ() - 2},
					{bb.maxX() - 2, bb.minZ() + 2}, {bb.maxX() - 2, bb.maxZ() - 2}}) {
				supportedFurniture(level, clip, corner[0], floorY + 1, corner[1], Blocks.FLOWER_POT.defaultBlockState());
				supportedFurniture(level, clip, corner[0], floorY + 1, corner[1] + 1, Blocks.BARREL.defaultBlockState());
			}
			// Dense junction dressing: candle sconces at the four arms, floor markers
			// radiating from the center cross, and candles in every corner nook.
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x += 2) {
				sconce(level, clip, x, floorY + 3, bb.minZ(), Direction.NORTH);
				sconce(level, clip, x, floorY + 3, bb.maxZ(), Direction.SOUTH);
			}
			for (int z = bb.minZ() + 1; z <= bb.maxZ() - 1; z += 2) {
				sconce(level, clip, bb.minX(), floorY + 3, z, Direction.WEST);
				sconce(level, clip, bb.maxX(), floorY + 3, z, Direction.EAST);
			}
			for (int d = 1; d <= 3; d++) {
				place(level, clip, cx, floorY + 1, cz - d, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				place(level, clip, cx, floorY + 1, cz + d, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				place(level, clip, cx - d, floorY + 1, cz, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				place(level, clip, cx + d, floorY + 1, cz, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			}
		}

		private void buildLibrary(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			carpet(level, clip, bb.minX() + 1, bb.minZ() + 1, bb.maxX() - 1, bb.maxZ() - 1, floorY, Blocks.POLISHED_ANDESITE.defaultBlockState());
			roomDressing(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY(), 5);
			// Bookshelf-lined walls, three shelves high, with a grounded reading dais.
			for (int x : new int[]{bb.minX() + 3, bb.maxX() - 3}) groundedPillar(level, clip, x, floorY, cz, Math.min(10, bb.getYSpan() - 1), Blocks.PURPUR_PILLAR.defaultBlockState());
			int inX0 = bb.minX() + 1, inZ0 = bb.minZ() + 1, inX1 = bb.maxX() - 1, inZ1 = bb.maxZ() - 1;
			for (int y = floorY + 1; y <= floorY + 2; y++) {
				for (int x = inX0; x <= inX1; x++) {
					if (!(x >= cx - 1 && x <= cx + 1)) place(level, clip, x, y, inZ0, Blocks.BOOKSHELF.defaultBlockState());
					place(level, clip, x, y, inZ1, Blocks.BOOKSHELF.defaultBlockState());
				}
				for (int z = inZ0; z <= inZ1; z++) {
					place(level, clip, inX0, y, z, Blocks.BOOKSHELF.defaultBlockState());
					place(level, clip, inX1, y, z, Blocks.BOOKSHELF.defaultBlockState());
				}
			}
			fill(level, clip, cx - 3, floorY + 1, cz - 3, cx + 3, floorY + 1, cz + 3, Blocks.DARK_OAK_PLANKS.defaultBlockState());
			place(level, clip, cx - 2, floorY + 2, cz - 2, Blocks.LECTERN.defaultBlockState());
			place(level, clip, cx + 2, floorY + 2, cz - 2, Blocks.LECTERN.defaultBlockState());
			place(level, clip, cx - 2, floorY + 1, cz + 2, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz + 2, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
			carpet(level, clip, cx - 2, cz - 2, cx + 2, cz + 2, floorY + 1, Blocks.BLUE_CARPET.defaultBlockState());
			chest(level, clip, random, cx - 1, floorY + 1, cz + 3, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
			chest(level, clip, random, cx + 1, floorY + 1, cz + 3, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
			// Additional library detail: flower pots on reading desks, candles.
			supportedFurniture(level, clip, cx - 2, floorY + 2, cz - 1, Blocks.FLOWER_POT.defaultBlockState());
			supportedFurniture(level, clip, cx + 2, floorY + 2, cz - 1, Blocks.FLOWER_POT.defaultBlockState());
			supportedFurniture(level, clip, cx, floorY + 2, cz - 2, Blocks.CANDLE.defaultBlockState());
			// Dense library dressing: chiseled bookshelf columns capping the stacks,
			// lecterns in the corners, candles down the aisles, and a chandelier.
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 4) {
				place(level, clip, x, floorY + 3, bb.minZ() + 1, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
				place(level, clip, x, floorY + 3, bb.maxZ() - 1, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
			}
			for (int z = bb.minZ() + 4; z <= bb.maxZ() - 4; z += 5) {
				place(level, clip, bb.minX() + 1, floorY + 3, z, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
				place(level, clip, bb.maxX() - 1, floorY + 3, z, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
			}
			for (int x = cx - 3; x <= cx + 3; x += 2) {
				supportedFurniture(level, clip, x, floorY + 1, cz + 5, Blocks.CANDLE.defaultBlockState());
			}
			if (bb.maxY() - floorY >= 8) chandelier(level, clip, cx, bb.maxY() - 1, cz - 4);
			for (int[] corner : new int[][]{{bb.minX() + 2, bb.minZ() + 2}, {bb.maxX() - 2, bb.maxZ() - 2},
					{bb.minX() + 2, bb.maxZ() - 2}, {bb.maxX() - 2, bb.minZ() + 2}}) {
				supportedFurniture(level, clip, corner[0], floorY + 1, corner[1], Blocks.LECTERN.defaultBlockState());
				supportedFurniture(level, clip, corner[0], floorY + 2, corner[1], Blocks.CANDLE.defaultBlockState());
			}
		}

		private void buildTreasury(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			roomDressing(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY(), 4);
			// Vault bars across the room, gated with a single opening.
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x++) {
				if (x == cx) continue;
				place(level, clip, x, floorY + 1, cz - 2, Blocks.IRON_BARS.defaultBlockState());
				place(level, clip, x, floorY + 2, cz - 2, Blocks.IRON_BARS.defaultBlockState());
			}
			// Dense treasury: multiple chests, barrels, gold, candles.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				if (x == cx) continue;
				supportedFurniture(level, clip, x, floorY + 1, cz + 3, Blocks.CHEST.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 1, bb.minZ() + 2, Blocks.BARREL.defaultBlockState());
			}
			chest(level, clip, random, cx - 1, floorY + 1, cz + 2, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
			chest(level, clip, random, cx + 1, floorY + 1, cz + 2, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
			place(level, clip, cx - 2, floorY + 1, cz + 2, Blocks.BARREL.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz + 2, Blocks.BARREL.defaultBlockState());
			place(level, clip, cx, floorY + 1, cz + 2, Blocks.CANDLE.defaultBlockState());
			place(level, clip, cx - 1, floorY + 1, cz + 3, Blocks.CANDLE.defaultBlockState());
			place(level, clip, cx + 1, floorY + 1, cz + 3, Blocks.CANDLE.defaultBlockState());
			spawnGuard(level, clip, cx, floorY + 1, cz - 1, 0.0F, true);
			// Dense treasury dressing: gold stacks on barrel plinths, barrel rows
			// along the walls, banners above the vault, and candles everywhere.
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x += 3) {
				if (x == cx) continue;
				supportedFurniture(level, clip, x, floorY + 1, bb.minZ() + 3, Blocks.GOLD_BLOCK.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 2, bb.minZ() + 3, Blocks.CANDLE.defaultBlockState());
			}
			for (int z = bb.minZ() + 3; z <= bb.maxZ() - 3; z += 3) {
				supportedFurniture(level, clip, bb.minX() + 2, floorY + 1, z, Blocks.BARREL.defaultBlockState());
				supportedFurniture(level, clip, bb.maxX() - 2, floorY + 1, z, Blocks.BARREL.defaultBlockState());
			}
			banner(level, clip, bb.minX() + 1, floorY + 4, cz, Direction.EAST);
			banner(level, clip, bb.maxX() - 1, floorY + 4, cz, Direction.WEST);
		}

		private void buildGuardPost(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			roomDressing(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY(), 4);
			// Guard post: weapon racks (fence posts), barrels, candles.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				wallDecor(level, clip, x, floorY + 1, bb.minZ() + 1, Direction.NORTH, Blocks.OAK_FENCE.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.NORTH, Blocks.CANDLE.defaultBlockState());
			}
			spawner(level, clip, cx, floorY + 2, cz, EntityType.PHANTOM, random);
			spawnGuard(level, clip, cx - 2, floorY + 1, cz - 1, 0.0F, false);
			spawnGuard(level, clip, cx + 2, floorY + 1, cz - 1, 0.0F, false);
			chest(level, clip, random, cx - 2, floorY + 1, cz + 2, Direction.NORTH, EndesiumLootTables.STRONGHOLD);
			place(level, clip, cx + 2, floorY + 1, cz + 2, Blocks.END_ROD.defaultBlockState());
			// Dense guard-post dressing: weapon racks down both walls, shield banners,
			// and a candle row over the phantom roost.
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x += 2) {
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.NORTH, Blocks.OAK_FENCE.defaultBlockState());
				wallDecor(level, clip, x, floorY + 3, bb.minZ() + 1, Direction.NORTH, Blocks.CANDLE.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.maxZ() - 1, Direction.SOUTH, Blocks.OAK_FENCE.defaultBlockState());
			}
			banner(level, clip, bb.minX() + 1, floorY + 4, bb.minZ(), Direction.SOUTH);
			banner(level, clip, bb.maxX() - 1, floorY + 4, bb.minZ(), Direction.SOUTH);
			banner(level, clip, bb.minX() + 1, floorY + 4, bb.maxZ(), Direction.NORTH);
			banner(level, clip, bb.maxX() - 1, floorY + 4, bb.maxZ(), Direction.NORTH);
			for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 3) {
				supportedFurniture(level, clip, bb.minX() + 2, floorY + 1, z, Blocks.BARREL.defaultBlockState());
			}
		}

		private void buildSanctum(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			carpet(level, clip, bb.minX() + 1, bb.minZ() + 1, bb.maxX() - 1, bb.maxZ() - 1, floorY, Blocks.END_STONE_BRICKS.defaultBlockState());
			int px0 = cx - 3, pz0 = cz - 3, px1 = cx + 3, pz1 = cz + 3;
			for (int[] corner : new int[][]{{px0, pz0}, {px0, pz1}, {px1, pz0}, {px1, pz1}}) {
				fill(level, clip, corner[0], floorY + 1, corner[1], corner[0], floorY + 2, corner[1],
						Blocks.PURPUR_PILLAR.defaultBlockState());
				place(level, clip, corner[0], floorY + 3, corner[1], Blocks.END_ROD.defaultBlockState());
			}
			place(level, clip, cx, floorY + 1, cz, Blocks.CRYING_OBSIDIAN.defaultBlockState());
			place(level, clip, cx, floorY + 2, cz, Blocks.CHORUS_PLANT.defaultBlockState());
			carpet(level, clip, cx - 1, cz - 1, cx + 1, cz + 1, floorY + 1, Blocks.PURPLE_CARPET.defaultBlockState());
			// Dense sanctum detail: chorus plants, candles, amethyst, barrels.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				wallDecor(level, clip, x, floorY + 1, bb.minZ() + 1, Direction.NORTH, Blocks.CHORUS_PLANT.defaultBlockState());
				wallDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Direction.SOUTH, Blocks.BARREL.defaultBlockState());
			}
			place(level, clip, cx - 2, floorY + 1, cz, Blocks.AMETHYST_CLUSTER.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz, Blocks.AMETHYST_CLUSTER.defaultBlockState());
			place(level, clip, cx - 1, floorY + 1, cz - 3, Blocks.CANDLE.defaultBlockState());
			place(level, clip, cx + 1, floorY + 1, cz - 3, Blocks.CANDLE.defaultBlockState());
			spawner(level, clip, bb.maxX() - 2, floorY + 1, bb.maxZ() - 2, EntityType.ENDERMAN, random);
			spawnMob(level, clip, EntityType.ENDERMAN, bb.minX() + 2, floorY + 1, bb.minZ() + 2, 90.0F);
			chest(level, clip, random, bb.minX() + 2, floorY + 1, bb.maxZ() - 2, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
			// Dense sanctum dressing: amethyst clusters and candles lining the walls,
			// a purple carpet ring around the altar, and end-rod corner lamps.
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x += 2) {
				supportedCluster(level, clip, x, floorY + 3, bb.minZ(), Direction.NORTH);
				supportedCluster(level, clip, x, floorY + 3, bb.maxZ(), Direction.SOUTH);
				supportedFurniture(level, clip, x, floorY + 1, bb.minZ() + 1, Blocks.CANDLE.defaultBlockState());
			}
			for (int z = bb.minZ() + 1; z <= bb.maxZ() - 1; z += 2) {
				supportedCluster(level, clip, bb.minX(), floorY + 3, z, Direction.WEST);
				supportedCluster(level, clip, bb.maxX(), floorY + 3, z, Direction.EAST);
				supportedFurniture(level, clip, bb.minX() + 1, floorY + 1, z, Blocks.CANDLE.defaultBlockState());
				supportedFurniture(level, clip, bb.maxX() - 1, floorY + 1, z, Blocks.CANDLE.defaultBlockState());
			}
			carpet(level, clip, px0 - 1, pz0 - 1, px1 + 1, pz1 + 1, floorY + 1, Blocks.PURPLE_CARPET.defaultBlockState());
			for (int[] corner : new int[][]{{bb.minX() + 1, bb.minZ() + 1}, {bb.maxX() - 1, bb.minZ() + 1},
					{bb.minX() + 1, bb.maxZ() - 1}, {bb.maxX() - 1, bb.maxZ() - 1}}) {
				place(level, clip, corner[0], floorY + 2, corner[1], Blocks.END_ROD.defaultBlockState());
			}
		}

		private void buildBarracks(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			carpet(level, clip, bb.minX() + 1, bb.minZ() + 1, bb.maxX() - 1, bb.maxZ() - 1, floorY, Blocks.STONE_BRICKS.defaultBlockState());
			roomDressing(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY(), 4);
			BlockState foot = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.FOOT);
			BlockState head = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.HEAD);
			for (int bx : new int[]{cx - 3, cx - 1, cx + 1, cx + 3}) {
				place(level, clip, bx, floorY + 1, bb.minZ() + 2, foot);
				place(level, clip, bx, floorY + 1, bb.minZ() + 3, head);
			}
			// Dense barracks: barrels, flower pots, furnaces along walls.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				wallDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Direction.SOUTH, Blocks.BARREL.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.maxZ() - 1, Direction.SOUTH, Blocks.FLOWER_POT.defaultBlockState());
			}
			chest(level, clip, random, cx - 3, floorY + 1, bb.minZ() + 4, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
			place(level, clip, cx, floorY + 1, bb.maxZ() - 2, Blocks.SMOKER.defaultBlockState());
			place(level, clip, cx, floorY + 2, bb.maxZ() - 2, Blocks.END_ROD.defaultBlockState());
			place(level, clip, cx - 1, floorY + 1, bb.maxZ() - 2, Blocks.FURNACE.defaultBlockState());
			place(level, clip, cx + 1, floorY + 1, bb.maxZ() - 2, Blocks.CRAFTING_TABLE.defaultBlockState());
			spawnGuard(level, clip, cx - 2, floorY + 1, cz + 1, 180.0F, false);
			spawnGuard(level, clip, cx, floorY + 1, cz + 1, 180.0F, false);
			spawnGuard(level, clip, cx + 2, floorY + 1, cz + 1, 180.0F, false);
			// Dense barracks dressing: lockers along the walls, footlockers at the
			// bed feet, armor stands in the aisle, and weapon racks on the end wall.
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x += 2) {
				supportedFurniture(level, clip, x, floorY + 1, bb.minZ() + 1, Blocks.BARREL.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 2, bb.minZ() + 1, Blocks.BARREL.defaultBlockState());
				wallDecor(level, clip, x, floorY + 3, bb.minZ() + 1, Direction.NORTH, Blocks.CANDLE.defaultBlockState());
			}
			for (int bx : new int[]{cx - 3, cx - 1, cx + 1, cx + 3}) {
				supportedFurniture(level, clip, bx, floorY + 1, bb.minZ() + 4, Blocks.CHEST.defaultBlockState());
			}
			armorStand(level, clip, cx - 3, floorY + 1, bb.maxZ() - 3, 90.0F);
			armorStand(level, clip, cx + 3, floorY + 1, bb.maxZ() - 3, 90.0F);
			for (int z = bb.minZ() + 5; z <= bb.maxZ() - 1; z += 2) {
				wallDecor(level, clip, bb.minX() + 1, floorY + 2, z, Direction.WEST, Blocks.OAK_FENCE.defaultBlockState());
				wallDecor(level, clip, bb.maxX() - 1, floorY + 2, z, Direction.EAST, Blocks.OAK_FENCE.defaultBlockState());
			}
		}

		private void buildScriptorium(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			carpet(level, clip, bb.minX() + 1, bb.minZ() + 1, bb.maxX() - 1, bb.maxZ() - 1, floorY, Blocks.POLISHED_ANDESITE.defaultBlockState());
			// Dense scriptorium: lecterns, bookshelves, candles, ink cauldron.
			for (int[] spot : new int[][]{{cx - 2, cz - 2}, {cx + 2, cz - 2}, {cx - 2, cz + 2}, {cx + 2, cz + 2}}) {
				place(level, clip, spot[0], floorY + 1, spot[1], Blocks.LECTERN.defaultBlockState());
				place(level, clip, spot[0], floorY + 2, spot[1], Blocks.CANDLE.defaultBlockState());
			}
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				wallDecor(level, clip, x, floorY + 1, bb.minZ() + 1, Direction.NORTH, Blocks.BOOKSHELF.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.NORTH, Blocks.BOOKSHELF.defaultBlockState());
				wallDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Direction.SOUTH, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
			}
			place(level, clip, cx - 2, floorY + 1, cz, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
			place(level, clip, cx, floorY + 1, cz, Blocks.CAULDRON.defaultBlockState());
			chest(level, clip, random, cx, floorY + 1, cz + 3, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
			spawnMob(level, clip, EntityType.BAT, cx, bb.maxY() - 2, cz, 0);
			// Dense scriptorium dressing: chiseled shelves capping the book walls,
			// ink cauldrons down the aisles, banners, and a chandelier.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				place(level, clip, x, floorY + 3, bb.minZ() + 1, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
				place(level, clip, x, floorY + 3, bb.maxZ() - 1, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
			}
			for (int z = bb.minZ() + 3; z <= bb.maxZ() - 3; z += 3) {
				supportedFurniture(level, clip, bb.minX() + 1, floorY + 1, z, Blocks.CAULDRON.defaultBlockState());
				supportedFurniture(level, clip, bb.maxX() - 1, floorY + 1, z, Blocks.CAULDRON.defaultBlockState());
			}
			for (int x = cx - 2; x <= cx + 2; x += 2) {
				supportedFurniture(level, clip, x, floorY + 1, cz + 2, Blocks.CANDLE.defaultBlockState());
			}
			banner(level, clip, bb.minX() + 1, floorY + 4, cz, Direction.EAST);
			banner(level, clip, bb.maxX() - 1, floorY + 4, cz, Direction.WEST);
			if (bb.maxY() - floorY >= 7) chandelier(level, clip, cx, bb.maxY() - 1, cz - 2);
		}

		private void buildArboretum(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			roomDressing(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY(), 5);
			fill(level, clip, bb.minX() + 1, bb.maxY() - 1, bb.minZ() + 1, bb.maxX() - 1, bb.maxY() - 1, bb.maxZ() - 1,
					Blocks.GLASS.defaultBlockState());
			fill(level, clip, bb.minX() + 1, floorY, bb.minZ() + 1, bb.maxX() - 1, floorY, bb.maxZ() - 1,
					Blocks.DIRT.defaultBlockState());
			place(level, clip, cx - 2, floorY + 1, cz - 2, Blocks.FLOWERING_AZALEA.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz - 2, Blocks.AZALEA.defaultBlockState());
			place(level, clip, cx - 2, floorY + 1, cz + 2, Blocks.FERN.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz + 2, Blocks.POTTED_FERN.defaultBlockState());
			fill(level, clip, cx - 1, floorY + 1, cz - 1, cx + 1, floorY + 1, cz + 1, Blocks.MOSS_CARPET.defaultBlockState());
			place(level, clip, cx, floorY + 1, cz, Blocks.SHORT_GRASS.defaultBlockState());
			fill(level, clip, cx - 1, floorY + 1, cz + 2, cx + 1, floorY + 1, cz + 3, Blocks.WATER.defaultBlockState());
			place(level, clip, cx, floorY + 2, cz + 2, Blocks.LILY_PAD.defaultBlockState());
			spawnMob(level, clip, EntityType.BAT, cx - 1, bb.maxY() - 2, cz, 30);
			spawnMob(level, clip, EntityType.BAT, cx + 1, bb.maxY() - 2, cz, 210);
			// Dense garden: a flower meadow across the dirt, moss patches, mushrooms
			// under the azaleas, potted plants, and lanterns hung from the glass roof.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 2) {
				for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 2) {
					if (Math.abs(x - cx) <= 1 && Math.abs(z - cz) <= 1) continue;
					int roll = Math.floorMod(x * 7 + z * 13, 8);
					if (roll < 4) plant(level, clip, x, floorY + 1, z);
					else if (roll == 4) supportedFurniture(level, clip, x, floorY + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
					else if (roll == 5) supportedFurniture(level, clip, x, floorY + 1, z, Blocks.RED_MUSHROOM.defaultBlockState());
					else if (roll == 6) supportedFurniture(level, clip, x, floorY + 1, z, Blocks.BROWN_MUSHROOM.defaultBlockState());
				}
			}
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 4) {
				hangingLantern(level, clip, x, bb.maxY() - 1, cz - 2);
				hangingLantern(level, clip, x, bb.maxY() - 1, cz + 2);
			}
			for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 4) {
				place(level, clip, bb.minX() + 2, floorY + 1, z, Blocks.FLOWER_POT.defaultBlockState());
				place(level, clip, bb.maxX() - 2, floorY + 1, z, Blocks.FLOWER_POT.defaultBlockState());
			}
			place(level, clip, cx - 1, floorY + 1, cz + 2, Blocks.POTTED_FERN.defaultBlockState());
			place(level, clip, cx + 1, floorY + 1, cz + 2, Blocks.POTTED_AZALEA.defaultBlockState());
		}

		private void buildPortalRoom(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());

			// The heart: a contained 3x3 lava pool sunk below the frame dais.
			fill(level, clip, cx - 1, floorY, cz - 1, cx + 1, floorY, cz + 1, Blocks.LAVA.defaultBlockState());
			fill(level, clip, cx - 3, floorY, cz - 3, cx + 3, floorY, cz + 3, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			fill(level, clip, cx - 1, floorY - 1, cz - 1, cx + 1, floorY - 1, cz + 1, Blocks.MAGMA_BLOCK.defaultBlockState());
			// Twelve frames, no eyes - progression stays intact.
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
					if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;
					Direction facing = dx == 2 ? Direction.WEST
							: dx == -2 ? Direction.EAST
							: dz == 2 ? Direction.NORTH : Direction.SOUTH;
					place(level, clip, cx + dx, floorY + 1, cz + dz,
							Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, facing));
				}
			}
			// Four cathedral pillars at the portal corners.
			for (int[] corner : new int[][]{{bb.minX() + 1, bb.minZ() + 1}, {bb.minX() + 1, bb.maxZ() - 1},
					{bb.maxX() - 1, bb.minZ() + 1}, {bb.maxX() - 1, bb.maxZ() - 1}}) {
				groundedPillar(level, clip, corner[0], floorY, corner[1], 4, Blocks.PURPUR_PILLAR.defaultBlockState());
				supportedLight(level, clip, corner[0], floorY + 3, corner[1], Direction.NORTH);
			}
			// Dense portal room: candles, carpets, soul lanterns, barrels.
			for (int dx = -3; dx <= 3; dx++) {
				floorDecor(level, clip, cx + dx, floorY + 1, cz - 4, Blocks.PURPLE_CARPET.defaultBlockState());
				floorDecor(level, clip, cx + dx, floorY + 1, cz + 4, Blocks.PURPLE_CARPET.defaultBlockState());
			}
			// Side chapel candles and barrels.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 4) {
				wallDecor(level, clip, x, floorY + 1, bb.minZ() + 1, Direction.NORTH, Blocks.CANDLE.defaultBlockState());
				wallDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Direction.SOUTH, Blocks.BARREL.defaultBlockState());
			}
			// Rear nave arch and side chapel thresholds frame the portal without touching its twelve sockets.
			for (int x : new int[]{bb.minX() + 3, bb.maxX() - 3}) groundedPillar(level, clip, x, floorY, cz, Math.min(10, bb.getYSpan() - 1), Blocks.PURPUR_PILLAR.defaultBlockState());
			beam(level, clip, bb.minX() + 3, bb.maxY() - 1, cz, bb.maxX() - 3, cz, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
			spawner(level, clip, bb.maxX() - 2, floorY + 1, bb.minZ() + 2, EntityType.SILVERFISH, random);
			chest(level, clip, random, bb.minX() + 2, floorY + 1, bb.minZ() + 2, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
			// Dense cathedral dressing: candle rows down both naves, barrel supplies
			// along the chapels, banners on the pillars, and chandeliers overhead.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				supportedFurniture(level, clip, x, floorY + 1, bb.minZ() + 2, Blocks.CANDLE.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 1, bb.maxZ() - 2, Blocks.CANDLE.defaultBlockState());
			}
			for (int z = bb.minZ() + 3; z <= bb.maxZ() - 3; z += 4) {
				supportedFurniture(level, clip, bb.minX() + 1, floorY + 1, z, Blocks.BARREL.defaultBlockState());
				supportedFurniture(level, clip, bb.maxX() - 1, floorY + 1, z, Blocks.BARREL.defaultBlockState());
			}
			banner(level, clip, bb.minX(), floorY + 4, cz, Direction.EAST);
			banner(level, clip, bb.maxX(), floorY + 4, cz, Direction.WEST);
			if (bb.maxY() - floorY >= 9) {
				chandelier(level, clip, cx - 6, bb.maxY() - 1, cz);
				chandelier(level, clip, cx + 6, bb.maxY() - 1, cz);
			}
			for (int dx = -3; dx <= 3; dx++) {
				place(level, clip, cx + dx, floorY + 1, cz - 5, Blocks.PURPLE_CARPET.defaultBlockState());
				place(level, clip, cx + dx, floorY + 1, cz + 5, Blocks.PURPLE_CARPET.defaultBlockState());
			}
		}

		private void buildCrypt(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			carpet(level, clip, bb.minX() + 1, bb.minZ() + 1, bb.maxX() - 1, bb.maxZ() - 1, floorY, Blocks.STONE_BRICKS.defaultBlockState());
			place(level, clip, cx - 2, floorY + 1, cz - 2, Blocks.BONE_BLOCK.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz - 2, Blocks.BONE_BLOCK.defaultBlockState());
			place(level, clip, cx - 2, floorY + 2, cz - 2, Blocks.STONE_BRICK_SLAB.defaultBlockState());
			place(level, clip, cx + 2, floorY + 2, cz - 2, Blocks.STONE_BRICK_SLAB.defaultBlockState());
			for (int[] web : new int[][]{{bb.minX() + 1, bb.minZ() + 1}, {bb.maxX() - 1, bb.maxZ() - 1},
					{bb.minX() + 1, bb.maxZ() - 1}, {bb.maxX() - 1, bb.minZ() + 1}}) {
				place(level, clip, web[0], floorY + 2, web[1], Blocks.COBWEB.defaultBlockState());
			}
			// Dense crypt: bone slabs, cobwebs, soul lanterns.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				supportedFurniture(level, clip, x, floorY + 1, bb.minZ() + 2, Blocks.BONE_BLOCK.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 1, bb.maxZ() - 2, Blocks.STONE_BRICK_SLAB.defaultBlockState());
			}
			place(level, clip, cx, floorY + 2, cz - 3, Blocks.SOUL_LANTERN.defaultBlockState());

			// The ladder shaft up to the hub: air column + ladder on the north wall.
			int hubFloorY = floorY + 10; // depth-1: the hub walk floor
			air(level, clip, cx - 2, floorY, cz - 3, cx - 1, hubFloorY, cz - 2);
			for (int y = floorY + 1; y <= hubFloorY; y++) {
				place(level, clip, cx - 2, y, cz - 3, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
			}
			place(level, clip, cx, floorY + 1, cz, Blocks.ENDER_CHEST.defaultBlockState());
			// Dense crypt dressing: coffin rows, skull niches, more cobwebs, and
			// soul lanterns hanging from the ceiling.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				coffin(level, clip, x, floorY + 1, bb.minZ() + 2);
				coffin(level, clip, x, floorY + 1, bb.maxZ() - 3);
			}
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.NORTH, Blocks.SKELETON_SKULL.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.maxZ() - 1, Direction.SOUTH, Blocks.SKELETON_SKULL.defaultBlockState());
			}
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 5) {
				place(level, clip, x, bb.maxY() - 1, cz, Blocks.SOUL_LANTERN.defaultBlockState());
			}
			place(level, clip, cx - 1, floorY + 1, cz + 1, Blocks.BONE_BLOCK.defaultBlockState());
			place(level, clip, cx + 1, floorY + 1, cz + 1, Blocks.BONE_BLOCK.defaultBlockState());
			place(level, clip, cx - 1, floorY + 2, cz + 1, Blocks.CANDLE.defaultBlockState());
			place(level, clip, cx + 1, floorY + 2, cz + 1, Blocks.CANDLE.defaultBlockState());
		}

		private void buildStarwell(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			fill(level, clip, bb.minX(), floorY, bb.minZ(), bb.maxX(), bb.maxY(), bb.maxZ(), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
			air(level, clip, bb.minX() + 1, floorY + 1, bb.minZ() + 1, bb.maxX() - 1, bb.maxY() - 1, bb.maxZ() - 1);
			// Vertical structural ribs on all four walls.
			for (int y = floorY + 1; y < bb.maxY(); y++) {
				for (int x : new int[]{bb.minX(), bb.maxX()}) {
					place(level, clip, x, y, cz, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				}
				for (int z : new int[]{bb.minZ(), bb.maxZ()}) {
					place(level, clip, cx, y, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				}
			}
			// Spiral purpur pillars ascending the shaft.
			for (int y = floorY + 2; y <= bb.maxY() - 2; y += 3) {
				for (int a = 0; a < 4; a++) {
					double angle = (Math.PI / 2.0D) * a + (y % 6) * 0.08D;
					int x = cx + (int) Math.round(Math.cos(angle) * 4.0D);
					int z = cz + (int) Math.round(Math.sin(angle) * 4.0D);
					place(level, clip, x, y, z, Blocks.PURPUR_PILLAR.defaultBlockState());
				}
			}
			// Amethyst clusters and end rods for vertical light columns.
			for (int y = floorY + 3; y <= bb.maxY() - 3; y += 4) {
				place(level, clip, cx + 2, y, cz, Blocks.AMETHYST_CLUSTER.defaultBlockState());
				place(level, clip, cx - 2, y, cz, Blocks.AMETHYST_CLUSTER.defaultBlockState());
			}
			place(level, clip, cx, floorY + 2, cz, Blocks.AMETHYST_BLOCK.defaultBlockState());
			place(level, clip, cx, floorY + 3, cz, Blocks.END_ROD.defaultBlockState());
			spawnMob(level, clip, EntityType.ENDERMAN, cx, floorY + 1, cz + 3, 180.0F);
			// Dense shaft dressing: amethyst clusters on every rib, end-rod light
			// columns, and a lantern hung near the top.
			for (int y = floorY + 2; y <= bb.maxY() - 2; y += 3) {
				place(level, clip, bb.minX(), y, cz, Blocks.AMETHYST_CLUSTER.defaultBlockState());
				place(level, clip, bb.maxX(), y, cz, Blocks.AMETHYST_CLUSTER.defaultBlockState());
				place(level, clip, cx, y, bb.minZ(), Blocks.AMETHYST_CLUSTER.defaultBlockState());
				place(level, clip, cx, y, bb.maxZ(), Blocks.AMETHYST_CLUSTER.defaultBlockState());
			}
			for (int y = floorY + 4; y <= bb.maxY() - 3; y += 6) {
				place(level, clip, cx + 3, y, cz, Blocks.END_ROD.defaultBlockState());
				place(level, clip, cx - 3, y, cz, Blocks.END_ROD.defaultBlockState());
			}
			if (bb.maxY() - floorY >= 10) {
				place(level, clip, cx, bb.maxY() - 3, cz, Blocks.CHAIN.defaultBlockState());
				place(level, clip, cx, bb.maxY() - 4, cz, Blocks.LANTERN.defaultBlockState());
			}
		}

		private void buildObservatory(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			// Circular floor inlay for observatory identity.
			for (int x = bb.minX() + 1; x < bb.maxX(); x++) {
				for (int z = bb.minZ() + 1; z < bb.maxZ(); z++) {
					if (Math.abs(x - cx) + Math.abs(z - cz) <= 5) place(level, clip, x, floorY, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				}
			}
			for (int r = 2; r <= 5; r++) {
				for (int x = cx - r; x <= cx + r; x++) {
					place(level, clip, x, floorY + 1, cz - r, Blocks.PURPUR_BLOCK.defaultBlockState());
					place(level, clip, x, floorY + 1, cz + r, Blocks.PURPUR_BLOCK.defaultBlockState());
				}
				for (int z = cz - r; z <= cz + r; z++) {
					place(level, clip, cx - r, floorY + 1, z, Blocks.PURPUR_BLOCK.defaultBlockState());
					place(level, clip, cx + r, floorY + 1, z, Blocks.PURPUR_BLOCK.defaultBlockState());
				}
			}
			fill(level, clip, cx - 2, floorY + 1, cz - 2, cx + 2, floorY + 1, cz + 2, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			place(level, clip, cx, floorY + 2, cz, Blocks.AMETHYST_BLOCK.defaultBlockState());
			place(level, clip, cx, floorY + 3, cz, Blocks.END_ROD.defaultBlockState());
			for (int x : new int[]{bb.minX() + 2, bb.maxX() - 2}) groundedPillar(level, clip, x, floorY, cz, 8, Blocks.PURPUR_PILLAR.defaultBlockState());
			// Telescope stands (end rods on chiseled stone) and storage.
			for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 4) {
				wallDecor(level, clip, bb.minX() + 1, floorY + 1, z, Direction.WEST, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
				wallDecor(level, clip, bb.minX() + 1, floorY + 2, z, Direction.WEST, Blocks.END_ROD.defaultBlockState());
				wallDecor(level, clip, bb.maxX() - 1, floorY + 1, z, Direction.EAST, Blocks.BARREL.defaultBlockState());
			}
			chest(level, clip, random, cx - 3, floorY + 1, cz, Direction.EAST, EndesiumLootTables.STRONGHOLD);
			// Dense observatory dressing: workbench stands down both walls, amethyst
			// clusters between them, corner barrels, and a chandelier overhead.
			for (int z = bb.minZ() + 3; z <= bb.maxZ() - 3; z += 4) {
				supportedFurniture(level, clip, bb.minX() + 1, floorY + 1, z, Blocks.CRAFTING_TABLE.defaultBlockState());
				supportedFurniture(level, clip, bb.minX() + 1, floorY + 2, z, Blocks.CANDLE.defaultBlockState());
				supportedFurniture(level, clip, bb.maxX() - 1, floorY + 1, z, Blocks.BARREL.defaultBlockState());
			}
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 4) {
				supportedCluster(level, clip, x, floorY + 4, bb.minZ(), Direction.NORTH);
				supportedCluster(level, clip, x, floorY + 4, bb.maxZ(), Direction.SOUTH);
			}
			if (bb.maxY() - floorY >= 8) chandelier(level, clip, cx, bb.maxY() - 1, cz);
			for (int[] corner : new int[][]{{bb.minX() + 2, bb.minZ() + 2}, {bb.maxX() - 2, bb.maxZ() - 2},
					{bb.minX() + 2, bb.maxZ() - 2}, {bb.maxX() - 2, bb.minZ() + 2}}) {
				supportedFurniture(level, clip, corner[0], floorY + 1, corner[1], Blocks.BARREL.defaultBlockState());
			}
		}

		private void buildResonanceEngine(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			// Resonance conduits: amethyst pillars with end rod emitters.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				fill(level, clip, x, floorY + 1, cz - 2, x, floorY + 3, cz + 2, Blocks.AMETHYST_BLOCK.defaultBlockState());
				place(level, clip, x, floorY + 4, cz, Blocks.END_ROD.defaultBlockState());
			}
			for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 3) {
				place(level, clip, cx, floorY + 1, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				place(level, clip, cx, floorY + 2, z, Blocks.AMETHYST_CLUSTER.defaultBlockState());
			}
			fill(level, clip, cx - 2, floorY + 1, cz - 2, cx + 2, floorY + 1, cz + 2, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			fill(level, clip, cx - 1, floorY + 2, cz - 1, cx + 1, floorY + 3, cz + 1, Blocks.CRYING_OBSIDIAN.defaultBlockState());
			place(level, clip, cx, floorY + 3, cz, Blocks.AMETHYST_BLOCK.defaultBlockState());
			place(level, clip, cx, floorY + 4, cz, Blocks.END_ROD.defaultBlockState());
			// Conduit control stations: barrels, levers, candles.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 5) {
				wallDecor(level, clip, x, floorY + 1, bb.minZ() + 1, Direction.NORTH, Blocks.BARREL.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.NORTH, Blocks.CANDLE.defaultBlockState());
			}
			spawner(level, clip, cx, floorY + 1, cz, EntityType.SILVERFISH, random);
			// Dense engineering dressing: lever control banks on the conduit
			// barrels, amethyst clusters on the walls, and candle rows at the stations.
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 4) {
				supportedFurniture(level, clip, x, floorY + 1, bb.minZ() + 2, Blocks.BARREL.defaultBlockState());
				place(level, clip, x, floorY + 2, bb.minZ() + 2, Blocks.LEVER.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 1, bb.maxZ() - 2, Blocks.BARREL.defaultBlockState());
				place(level, clip, x, floorY + 2, bb.maxZ() - 2, Blocks.LEVER.defaultBlockState());
			}
			for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 3) {
				supportedCluster(level, clip, bb.minX(), floorY + 4, z, Direction.WEST);
				supportedCluster(level, clip, bb.maxX(), floorY + 4, z, Direction.EAST);
			}
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				place(level, clip, x, floorY + 1, cz - 3, Blocks.CANDLE.defaultBlockState());
				place(level, clip, x, floorY + 1, cz + 3, Blocks.CANDLE.defaultBlockState());
			}
		}

		private void buildBastion(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			// Iron bar partition and deepslate pillars for military feel.
			for (int x : new int[]{bb.minX() + 2, bb.maxX() - 2}) groundedPillar(level, clip, x, floorY, bb.minZ() + 2, 4, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
			for (int z : new int[]{bb.minZ() + 2, bb.maxZ() - 2}) groundedPillar(level, clip, bb.minX() + 2, floorY, z, 4, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 2) place(level, clip, x, floorY + 1, cz, Blocks.IRON_BARS.defaultBlockState());
			// Weapon racks and barrels along walls.
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 4) {
				wallDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Direction.SOUTH, Blocks.OAK_FENCE.defaultBlockState());
				wallDecor(level, clip, x, floorY + 1, bb.minZ() + 1, Direction.NORTH, Blocks.BARREL.defaultBlockState());
			}
			spawnGuard(level, clip, cx, floorY + 1, cz, 0.0F, true);
			spawnGuard(level, clip, cx - 2, floorY + 1, cz + 2, 180.0F, false);
			spawnGuard(level, clip, cx + 2, floorY + 1, cz + 2, 180.0F, false);
			chest(level, clip, random, cx, floorY + 1, bb.minZ() + 2, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
			// Dense bastion dressing: weapon racks and candle rows down the walls,
			// banners at the gate, and iron bars and barrels in the corners.
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x += 2) {
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.NORTH, Blocks.OAK_FENCE.defaultBlockState());
				wallDecor(level, clip, x, floorY + 3, bb.minZ() + 1, Direction.NORTH, Blocks.CANDLE.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.maxZ() - 1, Direction.SOUTH, Blocks.OAK_FENCE.defaultBlockState());
				wallDecor(level, clip, x, floorY + 3, bb.maxZ() - 1, Direction.SOUTH, Blocks.CANDLE.defaultBlockState());
			}
			banner(level, clip, bb.minX() + 1, floorY + 4, bb.minZ(), Direction.SOUTH);
			banner(level, clip, bb.maxX() - 1, floorY + 4, bb.minZ(), Direction.SOUTH);
			banner(level, clip, bb.minX() + 1, floorY + 4, bb.maxZ(), Direction.NORTH);
			banner(level, clip, bb.maxX() - 1, floorY + 4, bb.maxZ(), Direction.NORTH);
			for (int[] corner : new int[][]{{bb.minX() + 2, bb.minZ() + 2}, {bb.minX() + 2, bb.maxZ() - 2},
					{bb.maxX() - 2, bb.minZ() + 2}, {bb.maxX() - 2, bb.maxZ() - 2}}) {
				supportedFurniture(level, clip, corner[0], floorY + 1, corner[1], Blocks.BARREL.defaultBlockState());
			}
			for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 3) {
				place(level, clip, bb.minX() + 1, floorY + 1, z, Blocks.IRON_BARS.defaultBlockState());
				place(level, clip, bb.maxX() - 1, floorY + 1, z, Blocks.IRON_BARS.defaultBlockState());
			}
		}

		private void buildConservatory(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			fill(level, clip, bb.minX() + 1, floorY, bb.minZ() + 1, bb.maxX() - 1, floorY, bb.maxZ() - 1, Blocks.END_STONE.defaultBlockState());
			// Dense chorus garden: plants at regular intervals.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				place(level, clip, x, floorY + 1, cz, Blocks.CHORUS_PLANT.defaultBlockState());
				place(level, clip, x, floorY + 2, cz, Blocks.CHORUS_FLOWER.defaultBlockState());
			}
			// Flower pots and moss along the perimeter.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 4) {
				wallDecor(level, clip, x, floorY + 1, bb.minZ() + 1, Direction.NORTH, Blocks.FLOWER_POT.defaultBlockState());
				wallDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Direction.SOUTH, Blocks.FLOWER_POT.defaultBlockState());
			}
			fill(level, clip, cx - 1, floorY + 1, cz - 3, cx + 1, floorY + 1, cz - 3, Blocks.MOSS_CARPET.defaultBlockState());
			place(level, clip, cx - 2, floorY + 1, cz - 2, Blocks.AMETHYST_BLOCK.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz + 2, Blocks.AMETHYST_BLOCK.defaultBlockState());
			spawnMob(level, clip, EntityType.ENDERMAN, cx, floorY + 1, cz, 0.0F);
			// Dense conservatory dressing: chorus rows across the floor, moss patches,
			// flowers in the border beds, and lanterns hung from the ceiling.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 2) {
				for (int z = bb.minZ() + 2; z <= bb.maxZ() - 2; z += 2) {
					if (Math.abs(x - cx) <= 1 && Math.abs(z - cz) <= 1) continue;
					int roll = Math.floorMod(x * 11 + z * 7, 6);
					if (roll == 0) supportedFurniture(level, clip, x, floorY + 1, z, Blocks.CHORUS_FLOWER.defaultBlockState());
					else if (roll == 1) supportedFurniture(level, clip, x, floorY + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
					else if (roll < 4) plant(level, clip, x, floorY + 1, z);
				}
			}
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 4) {
				hangingLantern(level, clip, x, bb.maxY() - 1, cz - 3);
				hangingLantern(level, clip, x, bb.maxY() - 1, cz + 3);
			}
			for (int z = bb.minZ() + 3; z <= bb.maxZ() - 3; z += 4) {
				place(level, clip, bb.minX() + 2, floorY + 1, z, Blocks.FLOWER_POT.defaultBlockState());
				place(level, clip, bb.maxX() - 2, floorY + 1, z, Blocks.FLOWER_POT.defaultBlockState());
			}
		}

		private void buildReliquary(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			roomDressing(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY(), 4);
			// Concentric obsidian rings for a ritual floor pattern.
			for (int r = 1; r <= 3; r++) {
				for (int x = cx - r; x <= cx + r; x++) {
					place(level, clip, x, floorY + 1, cz - r, Blocks.OBSIDIAN.defaultBlockState());
					place(level, clip, x, floorY + 1, cz + r, Blocks.OBSIDIAN.defaultBlockState());
				}
				for (int z = cz - r; z <= cz + r; z++) {
					place(level, clip, cx - r, floorY + 1, z, Blocks.OBSIDIAN.defaultBlockState());
					place(level, clip, cx + r, floorY + 1, z, Blocks.OBSIDIAN.defaultBlockState());
				}
			}
			// Dense reliquary: candles, ender chests, barrels.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 4) {
				wallDecor(level, clip, x, floorY + 1, bb.minZ() + 1, Direction.NORTH, Blocks.CANDLE.defaultBlockState());
				wallDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Direction.SOUTH, Blocks.BARREL.defaultBlockState());
			}
			place(level, clip, cx, floorY + 1, cz, Blocks.ENDER_CHEST.defaultBlockState());
			place(level, clip, cx - 2, floorY + 1, cz, Blocks.CANDLE.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz, Blocks.CANDLE.defaultBlockState());
			spawnGuard(level, clip, cx, floorY + 1, cz - 3, 0.0F, true);
			chest(level, clip, random, cx, floorY + 1, cz + 3, Direction.NORTH, EndesiumLootTables.STRONGHOLD);
			// Dense reliquary dressing: candles on the ritual rings, ender chests in
			// the corners, banners, and soul lanterns down the walls.
			for (int r = 1; r <= 3; r++) {
				for (int[] cardinal : new int[][]{{cx + r, cz}, {cx - r, cz}, {cx, cz + r}, {cx, cz - r}}) {
					if (r % 2 == 1) supportedFurniture(level, clip, cardinal[0], floorY + 1, cardinal[1], Blocks.CANDLE.defaultBlockState());
				}
			}
			for (int[] corner : new int[][]{{bb.minX() + 2, bb.minZ() + 2}, {bb.minX() + 2, bb.maxZ() - 2},
					{bb.maxX() - 2, bb.minZ() + 2}, {bb.maxX() - 2, bb.maxZ() - 2}}) {
				supportedFurniture(level, clip, corner[0], floorY + 1, corner[1], Blocks.ENDER_CHEST.defaultBlockState());
			}
			banner(level, clip, bb.minX() + 1, floorY + 4, cz, Direction.EAST);
			banner(level, clip, bb.maxX() - 1, floorY + 4, cz, Direction.WEST);
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x += 3) {
				place(level, clip, x, floorY + 1, bb.minZ() + 1, Blocks.SOUL_LANTERN.defaultBlockState());
				place(level, clip, x, floorY + 1, bb.maxZ() - 1, Blocks.SOUL_LANTERN.defaultBlockState());
			}
		}

		private void buildCatacombs(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			// Bone pillar alcoves along both walls.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 2) {
				groundedPillar(level, clip, x, floorY, bb.minZ() + 2, 3, Blocks.BONE_BLOCK.defaultBlockState());
				groundedPillar(level, clip, x, floorY, bb.maxZ() - 2, 3, Blocks.BONE_BLOCK.defaultBlockState());
			}
			// Cobwebs and soul lanterns for eerie atmosphere.
			for (int[] web : new int[][]{{bb.minX() + 2, bb.minZ() + 1}, {bb.maxX() - 2, bb.maxZ() - 1},
					{bb.minX() + 2, bb.maxZ() - 1}, {bb.maxX() - 2, bb.minZ() + 1}}) {
				place(level, clip, web[0], floorY + 2, web[1], Blocks.COBWEB.defaultBlockState());
			}
			place(level, clip, cx, floorY + 1, cz, Blocks.SOUL_LANTERN.defaultBlockState());
			place(level, clip, cx - 2, floorY + 1, cz, Blocks.SOUL_LANTERN.defaultBlockState());
			place(level, clip, cx + 2, floorY + 1, cz, Blocks.SOUL_LANTERN.defaultBlockState());
			spawner(level, clip, cx, floorY + 1, cz, EntityType.SKELETON, random);
			chest(level, clip, random, cx, floorY + 1, cz + 2, Direction.NORTH, EndesiumLootTables.STRONGHOLD);
			// Dense catacomb dressing: coffin rows between the bone pillars, wall
			// skulls, and soul lanterns hanging along the spine.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				coffin(level, clip, x, floorY + 1, cz + 4);
			}
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 2) {
				wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.NORTH, Blocks.SKELETON_SKULL.defaultBlockState());
				wallDecor(level, clip, x, floorY + 2, bb.maxZ() - 1, Direction.SOUTH, Blocks.SKELETON_SKULL.defaultBlockState());
			}
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 6) {
				place(level, clip, x, bb.maxY() - 1, cz - 3, Blocks.SOUL_LANTERN.defaultBlockState());
				place(level, clip, x, bb.maxY() - 1, cz + 3, Blocks.SOUL_LANTERN.defaultBlockState());
			}
		}

		private void buildBridge(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int y = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			fill(level, clip, bb.minX(), y, bb.minZ(), bb.maxX(), y, bb.maxZ(), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			for (int x = bb.minX(); x <= bb.maxX(); x++) {
				place(level, clip, x, y + 1, bb.minZ(), Blocks.IRON_BARS.defaultBlockState());
				place(level, clip, x, y + 1, bb.maxZ(), Blocks.IRON_BARS.defaultBlockState());
				if ((x - bb.minX()) % 4 == 0) {
					place(level, clip, x, y - 1, bb.minZ(), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
					place(level, clip, x, y - 1, bb.maxZ(), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				}
			}
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 4) {
				place(level, clip, x, y + 1, cz, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
				place(level, clip, x, y + 2, cz, Blocks.END_ROD.defaultBlockState());
			}
			// Dense bridge dressing: lanterns on the balustrades and floor tiles
			// across the span so the walkway reads as one piece.
			for (int x = bb.minX(); x <= bb.maxX(); x += 4) {
				place(level, clip, x, y + 2, bb.minZ(), Blocks.LANTERN.defaultBlockState());
				place(level, clip, x, y + 2, bb.maxZ(), Blocks.LANTERN.defaultBlockState());
			}
			for (int x = bb.minX() + 1; x <= bb.maxX() - 1; x += 2) {
				place(level, clip, x, y, bb.minZ() + 1, Blocks.STONE_BRICKS.defaultBlockState());
				place(level, clip, x, y, bb.maxZ() - 1, Blocks.STONE_BRICKS.defaultBlockState());
			}
		}

		private void buildGallery(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			int cx = (bb.minX() + bb.maxX()) / 2;
			int cz = (bb.minZ() + bb.maxZ()) / 2;
			shell(level, clip, random, bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ(), floorY, bb.maxY());
			// Gallery pillars and display shelves along the walls.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				groundedPillar(level, clip, x, floorY, bb.minZ() + 1, 4, Blocks.PURPUR_PILLAR.defaultBlockState());
				wallDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Direction.EAST, Blocks.BOOKSHELF.defaultBlockState());
			}
			// Gallery display: flower pots and candles on pedestals.
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 5) {
				supportedFurniture(level, clip, x, floorY + 1, cz - 2, Blocks.FLOWER_POT.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 1, cz + 2, Blocks.CANDLE.defaultBlockState());
			}
			carpet(level, clip, cx - 1, bb.minZ() + 2, cx + 1, bb.maxZ() - 2, floorY + 1, Blocks.PURPLE_CARPET.defaultBlockState());
			// Dense gallery dressing: banners between the pillars, display pedestals
			// down the hall, and chandeliers overhead.
			for (int x = bb.minX() + 2; x <= bb.maxX() - 2; x += 3) {
				banner(level, clip, x, floorY + 4, bb.minZ(), Direction.SOUTH);
				banner(level, clip, x, floorY + 4, bb.maxZ(), Direction.NORTH);
			}
			for (int x = bb.minX() + 3; x <= bb.maxX() - 3; x += 6) {
				supportedFurniture(level, clip, x, floorY + 1, cz - 3, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 2, cz - 3, Blocks.FLOWER_POT.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 1, cz + 3, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				supportedFurniture(level, clip, x, floorY + 2, cz + 3, Blocks.CANDLE.defaultBlockState());
			}
			if (bb.maxY() - floorY >= 7) {
				chandelier(level, clip, bb.minX() + 4, bb.maxY() - 1, cz);
				chandelier(level, clip, bb.maxX() - 4, bb.maxY() - 1, cz);
			}
		}

		private void buildCorridor(WorldGenLevel level, BoundingBox clip, RandomSource random) {
			BoundingBox bb = this.getBoundingBox();
			int floorY = bb.minY();
			boolean alongZ = bb.getXSpan() <= bb.getZSpan();
			int ax0 = bb.minX() + 1, ax1 = bb.maxX() - 1;
			int az0 = bb.minZ() + 1, az1 = bb.maxZ() - 1;
			// Tube: raised floor, ceiling, side walls, air. The raised floor makes the route readable.
			fill(level, clip, bb.minX(), floorY, bb.minZ(), bb.maxX(), floorY, bb.maxZ(), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
			fill(level, clip, bb.minX(), bb.maxY(), bb.minZ(), bb.maxX(), bb.maxY(), bb.maxZ(), Blocks.STONE_BRICKS.defaultBlockState());
			for (int y = floorY + 1; y <= bb.maxY() - 1; y++) {
				if (alongZ) {
					for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
						place(level, clip, bb.minX(), y, z, brick(random));
						place(level, clip, bb.maxX(), y, z, brick(random));
					}
				} else {
					for (int x = bb.minX(); x <= bb.maxX(); x++) {
						place(level, clip, x, y, bb.minZ(), brick(random));
						place(level, clip, x, y, bb.maxZ(), brick(random));
					}
				}
			}
			// Interior air spans the FULL travel length so the ends punch clean
			// doorways through whatever walls they meet.
			if (alongZ) {
				air(level, clip, ax0, floorY + 1, bb.minZ(), ax1, bb.maxY() - 1, bb.maxZ());
			} else {
				air(level, clip, bb.minX(), floorY + 1, az0, bb.maxX(), bb.maxY() - 1, az1);
			}

			int midX = (ax0 + ax1) / 2;
			int midZ = (az0 + az1) / 2;
			int len = alongZ ? az1 - az0 : ax1 - ax0;

			// Explicit doorway portals at both ends prevent the corridor from reading as a sealed tube.
			if (alongZ) {
				for (int z : new int[]{bb.minZ() + 1, bb.maxZ() - 1}) {
					for (int x = ax0; x <= ax1; x++) floorDecor(level, clip, x, floorY + 1, z, Blocks.PURPLE_CARPET.defaultBlockState());
				}
			} else {
				for (int x : new int[]{bb.minX() + 1, bb.maxX() - 1}) {
					for (int z = az0; z <= az1; z++) floorDecor(level, clip, x, floorY + 1, z, Blocks.PURPLE_CARPET.defaultBlockState());
				}
			}
			if (alongZ) {
				air(level, clip, ax0, floorY + 1, bb.minZ(), ax1, floorY + 3, bb.minZ() + 1);
				air(level, clip, ax0, floorY + 1, bb.maxZ() - 1, ax1, floorY + 3, bb.maxZ());
			} else {
				air(level, clip, bb.minX(), floorY + 1, az0, bb.minX() + 1, floorY + 3, az1);
				air(level, clip, bb.maxX() - 1, floorY + 1, az0, bb.maxX(), floorY + 3, az1);
			}
			// Lived-in corridor walls: sconces, candles and clutter down the whole run.
			if (alongZ) {
				for (int z = az0 + 2; z <= az1 - 2; z += 4) {
					supportedLight(level, clip, bb.minX(), bb.maxY() - 2, z, Direction.EAST);
					supportedLight(level, clip, bb.maxX(), bb.maxY() - 2, z, Direction.WEST);
					wallDecor(level, clip, bb.minX() + 1, floorY + 2, z, Direction.WEST, Blocks.CANDLE.defaultBlockState());
					wallDecor(level, clip, bb.maxX() - 1, floorY + 2, z, Direction.EAST, Blocks.CANDLE.defaultBlockState());
				}
				for (int z = az0 + 2; z <= az1 - 2; z += 4) {
					wallDecor(level, clip, bb.minX() + 1, floorY + 1, z, Direction.WEST, Blocks.FLOWER_POT.defaultBlockState());
					wallDecor(level, clip, bb.maxX() - 1, floorY + 1, z, Direction.EAST, Blocks.BARREL.defaultBlockState());
				}
				// Floor tiles along the edges so the whole length reads dressed.
				for (int z = az0 + 1; z <= az1 - 1; z += 2) {
					place(level, clip, ax0, floorY, z, z % 4 == 0 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.POLISHED_ANDESITE.defaultBlockState());
					place(level, clip, ax1, floorY, z, z % 4 == 1 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.POLISHED_ANDESITE.defaultBlockState());
				}
				if (bb.maxY() - floorY >= 5) {
					for (int z = az0 + 4; z <= az1 - 4; z += 6) hangingLantern(level, clip, midX, bb.maxY() - 1, z);
				}
			} else {
				for (int x = ax0 + 2; x <= ax1 - 2; x += 4) {
					supportedLight(level, clip, x, bb.maxY() - 2, bb.minZ(), Direction.SOUTH);
					supportedLight(level, clip, x, bb.maxY() - 2, bb.maxZ(), Direction.NORTH);
					wallDecor(level, clip, x, floorY + 2, bb.minZ() + 1, Direction.SOUTH, Blocks.CANDLE.defaultBlockState());
					wallDecor(level, clip, x, floorY + 2, bb.maxZ() - 1, Direction.NORTH, Blocks.CANDLE.defaultBlockState());
				}
				for (int x = ax0 + 2; x <= ax1 - 2; x += 4) {
					wallDecor(level, clip, x, floorY + 1, bb.minZ() + 1, Direction.SOUTH, Blocks.FLOWER_POT.defaultBlockState());
					wallDecor(level, clip, x, floorY + 1, bb.maxZ() - 1, Direction.NORTH, Blocks.BARREL.defaultBlockState());
				}
				for (int x = ax0 + 1; x <= ax1 - 1; x += 2) {
					place(level, clip, x, floorY, az0, x % 4 == 0 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.POLISHED_ANDESITE.defaultBlockState());
					place(level, clip, x, floorY, az1, x % 4 == 1 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.POLISHED_ANDESITE.defaultBlockState());
				}
				if (bb.maxY() - floorY >= 5) {
					for (int x = ax0 + 4; x <= ax1 - 4; x += 6) hangingLantern(level, clip, x, bb.maxY() - 1, midZ);
				}
			}

			switch (variant) {
				case 1 -> { // carpet runner
					if (alongZ) {
						carpet(level, clip, midX, az0 + 1, midX, az1 - 1, floorY + 1, Blocks.RED_CARPET.defaultBlockState());
					} else {
						carpet(level, clip, ax0 + 1, midZ, ax1 - 1, midZ, floorY + 1, Blocks.RED_CARPET.defaultBlockState());
					}
				}
				case 2 -> { // storage alcove: chest against the wall
					if (alongZ) {
						chest(level, clip, random, ax0 + 1, floorY + 1, midZ, Direction.EAST, EndesiumLootTables.STRONGHOLD);
					} else {
						chest(level, clip, random, midX, floorY + 1, az0 + 1, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
					}
				}
				case 3 -> { // dark ambush: enderman + cobwebs
					spawnMob(level, clip, EntityType.ENDERMAN, midX, floorY + 1, midZ, 0.0F);
					place(level, clip, midX - 1, floorY + 2, midZ, Blocks.COBWEB.defaultBlockState());
					place(level, clip, midX + 1, floorY + 2, midZ, Blocks.COBWEB.defaultBlockState());
				}
				case 4 -> { // ceremonial way: pillars of light toward the portal
					if (alongZ) {
						for (int z = az0 + 2; z <= az1 - 2; z += 4) {
							place(level, clip, midX, floorY + 1, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
							place(level, clip, midX, floorY + 2, z, Blocks.END_ROD.defaultBlockState());
						}
					} else {
						for (int x = ax0 + 2; x <= ax1 - 2; x += 4) {
							place(level, clip, x, floorY + 1, midZ, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
							place(level, clip, x, floorY + 2, midZ, Blocks.END_ROD.defaultBlockState());
						}
					}
				}
				default -> { // plain, with candle niches and flower pots
					place(level, clip, midX, floorY + 1, az0 + 1, Blocks.CANDLE.defaultBlockState());
					place(level, clip, midX, floorY + 1, az1 - 1, Blocks.CANDLE.defaultBlockState());
					if (alongZ) {
						place(level, clip, ax0 + 1, floorY + 1, midZ, Blocks.FLOWER_POT.defaultBlockState());
						place(level, clip, ax1 - 1, floorY + 1, midZ, Blocks.FLOWER_POT.defaultBlockState());
					} else {
						place(level, clip, midX, floorY + 1, az0 + 1, Blocks.FLOWER_POT.defaultBlockState());
						place(level, clip, midX, floorY + 1, az1 - 1, Blocks.FLOWER_POT.defaultBlockState());
					}
				}
			}
			// Long corridors get a little storage near the far end too.
			if (len >= 9 && variant != 2) {
				if (alongZ) {
					chest(level, clip, random, ax0 + 1, floorY + 1, az1 - 2, Direction.EAST, EndesiumLootTables.STRONGHOLD);
				} else {
					chest(level, clip, random, ax1 - 2, floorY + 1, az0 + 1, Direction.SOUTH, EndesiumLootTables.STRONGHOLD);
				}
			}
		}
	}
}
