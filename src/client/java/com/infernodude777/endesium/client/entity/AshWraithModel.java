package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.AshWraithEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AshWraithModel extends GeoModel<AshWraithEntity> {
	@Override
	public ResourceLocation getModelResource(AshWraithEntity animatable) {
		return Endesium.id("geo/entity/ash_wraith.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(AshWraithEntity animatable) {
		return Endesium.id("textures/entity/ash_wraith.png");
	}

	@Override
	public ResourceLocation getAnimationResource(AshWraithEntity animatable) {
		return Endesium.id("animations/entity/ash_wraith.animation.json");
	}
}
