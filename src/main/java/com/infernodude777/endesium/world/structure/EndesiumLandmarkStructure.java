package com.infernodude777.endesium.world.structure;

import com.infernodude777.endesium.world.EndBiomeProfiles;
import com.infernodude777.endesium.world.RegionLandmarkFeature;
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
 * A registered vanilla Structure for one region's landmark: medium,
 * hand-authored builds on the {@code endesium_landmarks} random_spread grid
 * (~256 blocks per region). Same contract as the flagship structure, with the
 * landmark tier's compact footprint and support checks.
 */
public final class EndesiumLandmarkStructure extends Structure {
	public static final MapCodec<EndesiumLandmarkStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Codec.intRange(0, 9).fieldOf("region").forGetter(s -> s.region),
					settingsCodec(instance)
			).apply(instance, EndesiumLandmarkStructure::new));

	/** Landmark footprints are compact; a small box margin covers them. */
	private static final int BOX_MARGIN = 20;

	private final int region;

	public EndesiumLandmarkStructure(int region, StructureSettings settings) {
		super(settings);
		this.region = region;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		long d2 = (long) chunkPos.x * chunkPos.x + (long) chunkPos.z * chunkPos.z;
		if (d2 < 100L) return Optional.empty();

		if (!regionMatches(context, chunkPos)) return Optional.empty();

		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG,
				builder -> generatePieces(builder, context));
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

	private boolean regionMatches(GenerationContext context, ChunkPos chunkPos) {
		try {
			int qx = QuartPos.fromBlock(chunkPos.getMiddleBlockX());
			int qz = QuartPos.fromBlock(chunkPos.getMiddleBlockZ());
			var source = context.chunkGenerator().getBiomeSource();
			var sampler = context.randomState().sampler();
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
		return EndesiumStructureTypes.LANDMARK;
	}

	/** Codec holder for the registry. */
	public static final class Type implements StructureType<EndesiumLandmarkStructure> {
		@Override
		public MapCodec<EndesiumLandmarkStructure> codec() {
			return CODEC;
		}
	}

	/** The single piece: validates terrain, then builds the landmark. */
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
			super(EndesiumStructureTypes.LANDMARK_PIECE, tag);
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
			// Build exactly once, from the anchor chunk (see flagship piece).
			if (chunkPos.x != (centerX >> 4) || chunkPos.z != (centerZ >> 4)) {
				return;
			}
			int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
			BlockPos base = new BlockPos(centerX, y, centerZ);
			StructurePlacement.beginPiece(box);
			try {
				RegionLandmarkFeature.generateInto(level, base, region, random);
			} finally {
				StructurePlacement.endPiece();
			}
		}
	}
}
