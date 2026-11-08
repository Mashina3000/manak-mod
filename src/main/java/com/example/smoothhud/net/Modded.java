package com.example.smoothhud.net;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;

/** Серверный список игроков, у которых установлен мод. Обновляется при входе и выходе. */
public final class Modded {

    private static final Set<UUID> PLAYERS = new HashSet<>();

    private Modded() {
    }

    public static void add(ServerPlayer player) {
        if (player != null) {
            PLAYERS.add(player.getUUID());
        }
    }

    public static void remove(UUID id) {
        PLAYERS.remove(id);
    }

    public static boolean contains(UUID id) {
        return PLAYERS.contains(id);
    }

    public static List<UUID> othersThan(UUID self) {
        List<UUID> list = new ArrayList<>();
        for (UUID id : PLAYERS) {
            if (!id.equals(self)) {
                list.add(id);
            }
        }
        return list;
    }
}
