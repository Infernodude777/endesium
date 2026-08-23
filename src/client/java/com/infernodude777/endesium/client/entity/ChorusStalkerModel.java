package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.ChorusStalkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ChorusStalkerModel extends GeoModel<ChorusStalkerEntity> {
	@Override
	public ResourceLocation getModelResource(ChorusStalkerEntity animatable) {
		return Endesium.id("geo/entity/chorus_stalker.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(ChorusStalkerEntity animatable) {
		return Endesium.id("textures/entity/chorus_stalker.png");
	}

	@Override
	public ResourceLocation getAnimationResource(ChorusStalkerEntity animatable) {
		return Endesium.id("animations/entity/chorus_stalker.animation.json");
	}
}
