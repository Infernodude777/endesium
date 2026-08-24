package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ModWorldgen {
	public static final Feature<NoneFeatureConfiguration> END_WASTES_FEATURE = Registry.register(BuiltInRegistries.FEATURE, Endesium.id("end_wastes_surface"), new EndWastesFeature());
	public static final Feature<NoneFeatureConfiguration> CHORUS_WILDS_TERRAIN_FEATURE = Registry.register(BuiltInRegistries.FEATURE, Endesium.id("chorus_wilds_terrain"), new ChorusWildsTerrainFeature());
	public static final Feature<NoneFeatureConfiguration> CHORUS_WILDS_VEGETATION_FEATURE = Registry.register(BuiltInRegistries.FEATURE, Endesium.id("chorus_wilds_vegetation"), new ChorusWildsVegetationFeature());
	public static final Feature<NoneFeatureConfiguration> DRAGON_ARENA_FEATURE = Registry.register(BuiltInRegistries.FEATURE, Endesium.id("dragon_arena"), new DragonArenaFeature());

	// The ecology overhaul: one terrain, one vegetation, and one landmark
	// feature dispatch on the biome at placement time. The landmark feature is
	// BiomeStructureFeature: exactly one grand flagship per region.
	public static final Feature<NoneFeatureConfiguration> BIOME_TERRAIN_FEATURE = Registry.register(BuiltInRegistries.FEATURE, Endesium.id("biome_terrain"), new BiomeTerrainFeature());
	public static final Feature<NoneFeatureConfiguration> BIOME_VEGETATION_FEATURE = Registry.register(BuiltInRegistries.FEATURE, Endesium.id("biome_vegetation"), new BiomeVegetationFeature());
	public static final Feature<NoneFeatureConfiguration> BIOME_STRUCTURE_FEATURE = Registry.register(BuiltInRegistries.FEATURE, Endesium.id("biome_structure"), new BiomeStructureFeature());
	// The landmark tier: medium hand-authored builds on a ~256-block grid,
	// an order of magnitude more common than flagships.
	public static final Feature<NoneFeatureConfiguration> BIOME_LANDMARK_FEATURE = Registry.register(BuiltInRegistries.FEATURE, Endesium.id("biome_landmark"), new RegionLandmarkFeature());

	private ModWorldgen() {
	}

	public static void register() {
		com.infernodude777.endesium.world.structure.EndesiumStructureTypes.register();
		// Resolve the Endesium biome holders from the server registry before any
		// level is created, so the biome-source mixin can rely on them even for
		// TheEndBiomeSource instances built through the dimension codec path.
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			var biomes = server.registryAccess().lookupOrThrow(Registries.BIOME);
			net.minecraft.resources.ResourceKey<Biome>[] keys = new net.minecraft.resources.ResourceKey[] {
					EndesiumBiomes.END_WASTES,
					EndesiumBiomes.SHATTERED_HIGHLANDS,
					EndesiumBiomes.VOID_MARSHES,
					EndesiumBiomes.CHORUS_WILDS,
					EndesiumBiomes.LUMINOUS_GROVES,
					EndesiumBiomes.ASHEN_EXPANSE,
					EndesiumBiomes.CRYSTAL_BARRENS,
					EndesiumBiomes.VOID_SKIRTS,
					EndesiumBiomes.VOID_CROWN,
					EndesiumBiomes.UMBRAL_REACH,
			};
			Holder<Biome>[] resolved = new Holder[EndesiumRegions.COUNT];
			boolean complete = true;
			for (int i = 0; i < EndesiumRegions.COUNT; i++) {
				var found = biomes.get(keys[i]);
				if (found.isPresent()) {
					resolved[i] = found.get();
				} else {
					complete = false;
					break;
				}
			}
			if (complete) {
				EndesiumBiomeHolders.set(resolved);
			} else {
				Endesium.LOGGER.error("Endesium biome holders could not be resolved; /locate biome and outer-End selection will fall back to vanilla");
			}
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			EndesiumBiomeHolders.clear();
			StructurePlacement.clearBoxes();
			// A stale captured seed must never leak into the next world opened
			// in the same JVM; the next RandomState creation re-captures it.
			EndesiumWorldgenSeeds.clear();
		});
		Endesium.LOGGER.info("Registered Endesium biome, ruin, landmark, and ecology worldgen features");
	}
}
