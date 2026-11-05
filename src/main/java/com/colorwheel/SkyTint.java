package com.colorwheel;

import net.minecraft.util.Util;

/** Estado (solo del lado del cliente) del cielo rojo y la grieta que aparecen cuando sale el color rojo. */
public final class SkyTint {
    private static final long FADE_IN_MS = 2500;
    private static final long HOLD_MS = 35_000;    // cuánto dura el cielo rojo
    private static final long FADE_OUT_MS = 5000;
    private static final long CRACK_APPEAR_MS = 1200;  // la grieta aparece cerrada
    private static final long OPEN_START_MS = 2500;    // y después de este tiempo empieza a abrirse
    private static final long OPEN_DURATION_MS = 5000; // tarda esto en abrirse del todo

    private static long startMs = -1;

    private SkyTint() {}

    public static void trigger() {
        startMs = Util.getMeasuringTimeMs();
    }

    /** 0 = cielo normal, 1 = cielo totalmente rojo. */
    public static float factor() {
        if (startMs < 0) return 0f;
        long t = Util.getMeasuringTimeMs() - startMs;
        if (t < FADE_IN_MS) return t / (float) FADE_IN_MS;
        if (t < FADE_IN_MS + HOLD_MS) return 1f;
        if (t < FADE_IN_MS + HOLD_MS + FADE_OUT_MS) {
            return 1f - (t - FADE_IN_MS - HOLD_MS) / (float) FADE_OUT_MS;
        }
        startMs = -1;
        return 0f;
    }

    /** 0..1: la grieta se va haciendo visible (cerrada). */
    public static float crackAppear() {
        return Math.min(1f, elapsedMs() / (float) CRACK_APPEAR_MS);
    }

    /** 0 = grieta cerrada, 1 = grieta completamente abierta. */
    public static float openProgress() {
        float t = (elapsedMs() - OPEN_START_MS) / (float) OPEN_DURATION_MS;
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    public static long elapsedMs() {
        return startMs < 0 ? 0 : Util.getMeasuringTimeMs() - startMs;
    }
}
