package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.DeepLurkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class DeepLurkerModel extends GeoModel<DeepLurkerEntity> {
	@Override
	public ResourceLocation getModelResource(DeepLurkerEntity animatable) {
		return Endesium.id("geo/entity/deep_lurker.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(DeepLurkerEntity animatable) {
		return Endesium.id("textures/entity/nullwalker.png");
	}

	@Override
	public ResourceLocation getAnimationResource(DeepLurkerEntity animatable) {
		return Endesium.id("animations/entity/deep_lurker.animation.json");
	}
}