package com.infernodude777.endesium;

import com.infernodude777.endesium.command.EndesiumCommands;
import com.infernodude777.endesium.dragon.ResonantWingsPassives;
import com.infernodude777.endesium.dragon.SonicBoomHandler;
import com.infernodude777.endesium.item.VoidBlackHoleManager;
import com.infernodude777.endesium.item.VoidEquipmentAbilities;
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
import com.infernodude777.endesium.net.EndesiumPackets;
import com.infernodude777.endesium.net.EndesiumPackets.SonicBoomPayload;
import com.infernodude777.endesium.particle.ModParticles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.infernodude777.endesium.registry.ModBlockEntities;
import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.registry.ModEntities;
import com.infernodude777.endesium.registry.ModItemGroups;
import com.infernodude777.endesium.registry.ModItems;
import com.infernodude777.endesium.registry.ModEndgear;
import com.infernodude777.endesium.registry.ModSounds;
import com.infernodude777.endesium.resonance.ResonanceSystem;
import com.infernodude777.endesium.world.ModWorldgen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endesium implements ModInitializer {
	public static final String MOD_ID = "endesium";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModItems.register();
		com.infernodude777.endesium.item.EndgearMaterials.register();
		com.infernodude777.endesium.registry.ModEndgear.register();
		ModItemGroups.register();
		ModEntities.register();
		ModBlockEntities.register();
		ModSounds.register();
		ModParticles.register();
		FabricDefaultAttributeRegistry.register(ModEntities.VOID_STALKER, ProductionVoidStalkerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.DUST_CRAWLER, DustCrawlerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.CHORUS_STALKER, ChorusStalkerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.VOID_RAY, VoidRayEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.MARSH_CRAWLER, MarshCrawlerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.LUMEN_MOTH, LumenMothEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.ASH_WRAITH, AshWraithEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.CRYSTAL_BURROWER, CrystalBurrowerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.NULLWALKER, NullwalkerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.VOID_WISP, VoidWispEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.CROWN_SENTINEL, CrownSentinelEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.END_WARDEN, EndWardenEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.END_GOLEM, EndGolemEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.SKY_JELLY, com.infernodude777.endesium.entity.SkyJellyEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.GALEFIN, com.infernodude777.endesium.entity.GalefinEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.DEEP_LURKER, com.infernodude777.endesium.entity.DeepLurkerEntity.createAttributes());
		com.infernodude777.endesium.world.AmbientSkyManager.register();
		ModWorldgen.register();
		com.infernodude777.endesium.gear.GearAbilities.register();
		EntityElytraEvents.CUSTOM.register((entity, tickElytra) ->
				entity instanceof net.minecraft.world.entity.LivingEntity living
						&& living.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.DRAGON_WINGS));
		com.infernodude777.endesium.registry.ModMenus.register();
		EndesiumCommands.register();
		EndesiumPackets.register();
		ResonanceSystem.register();
		com.infernodude777.endesium.dragon.DragonAssaultHandler.register();
		com.infernodude777.endesium.state.BossRewardEvents.register();
		com.infernodude777.endesium.state.RecipeUnlockEvents.register();
		EntityElytraEvents.CUSTOM.register((entity, tickElytra) -> {
			if (entity instanceof net.minecraft.world.entity.LivingEntity living
					&& living.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_WINGS)) {
				return true;
			}
			return false;
		});
		ServerPlayNetworking.registerGlobalReceiver(SonicBoomPayload.TYPE, (payload, context) ->
				context.server().execute(() -> SonicBoomHandler.fire(context.player())));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> VoidBlackHoleManager.clear());
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (var player : server.getPlayerList().getPlayers()) {
				VoidEquipmentAbilities.tick(player);
				if (server.getTickCount() % 5 == 0) {
					ResonantWingsPassives.tick(player, server.getTickCount());
				}
			}
			VoidBlackHoleManager.tick(server);
		});

		LOGGER.info("Endesium initialized: vertical slice + geography + ecology + post-Dragon state");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
