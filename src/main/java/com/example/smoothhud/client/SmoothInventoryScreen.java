package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Собственный инвентарь игрока в изометрической сетке (стиль второго скриншота).
 * Работает с меню игрока (InventoryMenu), поэтому предметы перекладываются
 * штатными пакетами и сохраняются на сервере.
 */
public final class SmoothInventoryScreen extends Screen {

    private static final ResourceLocation TEX_CELL = tex("inventory/cell.png");
    private static final ResourceLocation TEX_GLOW = tex("inventory/cell_glow.png");

    private static final int CELL_W = 44, CELL_H = 24;
    private static final int HW = 22, HH = 12;      // полушаги изометрической сетки

    private static final class Cell {
        final int col, row, menuSlot;
        float hover;                                 // 0..1, анимируется
        Cell(int col, int row, int menuSlot) {
            this.col = col;
            this.row = row;
            this.menuSlot = menuSlot;
        }
    }

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Cell> cells = new ArrayList<>();

    private float openT;
    private float time;
    private float frameDt;
    private long lastNanos;
    private float carryX, carryY;
    private boolean carryInit;

    public SmoothInventoryScreen() {
        super(Component.literal("Инвентарь"));
        // Весь инвентарь - 4 ячейки, те же, что в HUD (хотбар 1-4)
        for (int c = 0; c < 4; c++) {
            cells.add(new Cell(c, 0, 36 + c));
        }
    }

    private static ResourceLocation tex(String path) {
        return new ResourceLocation(SmoothHud.MODID, "textures/gui/" + path);
    }

    private int originX() {
        return width / 2;
    }

    private int originY() {
        return height / 2 - 40;
    }

    private static int cellX(Cell c, int ox) {
        return ox + (c.col - c.row - 2) * HW;
    }

    private static int cellY(Cell c, int oy) {
        return oy + (c.col + c.row - 5) * HH;
    }

    private Cell cellAt(int mx, int my) {
        int ox = originX(), oy = originY();
        for (Cell c : cells) {
            float dx = Math.abs(mx - cellX(c, ox)) / (float) HW;
            float dy = Math.abs(my - cellY(c, oy)) / (float) HH;
            if (dx + dy <= 1f) {
                return c;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Отрисовка
    // ------------------------------------------------------------------
    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        frameDt = deltaSeconds();
        time += frameDt;
        openT = Math.min(1f, openT + frameDt * 4f);
        float ease = 1f - (1f - openT) * (1f - openT) * (1f - openT);   // easeOutCubic

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Затемнение фона плавно появляется
        g.fill(0, 0, width, height, ((int) (150 * ease)) << 24);

        int ox = originX();
        int oy = originY() + (int) ((1f - ease) * 24f);   // выезд снизу при открытии

        Cell hovered = cellAt(mx, my);
        for (Cell c : cells) {
            float target = c == hovered ? 1f : 0f;
            c.hover += (target - c.hover) * Math.min(1f, frameDt * 12f);
        }

        Player player = mc.player;
        if (player == null) {
            return;
        }

        g.drawCenteredString(mc.font, "Инвентарь", ox, oy - 104, argb(ease, 0xFFFFFF));

        // Ячейки и подсветка
        for (Cell c : cells) {
            int cx = cellX(c, ox);
            int cy = cellY(c, oy);
            RenderSystem.setShaderColor(1f, 1f, 1f, ease);
            g.blit(TEX_CELL, cx - CELL_W / 2, cy - CELL_H / 2, 0, 0, CELL_W, CELL_H, CELL_W, CELL_H);

            if (c.hover > 0.01f) {
                float pulse = 0.8f + 0.2f * (float) Math.sin(time * 6f);
                RenderSystem.setShaderColor(1f, 1f, 1f, ease * c.hover * pulse);
                g.blit(TEX_GLOW, cx - CELL_W / 2, cy - CELL_H / 2, 0, 0, CELL_W, CELL_H, CELL_W, CELL_H);
            }
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        // Предметы (поверх подсветки)
        for (Cell c : cells) {
            ItemStack stack = player.inventoryMenu.getSlot(c.menuSlot).getItem();
            drawItem(g, cellX(c, ox), cellY(c, oy), stack, c.hover, ease);
        }

        // Предмет, который держит курсор - плавно следует за мышью
        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty()) {
            if (!carryInit) {
                carryX = mx;
                carryY = my;
                carryInit = true;
            }
            float k = Math.min(1f, frameDt * 20f);
            carryX += (mx - carryX) * k;
            carryY += (my - carryY) * k;
            g.pose().pushPose();
            g.pose().translate(carryX, carryY, 200);
            g.renderItem(carried, -8, -8);
            g.renderItemDecorations(mc.font, carried, -8, -8);
            g.pose().popPose();
        } else {
            carryInit = false;
        }

        // Подсказка с названием предмета, если курсор над предметом и рука пуста
        if (hovered != null && carried.isEmpty()) {
            ItemStack stack = player.inventoryMenu.getSlot(hovered.menuSlot).getItem();
            if (!stack.isEmpty()) {
                g.renderTooltip(mc.font, stack, mx, my);
            }
        }
    }

    private void drawItem(GuiGraphics g, int cx, int cy, ItemStack stack, float hover, float ease) {
        if (stack.isEmpty()) {
            return;
        }
        float s = 1f + 0.18f * hover;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(s, s, 1f);
        g.renderItem(stack, -8, -8);
        g.renderItemDecorations(mc.font, stack, -8, -8);
        g.pose().popPose();

        // Прочность в процентах, как на скриншоте ("100%")
        if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
            int pct = Math.round(100f * (1f - stack.getDamageValue() / (float) stack.getMaxDamage()));
            String text = pct + "%";
            int color = pct > 50 ? 0xFFE8E8E8 : pct > 20 ? 0xFFFFC040 : 0xFFFF5040;
            g.drawString(mc.font, text, cx - mc.font.width(text) / 2, cy + CELL_H / 2 + 1,
                    (color & 0x00FFFFFF) | ((int) (255 * ease) << 24), true);
        }
    }

    // ------------------------------------------------------------------
    // Ввод
    // ------------------------------------------------------------------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        Player player = mc.player;
        if (player == null || mc.gameMode == null) {
            return super.mouseClicked(mx, my, button);
        }
        Cell cell = cellAt((int) mx, (int) my);
        if (cell != null) {
            ClickType type = Screen.hasShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP;
            mc.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, cell.menuSlot,
                    button, type, player);
            return true;
        }
        // Клик вне сетки с предметом в руке - выбросить
        if (!player.containerMenu.getCarried().isEmpty()) {
            mc.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, -999,
                    button, ClickType.PICKUP, player);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (mc.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (mc.player != null) {
            mc.player.closeContainer();
        }
        super.onClose();
    }

    // ------------------------------------------------------------------
    // Утилиты
    // ------------------------------------------------------------------
    private float deltaSeconds() {
        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0f : (now - lastNanos) / 1_000_000_000f;
        lastNanos = now;
        return Math.min(dt, 0.1f);
    }

    private static int argb(float alpha, int rgb) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        return (a << 24) | (rgb & 0x00FFFFFF);
    }
}
