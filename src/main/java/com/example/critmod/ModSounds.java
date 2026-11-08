package com.example.critmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, CritMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> CRIT_HIT = register("crit_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINI_CRIT_HIT = register("mini_crit_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRIT_SPARK = register("crit_spark");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(CritMod.MODID, name)));
    }

}