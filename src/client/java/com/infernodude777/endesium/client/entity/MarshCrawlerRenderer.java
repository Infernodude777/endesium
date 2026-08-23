package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.MarshCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class MarshCrawlerRenderer extends GeoEntityRenderer<MarshCrawlerEntity> {
	public MarshCrawlerRenderer(EntityRendererProvider.Context context) {
		super(context, new MarshCrawlerModel());
		shadowRadius = 0.5F;
	}
}
