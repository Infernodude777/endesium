package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class ModEntities {
	private ModEntities() {
	}

	public static <T extends Entity> EntityType<T> register(String path, EntityType<T> entityType) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, Endesium.id(path), entityType);
	}

	public static void register() {
		Endesium.LOGGER.info("Endesium entity registry ready");
	}
}
