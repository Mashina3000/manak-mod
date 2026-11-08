package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SmoothHud.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModClientEvents {

    /** Дерево улучшений (K). */
    public static final KeyMapping OPEN_TREE = key("key.smoothhud.tree", InputConstants.KEY_K);
    /** Выбор маньяка (N). */
    public static final KeyMapping OPEN_MANIAC = key("key.smoothhud.maniac", InputConstants.KEY_N);
    /** Карта (M). */
    public static final KeyMapping OPEN_MAP = key("key.smoothhud.map", InputConstants.KEY_M);
    /** Способности маньяка: глаз (Z), подсветка (X), скорость (C). */
    public static final KeyMapping ABILITY_EYE = key("key.smoothhud.ability_eye", InputConstants.KEY_Z);
    public static final KeyMapping ABILITY_LIGHT = key("key.smoothhud.ability_light", InputConstants.KEY_X);
    public static final KeyMapping ABILITY_SPEED = key("key.smoothhud.ability_speed", InputConstants.KEY_C);
    /** Призрачная монашка (V). */
    public static final KeyMapping TOGGLE_GHOST = key("key.smoothhud.ghost", InputConstants.KEY_V);
    /** Установка ловушки (T). */
    public static final KeyMapping PLACE_TRAP = key("key.smoothhud.place_trap", InputConstants.KEY_T);

    private ModClientEvents() {
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping(name, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, code,
                "key.categories.smoothhud");
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("smooth_hud", SmoothHudOverlay.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SmoothHud.MONASHKA.get(), MonashkaRenderer::new);
        event.registerEntityRenderer(SmoothHud.FLYING_EYE.get(), FlyingEyeRenderer::new);
        event.registerEntityRenderer(SmoothHud.GHOST_NUN.get(), GhostNunRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MonashkaModel.LAYER, MonashkaModel::createBodyLayer);
        event.registerLayerDefinition(EyeModel.LAYER, EyeModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_TREE);
        event.register(OPEN_MANIAC);
        event.register(OPEN_MAP);
        event.register(ABILITY_EYE);
        event.register(ABILITY_LIGHT);
        event.register(ABILITY_SPEED);
        event.register(TOGGLE_GHOST);
        event.register(PLACE_TRAP);
    }
}
