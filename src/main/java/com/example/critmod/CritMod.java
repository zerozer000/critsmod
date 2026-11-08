package com.example.critmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(CritMod.MODID)
public class CritMod {
    public static final String MODID = "critmod";
    public static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public CritMod(IEventBus modBus, ModContainer container) {
        ModEffects.EFFECTS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        ModPotions.POTIONS.register(modBus);
        ModComponents.COMPONENTS.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);

        modBus.addListener(ModItems::addCreative);
        modBus.addListener(ModNetwork::register);


        container.registerConfig(ModConfig.Type.COMMON, CritConfig.SPEC);
    }
}