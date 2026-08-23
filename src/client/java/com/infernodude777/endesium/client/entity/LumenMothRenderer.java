package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.LumenMothEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class LumenMothRenderer extends GeoEntityRenderer<LumenMothEntity> {
	public LumenMothRenderer(EntityRendererProvider.Context context) {
		super(context, new LumenMothModel());
		shadowRadius = 0.2F;
	}
}
