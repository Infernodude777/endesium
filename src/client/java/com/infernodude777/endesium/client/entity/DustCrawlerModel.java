package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.DustCrawlerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class DustCrawlerModel extends GeoModel<DustCrawlerEntity> {
	@Override
	public ResourceLocation getModelResource(DustCrawlerEntity animatable) {
		return Endesium.id("geo/entity/dust_crawler.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(DustCrawlerEntity animatable) {
		return Endesium.id("textures/entity/dust_crawler.png");
	}

	@Override
	public ResourceLocation getAnimationResource(DustCrawlerEntity animatable) {
		return Endesium.id("animations/entity/dust_crawler.animation.json");
	}
}
