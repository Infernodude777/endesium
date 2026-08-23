package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.ChorusStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ChorusStalkerRenderer extends GeoEntityRenderer<ChorusStalkerEntity> {
	public ChorusStalkerRenderer(EntityRendererProvider.Context context) {
		super(context, new ChorusStalkerModel());
		shadowRadius = 0.35F;
	}
}
