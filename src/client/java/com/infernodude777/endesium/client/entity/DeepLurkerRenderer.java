package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.DeepLurkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class DeepLurkerRenderer extends GeoEntityRenderer<DeepLurkerEntity> {
	public DeepLurkerRenderer(EntityRendererProvider.Context context) {
		super(context, new DeepLurkerModel());
		shadowRadius = 0.5F;
	}
}