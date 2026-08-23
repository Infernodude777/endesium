package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.VoidWispEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class VoidWispRenderer extends GeoEntityRenderer<VoidWispEntity> {
	public VoidWispRenderer(EntityRendererProvider.Context context) {
		super(context, new VoidWispModel());
		shadowRadius = 0.3F;
	}
}
