package com.example.critmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// duration: тиков до конца метки, 0 = метки нет, меньше 0 = бессрочно
public record MarkedPayload(int entityId, int duration) implements CustomPacketPayload {

    public static final Type<MarkedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CritMod.MODID, "marked"));

    public static final StreamCodec<ByteBuf, MarkedPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MarkedPayload::entityId,
            ByteBufCodecs.VAR_INT, MarkedPayload::duration,
            MarkedPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}