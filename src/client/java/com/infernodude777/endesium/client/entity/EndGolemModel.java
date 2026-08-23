package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.EndGolemEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class EndGolemModel extends GeoModel<EndGolemEntity> {
	@Override
	public ResourceLocation getModelResource(EndGolemEntity animatable) {
		return Endesium.id("geo/entity/end_golem.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(EndGolemEntity animatable) {
		return Endesium.id("textures/entity/end_golem.png");
	}

	@Override
	public ResourceLocation getAnimationResource(EndGolemEntity animatable) {
		return Endesium.id("animations/entity/end_golem.animation.json");
	}
}
