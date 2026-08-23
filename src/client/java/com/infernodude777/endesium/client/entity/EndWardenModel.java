package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.EndWardenEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/**
 * The warden's texture and silhouette are keyed to its region attunement:
 * ten regional palettes over one shared body, plus region-specific accessory
 * bones (crests, tendrils, halos, horns) shown or hidden at render time, so
 * every flagship's guardian wears the colors — and the crown — of its own
 * biome.
 */
public final class EndWardenModel extends GeoModel<EndWardenEntity> {
	private static final ResourceLocation[] REGION_TEXTURES = new ResourceLocation[]{
			Endesium.id("textures/entity/end_warden_0.png"),
			Endesium.id("textures/entity/end_warden_1.png"),
			Endesium.id("textures/entity/end_warden_2.png"),
			Endesium.id("textures/entity/end_warden_3.png"),
			Endesium.id("textures/entity/end_warden_4.png"),
			Endesium.id("textures/entity/end_warden_5.png"),
			Endesium.id("textures/entity/end_warden_6.png"),
			Endesium.id("textures/entity/end_warden_7.png"),
			Endesium.id("textures/entity/end_warden_8.png"),
			Endesium.id("textures/entity/end_warden_9.png")
	};

	/** Accessory bone names; every region shows exactly one group (Umbral: none). */
	private static final String CREST = "crest_spine";
	private static final String TENDRIL_L = "hood_tendril_left";
	private static final String TENDRIL_R = "hood_tendril_right";
	private static final String HALO = "halo";
	private static final String HORN_L = "horn_left";
	private static final String HORN_R = "horn_right";

	@Override
	public ResourceLocation getModelResource(EndWardenEntity animatable) {
		return Endesium.id("geo/entity/end_warden.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(EndWardenEntity animatable) {
		int region = animatable.getRegion();
		if (region < 0 || region >= REGION_TEXTURES.length) {
			return Endesium.id("textures/entity/end_warden_default.png");
		}
		return REGION_TEXTURES[region];
	}

	@Override
	public ResourceLocation getAnimationResource(EndWardenEntity animatable) {
		return Endesium.id("animations/entity/end_warden.animation.json");
	}

	@Override
	public void setCustomAnimations(EndWardenEntity animatable, long instanceId,
			AnimationState<EndWardenEntity> animationState) {
		super.setCustomAnimations(animatable, instanceId, animationState);
		int region = animatable.getRegion();
		setVisible(CREST, region == 0 || region == 1 || region == 3);
		setVisible(TENDRIL_L, region == 2 || region == 7);
		setVisible(TENDRIL_R, region == 2 || region == 7);
		setVisible(HALO, region == 4 || region == 6);
		setVisible(HORN_L, region == 5 || region == 8);
		setVisible(HORN_R, region == 5 || region == 8);
	}

	private void setVisible(String boneName, boolean visible) {
		getBone(boneName).ifPresent(bone -> bone.setHidden(!visible));
	}
}
