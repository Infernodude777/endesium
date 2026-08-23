package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.NullwalkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class NullwalkerModel extends GeoModel<NullwalkerEntity> {
	@Override
	public ResourceLocation getModelResource(NullwalkerEntity animatable) {
		return Endesium.id("geo/entity/nullwalker.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(NullwalkerEntity animatable) {
		return Endesium.id("textures/entity/nullwalker.png");
	}

	@Override
	public ResourceLocation getAnimationResource(NullwalkerEntity animatable) {
		return Endesium.id("animations/entity/nullwalker.animation.json");
	}
}
