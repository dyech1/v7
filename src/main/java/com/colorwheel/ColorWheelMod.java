package com.colorwheel;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorWheelMod implements ModInitializer {
    public static final String MOD_ID = "colorwheel";
    public static final Identifier SPIN_ID = Identifier.of(MOD_ID, "spin");
    public static final SoundEvent SPIN_SOUND = SoundEvent.of(SPIN_ID);

    /** Nivel de permiso necesario (2 = operador/admin). */
    public static final int PERMISO_ADMIN = 2;
    /** Duración del giro en milisegundos (el sonido dura ~13 s). */
    public static final int SPIN_MS = 12_000;

    private static final Random RANDOM = new Random();
    private static int ticksLeft = 0;
    private static int activeIndex = -1;
    private static String activeMessage = "";
    private static final int MAX_MENSAJE = 200;
    private static Dificultad activeDificultad = null;
    private static int activeDay = 0;

    /** Comandos para deshacer una dificultad temporal cuando se acaba su tiempo. */
    private record Pendiente(int[] ticks, List<String> comandos) {}
    private static final List<Pendiente> pendientes = new ArrayList<>();

    @Override
    public void onInitialize() {
        Registry.register(Registries.SOUND_EVENT, SPIN_ID, SPIN_SOUND);

        PayloadTypeRegistry.playS2C().register(OpenMenuPayload.ID, OpenMenuPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpinPayload.ID, SpinPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SpinRequestPayload.ID, SpinRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DifficultyRequestPayload.ID, DifficultyRequestPayload.CODEC);

        // /ruleta: solo la ven y la usan los admins (nivel de permiso 2)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("ruleta")
                        .requires(src -> src.hasPermissionLevel(PERMISO_ADMIN))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            if (ServerPlayNetworking.canSend(player, OpenMenuPayload.ID)) {
                                ServerPlayNetworking.send(player, new OpenMenuPayload());
                            }
                            return 1;
                        })));

        // El admin eligió un color en el menú
        ServerPlayNetworking.registerGlobalReceiver(SpinRequestPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            // Se valida en el servidor, así nadie puede saltarse el permiso
            if (!player.hasPermissionLevel(PERMISO_ADMIN)) return;
            int i = payload.index();
            if (i < 0 || i >= WheelColor.COUNT) return;
            String msg = payload.message() == null ? "" : payload.message().trim();
            if (msg.length() > MAX_MENSAJE) msg = msg.substring(0, MAX_MENSAJE);
            iniciarGiro(context.server(), player, i, msg, null, 0, "");
        });

        // El admin eligió una dificultad: la ruleta siempre cae en amarillo
        ServerPlayNetworking.registerGlobalReceiver(DifficultyRequestPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!player.hasPermissionLevel(PERMISO_ADMIN)) return;
            int d = payload.day(), n = payload.index();
            if (d < 0 || d >= Dificultades.DIAS || n < 0 || n >= Dificultades.POR_DIA) return;
            Dificultad dif = Dificultades.LISTA[d][n];
            iniciarGiro(context.server(), player, Dificultades.COLOR_AMARILLO, "", dif, d + 1,
                    "Día " + (d + 1) + " · " + dif.nombre());
        });

        // Cuando termina el giro, el servidor avisa a todos y aplica la dificultad
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (ticksLeft > 0 && --ticksLeft == 0) {
                alTerminar(server, WheelColor.values()[activeIndex], activeMessage, activeDificultad, activeDay);
                activeIndex = -1;
                activeMessage = "";
                activeDificultad = null;
            }
            // Dificultades temporales: deshacer cuando se acaba el tiempo
            for (int i = pendientes.size() - 1; i >= 0; i--) {
                Pendiente p = pendientes.get(i);
                if (--p.ticks()[0] <= 0) {
                    pendientes.remove(i);
                    ejecutar(server, p.comandos());
                }
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            // Si el servidor se apaga a medias, se deshacen las dificultades temporales
            for (Pendiente p : pendientes) ejecutar(server, p.comandos());
            pendientes.clear();
            ticksLeft = 0; activeIndex = -1; activeMessage = ""; activeDificultad = null;
        });
    }

    /** Arranca la ruleta para todos los jugadores. */
    private static void iniciarGiro(MinecraftServer server, ServerPlayerEntity admin, int colorIndex, String mensaje,
                                    Dificultad dificultad, int dia, String etiqueta) {
        if (ticksLeft > 0) {
            admin.sendMessage(Text.literal("La ruleta ya está girando.").formatted(Formatting.RED), true);
            return;
        }
        activeMessage = mensaje;
        activeIndex = colorIndex;
        activeDificultad = dificultad;
        activeDay = dia;
        ticksLeft = SPIN_MS / 50;
        float jitter = (RANDOM.nextFloat() - 0.5f) * 28f; // desvío dentro del segmento
        SpinPayload spin = new SpinPayload(colorIndex, jitter, SPIN_MS, etiqueta);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (ServerPlayNetworking.canSend(p, SpinPayload.ID)) {
                ServerPlayNetworking.send(p, spin);
            }
        }
    }

    /** Ejecuta comandos con permisos de consola (solo los que están escritos en Dificultades.java). */
    private static void ejecutar(MinecraftServer server, List<String> comandos) {
        for (String c : comandos) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), c);
        }
    }

    /** Se ejecuta en el servidor cuando la ruleta se detiene. */
    private static void alTerminar(MinecraftServer server, WheelColor color, String mensaje, Dificultad dif, int dia) {
        server.getPlayerManager().broadcast(
                Text.literal("¡Salió ").formatted(Formatting.GOLD)
                        .append(Text.literal(color.nombre).styled(s -> s.withColor(color.rgb).withBold(true)))
                        .append(Text.literal("!").formatted(Formatting.GOLD)),
                false);
        if (!mensaje.isEmpty()) {
            // Se permiten códigos de color con & (por ejemplo &a = verde, &l = negrita)
            server.getPlayerManager().broadcast(Text.literal(mensaje.replaceAll("&([0-9a-fk-or])", "\u00a7$1")), false);
        }
        if (dif != null) {
            server.getPlayerManager().broadcast(
                    Text.literal("Dificultad (Día " + dia + "): ").formatted(Formatting.YELLOW)
                            .append(Text.literal(dif.nombre()).formatted(Formatting.RED, Formatting.BOLD))
                            .append(Text.literal(" - " + dif.descripcion()).formatted(Formatting.GRAY)),
                    false);
            ejecutar(server, dif.comandos());
            if (dif.duracionSeg() > 0 && !dif.deshacer().isEmpty()) {
                pendientes.add(new Pendiente(new int[]{dif.duracionSeg() * 20}, dif.deshacer()));
            }
        }
    }
}
