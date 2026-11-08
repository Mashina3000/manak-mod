package com.example.smoothhud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Дерево улучшений: две ветки по 3 уровня, покупка за очки и BloodCoins. */
public final class SkillTreeScreen extends Screen {

    private static final int BTN_W = 96, BTN_H = 36, GAP = 10;

    public SkillTreeScreen() {
        super(Component.literal("Дерево улучшений"));
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private int rowY(int row) {
        return height / 2 - 30 + row * 60;
    }

    private int btnX(int i) {
        return width / 2 - (3 * BTN_W + 2 * GAP) / 2 + i * (BTN_W + GAP);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        g.fill(0, 0, width, height, 0xB0000000);
        Font f = font();
        g.drawCenteredString(f, "Дерево улучшений", width / 2, height / 2 - 84, 0xFFFFFF);
        g.drawCenteredString(f,
                "Очки: " + Progress.points() + "    BloodCoins: " + Progress.coins(),
                width / 2, height / 2 - 66, 0xFFD060);
        drawRow(g, f, mx, my, 0, Progress.DRAIN, "Выносливость: меньше расход при беге");
        drawRow(g, f, mx, my, 1, Progress.REGEN, "Восстановление выносливости");
        g.drawCenteredString(f, "Клик по ячейке покупает следующий уровень. ESC - закрыть",
                width / 2, height / 2 + 110, 0xAAAAAA);
    }

    private void drawRow(GuiGraphics g, Font f, int mx, int my, int row, String node, String title) {
        int y = rowY(row);
        g.drawString(f, title, btnX(0), y - 14, 0xFFFFFF, true);
        int lvl = Progress.level(node);
        for (int i = 0; i < Progress.MAX_LEVEL; i++) {
            int x = btnX(i);
            boolean owned = lvl > i;
            boolean next = lvl == i;
            int bg;
            if (owned) {
                bg = 0xFF4A7A2E;
            } else if (next && Progress.canAfford(i)) {
                bg = 0xFF7A5A1E;
            } else {
                bg = 0xFF2A2A2A;
            }
            boolean hover = mx >= x && mx < x + BTN_W && my >= y && my < y + BTN_H;
            if (hover && next) {
                bg = 0xFFA07A2A;
            }
            g.fill(x, y, x + BTN_W, y + BTN_H, bg);
            g.fill(x, y, x + BTN_W, y + 1, 0xFF000000);
            g.fill(x, y + BTN_H - 1, x + BTN_W, y + BTN_H, 0xFF000000);
            g.fill(x, y, x + 1, y + BTN_H, 0xFF000000);
            g.fill(x + BTN_W - 1, y, x + BTN_W, y + BTN_H, 0xFF000000);

            g.drawCenteredString(f, "Ур. " + (i + 1), x + BTN_W / 2, y + 6, 0xFFFFFF);
            String info = owned
                    ? "куплено"
                    : Progress.COST_POINTS[i] + " оч. / " + Progress.COST_COINS[i] + " монет";
            g.drawCenteredString(f, info, x + BTN_W / 2, y + 20, 0xDDDDDD);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && (tryBuy(mx, my, 0, Progress.DRAIN) || tryBuy(mx, my, 1, Progress.REGEN))) {
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean tryBuy(double mx, double my, int row, String node) {
        int y = rowY(row);
        for (int i = 0; i < Progress.MAX_LEVEL; i++) {
            int x = btnX(i);
            if (mx >= x && mx < x + BTN_W && my >= y && my < y + BTN_H) {
                Progress.upgrade(node);
                return true;
            }
        }
        return false;
    }
}
