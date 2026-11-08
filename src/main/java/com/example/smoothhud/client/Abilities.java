package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.example.smoothhud.entity.FlyingEyeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Способности маньяка (только на клиенте):
 * Z - летающий глаз 10 с, X - подсветка игроков 5 с, C - скорость 10 с и ослепление себя.
 * Перезарядка 30 с у каждой.
 */
public final class Abilities {

    public static final float COOLDOWN = 30f;
    private static final float TICK = 0.05f;
    private static final int HIGHLIGHT_TICKS = 100;
    private static final int EFFECT_TICKS = 200;

    private static float eyeCd;
    private static float highlightCd;
    private static float speedCd;
    private static int highlightTicks;
    private static int eyeIds = -1000;
    private static int ghostIds = -2000;
    private static boolean ghostOn;
    private static com.example.smoothhud.entity.GhostNunEntity ghost;
    private static int trapRotation;

    /** Включает или выключает призрачную монашку (V). */
    public static void toggleGhost() {
        ghostOn = !ghostOn;
        if (!ghostOn && ghost != null) {
            ghost.discard();
            ghost = null;
        }
    }

    public static boolean ghostOn() {
        return ghostOn;
    }

    /** Ставит следующую выбранную ловушку (T). Сервер проверяет перезарядку. */
    public static void placeTrap() {
        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < TrapScreen.TRAPS.length; i++) {
            if (Progress.trapSelected(i)) {
                chosen.add(i);
            }
        }
        if (chosen.isEmpty()) {
            return;
        }
        int type = chosen.get(trapRotation % chosen.size());
        trapRotation++;
        com.example.smoothhud.net.Net.CHANNEL.sendToServer(new com.example.smoothhud.net.Net.PlaceTrap(type));
    }

    private static final List<Player> glowing = new ArrayList<>();
    private static final List<UUID> highlightIds = new ArrayList<>();

    private Abilities() {
    }

    public static boolean isManiac() {
        return Progress.role() == Progress.ROLE_MANIAC;
    }

    public static boolean highlightActive() {
        return highlightTicks > 0;
    }

    /** Вызывается раз в тик игры. */
    public static void tick(Minecraft mc) {
        if (ghostOn && mc.level != null && mc.player != null
                && (ghost == null || ghost.isRemoved())) {
            ghost = new com.example.smoothhud.entity.GhostNunEntity(SmoothHud.GHOST_NUN.get(), mc.level);
            ghost.setOwner(mc.player);
            mc.level.putNonPlayerEntity(ghostIds--, ghost);
        }
        eyeCd = Math.max(0f, eyeCd - TICK);
        highlightCd = Math.max(0f, highlightCd - TICK);
        speedCd = Math.max(0f, speedCd - TICK);
        if (highlightTicks > 0) {
            highlightTicks--;
            if (highlightTicks == 0) {
                clearGlow();
                highlightIds.clear();
            }
        }
    }

    public static void castEye(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || eyeCd > 0f) {
            return;
        }
        FlyingEyeEntity eye = new FlyingEyeEntity(SmoothHud.FLYING_EYE.get(), mc.level);
        eye.setOwner(p);
        mc.level.putNonPlayerEntity(eyeIds--, eye);
        eyeCd = COOLDOWN;
    }

    /** Просит сервер подсветить игроков с модом. Отвечает только тем, у кого есть мод. */
    public static void castHighlight(Minecraft mc) {
        if (mc.player == null || highlightCd > 0f) {
            return;
        }
        com.example.smoothhud.net.Net.CHANNEL.sendToServer(new com.example.smoothhud.net.Net.HighlightRequest());
        highlightCd = COOLDOWN;
    }

    /** Подсвечивает игроков из списка (пришёл от сервера). */
    public static void applyHighlight(List<UUID> ids) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) {
            return;
        }
        clearGlow();
        highlightIds.clear();
        highlightIds.addAll(ids);
        for (Player other : mc.level.players()) {
            if (other != p && ids.contains(other.getUUID())) {
                other.setGlowingTag(true);
                glowing.add(other);
            }
        }
        highlightTicks = HIGHLIGHT_TICKS;
    }

    public static boolean isHighlighted(UUID id) {
        return highlightIds.contains(id);
    }

    public static void castSpeedBlind(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || speedCd > 0f) {
            return;
        }
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_TICKS, 1, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, EFFECT_TICKS, 0, false, false));
        speedCd = COOLDOWN;
    }

    private static void clearGlow() {
        for (Player pl : glowing) {
            pl.setGlowingTag(false);
        }
        glowing.clear();
    }

    /** Строка способностей над HUD. Видна только маньяку: готовые - зелёным, перезарядка - серым. */
    public static void drawHud(GuiGraphics g, Font font, int sh) {
        if (!isManiac()) {
            return;
        }
        int x = 6;
        int y = sh - 64;
        x = segment(g, font, x, y, "Z глаз: ", eyeCd);
        x = segment(g, font, x + 6, y, "X свет: ", highlightCd);
        segment(g, font, x + 6, y, "C скорость: ", speedCd);
    }

    private static int segment(GuiGraphics g, Font font, int x, int y, String label, float cd) {
        g.drawString(font, label, x, y, 0xFFE0C0, true);
        int sx = x + font.width(label);
        boolean ready = cd <= 0f;
        String value = ready ? "готово" : (int) Math.ceil(cd) + "с";
        int color = ready ? 0xFF80FF80 : 0xFFAAAAAA;
        g.drawString(font, value, sx, y, color, true);
        return sx + font.width(value);
    }
}
