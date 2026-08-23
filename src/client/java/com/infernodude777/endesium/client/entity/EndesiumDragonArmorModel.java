package com.infernodude777.endesium.client.entity;

import com.infernodude777.endesium.Endesium;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/**
 * Endesium's visual upgrade for the vanilla Dragon.
 *
 * <p>This is deliberately an overlay rather than a replacement: vanilla's
 * head, neck, wings, flight animation, hitboxes, death animation, and beam
 * rendering remain authoritative. The overlay adds a more regal silhouette —
 * a fractured crown, dorsal plates, shoulder armor, and wing-root braces —
 * using the vanilla Dragon texture so the palette remains black, violet, and
 * restrained magenta rather than becoming a different creature.</p>
 *
 * <p>The part layout is intentionally Blockbench-friendly: every visible piece
 * is a named cuboid with a clean parent hierarchy and a 128x64 texture space.
 * It can be recreated or refined directly in Blockbench once that MCP is
 * available.</p>
 */
public final class EndesiumDragonArmorModel extends EntityModel<EnderDragon> {
	public static final net.minecraft.client.model.geom.ModelLayerLocation LAYER =
			new net.minecraft.client.model.geom.ModelLayerLocation(Endesium.id("ender_dragon_armor"), "main");

	private static final float DEG_TO_RAD = (float) (Math.PI / 180.0D);
	private final ModelPart root;
	private final ModelPart crown;
	private final ModelPart dorsalSpines;
	private final ModelPart shoulderMantle;
	private final ModelPart leftWingBrace;
	private final ModelPart rightWingBrace;
	private final ModelPart tailCrown;

	public EndesiumDragonArmorModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
		this.crown = root.getChild("crown");
		this.dorsalSpines = root.getChild("dorsal_spines");
		this.shoulderMantle = root.getChild("shoulder_mantle");
		this.leftWingBrace = root.getChild("left_wing_brace");
		this.rightWingBrace = root.getChild("right_wing_brace");
		this.tailCrown = root.getChild("tail_crown");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		CubeDeformation edge = new CubeDeformation(0.12F);

		// A broken crown follows the vanilla head direction without hiding the
		// face. The missing center tooth is intentional: it reads as ancient,
		// damaged regalia instead of a smooth helmet.
		root.addOrReplaceChild("crown", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-7.0F, -5.0F, -11.0F, 4.0F, 3.0F, 5.0F, edge)
				.addBox(3.0F, -5.0F, -11.0F, 4.0F, 3.0F, 5.0F, edge)
				.addBox(-6.0F, -8.0F, -8.0F, 3.0F, 4.0F, 3.0F, edge)
				.addBox(3.0F, -8.0F, -8.0F, 3.0F, 4.0F, 3.0F, edge)
				.addBox(-1.5F, -6.5F, -10.0F, 3.0F, 2.0F, 4.0F, edge), PartPose.offset(0.0F, 0.0F, -5.0F));

		// Five uneven dorsal plates make the body read as a powerful ancient
		// dragon instead of a flat vanilla tube when seen from above.
		PartDefinition spine = root.addOrReplaceChild("dorsal_spines", CubeListBuilder.create(), PartPose.ZERO);
		spine.addOrReplaceChild("plate_a", CubeListBuilder.create().texOffs(24, 0)
				.addBox(-2.0F, -9.0F, -5.0F, 4.0F, 7.0F, 3.0F, edge), PartPose.offset(0.0F, 0.0F, -2.0F));
		spine.addOrReplaceChild("plate_b", CubeListBuilder.create().texOffs(38, 0)
				.addBox(-2.5F, -8.0F, -4.0F, 5.0F, 6.0F, 3.0F, edge), PartPose.offset(0.0F, 0.0F, 5.0F));
		spine.addOrReplaceChild("plate_c", CubeListBuilder.create().texOffs(52, 0)
				.addBox(-2.0F, -7.0F, -3.0F, 4.0F, 5.0F, 3.0F, edge), PartPose.offset(0.0F, 0.0F, 13.0F));
		spine.addOrReplaceChild("plate_d", CubeListBuilder.create().texOffs(64, 0)
				.addBox(-1.5F, -6.0F, -2.5F, 3.0F, 4.0F, 3.0F, edge), PartPose.offset(0.0F, 0.0F, 20.0F));
		spine.addOrReplaceChild("plate_e", CubeListBuilder.create().texOffs(74, 0)
				.addBox(-1.0F, -5.0F, -2.0F, 2.0F, 3.0F, 3.0F, edge), PartPose.offset(0.0F, 0.0F, 27.0F));

		// A broad shoulder mantle adds volume where the vanilla model is visually
		// thinnest, but leaves the wing joints clear for vanilla animation.
		root.addOrReplaceChild("shoulder_mantle", CubeListBuilder.create()
				.texOffs(0, 14)
				.addBox(-13.0F, -3.0F, -8.0F, 7.0F, 5.0F, 10.0F, edge)
				.addBox(6.0F, -3.0F, -8.0F, 7.0F, 5.0F, 10.0F, edge)
				.addBox(-8.0F, -4.0F, -7.0F, 16.0F, 3.0F, 7.0F, edge), PartPose.offset(0.0F, 0.0F, 0.0F));

		// Narrow braces sit at the wing roots and echo the vanilla purple wing
		// membranes without replacing or obstructing them.
		root.addOrReplaceChild("left_wing_brace", CubeListBuilder.create()
				.texOffs(34, 14)
				.addBox(0.0F, -2.0F, -2.0F, 18.0F, 3.0F, 3.0F, edge)
				.addBox(13.0F, -5.0F, -1.0F, 5.0F, 8.0F, 2.0F, edge), PartPose.offset(7.0F, 0.0F, 0.0F));
		root.addOrReplaceChild("right_wing_brace", CubeListBuilder.create()
				.texOffs(34, 14)
				.addBox(-18.0F, -2.0F, -2.0F, 18.0F, 3.0F, 3.0F, edge)
				.addBox(-18.0F, -5.0F, -1.0F, 5.0F, 8.0F, 2.0F, edge), PartPose.offset(-7.0F, 0.0F, 0.0F));

		// Three smaller tail crowns taper toward the tail and make the rear
		// silhouette legible without adding a second animated tail.
		PartDefinition tail = root.addOrReplaceChild("tail_crown", CubeListBuilder.create(), PartPose.ZERO);
		tail.addOrReplaceChild("tail_left", CubeListBuilder.create().texOffs(64, 14)
				.addBox(-6.0F, -3.0F, -2.0F, 4.0F, 6.0F, 3.0F, edge), PartPose.offset(0.0F, 0.0F, 9.0F));
		tail.addOrReplaceChild("tail_right", CubeListBuilder.create().texOffs(78, 14)
				.addBox(2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 3.0F, edge), PartPose.offset(0.0F, 0.0F, 9.0F));
		tail.addOrReplaceChild("tail_cap", CubeListBuilder.create().texOffs(92, 14)
				.addBox(-1.5F, -4.0F, -1.5F, 3.0F, 5.0F, 3.0F, edge), PartPose.offset(0.0F, 0.0F, 17.0F));

		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(EnderDragon dragon, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		float pulse = (float) Math.sin(ageInTicks * 0.08F);
		float fastPulse = (float) Math.sin(ageInTicks * 0.17F);
		float stage = dragon.getMaxHealth() <= 0.0F ? 1.0F : dragon.getHealth() / dragon.getMaxHealth();
		float intensity = stage < 0.45F ? 1.0F : stage < 0.75F ? 0.65F : 0.35F;

		crown.xRot = pulse * 0.035F * intensity;
		crown.yRot = fastPulse * 0.018F * intensity;
		dorsalSpines.xRot = pulse * 0.025F * intensity;
		shoulderMantle.xRot = -pulse * 0.018F * intensity;
		leftWingBrace.zRot = pulse * 0.035F * intensity;
		rightWingBrace.zRot = -pulse * 0.035F * intensity;
		tailCrown.xRot = -pulse * 0.022F * intensity;
	}

	@Override
	public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
			VertexConsumer vertexConsumer, int packedLight,
			int packedOverlay, int color) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
