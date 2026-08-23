package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.LumenMothEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class LumenMothModel extends GeoModel<LumenMothEntity> {
	@Override
	public ResourceLocation getModelResource(LumenMothEntity animatable) {
		return Endesium.id("geo/entity/lumen_moth.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(LumenMothEntity animatable) {
		return Endesium.id("textures/entity/lumen_moth.png");
	}

	@Override
	public ResourceLocation getAnimationResource(LumenMothEntity animatable) {
		return Endesium.id("animations/entity/lumen_moth.animation.json");
	}
}
