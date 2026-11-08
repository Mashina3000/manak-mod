package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.example.smoothhud.entity.GhostNunEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Призрачная монашка: та же модель, полупрозрачная и холодного оттенка. */
public final class GhostNunRenderer extends EntityRenderer<GhostNunEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SmoothHud.MODID, "textures/entity/monashka.png");

    private final MonashkaModel model;

    public GhostNunRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MonashkaModel(context.bakeLayer(MonashkaModel.LAYER));
    }

    @Override
    public void render(GhostNunEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        model.renderToBuffer(poseStack,
                buffer.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 0.7F, 0.85F, 1.0F, 0.4F);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GhostNunEntity entity) {
        return TEXTURE;
    }
}
