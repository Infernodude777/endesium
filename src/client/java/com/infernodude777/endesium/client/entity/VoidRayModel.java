package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.VoidRayEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class VoidRayModel extends GeoModel<VoidRayEntity> {
	@Override
	public ResourceLocation getModelResource(VoidRayEntity animatable) {
		return Endesium.id("geo/entity/void_ray.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(VoidRayEntity animatable) {
		return Endesium.id("textures/entity/void_ray.png");
	}

	@Override
	public ResourceLocation getAnimationResource(VoidRayEntity animatable) {
		return Endesium.id("animations/entity/void_ray.animation.json");
	}
}
