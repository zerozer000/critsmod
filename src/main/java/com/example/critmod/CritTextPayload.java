package com.example.critmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CritTextPayload(double x, double y, double z, int kind, float damage)
        implements CustomPacketPayload {

    public static final Type<CritTextPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CritMod.MODID, "crit_text"));

    public static final StreamCodec<ByteBuf, CritTextPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, CritTextPayload::x,
            ByteBufCodecs.DOUBLE, CritTextPayload::y,
            ByteBufCodecs.DOUBLE, CritTextPayload::z,
            ByteBufCodecs.VAR_INT, CritTextPayload::kind,   // 0 = мини-крит, 1 = полный крит
            ByteBufCodecs.FLOAT, CritTextPayload::damage,
            CritTextPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}