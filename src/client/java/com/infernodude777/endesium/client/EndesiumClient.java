package com.infernodude777.endesium.client;

import com.infernodude777.endesium.client.entity.*;
import com.infernodude777.endesium.client.particle.ResonanceMoteParticle;
import com.infernodude777.endesium.dragon.DragonCompanionSystem;
import com.infernodude777.endesium.client.screen.ProgressionGuideScreen;
import com.infernodude777.endesium.item.VoidSwordItem;
import com.infernodude777.endesium.net.EndesiumPackets.SonicBoomPayload;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.registry.ModEntities;
import com.infernodude777.endesium.registry.ModItems;
import com.infernodude777.endesium.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.particles.SimpleParticleType;
import org.lwjgl.glfw.GLFW;

public class EndesiumClient implements ClientModInitializer {
    private static KeyMapping sonicBoomKey;

    @Override public void onInitializeClient(){
	MenuScreens.register(ModMenus.LORE_BOOK, ProgressionGuideScreen::new);
        // Every registered entity needs a renderer, otherwise LevelRenderer
        // NPEs the moment one of them comes into view (see crash 19.31.45).
        EntityRendererRegistry.register(ModEntities.VOID_STALKER, ProductionVoidStalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.DUST_CRAWLER, DustCrawlerRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHORUS_STALKER, ChorusStalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.VOID_RAY, VoidRayRenderer::new);
        EntityRendererRegistry.register(ModEntities.MARSH_CRAWLER, MarshCrawlerRenderer::new);
        EntityRendererRegistry.register(ModEntities.LUMEN_MOTH, LumenMothRenderer::new);
        EntityRendererRegistry.register(ModEntities.ASH_WRAITH, AshWraithRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRYSTAL_BURROWER, CrystalBurrowerRenderer::new);
        EntityRendererRegistry.register(ModEntities.NULLWALKER, NullwalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.VOID_WISP, VoidWispRenderer::new);
        EntityRendererRegistry.register(ModEntities.CROWN_SENTINEL, CrownSentinelRenderer::new);
        EntityRendererRegistry.register(ModEntities.END_WARDEN, EndWardenRenderer::new);
        EntityRendererRegistry.register(ModEntities.END_GOLEM, EndGolemRenderer::new);
		EntityRendererRegistry.register(ModEntities.SKY_JELLY, SkyJellyRenderer::new);
		EntityRendererRegistry.register(ModEntities.GALEFIN, GalefinRenderer::new);
		EntityRendererRegistry.register(ModEntities.DEEP_LURKER, DeepLurkerRenderer::new);
        EntityRendererRegistry.register(DragonCompanionSystem.COMPANION_DRAGON, CompanionDragonRenderer::new);
        EntityRendererRegistry.register(DragonCompanionSystem.COMPANION_DRAGON_BOLT,
                ctx -> new ThrownItemRenderer<>(ctx, 0.75F, true));
        EntityModelLayerRegistry.registerModelLayer(EndesiumDragonArmorModel.LAYER, EndesiumDragonArmorModel::createBodyLayer);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.VOID_GLASS, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MIREGLASS, RenderType.translucent());
        // Every particle type needs a client factory, or the server can emit
        // particles the client silently drops (invisible FX).
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        for (SimpleParticleType type : new SimpleParticleType[] {
                ModParticles.END_WASTES_MOTE,
                ModParticles.RESONANCE_PULSE,
                ModParticles.RESONANCE_ACTIVE,
                ModParticles.RUIN_GOLD_CONTACT,
                ModParticles.VOID_STALKER_TRACE,
                ModParticles.CHORUS_SPORE,
                ModParticles.RESONANCE_BEAM,
                ModParticles.HIGHLAND_WIND,
                ModParticles.MARSH_MIST,
                ModParticles.LUMEN_MOTE,
                ModParticles.ASH_MOTE,
                ModParticles.CRYSTAL_MOTE,
                ModParticles.NULL_DISTORTION,
                ModParticles.VOID_SKIRT_MOTE,
                ModParticles.VOID_CROWN_MOTE,
                ModParticles.UMBRAL_MOTE }) {
            registry.register(type, ResonanceMoteParticle.Factory::new);
        }

        // Sonic Boom: the client only requests; the server validates wings,
        // cooldown, and aliveness before performing the attack.
        sonicBoomKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.endesium.sonic_boom",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.endesium"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sonicBoomKey.consumeClick()) {
                ClientPlayNetworking.send(new SonicBoomPayload());
            }
        });

        // Void Sword charge bar: a restrained readout above the hotbar that
        // fills while the singularity charges, cyan to pale glow, and pulses
        // ancient gold at completion. Purely client-side: the use state is
        // already synced by vanilla.
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui || !mc.player.isUsingItem()) return;
            if (!mc.player.getUseItem().is(ModItems.VOID_SWORD)) return;
            float progress = VoidSwordItem.chargeProgress(mc.player.getUseItemRemainingTicks());
            int scaledWidth = mc.getWindow().getGuiScaledWidth();
            int scaledHeight = mc.getWindow().getGuiScaledHeight();
            int barWidth = 81;
            int barHeight = 5;
            int x = scaledWidth / 2 - barWidth / 2;
            int y = scaledHeight - 58;
            graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xC814101F);
            int fill = Math.round(barWidth * progress);
            if (fill > 0) {
                graphics.fill(x, y, x + fill, y + barHeight, chargeColor(progress));
            }
            String label = "VOID SINGULARITY";
            graphics.drawString(mc.font, label,
                    scaledWidth / 2 - mc.font.width(label) / 2, y - 11,
                    progress >= 0.999F ? 0xFFC9A227 : 0xFF8A80B0, true);
        });
    }

    /** Charge fill color: resonance cyan rising to pale glow, gold pulse when full. */
    private static int chargeColor(float progress) {
        if (progress >= 0.999F) {
            long phase = (System.currentTimeMillis() / 120L) & 1L;
            return phase == 0L ? 0xFFC9A227 : 0xFFE4C65B;
        }
        return lerpColor(0xFF7EA7A6, 0xFF9FE7E7, progress);
    }

    private static int lerpColor(int from, int to, float t) {
        t = Math.max(0.0F, Math.min(1.0F, t));
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (from & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}
