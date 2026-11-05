package com.colorwheel;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Admin -> servidor: "aplica esta dificultad" (día y número). El servidor valida que sea admin. */
public record DifficultyRequestPayload(int day, int index) implements CustomPayload {
    public static final Id<DifficultyRequestPayload> ID = new Id<>(Identifier.of("colorwheel", "difficulty_request"));
    public static final PacketCodec<RegistryByteBuf, DifficultyRequestPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, DifficultyRequestPayload::day,
            PacketCodecs.VAR_INT, DifficultyRequestPayload::index,
            DifficultyRequestPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
