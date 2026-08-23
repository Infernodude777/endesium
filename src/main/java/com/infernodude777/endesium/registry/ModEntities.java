package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.AshWraithEntity;
import com.infernodude777.endesium.entity.ChorusStalkerEntity;
import com.infernodude777.endesium.entity.CrystalBurrowerEntity;
import com.infernodude777.endesium.entity.DustCrawlerEntity;
import com.infernodude777.endesium.entity.LumenMothEntity;
import com.infernodude777.endesium.entity.MarshCrawlerEntity;
import com.infernodude777.endesium.entity.NullwalkerEntity;
import com.infernodude777.endesium.entity.ProductionVoidStalkerEntity;
import com.infernodude777.endesium.entity.VoidRayEntity;
import com.infernodude777.endesium.entity.VoidWispEntity;
import com.infernodude777.endesium.entity.CrownSentinelEntity;
import com.infernodude777.endesium.entity.EndWardenEntity;
import com.infernodude777.endesium.entity.EndGolemEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModEntities {
	public static final EntityType<ProductionVoidStalkerEntity> VOID_STALKER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("void_stalker"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, ProductionVoidStalkerEntity::new)
					.dimensions(EntityDimensions.fixed(0.9F, 2.4F)).trackRangeChunks(5).build()
	);

	public static final EntityType<DustCrawlerEntity> DUST_CRAWLER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("dust_crawler"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, DustCrawlerEntity::new)
					.dimensions(EntityDimensions.fixed(0.8F, 0.6F)).trackRangeChunks(5).build()
	);
	public static final EntityType<ChorusStalkerEntity> CHORUS_STALKER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("chorus_stalker"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, ChorusStalkerEntity::new)
					.dimensions(EntityDimensions.fixed(0.8F, 2.2F)).trackRangeChunks(5).build()
	);
	public static final EntityType<VoidRayEntity> VOID_RAY = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("void_ray"),
			FabricEntityTypeBuilder.create(MobCategory.CREATURE, VoidRayEntity::new)
					.dimensions(EntityDimensions.fixed(1.4F, 0.9F)).trackRangeChunks(5).build()
	);
	public static final EntityType<MarshCrawlerEntity> MARSH_CRAWLER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("marsh_crawler"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, MarshCrawlerEntity::new)
					.dimensions(EntityDimensions.fixed(0.9F, 0.8F)).trackRangeChunks(5).build()
	);
	public static final EntityType<LumenMothEntity> LUMEN_MOTH = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("lumen_moth"),
			FabricEntityTypeBuilder.create(MobCategory.CREATURE, LumenMothEntity::new)
					.dimensions(EntityDimensions.fixed(0.5F, 0.5F)).trackRangeChunks(5).build()
	);
	public static final EntityType<AshWraithEntity> ASH_WRAITH = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("ash_wraith"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, AshWraithEntity::new)
					.dimensions(EntityDimensions.fixed(0.7F, 1.8F)).trackRangeChunks(5).build()
	);
	public static final EntityType<CrystalBurrowerEntity> CRYSTAL_BURROWER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("crystal_burrower"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, CrystalBurrowerEntity::new)
					.dimensions(EntityDimensions.fixed(1.2F, 1.0F)).trackRangeChunks(5).build()
	);
	public static final EntityType<NullwalkerEntity> NULLWALKER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("nullwalker"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, NullwalkerEntity::new)
					.dimensions(EntityDimensions.fixed(0.7F, 2.4F)).trackRangeChunks(5).build()
	);
	public static final EntityType<VoidWispEntity> VOID_WISP = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("void_wisp"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, VoidWispEntity::new)
					.dimensions(EntityDimensions.fixed(0.6F, 0.6F)).trackRangeChunks(5).build()
	);
	public static final EntityType<CrownSentinelEntity> CROWN_SENTINEL = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("crown_sentinel"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, CrownSentinelEntity::new)
					.dimensions(EntityDimensions.fixed(1.0F, 2.6F)).trackRangeChunks(5).build()
	);
	// The regional miniboss and the major boss. MONSTER category keeps them
	// out of natural surface spawning entirely; they exist only where placed.
	public static final EntityType<EndWardenEntity> END_WARDEN = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("end_warden"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, EndWardenEntity::new)
					.dimensions(EntityDimensions.fixed(1.2F, 2.9F)).trackRangeChunks(8).build()
	);
	public static final EntityType<EndGolemEntity> END_GOLEM = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Endesium.id("end_golem"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, EndGolemEntity::new)
					.dimensions(EntityDimensions.fixed(2.2F, 4.5F)).trackRangeChunks(10).build()
	);

	private ModEntities() { }

	public static void register() {
		SpawnPlacements.register(
				VOID_STALKER,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				DUST_CRAWLER,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				CHORUS_STALKER,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				MARSH_CRAWLER,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				ASH_WRAITH,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				CRYSTAL_BURROWER,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				NULLWALKER,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				CROWN_SENTINEL,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				END_WARDEN,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				END_GOLEM,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules
		);
		SpawnPlacements.register(
				VOID_WISP,
				SpawnPlacementTypes.NO_RESTRICTIONS,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, level, spawnType, pos, random) -> level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
		);
		SpawnPlacements.register(
				VOID_RAY,
				SpawnPlacementTypes.NO_RESTRICTIONS,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, level, spawnType, pos, random) -> level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
		);
		SpawnPlacements.register(
				LUMEN_MOTH,
				SpawnPlacementTypes.NO_RESTRICTIONS,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, level, spawnType, pos, random) -> level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
		);

		Endesium.LOGGER.info("Registered Endesium ecology entities and spawn rules");
	}
}
