package com.example.smoothhud.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Экран роли при входе в игру. Текст зависит от выбранной роли. */
public final class Splash {

    private static final float DURATION = 5f;
    private static final float FADE_IN = 0.5f;
    private static final float FADE_OUT = 1f;

    private static float left;

    private Splash() {
    }

    public static void start() {
        left = DURATION;
    }

    public static void tick(float dt) {
        left = Math.max(0f, left - dt);
    }

    public static void draw(GuiGraphics g, Font font, int sw, int sh) {
        if (left <= 0f) {
            return;
        }
        float elapsed = DURATION - left;
        float a = Math.min(1f, elapsed / FADE_IN) * Math.min(1f, left / FADE_OUT);
        int y = sh / 3;

        boolean maniac = Progress.role() == Progress.ROLE_MANIAC;
        String role = maniac ? "Маньяк" : "Выживший";
        int roleColor = maniac ? 0xFF4040 : 0x55DDFF;
        String task = maniac ? "Ваша задача: охотиться" : "Ваша задача: бежать";
        String who = (maniac ? "Вы: " : "Маньяк: ") + ManiacScreen.NAMES[Progress.maniac()];

        g.drawCenteredString(font, "Ваша роль", sw / 2, y, withAlpha(a, 0xAAAAAA));

        g.pose().pushPose();
        g.pose().translate(sw / 2f, y + 14f, 0f);
        g.pose().scale(2f, 2f, 1f);
        g.drawCenteredString(font, role, 0, 0, withAlpha(a, roleColor));
        g.pose().popPose();

        g.drawCenteredString(font, task, sw / 2, y + 46, withAlpha(a, 0xFFFFFF));
        g.drawCenteredString(font, who, sw / 2, y + 60, withAlpha(a, 0xFFD060));
    }

    private static int withAlpha(float a, int rgb) {
        int alpha = Math.max(0, Math.min(255, Math.round(a * 255f)));
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }
}
