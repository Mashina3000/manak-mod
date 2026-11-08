package com.example.smoothhud.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Ручной фонарь: даёт ночное зрение на 30 секунд, перезарядка 2 секунды. */
public final class FlashlightItem extends Item {

    public FlashlightItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false));
        player.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.success(stack);
    }
}
