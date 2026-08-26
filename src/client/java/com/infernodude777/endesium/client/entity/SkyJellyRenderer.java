package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.SkyJellyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class SkyJellyRenderer extends GeoEntityRenderer<SkyJellyEntity> {
	public SkyJellyRenderer(EntityRendererProvider.Context context) {
		super(context, new SkyJellyModel());
		shadowRadius = 0.4F;
	}
}