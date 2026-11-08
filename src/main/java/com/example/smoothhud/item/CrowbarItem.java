package com.example.smoothhud.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/** Лом: ближний бой, урон как у железного меча, но медленнее. */
public final class CrowbarItem extends SwordItem {

    public CrowbarItem(Item.Properties properties) {
        super(Tiers.IRON, 4, -2.6F, properties);
    }
}
