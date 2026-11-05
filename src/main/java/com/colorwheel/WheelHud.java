package com.colorwheel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;

/** La ruleta que todos los jugadores ven girar en el centro de su pantalla. */
public final class WheelHud {
    private static final Identifier WHEEL = tex("wheel");
    private static final Identifier TINT = tex("tint");
    private static final Identifier GLOW = tex("glow");
    private static final Identifier HUB = tex("hub");
    private static final Identifier POINTER = tex("pointer");

    private static final int MAX_RADIUS = 92;  // radio en píxeles de GUI (se reduce en pantallas pequeñas)
    private static final int TEX = 512;
    private static final int HUB_TEX = 128;
    // Puntero (medido sobre la imagen de referencia), en múltiplos del radio de la ruleta
    private static final float PTR_W = 0.3963f, PTR_H = 0.4702f, PTR_TOP = -1.1434f;
    // Posición de la gema dentro del puntero (fracciones del ancho/alto)
    private static final float GEM_X0 = 0.2881f, GEM_Y0 = 0.1714f, GEM_X1 = 0.7458f, GEM_Y1 = 0.5286f;
    private static final int PTR_TEX_W = 236, PTR_TEX_H = 280;
    private static final int FULL_TURNS = 6;
    private static final long APPEAR_MS = 600;  // aparece con un pequeño "pop"
    private static final long FLASH_MS = 700;   // destello de color al detenerse
    private static final long HOLD_MS = 4500;   // cuánto se queda visible al terminar
    private static final long FADE_MS = 700;

    private static float savedAngle = 0f;
    private static boolean visible, spinning;
    private static long startMs, durationMs, endMs;
    private static float startAngle, targetAngle, angle;
    private static int selected;
    private static String label = "";

    private WheelHud() {}

    private static Identifier tex(String n) {
        return Identifier.of(ColorWheelMod.MOD_ID, "textures/gui/" + n + ".png");
    }

    public static void start(int index, float jitter, int duration, String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        selected = index;
        label = text == null ? "" : text;
        startAngle = savedAngle;
        angle = savedAngle;
        float needed = mod(-index * 45f + jitter - startAngle, 360f);
        targetAngle = startAngle + FULL_TURNS * 360f + needed;
        durationMs = duration;
        startMs = Util.getMeasuringTimeMs();
        spinning = true;
        visible = true;
        mc.getSoundManager().play(PositionedSoundInstance.master(ColorWheelMod.SPIN_SOUND, 1.0f));
    }

    private static float mod(float a, float m) {
        return ((a % m) + m) % m;
    }

    /** Color (RGB) del segmento que está bajo el puntero, mezclado suavemente al girar. */
    private static int colorUnder(float ang) {
        float f = mod(-ang, 360f) / 45f;
        int i0 = ((int) Math.floor(f)) % WheelColor.COUNT;
        int i1 = (i0 + 1) % WheelColor.COUNT;
        float fr = f - (float) Math.floor(f);
        fr = fr * fr * (3f - 2f * fr);
        int a = WheelColor.values()[i0].rgb, b = WheelColor.values()[i1].rgb;
        int r = (int) (((a >> 16) & 255) * (1f - fr) + ((b >> 16) & 255) * fr);
        int g = (int) (((a >> 8) & 255) * (1f - fr) + ((b >> 8) & 255) * fr);
        int bl = (int) ((a & 255) * (1f - fr) + (b & 255) * fr);
        return (r << 16) | (g << 8) | bl;
    }

    private static void drawTex(DrawContext ctx, Identifier tex, int texSize, int cx, int cy, int half, float deg,
                                float scale, float r, float g, float b, float a) {
        if (a <= 0.004f) return;
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(cx, cy, 0);
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(deg));
        m.scale(scale, scale, 1f);
        RenderSystem.setShaderColor(r, g, b, a);
        ctx.drawTexture(tex, -half, -half, half * 2, half * 2, 0, 0, texSize, texSize, texSize, texSize);
        m.pop();
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!visible) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;

        long now = Util.getMeasuringTimeMs();
        float alpha = 1f, scale = 1f, tint = 0f, glow = 0f, speed = 0f; // speed en grados por ms

        if (spinning) {
            float t = (now - startMs) / (float) durationMs;
            if (t >= 1f) {
                spinning = false;
                savedAngle = mod(targetAngle, 360f);
                angle = savedAngle;
                endMs = now;
                if (selected == WheelColor.ROJO.ordinal()) {
                    // Sale rojo: el cielo se vuelve rojo y se abre una grieta en el centro del mapa
                    SkyTint.trigger();
                }
            } else {
                float inv = 1f - t;
                angle = startAngle + (targetAngle - startAngle) * (1f - inv * inv * inv); // frena poco a poco
                speed = (targetAngle - startAngle) * 3f * inv * inv / durationMs;
            }
        }
        if (!spinning) {
            long since = now - endMs;
            if (since > HOLD_MS + FADE_MS) {
                visible = false;
                return;
            }
            float k = Math.min(1f, since / (float) FLASH_MS);
            float ke = 1f - (1f - k) * (1f - k);
            scale = 1f + 0.18f * ke;           // la ruleta crece un poco al terminar
            tint = 0.88f * ke;                  // todos los segmentos toman el color ganador
            glow = k < 1f ? k : 1f - 0.5f * Math.min(1f, (since - FLASH_MS) / 600f);
            if (since > HOLD_MS) alpha = 1f - (since - HOLD_MS) / (float) FADE_MS;
        }
        float ap = Math.min(1f, (now - startMs) / (float) APPEAR_MS);
        float apE = 1f - (1f - ap) * (1f - ap);
        alpha *= apE;
        scale *= 0.7f + 0.3f * apE;

        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        int radius = Math.min(MAX_RADIUS, (int) (ctx.getScaledWindowHeight() * 0.33f));
        WheelColor win = WheelColor.values()[selected];
        float wr = ((win.rgb >> 16) & 255) / 255f, wg = ((win.rgb >> 8) & 255) / 255f, wb = (win.rgb & 255) / 255f;
        int under = colorUnder(angle);
        float ur = ((under >> 16) & 255) / 255f, ug = ((under >> 8) & 255) / 255f, ub = (under & 255) / 255f;

        RenderSystem.enableBlend();

        // Estela de movimiento mientras gira rápido
        float step = speed * 18f;
        if (spinning && step > 1.5f) {
            drawTex(ctx, WHEEL, TEX, cx, cy, radius, angle - step * 2f, scale, 1f, 1f, 1f, 0.16f * alpha);
            drawTex(ctx, WHEEL, TEX, cx, cy, radius, angle - step, scale, 1f, 1f, 1f, 0.28f * alpha);
        }
        drawTex(ctx, WHEEL, TEX, cx, cy, radius, angle, scale, 1f, 1f, 1f, alpha);
        drawTex(ctx, TINT, TEX, cx, cy, radius, angle, scale, wr, wg, wb, tint * alpha);
        // El centro cambia de color según el segmento que va pasando bajo el puntero
        drawTex(ctx, HUB, HUB_TEX, cx, cy, Math.round(radius * 0.06f), 0f, scale, ur, ug, ub, alpha);
        drawTex(ctx, GLOW, TEX, cx, cy, radius, 0f, scale, 1f, 1f, 1f, glow * alpha);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Puntero: igual que en la imagen; la gema toma el color del segmento que tiene debajo
        int rs = (int) (radius * scale);
        int pw = Math.round(PTR_W * rs), ph = Math.round(PTR_H * rs);
        int px = cx - pw / 2, py = cy + Math.round(PTR_TOP * rs);
        int a255 = Math.max(4, (int) (alpha * 255)) << 24;
        ctx.fill(px + Math.round(GEM_X0 * pw), py + Math.round(GEM_Y0 * ph),
                px + Math.round(GEM_X1 * pw), py + Math.round(GEM_Y1 * ph), a255 | under);
        ctx.draw();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        ctx.drawTexture(POINTER, px, py, pw, ph, 0, 0, PTR_TEX_W, PTR_TEX_H, PTR_TEX_W, PTR_TEX_H);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (!spinning) {
            Text txt = Text.literal("¡Salió " + win.nombre + "!").styled(s -> s.withBold(true));
            ctx.drawCenteredTextWithShadow(mc.textRenderer, txt, cx, cy + rs + 8, a255 | win.rgb);
            if (!label.isEmpty()) {
                ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(label), cx, cy + rs + 20, a255 | 0xFFFFFF);
            }
        }
    }
}
