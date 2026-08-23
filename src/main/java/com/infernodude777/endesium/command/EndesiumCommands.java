package com.infernodude777.endesium.command;

import com.infernodude777.endesium.state.PostDragonEvents;
import com.infernodude777.endesium.state.PostDragonState;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Development-only Endesium commands, all gated to permission level 2. They
 * exist so the post-Dragon state can be exercised headlessly and in fresh
 * worlds without killing the Dragon; they are documented in
 * {@code docs/COMMANDS.md} and never become part of normal progression.
 *
 * <ul>
 *   <li>{@code /endesium dragonstate get} — read the persistent state.</li>
 *   <li>{@code /endesium dragonstate set <true|false>} — activate (fires the
 *       real transformation event once) or reset the state for testing.</li>
 * </ul>
 */
public final class EndesiumCommands {
	private EndesiumCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("endesium")
						.then(Commands.literal("dragonstate")
								.requires(source -> source.hasPermission(2))
								.then(Commands.literal("get")
										.executes(EndesiumCommands::getDragonState))
								.then(Commands.literal("set")
										.then(Commands.argument("active", BoolArgumentType.bool())
												.executes(ctx -> setDragonState(ctx,
														BoolArgumentType.getBool(ctx, "active"))))))
						// Locate is open to every player: Endesium structures are
						// features, not vanilla Structures, so vanilla /locate can
						// never find them - this is the supported route.
						.then(Commands.literal("locate")
								.then(Commands.literal("biome")
										.then(Commands.argument("biome", StringArgumentType.string())
												.executes(ctx -> locateBiome(ctx,
														StringArgumentType.getString(ctx, "biome")))))
								.then(Commands.literal("structure")
										.then(Commands.argument("structure", StringArgumentType.string())
												.suggests((context, builder) -> {
													for (String name : FLAGSHIP_REGIONS.keySet()) builder.suggest(name);
													for (String name : LANDMARK_NAMES) builder.suggest(name);
													return builder.buildFuture();
												})
												.executes(ctx -> locateStructure(ctx,
														StringArgumentType.getString(ctx, "structure"))))))));
	}

	private static int locateBiome(CommandContext<CommandSourceStack> ctx, String biomeId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerLevel level = ctx.getSource().getLevel();
		ResourceLocation id;
		try {
			id = ResourceLocation.parse(biomeId);
		} catch (Exception e) {
			ctx.getSource().sendFailure(Component.literal("Invalid biome id: " + biomeId));
			return 0;
		}
		ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, id);
		var holderOpt = ctx.getSource().getServer().registryAccess().lookupOrThrow(Registries.BIOME).get(key);
		if (holderOpt.isEmpty()) {
			// Try tag lookup
			var tagOpt = ctx.getSource().getServer().registryAccess().lookupOrThrow(Registries.BIOME).get(net.minecraft.tags.TagKey.create(Registries.BIOME, id));
			if (tagOpt.isPresent()) {
				var tag = tagOpt.get();
				BlockPos origin = BlockPos.containing(ctx.getSource().getPosition());
				java.util.function.Predicate<net.minecraft.core.Holder<Biome>> pred = h -> tag.contains(h);
				var found = level.findClosestBiome3d(pred, origin, 6400, 32, 64);
				if (found != null) {
					BlockPos pos = found.getFirst();
					String name = found.getSecond().unwrapKey().map(k -> k.location().toString()).orElse(id.toString());
					double dist = Math.sqrt(ctx.getSource().getPosition().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()));
					ctx.getSource().sendSuccess(() -> Component.literal("Nearest " + name + " is at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] (" + (int) dist + " blocks)"), false);
					return 1;
				}
			}
			ctx.getSource().sendFailure(Component.literal("Biome not found: " + biomeId));
			return 0;
		}
		var holder = holderOpt.get();
		BlockPos origin = BlockPos.containing(ctx.getSource().getPosition());
		java.util.function.Predicate<net.minecraft.core.Holder<Biome>> pred = h -> h.is(key);
		var found = level.findClosestBiome3d(pred, origin, 6400, 32, 64);
		if (found != null) {
			BlockPos pos = found.getFirst();
			String name = found.getSecond().unwrapKey().map(k -> k.location().toString()).orElse(id.toString());
			double dist = Math.sqrt(ctx.getSource().getPosition().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()));
			ctx.getSource().sendSuccess(() -> Component.literal("Nearest " + name + " is at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] (" + (int) dist + " blocks)"), false);
			return 1;
		}
		ctx.getSource().sendFailure(Component.literal("Biome not found within search radius."));
		return 0;
	}

	private static final Map<String, Integer> FLAGSHIP_REGIONS = Map.ofEntries(
			Map.entry("dust_cathedral", com.infernodude777.endesium.world.EndesiumRegions.END_WASTES),
			Map.entry("elderwood_sanctum", com.infernodude777.endesium.world.EndesiumRegions.CHORUS_WILDS),
			Map.entry("skyrend_keep", com.infernodude777.endesium.world.EndesiumRegions.SHATTERED_HIGHLANDS),
			Map.entry("drowned_cathedral", com.infernodude777.endesium.world.EndesiumRegions.VOID_MARSHES),
			Map.entry("lumen_cathedral", com.infernodude777.endesium.world.EndesiumRegions.LUMINOUS_GROVES),
			Map.entry("great_caldera", com.infernodude777.endesium.world.EndesiumRegions.ASHEN_EXPANSE),
			Map.entry("volcano", com.infernodude777.endesium.world.EndesiumRegions.ASHEN_EXPANSE),
			Map.entry("sunken_geode", com.infernodude777.endesium.world.EndesiumRegions.CRYSTAL_BARRENS),
			Map.entry("void_spire", com.infernodude777.endesium.world.EndesiumRegions.VOID_SKIRTS),
			Map.entry("crown_observatory", com.infernodude777.endesium.world.EndesiumRegions.VOID_CROWN),
			Map.entry("null_archive", com.infernodude777.endesium.world.EndesiumRegions.UMBRAL_REACH));

	private static final java.util.Set<String> LANDMARK_NAMES = java.util.Set.of(
			"dune_fossil_arch", "hollow_stump", "windvane_watchtower", "mire_bell_cairn",
			"lightwell_gazebo", "ember_shrine", "shard_spire_cluster", "anchor_ruin",
			"needle_circle", "null_obelisk");

	/**
	 * Grid-searches the deterministic scatter lattice for the nearest anchor
	 * of either tier. Flagships use the 24-chunk flagship grid; landmarks the
	 * 16-chunk landmark grid.
	 */
	private static int locateLattice(CommandContext<CommandSourceStack> ctx, String name,
			int region, boolean flagship) {
		ServerLevel level = ctx.getSource().getLevel();
		long seed = com.infernodude777.endesium.world.EndesiumWorldgenSeeds.get();
		BlockPos origin = BlockPos.containing(ctx.getSource().getPosition());
		int grid = flagship
				? com.infernodude777.endesium.world.BiomeStructureFeature.SPACING_GRID
				: com.infernodude777.endesium.world.RegionLandmarkFeature.SPACING_GRID;
		int playerCellX = Math.floorDiv(origin.getX() >> 4, grid);
		int playerCellZ = Math.floorDiv(origin.getZ() >> 4, grid);

		long bestDistSq = Long.MAX_VALUE;
		int[] bestChunk = null;
		for (int ring = 0; bestChunk == null || ring <= 6; ring++) {
			if (ring > 12) break;
			for (int dx = -ring; dx <= ring; dx++) {
				for (int dz = -ring; dz <= ring; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
					int[] chunk = flagship
							? com.infernodude777.endesium.world.BiomeStructureFeature
									.flagshipChunk(seed, region, playerCellX + dx, playerCellZ + dz)
							: com.infernodude777.endesium.world.RegionLandmarkFeature
									.landmarkChunk(seed, region, playerCellX + dx, playerCellZ + dz);
					int wx = chunk[0] * 16 + 8;
					int wz = chunk[1] * 16 + 8;
					long distSq = (long) (wx - origin.getX()) * (wx - origin.getX())
							+ (long) (wz - origin.getZ()) * (wz - origin.getZ());
					if (distSq < bestDistSq) {
						bestDistSq = distSq;
						bestChunk = chunk;
					}
				}
			}
		}
		if (bestChunk == null) {
			ctx.getSource().sendFailure(Component.literal("No anchor found nearby."));
			return 0;
		}
		int wx = bestChunk[0] * 16 + 8;
		int wz = bestChunk[1] * 16 + 8;
		int dist = (int) Math.sqrt(bestDistSq);
		String label = name.toLowerCase(Locale.ROOT).replace('_', ' ');
		ctx.getSource().sendSuccess(() -> Component.literal(
				"Nearest " + label + " anchor is near [" + wx + ", ~70, " + wz + "] (" + dist
						+ " blocks). The structure generates when its chunk loads."),
				false);
		return 1;
	}

	private static int locateStructure(CommandContext<CommandSourceStack> ctx, String structureId) {
		// Endesium flagships are features on a deterministic lattice, not
		// vanilla Structures — match them FIRST, tolerating a namespace
		// prefix ("endesium:void_spire") and spaces ("Void Spire").
		String bare = structureId.toLowerCase(Locale.ROOT).trim();
		if (bare.startsWith("endesium:")) {
			bare = bare.substring("endesium:".length());
		}
		if (bare.contains(" ")) {
			bare = bare.replace(' ', '_');
		}
		if (FLAGSHIP_REGIONS.containsKey(bare)) {
			return locateLattice(ctx, bare, FLAGSHIP_REGIONS.get(bare), true);
		}
		if (LANDMARK_NAMES.contains(bare)) {
			int region = landmarkRegionFor(bare);
			return locateLattice(ctx, bare, region, false);
		}

		ServerLevel level = ctx.getSource().getLevel();
		ResourceLocation id;
		try {
			id = ResourceLocation.parse(structureId);
		} catch (Exception e) {
			ctx.getSource().sendFailure(Component.literal("Invalid structure id: " + structureId));
			return 0;
		}
		ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, id);
		var holderOpt = ctx.getSource().getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE).get(key);
		if (holderOpt.isEmpty()) {
			var tagOpt = ctx.getSource().getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE).get(net.minecraft.tags.TagKey.create(Registries.STRUCTURE, id));
			if (tagOpt.isPresent()) {
				var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, tagOpt.get(), BlockPos.containing(ctx.getSource().getPosition()), 100, false);
				if (found != null) {
					BlockPos pos = new BlockPos(found.getFirst().getX(), found.getFirst().getY(), found.getFirst().getZ());
					String name = found.getSecond().unwrapKey().map(k -> k.location().toString()).orElse(id.toString());
					double dist = Math.sqrt(ctx.getSource().getPosition().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()));
					ctx.getSource().sendSuccess(() -> Component.literal("Nearest " + name + " is at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] (" + (int) dist + " blocks)"), false);
					return 1;
				}
			}
			ctx.getSource().sendFailure(Component.literal(
					"Structure not found: " + structureId + ". Endesium flagships: dust_cathedral, elderwood_sanctum, "
							+ "skyrend_keep, drowned_cathedral, lumen_cathedral, great_caldera, sunken_geode, void_spire, "
							+ "crown_observatory, null_archive."));
			return 0;
		}
		var holder = holderOpt.get();
		var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, net.minecraft.core.HolderSet.direct(holder), BlockPos.containing(ctx.getSource().getPosition()), 100, false);
		if (found != null) {
			BlockPos pos = new BlockPos(found.getFirst().getX(), found.getFirst().getY(), found.getFirst().getZ());
			String name = found.getSecond().unwrapKey().map(k -> k.location().toString()).orElse(id.toString());
			double dist = Math.sqrt(ctx.getSource().getPosition().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()));
			ctx.getSource().sendSuccess(() -> Component.literal("Nearest " + name + " is at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] (" + (int) dist + " blocks)"), false);
			return 1;
		}
		ctx.getSource().sendFailure(Component.literal(
				"Structure not found within search radius. Endesium flagships: dust_cathedral, elderwood_sanctum, "
						+ "skyrend_keep, drowned_cathedral, lumen_cathedral, great_caldera, sunken_geode, void_spire, "
						+ "crown_observatory, null_archive."));
		return 0;
	}

	/** Landmarks are biome-native, so each name maps to exactly one region. */
	private static int landmarkRegionFor(String name) {
		return switch (name) {
			case "dune_fossil_arch" -> com.infernodude777.endesium.world.EndesiumRegions.END_WASTES;
			case "hollow_stump" -> com.infernodude777.endesium.world.EndesiumRegions.CHORUS_WILDS;
			case "windvane_watchtower" -> com.infernodude777.endesium.world.EndesiumRegions.SHATTERED_HIGHLANDS;
			case "mire_bell_cairn" -> com.infernodude777.endesium.world.EndesiumRegions.VOID_MARSHES;
			case "lightwell_gazebo" -> com.infernodude777.endesium.world.EndesiumRegions.LUMINOUS_GROVES;
			case "ember_shrine" -> com.infernodude777.endesium.world.EndesiumRegions.ASHEN_EXPANSE;
			case "shard_spire_cluster" -> com.infernodude777.endesium.world.EndesiumRegions.CRYSTAL_BARRENS;
			case "anchor_ruin" -> com.infernodude777.endesium.world.EndesiumRegions.VOID_SKIRTS;
			case "needle_circle" -> com.infernodude777.endesium.world.EndesiumRegions.VOID_CROWN;
			default -> com.infernodude777.endesium.world.EndesiumRegions.UMBRAL_REACH; // null_obelisk
		};
	}

	private static int getDragonState(CommandContext<CommandSourceStack> ctx) {
		PostDragonState state = PostDragonState.get(ctx.getSource().getServer());
		ctx.getSource().sendSuccess(() -> Component.literal(
				"Endesium post-Dragon state: transformation=" + state.isTransformationActive()
						+ ", dragon defeated=" + state.isDragonDefeated()
						+ ", version=" + state.transformationVersion()), true);
		return 1;
	}

	private static int setDragonState(CommandContext<CommandSourceStack> ctx, boolean active) {
		PostDragonState state = PostDragonState.get(ctx.getSource().getServer());
		if (active) {
			if (state.markDragonDefeated()) {
				ServerLevel end = ctx.getSource().getServer().getLevel(Level.END);
				if (end != null) {
					PostDragonEvents.fireTransformation(end, new BlockPos(0, 70, 0));
				}
				ctx.getSource().sendSuccess(() -> Component.literal(
						"Endesium post-Dragon transformation activated (development command)."), true);
			} else {
				ctx.getSource().sendSuccess(() -> Component.literal(
						"Endesium post-Dragon transformation is already active."), true);
			}
		} else {
			state.resetForTesting();
			ctx.getSource().sendSuccess(() -> Component.literal(
					"Endesium post-Dragon state reset (development command; not part of normal gameplay)."), true);
		}
		return 1;
	}
}
