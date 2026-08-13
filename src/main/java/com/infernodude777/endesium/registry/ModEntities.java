package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.VoidStalkerEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	public static final EntityType<VoidStalkerEntity> VOID_STALKER = register(
			"void_stalker",
			EntityType.Builder.of(VoidStalkerEntity::new, MobCategory.MONSTER)
					.sized(0.8F, 1.8F)
					.clientTrackingRange(8)
					.build(Endesium.id("void_stalker").toString())
	);

	private ModEntities() {
	}

	private static <T extends Entity> EntityType<T> register(String path, EntityType<T> entityType) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, Endesium.id(path), entityType);
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(VOID_STALKER, VoidStalkerEntity.createAttributes());
		Endesium.LOGGER.info("Registered Endesium entities");
	}
}
