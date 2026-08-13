package com.infernodude777.endesium.client;

import com.infernodude777.endesium.client.entity.VoidStalkerRenderer;
import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;

public class EndesiumClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.VOID_STALKER, VoidStalkerRenderer::new);
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FOUNDATION_TEST_BLOCK, RenderType.cutout());
	}
}