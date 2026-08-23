package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.ProductionVoidStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ProductionVoidStalkerRenderer extends GeoEntityRenderer<ProductionVoidStalkerEntity> {
	public ProductionVoidStalkerRenderer(EntityRendererProvider.Context context) {
		super(context, new ProductionVoidStalkerModel());
		shadowRadius = 0.35F;
	}
}
