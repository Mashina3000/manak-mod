package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Arrays;

public final class SmoothHudOverlay implements IGuiOverlay {

    public static final SmoothHudOverlay INSTANCE = new SmoothHudOverlay();

    // ---- Текстуры ----
    private static final ResourceLocation TEX_BG = tex("hud/hud_bg.png");
    private static final ResourceLocation TEX_HEART = tex("hud/heart.png");
    private static final ResourceLocation TEX_GEM_RED = tex("hud/gem_red.png");
    private static final ResourceLocation TEX_GEM_BLUE = tex("hud/gem_blue.png");
    private static final ResourceLocation TEX_CELL = tex("inventory/cell.png");
    private static final ResourceLocation TEX_GLOW = tex("inventory/cell_glow.png");

    // ---- HUD здоровья и энергии: левый нижний угол ----
    private static final int HUD_W = 128, HUD_H = 44, HUD_MARGIN = 6;
    private static final int GEMS = 10;
    private static final int GEM_W = 6, GEM_H = 5;
    private static final int[] GEM_X = {35, 42, 50, 58, 67, 75, 83, 91, 99, 106};
    private static final int RED_Y = 15, BLUE_Y = 24;
    private static final int HEART_X = 15, HEART_Y = 16, HEART_W = 13, HEART_H = 12;

    // ---- Ромбовидная панель из 4 ячеек: правый нижний угол ----
    private static final int QUICK_SLOTS = 4;            // весь инвентарь: 4 ячейки
    private static final int ISO_HW = 22, ISO_HH = 12;   // полушаги изометрической сетки
    private static final int CELL_W = 44, CELL_H = 24;
    private static final int QUICK_MARGIN_X = 24;        // отступ справа
    private static final int BOTTOM_MARGIN = 14;         // отступ сетки от низа экрана

    // ---- Полоска опыта: внизу по центру ----
    private static final int XP_W = 182, XP_H = 5, XP_BOTTOM = 14;
    private static final int COL_XP = 0xFF7FDB2A;

    // ---- Анимации ----
    private static final float POP_TIME = 0.3f;
    private static final float POP_AMP = 0.35f;
    private static final float FLASH_TIME = 0.25f;


    private static final int COL_PANEL = 0x78000000;

    // ---- Состояние ----
    private final SmoothValue health = new SmoothValue(8f, 1.5f);
    private final SmoothValue stamina = new SmoothValue(8f, 1.5f);
    private final SmoothValue glowX = new SmoothValue(18f, 0f);
    private final SmoothValue glowY = new SmoothValue(18f, 0f);
    private final SmoothValue xp = new SmoothValue(10f, 0f);

    private final float[] redPrev = new float[GEMS];
    private final float[] bluePrev = new float[GEMS];
    private final float[] redPop = new float[GEMS];
    private final float[] bluePop = new float[GEMS];

    private float time;
    private float frameDt;
    private long lastNanos;
    private float flashT;
    private float prevHp = -1f;

    private SmoothHudOverlay() {
        Arrays.fill(redPop, POP_TIME);
        Arrays.fill(bluePop, POP_TIME);
    }

    private static ResourceLocation tex(String path) {
        return new ResourceLocation(SmoothHud.MODID, "textures/gui/" + path);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || player.isSpectator()) {
            return;
        }

        frameDt = deltaSeconds();
        time += frameDt;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Inventory inv = player.getInventory();

        float hpRatio = clamp01(player.getHealth() / Math.max(1f, player.getMaxHealth()));
        float staminaRatio = Stamina.value();

        if (prevHp >= 0f && hpRatio < prevHp - 0.001f) {
            flashT = FLASH_TIME;
        }
        flashT = Math.max(0f, flashT - frameDt);
        prevHp = hpRatio;

        health.update(hpRatio, frameDt);
        stamina.update(staminaRatio, frameDt);

        if (!player.isCreative()) {
            int hx = HUD_MARGIN;
            int hy = sh - HUD_H - HUD_MARGIN;
            drawHud(g, hx, hy, hpRatio);
        }

        drawVignette(g, sw, sh);
        if (!player.isCreative()) {
            drawQuickBar(g, mc.font, inv, sw, sh);
            drawXpBar(g, mc.font, player, sw, sh);
        }
        Abilities.drawHud(g, mc.font, sh);
        Splash.draw(g, mc.font, sw, sh);
    }

    // ------------------------------------------------------------------
    // HUD здоровья и энергии
    // ------------------------------------------------------------------
    private void drawHud(GuiGraphics g, int hx, int hy, float hpRatio) {
        g.blit(TEX_BG, hx, hy, 0, 0, HUD_W, HUD_H, HUD_W, HUD_H);
        drawHeart(g, hx, hy, hpRatio);
        drawGemRow(g, hx, hy + RED_Y, TEX_GEM_RED, health, redPrev, redPop, 0f);
        if (Stamina.isExhausted()) {
            RenderSystem.setShaderColor(1f, 0.45f, 0.45f, 1f);
        }
        drawGemRow(g, hx, hy + BLUE_Y, TEX_GEM_BLUE, stamina, bluePrev, bluePop, 0.5f);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void drawHeart(GuiGraphics g, int hx, int hy, float hpRatio) {
        float period = 0.5f + 1.2f * hpRatio;
        float t = (time % period) / period;
        float beat = bump(t, 0.07f, 0.04f) + 0.7f * bump(t, 0.25f, 0.04f);
        float s = 1f + 0.14f * beat;

        int w = Math.round(HEART_W * s);
        int h = Math.round(HEART_H * s);
        int x = hx + HEART_X + HEART_W / 2 - w / 2;
        int y = hy + HEART_Y + HEART_H / 2 - h / 2;
        g.blit(TEX_HEART, x, y, w, h, 0f, 0f, HEART_W, HEART_H, HEART_W, HEART_H);

        if (flashT > 0f) {
            float a = flashT / FLASH_TIME;
            RenderSystem.setShaderColor(1.6f, 1.3f, 1.3f, a);
            g.blit(TEX_HEART, x, y, w, h, 0f, 0f, HEART_W, HEART_H, HEART_W, HEART_H);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    private void drawGemRow(GuiGraphics g, int hx, int y, ResourceLocation tex,
                            SmoothValue v, float[] prev, float[] pop, float sheenOffset) {
        for (int i = 0; i < GEMS; i++) {
            int gx = hx + GEM_X[i];
            float fill = clamp01(v.value() * GEMS - i);
            float ghost = clamp01(v.ghost() * GEMS - i);

            if (fill >= 1f && prev[i] < 1f) {
                pop[i] = 0f;
            }
            prev[i] = fill;
            pop[i] = Math.min(POP_TIME, pop[i] + frameDt);

            if (ghost > fill) {
                RenderSystem.setShaderColor(1f, 1f, 1f, 0.5f);
                g.enableScissor(gx + Math.round(GEM_W * fill), y,
                        gx + Math.round(GEM_W * ghost), y + GEM_H);
                g.blit(tex, gx, y, GEM_W, GEM_H, 0f, 0f, GEM_W, GEM_H, GEM_W, GEM_H);
                g.disableScissor();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }

            if (fill <= 0f) {
                continue;
            }

            if (fill < 1f) {
                g.enableScissor(gx, y, gx + Math.round(GEM_W * fill), y + GEM_H);
                g.blit(tex, gx, y, GEM_W, GEM_H, 0f, 0f, GEM_W, GEM_H, GEM_W, GEM_H);
                g.disableScissor();
            } else {
                float s = pop[i] < POP_TIME
                        ? 1f + POP_AMP * (float) Math.sin(pop[i] / POP_TIME * Math.PI)
                        : 1f;
                int w = Math.round(GEM_W * s);
                int h = Math.round(GEM_H * s);
                g.blit(tex, gx + (GEM_W - w) / 2, y + (GEM_H - h) / 2, w, h,
                        0f, 0f, GEM_W, GEM_H, GEM_W, GEM_H);
            }

            float phase = (time * 0.45f + i * 0.07f + sheenOffset) % 1f;
            if (phase < 0.5f) {
                float sweep = phase / 0.5f;
                int sx = Math.round(gx - 3 + sweep * (GEM_W + 6));
                g.enableScissor(gx, y, gx + Math.round(GEM_W * fill), y + GEM_H);
                g.fill(sx, y + 1, sx + 3, y + GEM_H - 1, argb(0.45f));
                g.disableScissor();
            }
        }
    }

    // ------------------------------------------------------------------
    // Инвентарь: 4 ячейки (хотбар 1-4) по диагонали в правом нижнем углу.
    // Выбранная ячейка светится оранжевым и плавно переезжает.
    /** Красные края экрана: вспышка при уроне и пульс при низком здоровье. */
    private void drawVignette(GuiGraphics g, int sw, int sh) {
        float beat = 0.5f + 0.5f * (float) Math.sin(time * 6f);
        float alpha = Math.max(Vitals.flash() * 0.55f, Vitals.lowHpStrength() * (0.12f + 0.18f * beat));
        if (alpha <= 0.01f) {
            return;
        }
        int color = (Math.round(alpha * 255f) << 24) | 0xCC0000;
        int edge = Math.max(12, sw / 14);
        g.fill(0, 0, sw, edge, color);
        g.fill(0, sh - edge, sw, sh, color);
        g.fill(0, 0, edge, sh, color);
        g.fill(sw - edge, 0, sw, sh, color);
    }

    // ------------------------------------------------------------------
    private void drawQuickBar(GuiGraphics g, Font font, Inventory inv, int sw, int sh) {
        int sel = inv.selected;
        int baseX = sw - QUICK_MARGIN_X - 4 * ISO_HW;
        int baseY = sh - BOTTOM_MARGIN - ISO_HH;

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        for (int i = 0; i < QUICK_SLOTS; i++) {
            int cx = cellX(baseX, i);
            int cy = cellY(baseY, i);
            g.blit(TEX_CELL, cx - CELL_W / 2, cy - CELL_H / 2, 0, 0, CELL_W, CELL_H, CELL_W, CELL_H);
        }

        if (sel < QUICK_SLOTS) {
            glowX.update(cellX(baseX, sel), frameDt);
            glowY.update(cellY(baseY, sel), frameDt);
            float pulse = 0.75f + 0.25f * (float) Math.sin(time * 6f);
            RenderSystem.setShaderColor(1f, 1f, 1f, pulse);
            g.blit(TEX_GLOW,
                    Math.round(glowX.value()) - CELL_W / 2, Math.round(glowY.value()) - CELL_H / 2,
                    0, 0, CELL_W, CELL_H, CELL_W, CELL_H);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        for (int i = 0; i < QUICK_SLOTS; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            int cx = cellX(baseX, i);
            int cy = cellY(baseY, i);
            int bob = i == sel ? -Math.round(2f * (0.5f + 0.5f * (float) Math.sin(time * 5f))) : 0;
            g.renderItem(stack, cx - 8, cy - 8 + bob);
            g.renderItemDecorations(font, stack, cx - 8, cy - 8 + bob);
            if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
                int pct = Math.round(100f * (1f - stack.getDamageValue() / (float) stack.getMaxDamage()));
                String text = pct + "%";
                g.drawString(font, text, cx - font.width(text) / 2, cy + 6,
                        pct > 50 ? 0xFFE8E8E8 : pct > 20 ? 0xFFFFC040 : 0xFFFF5040, true);
            }
        }
    }

    // Ячейки 0-2: диагональ снизу-слева вверх-вправо. Ячейка 3: от верхней (2)
    // один шаг вниз-вправо, как на референсе.
    private static final int[] CELL_DX = {0, ISO_HW, 2 * ISO_HW, 3 * ISO_HW};
    private static final int[] CELL_DY = {0, -ISO_HH, -2 * ISO_HH, -ISO_HH};

    private static int cellX(int baseX, int i) {
        return baseX + CELL_DX[i];
    }

    private static int cellY(int baseY, int i) {
        return baseY + CELL_DY[i];
    }

    // ------------------------------------------------------------------
    // Полоска опыта (внизу по центру, ванильная скрыта)
    // ------------------------------------------------------------------
    private void drawXpBar(GuiGraphics g, Font font, LocalPlayer player, int sw, int sh) {
        xp.update(clamp01(player.experienceProgress), frameDt);

        int x = sw / 2 - XP_W / 2;
        int y = sh - XP_BOTTOM - XP_H;

        g.fill(x - 1, y - 1, x + XP_W + 1, y + XP_H + 1, COL_PANEL);
        int fillW = Math.round(XP_W * xp.value());
        if (fillW > 0) {
            g.fill(x, y, x + fillW, y + XP_H, COL_XP);
        }

        int level = player.experienceLevel;
        if (level > 0) {
            String text = String.valueOf(level);
            g.drawString(font, text, sw / 2 - font.width(text) / 2, y - 9, 0xFF80FF20, true);
        }
    }

    // ------------------------------------------------------------------
    // Вспомогательное
    // ------------------------------------------------------------------
    private float deltaSeconds() {
        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0f : (now - lastNanos) / 1_000_000_000f;
        lastNanos = now;
        return Math.min(dt, 0.1f);
    }

    private static float bump(float t, float center, float width) {
        float d = (t - center) / width;
        return (float) Math.exp(-0.5f * d * d);
    }

    private static int argb(float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        return (a << 24) | 0x00FFFFFF;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
