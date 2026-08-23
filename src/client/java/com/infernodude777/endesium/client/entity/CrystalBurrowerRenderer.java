package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.entity.CrystalBurrowerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class CrystalBurrowerRenderer extends GeoEntityRenderer<CrystalBurrowerEntity> {
	public CrystalBurrowerRenderer(EntityRendererProvider.Context context) {
		super(context, new CrystalBurrowerModel());
		shadowRadius = 0.6F;
	}
}
