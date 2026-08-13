package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.VoidStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidStalkerRenderer extends GeoEntityRenderer<VoidStalkerEntity> {
	public VoidStalkerRenderer(EntityRendererProvider.Context context) {
		super(context, new VoidStalkerModel());
		this.shadowRadius = 0.45F;
	}
}
