package com.example.smoothhud.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Прогресс игрока: очки, BloodCoins и уровни веток дерева улучшений.
 * Хранится локально в config/smoothhud-progress.json. Серверу не передаётся.
 */
public final class Progress {

    public static final String DRAIN = "drain";
    public static final String REGEN = "regen";

    public static final int MAX_LEVEL = 3;
    public static final int[] COST_POINTS = {1, 2, 3};
    public static final int[] COST_COINS = {10, 25, 50};

    private static final int TICKS_PER_COIN = 20 * 60;     // 1 монета в минуту
    private static final int TICKS_PER_POINT = 20 * 180;   // 1 очко каждые 3 минуты
    private static final int TICKS_PER_SAVE = 20 * 60;     // автосохранение раз в минуту

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Data {
        int points;
        int coins;
        int drain;
        int regen;
        int maniac;
        int role;
        int trapMask;
    }

    private static Data data = new Data();
    private static boolean loaded;
    private static int tickCounter;

    private Progress() {
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("smoothhud-progress.json");
    }

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path f = file();
        if (Files.exists(f)) {
            try (Reader r = Files.newBufferedReader(f)) {
                Data d = GSON.fromJson(r, Data.class);
                if (d != null) {
                    data = d;
                }
            } catch (IOException | RuntimeException e) {
                // Файл повреждён - начинаем с нуля
            }
        }
    }

    public static void save() {
        ensureLoaded();
        try (Writer w = Files.newBufferedWriter(file())) {
            GSON.toJson(data, w);
        } catch (IOException e) {
            // Не удалось записать - попробуем при следующем сохранении
        }
    }

    /** Вызывается раз в тик, пока игрок в мире. */
    public static void tick() {
        ensureLoaded();
        tickCounter++;
        if (tickCounter % TICKS_PER_COIN == 0) {
            data.coins++;
        }
        if (tickCounter % TICKS_PER_POINT == 0) {
            data.points++;
        }
        if (tickCounter % TICKS_PER_SAVE == 0) {
            save();
        }
    }

    public static int points() {
        ensureLoaded();
        return data.points;
    }

    public static int coins() {
        ensureLoaded();
        return data.coins;
    }

    public static int level(String node) {
        ensureLoaded();
        return DRAIN.equals(node) ? data.drain : data.regen;
    }

    /** Можно ли оплатить следующий уровень с индексом index (0..2). */
    public static boolean canAfford(int index) {
        ensureLoaded();
        return data.points >= COST_POINTS[index] && data.coins >= COST_COINS[index];
    }

    /** Покупает следующий уровень ветки. Возвращает true, если получилось. */
    public static boolean upgrade(String node) {
        ensureLoaded();
        int lvl = level(node);
        if (lvl >= MAX_LEVEL || !canAfford(lvl)) {
            return false;
        }
        data.points -= COST_POINTS[lvl];
        data.coins -= COST_COINS[lvl];
        if (DRAIN.equals(node)) {
            data.drain++;
        } else {
            data.regen++;
        }
        save();
        return true;
    }

    public static int maniac() {
        ensureLoaded();
        return Math.max(0, Math.min(ManiacScreen.NAMES.length - 1, data.maniac));
    }

    public static void setManiac(int index) {
        ensureLoaded();
        data.maniac = Math.max(0, Math.min(ManiacScreen.NAMES.length - 1, index));
        save();
    }

    public static final int ROLE_SURVIVOR = 0;
    public static final int ROLE_MANIAC = 1;

    public static int role() {
        ensureLoaded();
        return data.role == ROLE_MANIAC ? ROLE_MANIAC : ROLE_SURVIVOR;
    }

    public static void setRole(int role) {
        ensureLoaded();
        data.role = role == ROLE_MANIAC ? ROLE_MANIAC : ROLE_SURVIVOR;
        save();
    }

    public static boolean trapSelected(int index) {
        ensureLoaded();
        return (data.trapMask & (1 << index)) != 0;
    }

    public static int trapCount() {
        ensureLoaded();
        return Integer.bitCount(data.trapMask);
    }

    /** Выбрать или снять ловушку. Не больше трёх. */
    public static void toggleTrap(int index) {
        ensureLoaded();
        int bit = 1 << index;
        if ((data.trapMask & bit) != 0) {
            data.trapMask &= ~bit;
        } else if (Integer.bitCount(data.trapMask) < 3) {
            data.trapMask |= bit;
        }
        save();
    }

    /** Множитель расхода выносливости: 1.0, 0.85, 0.7, 0.55. */
    public static float drainMultiplier() {
        ensureLoaded();
        return 1f - 0.15f * data.drain;
    }

    /** Множитель восстановления выносливости: 1.0, 1.2, 1.4, 1.6. */
    public static float regenMultiplier() {
        ensureLoaded();
        return 1f + 0.2f * data.regen;
    }
}
