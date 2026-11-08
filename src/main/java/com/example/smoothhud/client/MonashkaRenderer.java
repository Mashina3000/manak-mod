package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.example.smoothhud.entity.MonashkaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class MonashkaRenderer extends MobRenderer<MonashkaEntity, MonashkaModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SmoothHud.MODID, "textures/entity/monashka.png");

    public MonashkaRenderer(EntityRendererProvider.Context context) {
        super(context, new MonashkaModel(context.bakeLayer(MonashkaModel.LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(MonashkaEntity entity) {
        return TEXTURE;
    }
}
