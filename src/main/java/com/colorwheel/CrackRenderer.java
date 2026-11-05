package com.colorwheel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * La grieta gigante en el cielo, sobre el centro del mapa (coordenadas 0, 0).
 * Todos los jugadores la ven hacia esa dirección: aparece cerrada, se abre y dentro se ven estrellas.
 */
public final class CrackRenderer {
    private static final int FRAMES = 8;
    private static final Identifier[] TEX = new Identifier[FRAMES];

    static {
        for (int i = 0; i < FRAMES; i++) {
            TEX[i] = Identifier.of(ColorWheelMod.MOD_ID, "textures/gui/crack_" + i + ".png");
        }
    }

    // Centro del mapa y altura a la que flota la grieta
    private static final double CENTER_X = 0, CENTER_Y = 150, CENTER_Z = 0;
    // Tamaño: la grieta mide ~48° de alto en pantalla, sin importar desde dónde se mire
    private static final double BASE_DIST = 200;
    private static final double MIN_DIST = 150, MAX_DIST = 300;
    private static final float BASE_HALF_W = 60f, BASE_HALF_H = 90f;
    private static final double MIN_ELEV = Math.toRadians(30), MAX_ELEV = Math.toRadians(62);

    private CrackRenderer() {}

    public static void render(WorldRenderContext ctx) {
        float k = SkyTint.factor();
        if (k <= 0f) return;
        MatrixStack ms = ctx.matrixStack();
        if (ms == null) return;

        // Dirección desde la cámara hacia el centro del mapa
        Vec3d cam = ctx.camera().getPos();
        double ox = CENTER_X - cam.x, oy = CENTER_Y - cam.y, oz = CENTER_Z - cam.z;
        double hd = Math.hypot(ox, oz);
        double hx = 0, hz = 1;
        if (hd > 1.0) { hx = ox / hd; hz = oz / hd; }
        double el = Math.max(MIN_ELEV, Math.min(MAX_ELEV, Math.atan2(oy, hd)));
        double len = Math.sqrt(ox * ox + oy * oy + oz * oz);
        double dist = Math.max(MIN_DIST, Math.min(MAX_DIST, len));
        double s = dist / BASE_DIST;

        double dx = hx * Math.cos(el), dy = Math.sin(el), dz = hz * Math.cos(el);
        double px = dx * dist, py = dy * dist, pz = dz * dist;

        // Vectores "derecha" y "arriba" del cuadro, perpendiculares a la dirección
        double rx = -dz, rz = dx;
        double rl = Math.sqrt(rx * rx + rz * rz);
        rx /= rl; rz /= rl;
        double ux = dy * rz, uy = dz * rx - dx * rz, uz = -dy * rx;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul; uy /= ul; uz /= ul;
        if (uy < 0) { ux = -ux; uy = -uy; uz = -uz; }

        float w = (float) (BASE_HALF_W * s), h = (float) (BASE_HALF_H * s);
        float pulse = 0.93f + 0.07f * (float) Math.sin(SkyTint.elapsedMs() * 0.005);
        float alpha = Math.min(1f, k * 1.3f) * SkyTint.crackAppear() * pulse;

        // Cerrada -> abierta: se mezclan dos cuadros seguidos de la animación
        float f = SkyTint.openProgress() * (FRAMES - 1);
        int i = Math.min(FRAMES - 1, (int) f);
        float fr = f - i;

        Matrix4f m = ms.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        quad(m, TEX[i], px, py, pz, rx, rz, ux, uy, uz, w, h, alpha);
        if (fr > 0.01f && i < FRAMES - 1) {
            quad(m, TEX[i + 1], px, py, pz, rx, rz, ux, uy, uz, w, h, alpha * fr);
        }
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void quad(Matrix4f m, Identifier tex, double px, double py, double pz,
                             double rx, double rz, double ux, double uy, double uz,
                             float w, float h, float a) {
        RenderSystem.setShaderTexture(0, tex);
        BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        v(b, m, px - rx * w - ux * h, py - uy * h, pz - rz * w - uz * h, 0f, 1f, a);
        v(b, m, px + rx * w - ux * h, py - uy * h, pz + rz * w - uz * h, 1f, 1f, a);
        v(b, m, px + rx * w + ux * h, py + uy * h, pz + rz * w + uz * h, 1f, 0f, a);
        v(b, m, px - rx * w + ux * h, py + uy * h, pz - rz * w + uz * h, 0f, 0f, a);
        BufferRenderer.drawWithGlobalProgram(b.end());
    }

    private static void v(BufferBuilder b, Matrix4f m, double x, double y, double z, float u, float t, float a) {
        b.vertex(m, (float) x, (float) y, (float) z).texture(u, t).color(1f, 1f, 1f, a);
    }
}
