package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.block.ResonantMechanismBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
	public static final BlockEntityType<ResonantMechanismBlockEntity> RESONANT_MECHANISM = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			Endesium.id("resonant_mechanism"),
			FabricBlockEntityTypeBuilder.create(ResonantMechanismBlockEntity::new, ModBlocks.RESONANT_MECHANISM).build()
	);

	private ModBlockEntities() {
	}

	public static void register() {
		Endesium.LOGGER.info("Registered Endesium production block entities");
	}
}
