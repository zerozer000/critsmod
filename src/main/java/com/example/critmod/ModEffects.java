package com.example.critmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, CritMod.MODID);

    public static final DeferredHolder<MobEffect, CritBoostEffect> CRIT_BOOST =
            EFFECTS.register("crit_boost", CritBoostEffect::new);

    public static class CritBoostEffect extends MobEffect {
        public CritBoostEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xFF3030);
        }
    }

    public static final DeferredHolder<MobEffect, MarkedEffect> MARKED_FOR_DEATH =
            EFFECTS.register("marked_for_death", MarkedEffect::new);

    public static class MarkedEffect extends MobEffect {
        public MarkedEffect() {
            super(MobEffectCategory.HARMFUL, 0xFFFFFF);
        }
    }
}