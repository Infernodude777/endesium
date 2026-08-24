package com.infernodude777.endesium.world.structure;

import com.infernodude777.endesium.world.EndBiomeProfiles;
import com.infernodude777.endesium.world.ModWorldgen;
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
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

/**
 * The vanilla Structure wrapper for one region's landmark: medium,
 * hand-authored builds whose placement is owned entirely by the
 * {@code endesium_landmarks} random_spread structure set. Builds are delegated
 * to the original Feature code, so every landmark is identical to the
 * Feature-era builds while gaining vanilla {@code /locate structure} and
 * {@code /place structure} support.
 */
public final class EndesiumLandmarkStructure extends Structure {
	public static final MapCodec<EndesiumLandmarkStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Codec.intRange(0, 9).fieldOf("region").forGetter(s -> s.region),
					settingsCodec(instance)
			).apply(instance, EndesiumLandmarkStructure::new));

	/** Landmark footprints are compact; a small box margin covers them. */
	private static final int BOX_MARGIN = 22;

	private final int region;

	public EndesiumLandmarkStructure(int region, StructureSettings settings) {
		super(settings);
		this.region = region;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		long d2 = (long) chunkPos.x * chunkPos.x + (long) chunkPos.z * chunkPos.z;
		if (d2 < 100L) {
			return Optional.empty();
		}
		int region = regionAt(context, chunkPos);
		if (region != this.region) {
			return Optional.empty();
		}
		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG,
				builder -> generatePieces(builder, context));
	}

	private int regionAt(GenerationContext context, ChunkPos chunkPos) {
		try {
			var source = context.chunkGenerator().getBiomeSource();
			var sampler = context.randomState().sampler();
			int qx = QuartPos.fromBlock(chunkPos.getMiddleBlockX());
			int qz = QuartPos.fromBlock(chunkPos.getMiddleBlockZ());
			for (int qy : new int[] {0, 8, 16}) {
				int region = EndBiomeProfiles.regionOf(source.getNoiseBiome(qx, qy, qz, sampler));
				if (region >= 0) {
					return region;
				}
			}
			return -1;
		} catch (Exception e) {
			return -1;
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

	@Override
	public StructureType<?> type() {
		return EndesiumStructureTypes.LANDMARK;
	}

	public static final class Type implements StructureType<EndesiumLandmarkStructure> {
		@Override
		public MapCodec<EndesiumLandmarkStructure> codec() {
			return CODEC;
		}
	}

	public static final class Piece extends net.minecraft.world.level.levelgen.structure.StructurePiece {
		private final int centerX;
		private final int centerZ;
		private final int region;

		Piece(BoundingBox box, int centerX, int centerZ, int region) {
			this(makeTag(box, centerX, centerZ, region));
		}

		private static CompoundTag makeTag(BoundingBox box, int centerX, int centerZ, int region) {
			CompoundTag tag = new CompoundTag();
			tag.putIntArray("BB", new int[] {box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()});
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
			if (chunkPos.x != (centerX >> 4) || chunkPos.z != (centerZ >> 4)) {
				return;
			}
			int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
			BlockPos origin = new BlockPos(centerX, y, centerZ);
			FeaturePlaceContext<NoneFeatureConfiguration> ctx = new FeaturePlaceContext<>(
					Optional.empty(), level, chunkGenerator, random, origin, NoneFeatureConfiguration.INSTANCE);
			StructurePlacement.structureDriven = true;
			try {
				ModWorldgen.BIOME_LANDMARK_FEATURE.place(ctx);
			} finally {
				StructurePlacement.structureDriven = false;
			}
		}
	}
}
