package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.VoidStalkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidStalkerModel extends GeoModel<VoidStalkerEntity> {
	@Override
	public ResourceLocation getModelResource(VoidStalkerEntity animatable) {
		return Endesium.id("geo/entity/void_stalker.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(VoidStalkerEntity animatable) {
		return Endesium.id("textures/item/foundation_test_item.png");
	}

	@Override
	public ResourceLocation getAnimationResource(VoidStalkerEntity animatable) {
		return Endesium.id("animations/entity/void_stalker.animation.json");
	}
}
