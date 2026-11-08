package com.example.smoothhud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;

/**
 * Состояние здоровья для эффектов: вспышка при уроне, сердцебиение при низком HP.
 * Обновляется каждый тик игры.
 */
public final class Vitals {

    /** Ниже этого уровня HP начинается сердцебиение и виньетка. */
    public static final float LOW_HP = 0.3f;

    private static float prevHp = -1f;
    private static float flash;
    private static float beatTimer;
    private static float hpRatio = 1f;

    private Vitals() {
    }

    public static void tick(LocalPlayer player, float dt) {
        hpRatio = clamp01(player.getHealth() / Math.max(1f, player.getMaxHealth()));

        if (prevHp >= 0f && hpRatio < prevHp - 0.001f) {
            flash = 1f;
        }
        prevHp = hpRatio;
        flash = Math.max(0f, flash - dt * 2.5f);

        if (hpRatio < LOW_HP && player.isAlive()) {
            beatTimer -= dt;
            if (beatTimer <= 0f) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASEDRUM.value(), 0.6f, 0.4f));
                // Чем меньше HP, тем быстрее удары
                beatTimer = 0.45f + hpRatio * 1.4f;
            }
        } else {
            beatTimer = 0f;
        }
    }

    /** Вспышка при уроне: 1 сразу после удара, затем гаснет. */
    public static float flash() {
        return flash;
    }

    /** Сила низкого HP: 0 у порога, 1 при нуле. */
    public static float lowHpStrength() {
        if (hpRatio >= LOW_HP) {
            return 0f;
        }
        return 1f - hpRatio / LOW_HP;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
