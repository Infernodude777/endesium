package com.infernodude777.endesium;

import com.infernodude777.endesium.command.EndesiumCommands;
import com.infernodude777.endesium.dragon.DragonAssaultHandler;
import com.infernodude777.endesium.dragon.ResonantWingsPassives;
import com.infernodude777.endesium.dragon.SonicBoomHandler;
import com.infernodude777.endesium.entity.*;
import com.infernodude777.endesium.item.VoidBlackHoleManager;
import com.infernodude777.endesium.item.VoidEquipmentAbilities;
import com.infernodude777.endesium.net.EndesiumPackets;
import com.infernodude777.endesium.net.EndesiumPackets.SonicBoomPayload;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.*;
import com.infernodude777.endesium.resonance.ResonanceSystem;
import com.infernodude777.endesium.world.ModWorldgen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endesium mod entry point. Registers every subsystem in the correct
 * dependency order so the registry freezes with all content present.
 */
public class Endesium implements ModInitializer {
    public static final String MOD_ID = "endesium";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // --- Core registries ---
        ModBlocks.register();
        ModItems.register();
        com.infernodude777.endesium.item.EndgearMaterials.register();
        ModEndgear.register();
        ModItemGroups.register();

        // --- Entities and block entities ---
        ModEntities.register();
        ModBlockEntities.register();

        // --- Sounds and particles ---
        ModSounds.register();
        ModParticles.register();

        // --- Entity attribute registration ---
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
        FabricDefaultAttributeRegistry.register(ModEntities.SKY_JELLY, SkyJellyEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.GALEFIN, GalefinEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.DEEP_LURKER, DeepLurkerEntity.createAttributes());

        // --- World generation ---
        com.infernodude777.endesium.world.AmbientSkyManager.register();
        ModWorldgen.register();

        // --- Gear abilities ---
        com.infernodude777.endesium.gear.GearAbilities.register();

        // --- Elytra events ---
        EntityElytraEvents.CUSTOM.register((entity, tickElytra) ->
                entity instanceof net.minecraft.world.entity.LivingEntity living
                        && living.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.DRAGON_WINGS));

        // --- Menus, commands, networking ---
        ModMenus.register();
        EndesiumCommands.register();
        EndesiumPackets.register();
        ResonanceSystem.register();

        // --- Dragon fight and world events ---
        DragonAssaultHandler.register();
        com.infernodude777.endesium.state.BossRewardEvents.register();
        com.infernodude777.endesium.state.RecipeUnlockEvents.register();

        // --- Resonant Wings elytra event ---
        EntityElytraEvents.CUSTOM.register((entity, tickElytra) -> {
            if (entity instanceof net.minecraft.world.entity.LivingEntity living
                    && living.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_WINGS)) {
                return true;
            }
            return false;
        });

        // --- Sonic Boom network receiver ---
        ServerPlayNetworking.registerGlobalReceiver(SonicBoomPayload.TYPE, (payload, context) ->
                context.server().execute(() -> SonicBoomHandler.fire(context.player())));

        // --- Server lifecycle hooks ---
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

    /** Convenience helper for building resource locations under the Endesium namespace. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
