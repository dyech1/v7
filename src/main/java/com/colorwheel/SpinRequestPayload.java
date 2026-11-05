package com.colorwheel;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Admin -> servidor: color elegido + texto para el chat. El servidor valida que sea admin. */
public record SpinRequestPayload(int index, String message) implements CustomPayload {
    public static final Id<SpinRequestPayload> ID = new Id<>(Identifier.of("colorwheel", "spin_request"));
    public static final PacketCodec<RegistryByteBuf, SpinRequestPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SpinRequestPayload::index,
            PacketCodecs.string(256), SpinRequestPayload::message,
            SpinRequestPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
