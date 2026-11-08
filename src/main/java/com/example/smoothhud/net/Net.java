package com.example.smoothhud.net;

import com.example.smoothhud.SmoothHud;
import com.example.smoothhud.client.Abilities;
import com.example.smoothhud.client.TeamClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** Сетевой канал мода: синхронизация карты и подсветки между игроками с модом. */
public final class Net {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SmoothHud.MODID, "main"),
            () -> PROTOCOL,
            NetworkRegistry.acceptMissingOr(PROTOCOL),
            NetworkRegistry.acceptMissingOr(PROTOCOL));

    private static int nextId;

    private Net() {
    }

    public static void register() {
        CHANNEL.registerMessage(nextId++, Hello.class, Hello::encode, Hello::decode, Hello::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(nextId++, TeamPositions.class, TeamPositions::encode, TeamPositions::decode,
                TeamPositions::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(nextId++, HighlightRequest.class, HighlightRequest::encode,
                HighlightRequest::decode, HighlightRequest::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(nextId++, HighlightReply.class, HighlightReply::encode,
                HighlightReply::decode, HighlightReply::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(nextId++, PlaceTrap.class, PlaceTrap::encode, PlaceTrap::decode,
                PlaceTrap::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    /** Клиент сообщает серверу, что у него установлен мод. */
    public static final class Hello {
        public Hello() {
        }

        static void encode(Hello msg, FriendlyByteBuf buf) {
        }

        static Hello decode(FriendlyByteBuf buf) {
            return new Hello();
        }

        static void handle(Hello msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context context = ctx.get();
            ServerPlayer sender = context.getSender();
            context.enqueueWork(() -> Modded.add(sender));
            context.setPacketHandled(true);
        }
    }

    /** Сервер раз в секунду присылает позиции всех игроков с модом. */
    public static final class TeamPositions {
        public record Entry(UUID id, String name, double x, double z) {
        }

        private final List<Entry> entries;

        public TeamPositions(List<Entry> entries) {
            this.entries = entries;
        }

        static void encode(TeamPositions msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.entries.size());
            for (Entry e : msg.entries) {
                buf.writeUUID(e.id());
                buf.writeUtf(e.name());
                buf.writeDouble(e.x());
                buf.writeDouble(e.z());
            }
        }

        static TeamPositions decode(FriendlyByteBuf buf) {
            int n = buf.readVarInt();
            List<Entry> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                list.add(new Entry(buf.readUUID(), buf.readUtf(), buf.readDouble(), buf.readDouble()));
            }
            return new TeamPositions(list);
        }

        static void handle(TeamPositions msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context context = ctx.get();
            context.enqueueWork(() -> {
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    TeamClient.setPositions(msg.entries);
                }
            });
            context.setPacketHandled(true);
        }
    }

    /** Маньяк просит подсветку игроков с модом. */
    public static final class HighlightRequest {
        public HighlightRequest() {
        }

        static void encode(HighlightRequest msg, FriendlyByteBuf buf) {
        }

        static HighlightRequest decode(FriendlyByteBuf buf) {
            return new HighlightRequest();
        }

        static void handle(HighlightRequest msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context context = ctx.get();
            ServerPlayer sender = context.getSender();
            context.enqueueWork(() -> {
                if (sender != null && Modded.contains(sender.getUUID())) {
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender),
                            new HighlightReply(Modded.othersThan(sender.getUUID())));
                }
            });
            context.setPacketHandled(true);
        }
    }

    /** Маньяк ставит ловушку под себя. Тип - индекс в списке ловушек. */
    public static final class PlaceTrap {
        private final int type;

        public PlaceTrap(int type) {
            this.type = type;
        }

        static void encode(PlaceTrap msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.type);
        }

        static PlaceTrap decode(FriendlyByteBuf buf) {
            return new PlaceTrap(buf.readVarInt());
        }

        static void handle(PlaceTrap msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context context = ctx.get();
            ServerPlayer sender = context.getSender();
            context.enqueueWork(() -> {
                if (sender != null && Modded.contains(sender.getUUID())) {
                    Traps.place(sender, msg.type);
                }
            });
            context.setPacketHandled(true);
        }
    }

    /** Сервер присылает список игроков с модом, которых нужно подсветить. */
    public static final class HighlightReply {
        private final List<UUID> ids;

        public HighlightReply(List<UUID> ids) {
            this.ids = ids;
        }

        static void encode(HighlightReply msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.ids.size());
            for (UUID id : msg.ids) {
                buf.writeUUID(id);
            }
        }

        static HighlightReply decode(FriendlyByteBuf buf) {
            int n = buf.readVarInt();
            List<UUID> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                list.add(buf.readUUID());
            }
            return new HighlightReply(list);
        }

        static void handle(HighlightReply msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context context = ctx.get();
            context.enqueueWork(() -> {
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    Abilities.applyHighlight(msg.ids);
                }
            });
            context.setPacketHandled(true);
        }
    }
}
