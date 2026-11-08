package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SmoothHud.MODID, value = Dist.CLIENT)
public final class ForgeClientEvents {

    private ForgeClientEvents() {
    }

    /** Отключаем стандартные элементы HUD, которые заменяем своими. */
    @SubscribeEvent
    public static void onRenderOverlayPre(RenderGuiOverlayEvent.Pre event) {
        NamedGuiOverlay overlay = event.getOverlay();
        var player = Minecraft.getInstance().player;
        if (player != null && player.isCreative()) {
            // В креативе остаётся обычный хотбар Minecraft
            if (overlay == VanillaGuiOverlay.HOTBAR.type()) {
                return;
            }
        }
        if (overlay == VanillaGuiOverlay.PLAYER_HEALTH.type()
                || overlay == VanillaGuiOverlay.FOOD_LEVEL.type()
                || overlay == VanillaGuiOverlay.ARMOR_LEVEL.type()
                || overlay == VanillaGuiOverlay.AIR_LEVEL.type()
                || overlay == VanillaGuiOverlay.HOTBAR.type()
                || overlay == VanillaGuiOverlay.EXPERIENCE_BAR.type()) {
            event.setCanceled(true);
        }
    }

    /** Открытие инвентаря (клавиша E) подменяем на наш экран. */
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof InventoryScreen) {
            event.setNewScreen(new SmoothInventoryScreen());
        }
    }

    /** Выносливость считаем раз в тик игры. */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        Progress.tick();
        Splash.tick(0.05f);
        TeamClient.tick(mc);
        Abilities.tick(mc);
        // Бег: если нажата клавиша бега и вперёд, и выносливость не истощена, бег включается сам
        if (mc.options.keySprint.isDown() && mc.options.keyUp.isDown()
                && !Stamina.isExhausted() && !player.isSprinting() && !player.isCreative()) {
            player.setSprinting(true);
        }
        if (mc.screen == null) {
            while (ModClientEvents.OPEN_TREE.consumeClick()) {
                mc.setScreen(new SkillTreeScreen());
            }
            while (ModClientEvents.OPEN_MANIAC.consumeClick()) {
                mc.setScreen(new ManiacScreen());
            }
            while (ModClientEvents.OPEN_MAP.consumeClick()) {
                mc.setScreen(new MapScreen());
            }
            if (Abilities.isManiac()) {
                while (ModClientEvents.ABILITY_EYE.consumeClick()) {
                    Abilities.castEye(mc);
                }
                while (ModClientEvents.ABILITY_LIGHT.consumeClick()) {
                    Abilities.castHighlight(mc);
                }
                while (ModClientEvents.ABILITY_SPEED.consumeClick()) {
                    Abilities.castSpeedBlind(mc);
                }
                while (ModClientEvents.TOGGLE_GHOST.consumeClick()) {
                    Abilities.toggleGhost();
                }
                while (ModClientEvents.PLACE_TRAP.consumeClick()) {
                    Abilities.placeTrap();
                }
            }
        }
        Vitals.tick(player, 0.05f);
        Stamina.tick(player, 0.05f);
        if (Stamina.isExhausted() && player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    /** Показываем экран роли при входе в мир. */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Splash.start();
        TeamClient.onLogin();
    }

    /** Сохраняем прогресс при выходе с сервера. */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Progress.save();
        TeamClient.onLogout();
    }

    /**
     * Пока выносливость исчерпана, сила движения вперёд ограничена
     * ниже порога бега (0.8). Ходьба остаётся, бег не включается.
     */
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (Stamina.isExhausted() && event.getInput().forwardImpulse > 0.79f) {
            event.getInput().forwardImpulse = 0.79f;
        }
    }
}
