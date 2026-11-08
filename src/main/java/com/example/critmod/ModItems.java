package com.example.critmod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CritMod.MODID);

    public static final DeferredItem<CritColaItem> CRIT_COLA = ITEMS.registerItem("crit_cola",
            props -> new CritColaItem(props
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(0)
                            .saturationModifier(0f)
                            .alwaysEdible()
                            .effect(() -> new MobEffectInstance(ModEffects.CRIT_BOOST, 20 * 10), 1.0f)
                            .build())));

    public static final DeferredItem<Item> CRIT_ESSENCE =
            ITEMS.registerSimpleItem("strangifier_crit", new Item.Properties().stacksTo(16));

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(CRIT_COLA);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(CRIT_ESSENCE);
        }
    }
}