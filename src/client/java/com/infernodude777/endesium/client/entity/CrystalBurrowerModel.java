package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.CrystalBurrowerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class CrystalBurrowerModel extends GeoModel<CrystalBurrowerEntity> {
	@Override
	public ResourceLocation getModelResource(CrystalBurrowerEntity animatable) {
		return Endesium.id("geo/entity/crystal_burrower.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(CrystalBurrowerEntity animatable) {
		return Endesium.id("textures/entity/crystal_burrower.png");
	}

	@Override
	public ResourceLocation getAnimationResource(CrystalBurrowerEntity animatable) {
		return Endesium.id("animations/entity/crystal_burrower.animation.json");
	}
}
