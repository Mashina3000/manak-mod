package com.example.smoothhud.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Шокер: бьёт ближайшего врага перед собой, замедляет его. Перезарядка 2 секунды. */
public final class ShockerItem extends Item {

    private static final double RANGE = 4.0D;
    private static final float DAMAGE = 4.0F;

    public ShockerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        player.getCooldowns().addCooldown(this, 40);
        if (!level.isClientSide) {
            LivingEntity target = findTarget(level, player);
            if (target != null) {
                target.hurt(level.damageSources().playerAttack(player), DAMAGE);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                level.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                        SoundSource.PLAYERS, 1.0F, 1.5F);
            }
        }
        return InteractionResultHolder.success(stack);
    }

    private static LivingEntity findTarget(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        LivingEntity best = null;
        double bestDist = RANGE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(RANGE), x -> x != player && x.isAlive())) {
            Vec3 to = e.position().subtract(player.position());
            double dist = to.length();
            if (dist > 0.01D && dist < bestDist && to.normalize().dot(look) > 0.85D) {
                best = e;
                bestDist = dist;
            }
        }
        return best;
    }
}
