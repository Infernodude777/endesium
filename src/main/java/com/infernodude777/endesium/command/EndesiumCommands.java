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
										// Mutating world progression is dev-only: it must
										// be enabled explicitly with
										// -Dendesium.devcommands=true, never available
										// on a production server by accident.
										.requires(source -> DEV_COMMANDS_ENABLED)
										.then(Commands.argument("active", BoolArgumentType.bool())
												.executes(ctx -> setDragonState(ctx,
														BoolArgumentType.getBool(ctx, "active"))))))
						// Locate is open to every player. Flagships and landmarks are
						// registered vanilla Structures now, so this command and
						// vanilla /locate both resolve them through the registry.
						.then(Commands.literal("locate")
								.then(Commands.literal("biome")
										.then(Commands.argument("biome", StringArgumentType.string())
												.executes(ctx -> locateBiome(ctx,
														StringArgumentType.getString(ctx, "biome")))))
								.then(Commands.literal("structure")
										.then(Commands.argument("structure", StringArgumentType.string())
												.suggests((context, builder) -> {
													for (String name : STRUCTURE_NAMES) builder.suggest(name);
													return builder.buildFuture();
												})
												.executes(ctx -> locateStructure(ctx,
														StringArgumentType.getString(ctx, "structure"))))))));
	}

	/** True only when the JVM was started with -Dendesium.devcommands=true. */
	private static final boolean DEV_COMMANDS_ENABLED =
			Boolean.getBoolean("endesium.devcommands");

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

	/** Every registered Endesium structure id, for suggestions. */
	private static final java.util.Set<String> STRUCTURE_NAMES = java.util.Set.of(
			// Flagships
			"dust_cathedral", "elderwood_sanctum", "skyrend_keep", "drowned_cathedral",
			"lumen_cathedral", "great_caldera", "sunken_geode", "void_spire",
			"crown_observatory", "null_archive",
			// Landmarks
			"dune_fossil_arch", "hollow_stump", "windvane_watchtower", "mire_bell_cairn",
			"lightwell_gazebo", "ember_shrine", "shard_spire_cluster", "anchor_ruin",
			"needle_circle", "null_obelisk");

	private static int locateStructure(CommandContext<CommandSourceStack> ctx, String structureId) {
		// All Endesium anchors are vanilla Structures now; resolve through the
		// registry, tolerating a namespace prefix and spaces in the name.
		String bare = structureId.toLowerCase(Locale.ROOT).trim();
		if (bare.startsWith("endesium:")) {
			bare = bare.substring("endesium:".length());
		}
		if (bare.contains(" ")) {
			bare = bare.replace(' ', '_');
		}

		ServerLevel level = ctx.getSource().getLevel();
		ResourceLocation id;
		try {
			id = ResourceLocation.parse(
					java.util.Set.copyOf(STRUCTURE_NAMES).contains(bare) ? "endesium:" + bare : structureId);
		} catch (Exception e) {
			ctx.getSource().sendFailure(Component.literal("Invalid structure id: " + structureId));
			return 0;
		}
		ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, id);
		var holderOpt = ctx.getSource().getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE).get(key);
		if (holderOpt.isEmpty()) {
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
			ctx.getSource().sendSuccess(() -> Component.literal("Nearest " + name + " is at [" + pos.getX() + ", ~70, " + pos.getZ() + "] (" + (int) dist + " blocks)"), false);
			return 1;
		}
		ctx.getSource().sendFailure(Component.literal(
				"Structure not found within search radius. Endesium flagships: dust_cathedral, elderwood_sanctum, "
						+ "skyrend_keep, drowned_cathedral, lumen_cathedral, great_caldera, sunken_geode, void_spire, "
						+ "crown_observatory, null_archive."));
		return 0;
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
