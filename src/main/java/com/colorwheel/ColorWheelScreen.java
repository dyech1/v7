package com.colorwheel;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Menú del admin con dos pestañas: "Ruleta" (elegir color + mensaje) y "Cambios de dificultad" (días 1 al 15). */
public class ColorWheelScreen extends Screen {
    // Se recuerda lo último elegido mientras el juego siga abierto
    private static String lastMessage = "";
    private static int lastSelected = 0;
    private static int lastTab = 0;   // 0 = ruleta, 1 = dificultades
    private static int lastDay = 0;   // 0..14

    private static final int W = 208;  // ancho del contenido

    private ButtonWidget[] colorButtons;
    private TextFieldWidget messageField;
    private int selected = lastSelected;
    private int tab = lastTab;
    private int day = lastDay;

    public ColorWheelScreen() {
        super(Text.literal("Ruleta"));
    }

    @Override
    protected void init() {
        int x0 = this.width / 2 - W / 2;
        int top = Math.max(6, this.height / 2 - 92);

        // Pestañas principales
        addDrawableChild(ButtonWidget.builder(tabLabel("Ruleta de colores", tab == 0), b -> switchTab(0))
                .dimensions(x0, top, 102, 18).build());
        addDrawableChild(ButtonWidget.builder(tabLabel("Cambios de dificultad", tab == 1), b -> switchTab(1))
                .dimensions(x0 + 106, top, 102, 18).build());

        int y = top + 26;
        if (tab == 0) initColorTab(x0, y); else initDifficultyTab(x0, y);
    }

    private Text tabLabel(String s, boolean active) {
        return active ? Text.literal(s).formatted(Formatting.YELLOW, Formatting.BOLD) : Text.literal(s);
    }

    private void switchTab(int t) {
        saveState();
        tab = t;
        lastTab = t;
        this.clearAndInit();
    }

    private void saveState() {
        if (messageField != null) lastMessage = messageField.getText();
        lastSelected = selected;
        lastDay = day;
    }

    // ---------------- Pestaña 1: ruleta de colores ----------------
    private void initColorTab(int x0, int y) {
        int w = 100, gap = 8;
        colorButtons = new ButtonWidget[WheelColor.COUNT];
        for (int i = 0; i < WheelColor.COUNT; i++) {
            final int idx = i;
            int col = i / 4, row = i % 4;
            colorButtons[i] = ButtonWidget.builder(Text.empty(), b -> { selected = idx; refreshColors(); })
                    .dimensions(x0 + col * (w + gap), y + row * 22, w, 20).build();
            addDrawableChild(colorButtons[i]);
        }
        int fieldY = y + 4 * 22 + 16;
        messageField = new TextFieldWidget(this.textRenderer, x0, fieldY, W, 20, Text.literal("Mensaje"));
        messageField.setMaxLength(200);
        messageField.setPlaceholder(Text.literal("Mensaje para el chat (opcional)"));
        messageField.setText(lastMessage);
        addDrawableChild(messageField);

        addDrawableChild(ButtonWidget.builder(Text.literal("¡GIRAR!").formatted(Formatting.BOLD), b -> {
            saveState();
            ClientPlayNetworking.send(new SpinRequestPayload(selected, messageField.getText()));
            this.close();
        }).dimensions(x0, fieldY + 26, W, 24).build());
        refreshColors();
    }

    private void refreshColors() {
        for (int i = 0; i < WheelColor.COUNT; i++) {
            WheelColor c = WheelColor.values()[i];
            String prefix = (i == selected) ? "» " : "";
            colorButtons[i].setMessage(Text.literal(prefix + c.nombre).styled(s -> s.withColor(c.rgb)));
        }
    }

    // ---------------- Pestaña 2: cambios de dificultad ----------------
    private void initDifficultyTab(int x0, int y) {
        // 15 sub-pestañas "Día 1".."Día 15" en 3 filas de 5
        int bw = 40, gap = 2;
        for (int d = 0; d < Dificultades.DIAS; d++) {
            final int dd = d;
            int col = d % 5, row = d / 5;
            Text t = Text.literal("Día " + (d + 1));
            if (d == day) t = t.copy().formatted(Formatting.YELLOW, Formatting.BOLD);
            addDrawableChild(ButtonWidget.builder(t, b -> { day = dd; lastDay = dd; this.clearAndInit(); })
                    .dimensions(x0 + col * (bw + gap), y + row * 18, bw, 16).build());
        }
        // 5 dificultades del día elegido
        int ly = y + 3 * 18 + 8;
        for (int i = 0; i < Dificultades.POR_DIA; i++) {
            final int idx = i;
            Dificultad dif = Dificultades.LISTA[day][i];
            ButtonWidget btn = ButtonWidget.builder(Text.literal(dif.nombre()), b -> {
                saveState();
                ClientPlayNetworking.send(new DifficultyRequestPayload(day, idx));
                this.close();
            }).dimensions(x0, ly + i * 22, W, 20).build();
            btn.setTooltip(Tooltip.of(Text.literal(dif.descripcion())));
            addDrawableChild(btn);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int x0 = this.width / 2 - W / 2;
        int top = Math.max(6, this.height / 2 - 92);
        int y = top + 26;
        if (tab == 0) {
            ctx.drawTextWithShadow(this.textRenderer, Text.literal("Texto para el chat al terminar:"), x0, y + 4 * 22 + 4, 0xA0A0A0);
        } else {
            ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal("Día " + (day + 1) + ": elige una dificultad (la ruleta cae en amarillo)"),
                    x0, y + 3 * 18 - 1, 0xA0A0A0);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
