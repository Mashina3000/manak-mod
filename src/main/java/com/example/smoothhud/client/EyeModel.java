package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.example.smoothhud.entity.FlyingEyeEntity;
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

/** 3D-модель летающего глаза: белый шар и красная радужка спереди. */
public final class EyeModel extends EntityModel<FlyingEyeEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(SmoothHud.MODID, "flying_eye"), "main");

    private final ModelPart root;

    public EyeModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        PartDefinition eye = r.addOrReplaceChild("eye",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -4f, -4f, 8f, 8f, 8f),
                PartPose.ZERO);
        eye.addOrReplaceChild("iris",
                CubeListBuilder.create().texOffs(12, 8).addBox(-2f, -2f, -4.3f, 4f, 4f, 0.5f),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(FlyingEyeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
