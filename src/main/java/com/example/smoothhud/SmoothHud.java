package com.example.smoothhud;

import com.example.smoothhud.entity.FlyingEyeEntity;
import com.example.smoothhud.entity.GhostNunEntity;
import com.example.smoothhud.item.CrowbarItem;
import com.example.smoothhud.item.FlashlightItem;
import com.example.smoothhud.item.ShockerItem;
import com.example.smoothhud.entity.MonashkaEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(SmoothHud.MODID)
public final class SmoothHud {

    public static final String MODID = "smoothhud";

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<MonashkaEntity>> MONASHKA = ENTITIES.register("monashka",
            () -> EntityType.Builder.of(MonashkaEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .build("monashka"));

    /** Летающий глаз: существует только на клиенте у маньяка, серверу не передаётся. */
    public static final RegistryObject<EntityType<FlyingEyeEntity>> FLYING_EYE = ENTITIES.register("flying_eye",
            () -> EntityType.Builder.of(FlyingEyeEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .build("flying_eye"));

    public static final RegistryObject<EntityType<GhostNunEntity>> GHOST_NUN = ENTITIES.register("ghost_nun",
            () -> EntityType.Builder.of(GhostNunEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .build("ghost_nun"));

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> CROWBAR = ITEMS.register("crowbar",
            () -> new CrowbarItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLASHLIGHT = ITEMS.register("flashlight",
            () -> new FlashlightItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHOCKER = ITEMS.register("shocker",
            () -> new ShockerItem(new Item.Properties().stacksTo(1)));

    public SmoothHud() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        com.example.smoothhud.net.Net.register();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class CommonEvents {

        private CommonEvents() {
        }

        @SubscribeEvent
        public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(CROWBAR.get());
                event.accept(FLASHLIGHT.get());
                event.accept(SHOCKER.get());
            }
        }

        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(MONASHKA.get(), MonashkaEntity.createAttributes().build());
        }
    }
}
