package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.NullwalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class NullwalkerRenderer extends GeoEntityRenderer<NullwalkerEntity> {
	public NullwalkerRenderer(EntityRendererProvider.Context context) {
		super(context, new NullwalkerModel());
		shadowRadius = 0.4F;
	}
}
