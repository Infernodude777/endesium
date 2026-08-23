package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.AshWraithEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class AshWraithRenderer extends GeoEntityRenderer<AshWraithEntity> {
	public AshWraithRenderer(EntityRendererProvider.Context context) {
		super(context, new AshWraithModel());
		shadowRadius = 0.3F;
	}
}
