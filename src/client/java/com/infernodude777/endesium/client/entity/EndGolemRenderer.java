package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.EndGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class EndGolemRenderer extends GeoEntityRenderer<EndGolemEntity> {
	public EndGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new EndGolemModel());
		shadowRadius = 1.4F;
	}
}
