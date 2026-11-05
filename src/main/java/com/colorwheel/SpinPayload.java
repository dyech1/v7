package com.colorwheel;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Servidor -> TODOS los jugadores: "gira la ruleta hasta este color" (con un texto opcional bajo la ruleta). */
public record SpinPayload(int index, float jitter, int durationMs, String label) implements CustomPayload {
    public static final Id<SpinPayload> ID = new Id<>(Identifier.of("colorwheel", "spin"));
    public static final PacketCodec<RegistryByteBuf, SpinPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SpinPayload::index,
            PacketCodecs.FLOAT, SpinPayload::jitter,
            PacketCodecs.VAR_INT, SpinPayload::durationMs,
            PacketCodecs.string(100), SpinPayload::label,
            SpinPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
