package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.ProductionVoidStalkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ProductionVoidStalkerModel extends GeoModel<ProductionVoidStalkerEntity> {
	@Override
	public ResourceLocation getModelResource(ProductionVoidStalkerEntity animatable) {
		return Endesium.id("geo/entity/void_stalker_v2.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(ProductionVoidStalkerEntity animatable) {
		return Endesium.id("textures/entity/void_stalker_v2_tall.png");
	}

	@Override
	public ResourceLocation getAnimationResource(ProductionVoidStalkerEntity animatable) {
		return Endesium.id("animations/entity/void_stalker_v2.animation.json");
	}
}
