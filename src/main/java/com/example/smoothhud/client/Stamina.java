package com.example.smoothhud.client;

import net.minecraft.client.player.LocalPlayer;

/**
 * Выносливость, общая для HUD и управления бегом.
 * Считается в тиках игры (20 раз в секунду), поэтому не зависит от FPS.
 * Скорости по измерениям видео: ~10%/с падение при беге, ~25%/с восстановление.
 */
public final class Stamina {

    public static final float DRAIN_PER_SEC = 0.10f;
    public static final float REGEN_PER_SEC = 0.25f;
    /** Пока выносливость ниже этого порога после обнуления, бежать нельзя. */
    public static final float RECOVER_AT = 0.2f;

    private static float value = 1f;
    private static boolean exhausted;

    private Stamina() {
    }

    public static void tick(LocalPlayer player, float dt) {
        if (player.isCreative() || player.isSpectator()) {
            value = 1f;
            exhausted = false;
            return;
        }
        if (player.isSprinting() && !exhausted) {
            value = Math.max(0f, value - DRAIN_PER_SEC * Progress.drainMultiplier() * dt);
        } else {
            value = Math.min(1f, value + REGEN_PER_SEC * Progress.regenMultiplier() * dt);
        }
        if (value <= 0f) {
            exhausted = true;
        } else if (value >= RECOVER_AT) {
            exhausted = false;
        }
    }

    public static float value() {
        return value;
    }

    public static boolean isExhausted() {
        return exhausted;
    }
}
