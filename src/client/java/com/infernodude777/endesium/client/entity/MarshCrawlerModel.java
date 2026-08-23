package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.MarshCrawlerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MarshCrawlerModel extends GeoModel<MarshCrawlerEntity> {
	@Override
	public ResourceLocation getModelResource(MarshCrawlerEntity animatable) {
		return Endesium.id("geo/entity/marsh_crawler.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(MarshCrawlerEntity animatable) {
		return Endesium.id("textures/entity/marsh_crawler.png");
	}

	@Override
	public ResourceLocation getAnimationResource(MarshCrawlerEntity animatable) {
		return Endesium.id("animations/entity/marsh_crawler.animation.json");
	}
}
