package com.example.critmod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@EventBusSubscriber(modid = CritMod.MODID)
public class MarkedSync {
    private static final Set<LivingEntity> TRACKED = new HashSet<>();

    // Метку только что наложили: запоминаем и сразу сообщаем клиентам
    @SubscribeEvent
    public static void onAdded(MobEffectEvent.Added event) {
        if (!event.getEffectInstance().getEffect().is(ModEffects.MARKED_FOR_DEATH.getId())) return;

        LivingEntity e = event.getEntity();
        if (e.level().isClientSide) return;

        TRACKED.add(e);
        send(e, event.getEffectInstance().getDuration());
    }

    // Раз в полсекунды обновляем время и ловим снятие метки (молоко, смерть, команда clear)
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 10 != 0) return;

        Iterator<LivingEntity> it = TRACKED.iterator();
        while (it.hasNext()) {
            LivingEntity e = it.next();
            MobEffectInstance inst = e.getEffect(ModEffects.MARKED_FOR_DEATH);

            if (e.isRemoved() || !e.isAlive() || inst == null) {
                send(e, 0);
                it.remove();
            } else {
                send(e, inst.getDuration());
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TRACKED.clear();
    }

    private static void send(LivingEntity e, int duration) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(e, new MarkedPayload(e.getId(), duration));
    }
}