package com.infernodude777.endesium.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.infernodude777.endesium.client.entity.EndesiumDragonArmorModel;
import com.infernodude777.endesium.client.entity.EndesiumDragonCoreModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives the Dragon a readable visual arc without replacing vanilla geometry.
 * Stage changes are driven from synchronized health and scale, so clients do
 * not need private server combat state. The transform is intentionally small:
 * it preserves hitbox readability while making the awakened model feel heavier
 * and more unstable as it approaches death. It is injected after the renderer's
 * first pushPose so vanilla's matching popPose removes the transform cleanly.
 *
 * <p>The regalia assembles per combat phase ({@code setStage}): crown only in
 * the first fight, then horns/neck, dorsal plates, mantle and braces, and
 * finally the tail crown plus an emissive chest core that renders full-bright
 * through the vanilla body.</p>
 */
@Mixin(EnderDragonRenderer.class)
public abstract class EnderDragonRendererMixin {
	@Unique
	private EndesiumDragonArmorModel endesium$armorModel;

	@Unique
	private EndesiumDragonCoreModel endesium$coreModel;

	@Unique
	private static final ResourceLocation ENDESIUM_DRAGON_TEXTURE =
			ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/enderdragon.png");

	@Unique
	private static final ResourceLocation ENDESIUM_CORE_TEXTURE =
			ResourceLocation.withDefaultNamespace("textures/entity/endesium/dragon_core.png");

	@Inject(method = "<init>", at = @At("TAIL"))
	private void endesium$createArmorModel(EntityRendererProvider.Context context, CallbackInfo ci) {
		endesium$armorModel = new EndesiumDragonArmorModel(
				context.bakeLayer(EndesiumDragonArmorModel.LAYER));
		endesium$coreModel = new EndesiumDragonCoreModel(
				context.bakeLayer(EndesiumDragonCoreModel.LAYER));
	}

	@Unique
	private int endesium$stageOf(EnderDragon dragon) {
		float healthFraction = dragon.getMaxHealth() <= 0.0F ? 1.0F : dragon.getHealth() / dragon.getMaxHealth();
		return healthFraction > 0.75F ? 1 : healthFraction > 0.45F ? 2 : healthFraction > 0.20F ? 3 : 4;
	}

	@Inject(
			method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
					ordinal = 0, shift = At.Shift.AFTER))
	private void endesium$stageTransform(EnderDragon dragon, float yaw, float tickDelta, PoseStack pose,
			MultiBufferSource buffers, int light, CallbackInfo ci) {
		int stage = endesium$stageOf(dragon);
		boolean awakened = dragon.getScale() > 1.1F;
		// Keep the first fight's scale and pitch nearly vanilla; the restrained
		// armor overlay is safe to show in both fights, while stronger transforms
		// begin only after the persistent awakening.
		if (!awakened) return;
		float stageScale = 1.0F + (stage - 1) * 0.025F + 0.035F;
		float stagePulse = stage >= 3
				? (float) Math.sin((dragon.tickCount + tickDelta) * 0.12F) * 0.006F
				: 0.0F;
		pose.scale(stageScale + stagePulse, stageScale * (stage == 4 ? 1.04F : 1.0F), stageScale - stagePulse);
		if (stage >= 3) {
			// A tiny forward pitch creates a more predatory silhouette while the
			// vanilla animation still controls wings, head, and tail. The late
			// stages also breathe by a fraction of a pixel; this is deliberately
			// applied inside the pushed pose so vanilla's popPose always resets it.
			pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(stage == 4 ? 2.0F : 1.0F));
			pose.translate(0.0D, stagePulse * 0.5D, 0.0D);
		}
	}

	/** Render the regalia overlay after vanilla's model has rendered. */
	@Inject(
			method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At("TAIL"))
	private void endesium$renderArmor(EnderDragon dragon, float yaw, float tickDelta, PoseStack pose,
			MultiBufferSource buffers, int light, CallbackInfo ci) {
		if (endesium$armorModel == null) return;
		int stage = endesium$stageOf(dragon);
		boolean awakened = dragon.getScale() > 1.1F;
		endesium$armorModel.setStage(stage, awakened);
		float stageScale = awakened
				? 1.0F + (stage - 1) * 0.045F
				: 1.0F + (stage - 1) * 0.018F;
		float bob = (float) Math.sin((dragon.tickCount + tickDelta) * 0.08F) * 0.015F;
		pose.pushPose();
		// EnderDragonRenderer leaves the stack at the entity origin after its
		// vanilla popPose. This orientation keeps the overlay aligned with the
		// dragon's forward axis without touching its hitbox or animation state.
		pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - yaw));
		pose.translate(0.0D, 0.72D + bob, 0.0D);
		pose.scale(stageScale, stageScale, stageScale);
		endesium$armorModel.setupAnim(dragon, 0.0F, 0.0F, dragon.tickCount + tickDelta, 0.0F, 0.0F);
		endesium$armorModel.renderToBuffer(pose,
				buffers.getBuffer(RenderType.entityCutoutNoCull(ENDESIUM_DRAGON_TEXTURE)),
				light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		if (endesium$coreModel != null && endesium$armorModel.coreVisible(stage, awakened)) {
			// Same transform chain; the core renders full-bright through the
			// vanilla chest with an eyes-style buffer.
			endesium$coreModel.setupAnim(dragon, 0.0F, 0.0F, dragon.tickCount + tickDelta, 0.0F, 0.0F);
			endesium$coreModel.renderToBuffer(pose,
					buffers.getBuffer(RenderType.eyes(ENDESIUM_CORE_TEXTURE)),
					0xF000F0, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		}
		pose.popPose();
	}
}
