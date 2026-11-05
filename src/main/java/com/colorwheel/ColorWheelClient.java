package com.colorwheel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class ColorWheelClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Admin: abrir el menú
        ClientPlayNetworking.registerGlobalReceiver(OpenMenuPayload.ID, (payload, context) ->
                context.client().setScreen(new ColorWheelScreen()));

        // Todos: ver girar la ruleta sobre la pantalla
        ClientPlayNetworking.registerGlobalReceiver(SpinPayload.ID, (payload, context) ->
                WheelHud.start(payload.index(), payload.jitter(), payload.durationMs(), payload.label()));

        HudRenderCallback.EVENT.register(WheelHud::render);

        // La grieta en el cielo cuando sale rojo
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CrackRenderer::render);
    }
}
