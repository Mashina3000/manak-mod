package com.example.smoothhud.client;

import com.example.smoothhud.net.Net;
import net.minecraft.client.Minecraft;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Позиции игроков с модом, присланные сервером. */
public final class TeamClient {

    private static final Map<UUID, Net.TeamPositions.Entry> TEAM = new LinkedHashMap<>();
    private static boolean helloPending;

    private TeamClient() {
    }

    public static void onLogin() {
        TEAM.clear();
        helloPending = true;
    }

    public static void onLogout() {
        TEAM.clear();
        helloPending = false;
    }

    /** Отправляет серверу сообщение о моде, когда игрок уже в мире. */
    public static void tick(Minecraft mc) {
        if (helloPending && mc.player != null) {
            Net.CHANNEL.sendToServer(new Net.Hello());
            helloPending = false;
        }
    }

    public static void setPositions(List<Net.TeamPositions.Entry> list) {
        TEAM.clear();
        for (Net.TeamPositions.Entry e : list) {
            TEAM.put(e.id(), e);
        }
    }

    public static Collection<Net.TeamPositions.Entry> team() {
        return TEAM.values();
    }
}
