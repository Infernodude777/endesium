package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.CrownSentinelEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class CrownSentinelModel extends GeoModel<CrownSentinelEntity> {
	@Override
	public ResourceLocation getModelResource(CrownSentinelEntity animatable) {
		return Endesium.id("geo/entity/crown_sentinel.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(CrownSentinelEntity animatable) {
		return Endesium.id("textures/entity/crown_sentinel.png");
	}

	@Override
	public ResourceLocation getAnimationResource(CrownSentinelEntity animatable) {
		return Endesium.id("animations/entity/crown_sentinel.animation.json");
	}
}
