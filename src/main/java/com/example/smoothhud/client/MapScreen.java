package com.example.smoothhud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import com.example.smoothhud.net.Net;
import net.minecraft.util.Mth;

/** Карта: показывает загруженную местность вокруг игрока, игрок в центре. */
public final class MapScreen extends Screen {

    private static final int VIEW = 360;
    private static final int REFRESH_FRAMES = 30;
    private int refresh;

    public MapScreen() {
        super(Component.literal("Карта"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        g.fill(0, 0, width, height, 0xD0000000);

        LocalPlayer p = mc.player;
        if (p == null) {
            return;
        }
        int px = Mth.floor(p.getX());
        int pz = Mth.floor(p.getZ());

        if (refresh <= 0) {
            MapTexture.update(mc, px, pz);
            refresh = REFRESH_FRAMES;
        }
        refresh--;

        int x0 = width / 2 - VIEW / 2;
        int y0 = height / 2 - VIEW / 2;
        g.fill(x0 - 2, y0 - 2, x0 + VIEW + 2, y0 + VIEW + 2, 0xFF000000);
        g.blit(MapTexture.id(), x0, y0, VIEW, VIEW, 0f, 0f,
                MapTexture.SIZE, MapTexture.SIZE, MapTexture.SIZE, MapTexture.SIZE);

        // Маркер игрока в центре
        int cx = width / 2;
        int cy = height / 2;
        g.fill(cx - 4, cy - 4, cx + 4, cy + 4, 0xFFFFFFFF);
        g.fill(cx - 3, cy - 3, cx + 3, cy + 3, 0xFFE02020);

        // Подсветка игроков маньяка (5 секунд)
        // Общая карта: игроки с модом (зелёные точки)
        float tScale = VIEW / (float) MapTexture.SIZE;
        for (Net.TeamPositions.Entry e : TeamClient.team()) {
            if (e.id().equals(p.getUUID())) {
                continue;
            }
            int tx = width / 2 + Math.round((float) (e.x() - p.getX()) * tScale);
            int ty = height / 2 + Math.round((float) (e.z() - p.getZ()) * tScale);
            if (tx >= x0 && tx < x0 + VIEW && ty >= y0 && ty < y0 + VIEW) {
                g.fill(tx - 3, ty - 3, tx + 3, ty + 3, 0xFF40FF40);
                g.drawString(f, e.name(), tx + 5, ty - 4, 0x80FF80, true);
            }
        }

        if (Abilities.highlightActive() && mc.level != null) {
            float scale = VIEW / (float) MapTexture.SIZE;
            for (Player other : mc.level.players()) {
                if (other == p || !Abilities.isHighlighted(other.getUUID())) {
                    continue;
                }
                int ox = width / 2 + Math.round((float) (other.getX() - p.getX()) * scale);
                int oy = height / 2 + Math.round((float) (other.getZ() - p.getZ()) * scale);
                if (ox >= x0 && ox < x0 + VIEW && oy >= y0 && oy < y0 + VIEW) {
                    g.fill(ox - 3, oy - 3, ox + 3, oy + 3, 0xFFFF3030);
                }
            }
        }

        g.drawCenteredString(f, "Карта", width / 2, y0 - 16, 0xFFFFFF);
        g.drawCenteredString(f, "X: " + px + "   Z: " + pz, width / 2, y0 + VIEW + 8, 0xFFD060);
        g.drawCenteredString(f, "M или ESC - закрыть", width / 2, y0 + VIEW + 22, 0xAAAAAA);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModClientEvents.OPEN_MAP.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
