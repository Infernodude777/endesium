package com.infernodude777.endesium.world.structure;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Registry for Endesium's two Structure types and their piece serializers.
 * The twenty structure entries (ten flagships, ten landmarks) are data-driven
 * JSON under {@code worldgen/structure/}, wired through two random_spread
 * structure sets - which is what makes {@code /locate structure} and
 * {@code /place structure} resolve them.
 */
public final class EndesiumStructureTypes {
	public static StructureType<EndesiumFlagshipStructure> FLAGSHIP;
	public static StructureType<EndesiumLandmarkStructure> LANDMARK;
	public static StructureType<EndStrongholdStructure> END_STRONGHOLD;

	/** Piece serializers (region-tagged on NBT). */
	public static StructurePieceType FLAGSHIP_PIECE;
	public static StructurePieceType LANDMARK_PIECE;
	public static StructurePieceType END_STRONGHOLD_PIECE;

	private EndesiumStructureTypes() {
	}

	public static void register() {
		FLAGSHIP = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, Endesium.id("flagship"),
				new EndesiumFlagshipStructure.Type());
		LANDMARK = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, Endesium.id("landmark"),
				new EndesiumLandmarkStructure.Type());
		FLAGSHIP_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, Endesium.id("flagship_piece"),
				EndesiumFlagshipStructure.Piece::load);
		LANDMARK_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, Endesium.id("landmark_piece"),
				EndesiumLandmarkStructure.Piece::load);
		// The Endesium stronghold replaces vanilla's: the data-driven override
		// at data/minecraft/worldgen/structure/stronghold.json points at it.
		END_STRONGHOLD = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, Endesium.id("end_stronghold"),
				new EndStrongholdStructure.Type());
		END_STRONGHOLD_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, Endesium.id("end_stronghold_piece"),
				EndStrongholdStructure.Piece::load);
		Endesium.LOGGER.info("Registered Endesium structure types");
	}
}
