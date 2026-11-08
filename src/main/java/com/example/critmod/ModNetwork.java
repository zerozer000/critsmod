package com.example.critmod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetwork {
    public static void register(RegisterPayloadHandlersEvent event) {
        CritMod.LOGGER.info("[crit] packet received");
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(CritTextPayload.TYPE, CritTextPayload.CODEC, (payload, ctx) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                CritTextRenderer.add(payload.x(), payload.y(), payload.z(),
                        payload.kind(), payload.damage());
            }
        });
        registrar.playToClient(MarkedPayload.TYPE, MarkedPayload.CODEC, (payload, ctx) -> {
            MarkedClient.set(payload.entityId(), payload.duration(),
                    ctx.player().level().getGameTime());
        });
    }
}