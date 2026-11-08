package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.example.smoothhud.entity.FlyingEyeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class FlyingEyeRenderer extends EntityRenderer<FlyingEyeEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SmoothHud.MODID, "textures/entity/flying_eye.png");

    private final EyeModel model;

    public FlyingEyeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new EyeModel(context.bakeLayer(EyeModel.LAYER));
    }

    @Override
    public void render(FlyingEyeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        model.renderToBuffer(poseStack,
                buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingEyeEntity entity) {
        return TEXTURE;
    }
}
