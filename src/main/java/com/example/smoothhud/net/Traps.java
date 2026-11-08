package com.example.smoothhud.net;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Серверные ловушки маньяка. Срабатывают на любого игрока, кроме владельца.
 * Типы совпадают с порядком списка TrapScreen.TRAPS на клиенте.
 */
public final class Traps {

    private static final int MAX_AGE = 20 * 60 * 5;      // 5 минут
    private static final int COOLDOWN_MS = 10_000;        // 10 секунд между установками
    private static final int MAX_ACTIVE_PER_OWNER = 3;
    private static final double TRIGGER_RADIUS_SQ = 1.5D * 1.5D;

    private static final class Trap {
        final ServerLevel level;
        final double x, y, z;
        final int type;
        final UUID owner;
        int age;

        Trap(ServerLevel level, double x, double y, double z, int type, UUID owner) {
            this.level = level;
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
            this.owner = owner;
        }
    }

    private static final List<Trap> TRAPS = new ArrayList<>();
    private static final Map<UUID, Long> LAST_PLACED = new HashMap<>();

    private Traps() {
    }

    public static void place(ServerPlayer owner, int type) {
        long now = System.currentTimeMillis();
        Long last = LAST_PLACED.get(owner.getUUID());
        if (last != null && now - last < COOLDOWN_MS) {
            return;
        }
        long active = TRAPS.stream().filter(t -> t.owner.equals(owner.getUUID())).count();
        if (active >= MAX_ACTIVE_PER_OWNER) {
            return;
        }
        LAST_PLACED.put(owner.getUUID(), now);
        TRAPS.add(new Trap(owner.serverLevel(), owner.getX(), owner.getY(), owner.getZ(),
                Math.max(0, Math.min(5, type)), owner.getUUID()));
        owner.serverLevel().sendParticles(ParticleTypes.SMOKE,
                owner.getX(), owner.getY() + 0.1D, owner.getZ(), 12, 0.3D, 0.05D, 0.3D, 0.01D);
    }

    /** Вызывается раз в тик сервера. */
    public static void tick() {
        Iterator<Trap> it = TRAPS.iterator();
        while (it.hasNext()) {
            Trap trap = it.next();
            trap.age++;
            if (trap.age > MAX_AGE) {
                it.remove();
                continue;
            }
            for (ServerPlayer p : trap.level.players()) {
                if (p.getUUID().equals(trap.owner) || !p.isAlive()) {
                    continue;
                }
                double dx = p.getX() - trap.x;
                double dz = p.getZ() - trap.z;
                if (dx * dx + dz * dz < TRIGGER_RADIUS_SQ) {
                    trigger(trap, p);
                    it.remove();
                    break;
                }
            }
        }
    }

    private static void trigger(Trap trap, ServerPlayer victim) {
        switch (trap.type) {
            case 0 -> {   // Капкан
                victim.hurt(trap.level.damageSources().generic(), 4.0F);
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
            }
            case 1 -> {   // Мина
                victim.hurt(trap.level.damageSources().generic(), 8.0F);
            }
            case 2 -> {   // Сеть
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
            }
            case 3 -> {   // Смоляная бомба
                victim.hurt(trap.level.damageSources().generic(), 6.0F);
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
            case 4 -> {   // Клетка
                victim.hurt(trap.level.damageSources().generic(), 3.0F);
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3));
            }
            default -> {  // Шип-ловушка
                victim.hurt(trap.level.damageSources().generic(), 5.0F);
            }
        }
        trap.level.playSound(null, trap.x, trap.y, trap.z,
                SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 1.0F, 0.8F);
    }
}
