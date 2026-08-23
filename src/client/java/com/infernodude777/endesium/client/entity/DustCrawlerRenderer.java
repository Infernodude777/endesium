package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.DustCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class DustCrawlerRenderer extends GeoEntityRenderer<DustCrawlerEntity> {
	public DustCrawlerRenderer(EntityRendererProvider.Context context) {
		super(context, new DustCrawlerModel());
		shadowRadius = 0.4F;
	}
}
