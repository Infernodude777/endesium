package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.CrownSentinelEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class CrownSentinelRenderer extends GeoEntityRenderer<CrownSentinelEntity> {
	public CrownSentinelRenderer(EntityRendererProvider.Context context) {
		super(context, new CrownSentinelModel());
		shadowRadius = 0.7F;
	}
}
