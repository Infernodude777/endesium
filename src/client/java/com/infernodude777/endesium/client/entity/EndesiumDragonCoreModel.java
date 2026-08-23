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
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/**
 * The End Golem's resonance heart, visible through the awakened Dragon's
 * chest as a full-bright pulsing crystal. Rendered with an eyes-style render
 * type so it glows without any lightmap input; the surrounding armor plates
 * come from {@link EndesiumDragonArmorModel}.
 */
public final class EndesiumDragonCoreModel extends EntityModel<EnderDragon> {
	public static final net.minecraft.client.model.geom.ModelLayerLocation LAYER =
			new net.minecraft.client.model.geom.ModelLayerLocation(Endesium.id("ender_dragon_core"), "main");

	private final ModelPart root;
	private final ModelPart core;
	private final ModelPart shardA;
	private final ModelPart shardB;

	public EndesiumDragonCoreModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
		this.core = root.getChild("core");
		this.shardA = root.getChild("shard_a");
		this.shardB = root.getChild("shard_b");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		CubeDeformation edge = new CubeDeformation(0.08F);

		root.addOrReplaceChild("core", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-3.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, edge), PartPose.offset(0.0F, 0.0F, 1.0F));
		root.addOrReplaceChild("shard_a", CubeListBuilder.create()
				.texOffs(20, 0)
				.addBox(-1.5F, -5.0F, -1.0F, 3.0F, 4.0F, 2.0F, edge),
				PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.35F));
		root.addOrReplaceChild("shard_b", CubeListBuilder.create()
				.texOffs(30, 0)
				.addBox(-1.0F, 1.0F, -1.0F, 2.0F, 4.0F, 2.0F, edge),
				PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -0.28F));

		return LayerDefinition.create(mesh, 64, 16);
	}

	@Override
	public void setupAnim(EnderDragon dragon, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		// The core beats: a slow breath plus a quicker flutter that reads as
		// unstable resonance in the final stage.
		float beat = (float) Math.sin(ageInTicks * 0.15F) * 0.06F
				+ (float) Math.sin(ageInTicks * 0.42F) * 0.03F;
		core.scale(1.0F + beat, 1.0F - beat, 1.0F + beat);
		shardA.zRot = 0.35F + (float) Math.sin(ageInTicks * 0.23F) * 0.10F;
		shardB.zRot = -0.28F + (float) Math.sin(ageInTicks * 0.19F + 1.7F) * 0.10F;
	}

	@Override
	public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
			VertexConsumer vertexConsumer, int packedLight,
			int packedOverlay, int color) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
