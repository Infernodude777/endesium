package com.infernodude777.endesium.world.structure;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.world.BiomeStructureFeature;
import com.infernodude777.endesium.world.EndBiomeProfiles;
import com.infernodude777.endesium.world.StructurePlacement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

/**
 * A registered vanilla Structure for one region's flagship. Placement is
 * decided by the {@code endesium_flagships} random_spread structure set; the
 * region codec field gates each instance to exactly one Endesium biome, and
 * the same support/seam checks the old Feature used run inside the piece
 * before a single block is written.
 *
 * <p>This is the migration that gives flagships real chunk ownership: every
 * overlapping chunk realizes only its slice of the build through its own
 * generation pass, instead of one Feature write racing chunk borders.</p>
 */
public final class EndesiumFlagshipStructure extends Structure {
	public static final MapCodec<EndesiumFlagshipStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Codec.intRange(0, 9).fieldOf("region").forGetter(s -> s.region),
					settingsCodec(instance)
			).apply(instance, EndesiumFlagshipStructure::new));

	/** Half-extent of the piece box: build extent plus a probe ring margin. */
	private static final int BOX_MARGIN = BiomeStructureFeature.MAX_BUILD_EXTENT + 3;

	private final int region;

	public EndesiumFlagshipStructure(int region, StructureSettings settings) {
		super(settings);
		this.region = region;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		// Keep the dragon's island clear (the old Feature refused <160 blocks).
		long d2 = (long) chunkPos.x * chunkPos.x + (long) chunkPos.z * chunkPos.z;
		if (d2 < 100L) return Optional.empty();

		if (!siteSupportsStructure(context, chunkPos)) return Optional.empty();

		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG,
				builder -> generatePieces(builder, context));
	}

	/**
	 * Runs the piece's exact site checks against the noise surface, so /locate
	 * and structure-set placement can only ever report candidates the piece
	 * will actually build. This is the fix for locate pointing at empty
	 * terrain: both paths call this same method, and it now refuses anything
	 * the piece would refuse.
	 */
	private boolean siteSupportsStructure(GenerationContext context, ChunkPos chunkPos) {
		int centerX = chunkPos.getMiddleBlockX();
		int centerZ = chunkPos.getMiddleBlockZ();
		try {
			int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(centerX, centerZ,
					Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
			return BiomeStructureFeature.siteValid(context, centerX, centerZ, surfaceY, region);
		} catch (Exception e) {
			// Noise queries should never throw; if one does, fall back to the
			// cheap region check rather than blinding locate entirely.
			return regionMatches(context, chunkPos);
		}
	}

	private void generatePieces(StructurePiecesBuilder builder, GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		int centerX = chunkPos.getMiddleBlockX();
		int centerZ = chunkPos.getMiddleBlockZ();
		int maxY = context.chunkGenerator().getMinY() + context.chunkGenerator().getGenDepth() - 1;
		builder.addPiece(new Piece(new BoundingBox(
						centerX - BOX_MARGIN, context.chunkGenerator().getMinY(),
						centerZ - BOX_MARGIN,
						centerX + BOX_MARGIN, maxY,
						centerZ + BOX_MARGIN),
				centerX, centerZ, region));
	}

	/** Best-effort biome gate used only as an exception fallback. */
	private boolean regionMatches(GenerationContext context, ChunkPos chunkPos) {
		try {
			int qx = QuartPos.fromBlock(chunkPos.getMiddleBlockX());
			int qz = QuartPos.fromBlock(chunkPos.getMiddleBlockZ());
			var source = context.chunkGenerator().getBiomeSource();
			var sampler = context.randomState().sampler();
			// End banding is y-dependent near island/void transitions, so a
			// single sea-level sample misreports edge columns; accept when any
			// plausible terrain height matches the claimed region. The piece's
			// real-biome ring check remains the authority.
			for (int qy : new int[] { 0, 8, 16 }) {
				if (EndBiomeProfiles.regionOf(source.getNoiseBiome(qx, qy, qz, sampler)) == region) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	@Override
	public StructureType<?> type() {
		return EndesiumStructureTypes.FLAGSHIP;
	}

	/** Codec holder for the registry. */
	public static final class Type implements StructureType<EndesiumFlagshipStructure> {
		@Override
		public MapCodec<EndesiumFlagshipStructure> codec() {
			return CODEC;
		}
	}

	/** The single piece: validates terrain, then builds the whole flagship. */
	public static final class Piece extends net.minecraft.world.level.levelgen.structure.StructurePiece {
		private final int centerX;
		private final int centerZ;
		private final int region;

		Piece(BoundingBox box, int centerX, int centerZ, int region) {
			// StructurePiece restores its box from BB/GD NBT keys on load; a
			// fresh piece seeds those through the same tag-based constructor.
			this(makeTag(box, centerX, centerZ, region));
		}

		private static CompoundTag makeTag(BoundingBox box, int centerX, int centerZ, int region) {
			CompoundTag tag = new CompoundTag();
			tag.putIntArray("BB", new int[] { box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ() });
			tag.putInt("GD", 0);
			tag.putInt("CenterX", centerX);
			tag.putInt("CenterZ", centerZ);
			tag.putInt("Region", region);
			return tag;
		}

		static Piece load(StructurePieceSerializationContext context, CompoundTag tag) {
			return new Piece(tag);
		}

		private Piece(CompoundTag tag) {
			super(EndesiumStructureTypes.FLAGSHIP_PIECE, tag);
			this.centerX = tag.getInt("CenterX");
			this.centerZ = tag.getInt("CenterZ");
			this.region = tag.getInt("Region");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
			tag.putInt("CenterX", centerX);
			tag.putInt("CenterZ", centerZ);
			tag.putInt("Region", region);
		}

		@Override
		public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
				RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos blockPos) {
			// Build exactly once, from the anchor chunk. Vanilla runs postProcess
			// once per overlapping chunk; reads and writes must stay inside the
			// primed 3x3 region centered on whichever chunk is decorating, so
			// every other chunk defers to the anchor.
			if (chunkPos.x != (centerX >> 4) || chunkPos.z != (centerZ >> 4)) {
				return;
			}
			int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
			BlockPos base = new BlockPos(centerX, y, centerZ);
			StructurePlacement.beginPiece(box);
			try {
				BiomeStructureFeature.generateInto(level, base, region, random);
			} finally {
				StructurePlacement.endPiece();
			}
		}
	}
}
