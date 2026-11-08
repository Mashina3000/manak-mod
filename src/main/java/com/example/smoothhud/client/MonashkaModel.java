package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.example.smoothhud.entity.MonashkaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** 3D-модель монашки: длинная ряса, белая повязка на голове, крест на груди. */
public final class MonashkaModel extends EntityModel<MonashkaEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(SmoothHud.MODID, "monashka"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public MonashkaModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();

        PartDefinition head = r.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -8f, -4f, 8f, 8f, 8f),
                PartPose.offset(0f, 0f, 0f));
        // Белая повязка поверх головы
        head.addOrReplaceChild("wimple",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.5f, -8.5f, -4.5f, 9f, 5f, 9f),
                PartPose.ZERO);

        PartDefinition body = r.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4f, 0f, -2.5f, 8f, 24f, 5f),
                PartPose.offset(0f, 0f, 0f));
        // Крест на груди
        body.addOrReplaceChild("cross_v",
                CubeListBuilder.create().texOffs(56, 0).addBox(-1f, 3f, -3f, 2f, 6f, 0.5f),
                PartPose.ZERO);
        body.addOrReplaceChild("cross_h",
                CubeListBuilder.create().texOffs(56, 0).addBox(-2.5f, 5f, -3f, 5f, 2f, 0.5f),
                PartPose.ZERO);

        r.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3f, -2f, -2f, 4f, 12f, 4f),
                PartPose.offset(-5f, 2f, 0f));
        r.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1f, -2f, -2f, 4f, 12f, 4f),
                PartPose.offset(5f, 2f, 0f));
        r.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2f, 0f, -2f, 4f, 12f, 4f),
                PartPose.offset(-1.9f, 12f, 0f));
        r.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2f, 0f, -2f, 4f, 12f, 4f),
                PartPose.offset(1.9f, 12f, 0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(MonashkaEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
