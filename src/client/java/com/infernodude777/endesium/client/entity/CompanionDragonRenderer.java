package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/**
 * The companion dragon rides the vanilla dragon model but wears its own skin:
 * a fire palette of red, orange, and yellow instead of the boss's near-black
 * purple. It's the same silhouette, so the animation and hitbox stay vanilla -
 * just a different color, which is all you need to tell Ember from the boss at
 * a glance.
 */
public class CompanionDragonRenderer extends EnderDragonRenderer {
	private static final ResourceLocation COMPANION_DRAGON_TEXTURE =
			ResourceLocation.fromNamespaceAndPath(Endesium.MOD_ID, "textures/entity/enderdragon/dragon.png");

	public CompanionDragonRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(EnderDragon dragon) {
		return COMPANION_DRAGON_TEXTURE;
	}

	@Override
	public void render(EnderDragon dragon, float yaw, float tickDelta, PoseStack poseStack,
			MultiBufferSource buffers, int light) {
		// Vanilla's dragon renderer draws the model at a fixed size, but the
		// companion's hitbox scales with her growth stage. Wrap the whole
		// render in the same scale so baby Ember actually looks like a baby.
		poseStack.pushPose();
		float scale = dragon.getScale();
		poseStack.scale(scale, scale, scale);
		super.render(dragon, yaw, tickDelta, poseStack, buffers, light);
		poseStack.popPose();
	}
}
