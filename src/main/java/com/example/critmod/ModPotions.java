package com.example.critmod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = CritMod.MODID)
public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, CritMod.MODID);

    public static final DeferredHolder<Potion, Potion> MARKED_FOR_DEATH =
            POTIONS.register("marked_for_death",
                    () -> new Potion(new MobEffectInstance(ModEffects.MARKED_FOR_DEATH, 20 * 45)));

    // Первый аргумент задаёт название для перевода, поэтому оно общее с обычным зельем
    public static final DeferredHolder<Potion, Potion> LONG_MARKED_FOR_DEATH =
            POTIONS.register("long_marked_for_death",
                    () -> new Potion("marked_for_death",
                            new MobEffectInstance(ModEffects.MARKED_FOR_DEATH, 20 * 90)));

    @SubscribeEvent
    public static void onBrewing(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();

        // Корявое зелье + череп скелета = Метка смерти
        builder.addMix(Potions.AWKWARD, Items.SKELETON_SKULL, MARKED_FOR_DEATH);

        // Метка смерти + красная пыль = длинная версия
        builder.addMix(MARKED_FOR_DEATH, Items.REDSTONE, LONG_MARKED_FOR_DEATH);
    }
}