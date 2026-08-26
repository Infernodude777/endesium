package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.SkyJellyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class SkyJellyModel extends GeoModel<SkyJellyEntity> {
	@Override
	public ResourceLocation getModelResource(SkyJellyEntity animatable) {
		return Endesium.id("geo/entity/sky_jelly.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(SkyJellyEntity animatable) {
		return Endesium.id("textures/entity/lumen_moth.png");
	}

	@Override
	public ResourceLocation getAnimationResource(SkyJellyEntity animatable) {
		return Endesium.id("animations/entity/sky_jelly.animation.json");
	}
}