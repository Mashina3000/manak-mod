package com.example.smoothhud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Выбор ловушек маньяка: ровно 3 из списка. Выбор сохраняется локально. */
public final class TrapScreen extends Screen {

    public static final String[] TRAPS = {"Капкан", "Мина", "Сеть", "Смоляная бомба", "Клетка", "Шип-ловушка"};

    private static final int W = 170, H = 40, GAP = 10;

    public TrapScreen() {
        super(Component.literal("Выбор ловушек"));
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private int x(int i) {
        return width / 2 - (2 * W + GAP) / 2 + (i % 2) * (W + GAP);
    }

    private int y(int i) {
        return height / 2 - 70 + (i / 2) * (H + GAP);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        g.fill(0, 0, width, height, 0xB0000000);
        Font f = font();
        g.drawCenteredString(f, "Выберите ловушки (3)", width / 2, height / 2 - 96, 0xFFFFFF);
        for (int i = 0; i < TRAPS.length; i++) {
            int x = x(i);
            int y = y(i);
            boolean sel = Progress.trapSelected(i);
            boolean hover = mx >= x && mx < x + W && my >= y && my < y + H;
            int bg = sel ? 0xFF6A4A1A : hover ? 0xFF3A3A3A : 0xFF262626;
            int border = sel ? 0xFFFFC040 : 0xFF000000;
            g.fill(x, y, x + W, y + H, bg);
            g.fill(x, y, x + W, y + 2, border);
            g.fill(x, y + H - 2, x + W, y + H, border);
            g.fill(x, y, x + 2, y + H, border);
            g.fill(x + W - 2, y, x + W, y + H, border);
            g.drawCenteredString(f, TRAPS[i], x + W / 2, y + 16, 0xFFFFFF);
        }
        g.drawCenteredString(f, "Выбрано: " + Progress.trapCount() + "/3   ESC - закрыть",
                width / 2, height / 2 + 100, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = 0; i < TRAPS.length; i++) {
                int x = x(i);
                int y = y(i);
                if (mx >= x && mx < x + W && my >= y && my < y + H) {
                    Progress.toggleTrap(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
