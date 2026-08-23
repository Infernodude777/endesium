package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.VoidWispEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class VoidWispModel extends GeoModel<VoidWispEntity> {
	@Override
	public ResourceLocation getModelResource(VoidWispEntity animatable) {
		return Endesium.id("geo/entity/void_wisp.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(VoidWispEntity animatable) {
		return Endesium.id("textures/entity/void_wisp.png");
	}

	@Override
	public ResourceLocation getAnimationResource(VoidWispEntity animatable) {
		return Endesium.id("animations/entity/void_wisp.animation.json");
	}
}
