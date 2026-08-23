package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.block.EndPlantBlock;
import com.infernodude777.endesium.block.InscribedSlateBlock;
import com.infernodude777.endesium.block.ResonantMechanismBlock;
import com.infernodude777.endesium.block.VoidGlassBlock;
import com.infernodude777.endesium.block.VoidOreBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class ModBlocks {
	// --- Core / progression ---
	public static final Block RESONANT_SLATE = register("resonant_slate", new Block(BlockBehaviour.Properties.of().strength(2.4F).sound(SoundType.STONE)));
	public static final Block END_GRAY = register("end_gray", new Block(BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.STONE)));
	public static final Block DORMANT_RESONANT_CRYSTAL = register("dormant_resonant_crystal", new Block(BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.AMETHYST)));
	public static final Block RESONANT_MECHANISM = register("resonant_mechanism", new ResonantMechanismBlock(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL)));

	// --- Chorus Wilds flora (legacy) ---
	public static final Block CHORUS_SPROUT = register("chorus_sprout", plant());
	public static final Block WILD_TENDRIL = register("wild_tendril", plant());
	public static final Block RESONANT_BLOOM = register("resonant_bloom", plant(2));

	// --- Environmental storytelling ---
	public static final Block INSCRIBED_SLATE = register("inscribed_slate", new InscribedSlateBlock(BlockBehaviour.Properties.of().strength(2.2F).sound(SoundType.STONE)));
	public static final Block RESONANT_PILLAR = register("resonant_pillar", new Block(BlockBehaviour.Properties.of().strength(3.5F).sound(SoundType.STONE).lightLevel(state -> 3)));
	public static final Block CRACKED_SPIRE_STONE = register("cracked_spire_stone", new Block(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.STONE)));

	// --- End Wastes geology / growth ---
	public static final Block WASTES_STONE = register("wastes_stone", new Block(BlockBehaviour.Properties.of().strength(2.6F).sound(SoundType.STONE)));
	public static final Block WASTES_GRAVEL = register("wastes_gravel", new Block(BlockBehaviour.Properties.of().strength(0.7F).sound(SoundType.GRAVEL)));
	public static final Block DUST_REED = register("dust_reed", plant());
	public static final Block VOID_GRASS = register("void_grass", plant());

	// --- Chorus Wilds geology / growth ---
	public static final Block ELDER_CHORUS_WOOD = register("elder_chorus_wood", new Block(BlockBehaviour.Properties.of().strength(2.2F).sound(SoundType.WOOD)));
	public static final Block ELDER_CHORUS_BARK = register("elder_chorus_bark", new Block(BlockBehaviour.Properties.of().strength(2.2F).sound(SoundType.WOOD)));
	public static final Block CHORUS_ROOT = register("chorus_root", new Block(BlockBehaviour.Properties.of().strength(1.6F).sound(SoundType.WOOD)));
	public static final Block CHORUS_MOSS = register("chorus_moss", new Block(BlockBehaviour.Properties.of().strength(0.9F).sound(SoundType.MOSS)));
	public static final Block HOLLOW_CHORUS_WOOD = register("hollow_chorus_wood", new Block(BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD)));

	// --- Shattered Highlands geology ---
	public static final Block HIGHLAND_STONE = register("highland_stone", new Block(BlockBehaviour.Properties.of().strength(3.4F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
	public static final Block HIGHLAND_SLATE = register("highland_slate", new Block(BlockBehaviour.Properties.of().strength(3.2F).sound(SoundType.STONE).requiresCorrectToolForDrops()));

	// --- Void Marshes geology / growth ---
	public static final Block VOID_MARSH_SOIL = register("void_marsh_soil", new Block(BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.MUD)));
	public static final Block VOID_REED = register("void_reed", plant());
	public static final Block MARSH_MOSS = register("marsh_moss", new Block(BlockBehaviour.Properties.of().strength(0.7F).sound(SoundType.MOSS)));

	// --- Luminous Groves geology / growth ---
	public static final Block LUMEN_STONE = register("lumen_stone", new Block(BlockBehaviour.Properties.of().strength(2.8F).sound(SoundType.STONE).lightLevel(state -> 7)));
	public static final Block LUMEN_MOSS = register("lumen_moss", plant(6));
	public static final Block LUMEN_BLOOM = register("lumen_bloom", plant(9));

	// --- Ashen Expanse geology ---
	public static final Block ASH_STONE = register("ash_stone", new Block(BlockBehaviour.Properties.of().strength(2.4F).sound(SoundType.STONE)));
	public static final Block ASHEN_SOIL = register("ashen_soil", new Block(BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.SAND)));

	// --- Crystal Barrens geology ---
	public static final Block CRYSTAL_SHARD_BLOCK = register("crystal_shard_block", new Block(BlockBehaviour.Properties.of().strength(1.8F).sound(SoundType.AMETHYST)));
	public static final Block CRYSTAL_CLUSTER = register("crystal_cluster", plant(6));
	public static final Block DARK_CRYSTAL_BLOCK = register("dark_crystal_block", new Block(BlockBehaviour.Properties.of().strength(1.9F).sound(SoundType.AMETHYST).lightLevel(state -> 2)));
	public static final Block PALE_CRYSTAL_BLOCK = register("pale_crystal_block", new Block(BlockBehaviour.Properties.of().strength(1.7F).sound(SoundType.AMETHYST).lightLevel(state -> 3)));

	// --- Deep / common End geology ---
	public static final Block RESONANT_BASALT = register("resonant_basalt", new Block(BlockBehaviour.Properties.of().strength(3.2F).sound(SoundType.BASALT).requiresCorrectToolForDrops()));
	public static final Block END_CLAY = register("end_clay", new Block(BlockBehaviour.Properties.of().strength(0.7F).sound(SoundType.GRAVEL)));
	public static final Block VOIDSTONE = register("voidstone", new Block(BlockBehaviour.Properties.of().strength(3.8F).sound(SoundType.STONE).requiresCorrectToolForDrops()));

	// --- Void Skirts geology / growth / structures ---
	public static final Block VOID_SLATE = register("void_slate", new Block(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
	public static final Block VOID_GRAVEL = register("void_gravel", new Block(BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.GRAVEL)));
	public static final Block VOID_SOIL = register("void_soil", new Block(BlockBehaviour.Properties.of().strength(0.9F).sound(SoundType.MUD)));
	public static final Block VOID_GLASS = register("void_glass", new VoidGlassBlock(BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.GLASS).noOcclusion()));
	public static final Block VOID_BRICK = register("void_brick", new Block(BlockBehaviour.Properties.of().strength(3.2F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
	public static final Block VOID_BRICK_SLAB = register("void_brick_slab", new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.of().strength(3.2F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
	public static final Block VOID_BRICK_STAIRS = register("void_brick_stairs",
			new net.minecraft.world.level.block.StairBlock(ModBlocks.VOID_BRICK.defaultBlockState(), BlockBehaviour.Properties.of().strength(3.2F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
	public static final Block VOID_BRICK_WALL = register("void_brick_wall",
			new net.minecraft.world.level.block.WallBlock(BlockBehaviour.Properties.of().strength(3.2F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
	public static final Block VOID_LAMP = register("void_lamp", new Block(BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.AMETHYST).lightLevel(state -> 14)));
	public static final Block VOID_CRYSTAL = register("void_crystal", plant(5));
	public static final Block UMBRAL_GRASS = register("umbral_grass", plant());
	public static final Block VOID_FERN = register("void_fern", plant());
	public static final Block VOID_WEAVE = register("void_weave", new Block(BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.WOOL)));
	public static final Block VOID_SPIRE = register("void_spire", new Block(BlockBehaviour.Properties.of().strength(3.4F).sound(SoundType.STONE).requiresCorrectToolForDrops().lightLevel(state -> 4)));
	public static final Block UMBRAL_STONE = register("umbral_stone", new Block(BlockBehaviour.Properties.of().strength(3.6F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
	public static final Block VOID_ORE = register("void_ore", new VoidOreBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));

	// --- Expanded landmark showcase blocks ---
	public static final Block HIGHLAND_LENSSTONE = register("highland_lensstone",
			new Block(BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.AMETHYST).lightLevel(state -> 5)));
	public static final Block WINDSCAR_BRACKET = register("windscar_bracket",
			new Block(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
	public static final Block TIDE_IRON = register("tide_iron",
			new Block(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
	public static final Block MIREGLASS = register("mireglass",
			new VoidGlassBlock(BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.GLASS).noOcclusion()));
	public static final Block LUMEN_GRAFT_BLOCK = register("lumen_graft_block",
			new Block(BlockBehaviour.Properties.of().strength(1.4F).sound(SoundType.WOOD).lightLevel(state -> 8)));
	public static final Block PRISM_CANOPY_BLOCK = register("prism_canopy_block",
			new Block(BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.AMETHYST).lightLevel(state -> 5)));
	public static final Block CROWN_NEEDLE_BLOCK = register("crown_needle_block",
			new Block(BlockBehaviour.Properties.of().strength(2.6F).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(state -> 4)));
	public static final Block CROWN_SEAL_BLOCK = register("crown_seal_block",
			new Block(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().lightLevel(state -> 2)));
	public static final Block NULL_ARCHIVE_FRAME = register("null_archive_frame",
			new Block(BlockBehaviour.Properties.of().strength(3.4F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
	public static final Block THRESHOLD_CORE_BLOCK = register("threshold_core_block",
			new Block(BlockBehaviour.Properties.of().strength(2.8F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops().lightLevel(state -> 4)));

	// --- Ashen Expanse: lava-walk crust ---
	public static final Block ASHEN_CRUST = register("ashen_crust",
			new com.infernodude777.endesium.block.AshenCrustBlock(BlockBehaviour.Properties.of().strength(0.4F).sound(SoundType.STONE).noOcclusion()));

	private ModBlocks() { }

	private static Block plant() {
		return plant(0);
	}

	private static Block plant(int lightLevel) {
		BlockBehaviour.Properties props = BlockBehaviour.Properties.of().instabreak().noCollission().sound(SoundType.GRASS);
		if (lightLevel > 0) {
			props = props.lightLevel(state -> lightLevel);
		}
		return new EndPlantBlock(props);
	}

	private static Block register(String id, Block block) {
		return Registry.register(BuiltInRegistries.BLOCK, Endesium.id(id), block);
	}

	/** Ground blocks Endesium plants may root on, including the new biome soils. */
	public static boolean isPlantGround(BlockState state) {
		return state.is(Blocks.END_STONE)
				|| state.is(Blocks.END_STONE_BRICKS)
				|| state.is(Blocks.CHORUS_PLANT)
				|| state.is(Blocks.CHORUS_FLOWER)
				|| state.is(END_GRAY)
				|| state.is(RESONANT_SLATE)
				|| state.is(WASTES_STONE)
				|| state.is(WASTES_GRAVEL)
				|| state.is(CHORUS_MOSS)
				|| state.is(CHORUS_ROOT)
				|| state.is(HIGHLAND_STONE)
				|| state.is(HIGHLAND_SLATE)
				|| state.is(VOID_MARSH_SOIL)
				|| state.is(MARSH_MOSS)
				|| state.is(LUMEN_STONE)
				|| state.is(ASH_STONE)
				|| state.is(ASHEN_SOIL)
				|| state.is(CRYSTAL_SHARD_BLOCK)
				|| state.is(DARK_CRYSTAL_BLOCK)
				|| state.is(PALE_CRYSTAL_BLOCK)
				|| state.is(RESONANT_BASALT)
				|| state.is(END_CLAY)
				|| state.is(VOIDSTONE)
				|| state.is(VOID_SLATE)
				|| state.is(VOID_GRAVEL)
				|| state.is(VOID_SOIL)
				|| state.is(VOID_BRICK)
				|| state.is(UMBRAL_STONE)
				|| state.is(VOID_ORE)
				|| state.is(HIGHLAND_LENSSTONE)
				|| state.is(WINDSCAR_BRACKET)
				|| state.is(TIDE_IRON)
				|| state.is(LUMEN_GRAFT_BLOCK)
				|| state.is(PRISM_CANOPY_BLOCK)
				|| state.is(CROWN_NEEDLE_BLOCK)
				|| state.is(CROWN_SEAL_BLOCK)
				|| state.is(NULL_ARCHIVE_FRAME)
				|| state.is(THRESHOLD_CORE_BLOCK);
	}

	public static void register() {
		Endesium.LOGGER.info("Registered Endesium production blocks");
	}
}
