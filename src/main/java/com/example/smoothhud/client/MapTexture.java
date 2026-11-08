package com.example.smoothhud.client;

import com.example.smoothhud.SmoothHud;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

/** Текстура карты: 256x256 блоков вокруг игрока, цвет по верхнему блоку. */
public final class MapTexture {

    public static final int SIZE = 256;
    private static final ResourceLocation ID = new ResourceLocation(SmoothHud.MODID, "map");

    private static DynamicTexture texture;

    private MapTexture() {
    }

    public static ResourceLocation id() {
        return ID;
    }

    private static void ensure() {
        if (texture == null) {
            texture = new DynamicTexture(SIZE, SIZE, false);
            Minecraft.getInstance().getTextureManager().register(ID, texture);
        }
    }

    /** Перерисовывает карту вокруг точки (cx, cz). Вызывать не чаще раза в секунду. */
    public static void update(Minecraft mc, int cx, int cz) {
        Level level = mc.level;
        if (level == null) {
            return;
        }
        ensure();
        NativeImage img = texture.getPixels();
        if (img == null) {
            return;
        }
        int half = SIZE / 2;
        for (int px = 0; px < SIZE; px++) {
            for (int pz = 0; pz < SIZE; pz++) {
                int x = cx - half + px;
                int z = cz - half + pz;
                int abgr = 0xFF000000;
                if (level.hasChunkAt(new BlockPos(x, 64, z))) {
                    int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                    BlockPos pos = new BlockPos(x, y - 1, z);
                    BlockState state = level.getBlockState(pos);
                    MapColor mapColor = state.getMapColor(level, pos);
                    if (mapColor != MapColor.NONE) {
                        int rgb = mapColor.col;
                        float shade = 0.75f + 0.25f * (y / (float) level.getMaxBuildHeight());
                        int r = (int) (((rgb >> 16) & 255) * shade);
                        int g = (int) (((rgb >> 8) & 255) * shade);
                        int b = (int) ((rgb & 255) * shade);
                        abgr = 0xFF000000 | (b << 16) | (g << 8) | r;
                    }
                }
                img.setPixelRGBA(px, pz, abgr);
            }
        }
        texture.upload();
    }
}
