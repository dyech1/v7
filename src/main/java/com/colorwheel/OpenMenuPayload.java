package com.colorwheel;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Servidor -> admin: "abre el menú de colores". */
public record OpenMenuPayload() implements CustomPayload {
    public static final Id<OpenMenuPayload> ID = new Id<>(Identifier.of("colorwheel", "open_menu"));
    public static final PacketCodec<RegistryByteBuf, OpenMenuPayload> CODEC = PacketCodec.unit(new OpenMenuPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
