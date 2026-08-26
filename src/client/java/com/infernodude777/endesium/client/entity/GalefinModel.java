package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.GalefinEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class GalefinModel extends GeoModel<GalefinEntity> {
	@Override
	public ResourceLocation getModelResource(GalefinEntity animatable) {
		return Endesium.id("geo/entity/galefin.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(GalefinEntity animatable) {
		return Endesium.id("textures/entity/void_ray.png");
	}

	@Override
	public ResourceLocation getAnimationResource(GalefinEntity animatable) {
		return Endesium.id("animations/entity/galefin.animation.json");
	}
}