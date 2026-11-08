package com.example.smoothhud.net;

import com.example.smoothhud.SmoothHud;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = SmoothHud.MODID)
public final class ServerEvents {

    private static int counter;

    private ServerEvents() {
    }

    /** Раз в секунду (20 тиков) рассылаем позиции игрокам с модом. */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Traps.tick();
        counter++;
        if (counter % 20 != 0) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        List<Net.TeamPositions.Entry> entries = new ArrayList<>();
        List<ServerPlayer> recipients = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (Modded.contains(p.getUUID())) {
                entries.add(new Net.TeamPositions.Entry(
                        p.getUUID(), p.getGameProfile().getName(), p.getX(), p.getZ()));
                recipients.add(p);
            }
        }
        for (ServerPlayer p : recipients) {
            Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new Net.TeamPositions(entries));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            Modded.remove(p.getUUID());
        }
    }
}
