package com.example.smoothhud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Выбор маньяка. Клик по карточке делает тебя маньяком и открывает выбор ловушек. */
public final class ManiacScreen extends Screen {

    public static final String[] NAMES = {"Японская гейша", "Охотник", "Тень", "Кукольник"};
    public static final String[] DESCRIPTIONS = {"Быстрые удары", "Видит следы", "Короткая невидимость", "Ставит ловушки"};

    private static final int CARD_W = 150, CARD_H = 110, GAP = 14;
    private static final int BTN_W = 170, BTN_H = 24;

    public ManiacScreen() {
        super(Component.literal("Выбор маньяка"));
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private int cardX(int i) {
        return width / 2 - (NAMES.length * CARD_W + (NAMES.length - 1) * GAP) / 2 + i * (CARD_W + GAP);
    }

    private int cardY() {
        return height / 2 - CARD_H / 2;
    }

    private int btnX() {
        return width / 2 - BTN_W / 2;
    }

    private int btnY() {
        return cardY() + CARD_H + 44;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        g.fill(0, 0, width, height, 0xB0000000);
        Font f = font();
        g.drawCenteredString(f, "Выбор маньяка", width / 2, cardY() - 40, 0xFFFFFF);

        int sel = Progress.maniac();
        int y = cardY();
        for (int i = 0; i < NAMES.length; i++) {
            int x = cardX(i);
            boolean hover = mx >= x && mx < x + CARD_W && my >= y && my < y + CARD_H;
            int bg = i == sel ? 0xFF6A4A1A : hover ? 0xFF3A3A3A : 0xFF262626;
            g.fill(x, y, x + CARD_W, y + CARD_H, bg);
            int border = i == sel ? 0xFFFFC040 : 0xFF000000;
            g.fill(x, y, x + CARD_W, y + 2, border);
            g.fill(x, y + CARD_H - 2, x + CARD_W, y + CARD_H, border);
            g.fill(x, y, x + 2, y + CARD_H, border);
            g.fill(x + CARD_W - 2, y, x + CARD_W, y + CARD_H, border);
            g.drawCenteredString(f, NAMES[i], x + CARD_W / 2, y + 16, 0xFFFFFF);
            g.drawCenteredString(f, DESCRIPTIONS[i], x + CARD_W / 2, y + 52, 0xDDDDDD);
        }

        boolean bh = mx >= btnX() && mx < btnX() + BTN_W && my >= btnY() && my < btnY() + BTN_H;
        g.fill(btnX(), btnY(), btnX() + BTN_W, btnY() + BTN_H, bh ? 0xFF3A5A3A : 0xFF263A26);
        g.drawCenteredString(f, "Играть выжившим", width / 2, btnY() + 8, 0xFFFFFF);

        g.drawCenteredString(f, "Клик по карточке - стать маньяком и выбрать ловушки. ESC - закрыть",
                width / 2, cardY() + CARD_H + 26, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int y = cardY();
            for (int i = 0; i < NAMES.length; i++) {
                int x = cardX(i);
                if (mx >= x && mx < x + CARD_W && my >= y && my < y + CARD_H) {
                    Progress.setManiac(i);
                    Progress.setRole(Progress.ROLE_MANIAC);
                    Minecraft.getInstance().setScreen(new TrapScreen());
                    return true;
                }
            }
            if (mx >= btnX() && mx < btnX() + BTN_W && my >= btnY() && my < btnY() + BTN_H) {
                Progress.setRole(Progress.ROLE_SURVIVOR);
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
