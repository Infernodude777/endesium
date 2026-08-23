package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.EndWardenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class EndWardenRenderer extends GeoEntityRenderer<EndWardenEntity> {
	public EndWardenRenderer(EntityRendererProvider.Context context) {
		super(context, new EndWardenModel());
		shadowRadius = 0.9F;
	}
}
