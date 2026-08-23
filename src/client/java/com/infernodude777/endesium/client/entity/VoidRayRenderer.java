package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.VoidRayEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class VoidRayRenderer extends GeoEntityRenderer<VoidRayEntity> {
	public VoidRayRenderer(EntityRendererProvider.Context context) {
		super(context, new VoidRayModel());
		shadowRadius = 0.6F;
	}
}
