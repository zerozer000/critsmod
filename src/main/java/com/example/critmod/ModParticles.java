package com.example.critmod;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, CritMod.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRIT_SPARK =
            PARTICLES.register("crit_spark", () -> new SimpleParticleType(false));
}