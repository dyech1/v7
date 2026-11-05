package com.colorwheel;

/** Los 8 colores de la ruleta, en el orden en que aparecen (sentido horario desde arriba). */
public enum WheelColor {
    ROJO("Rojo", 0xE81628),
    ROSA("Rosa", 0xEC1EAA),
    MORADO("Morado", 0x961EE1),
    AZUL("Azul", 0x192DEB),
    CELESTE("Celeste", 0x14E1F0),
    VERDE("Verde", 0x2DE123),
    AMARILLO("Amarillo", 0xFFE814),
    NARANJA("Naranja", 0xFF9614);

    public final String nombre;
    public final int rgb;

    WheelColor(String nombre, int rgb) {
        this.nombre = nombre;
        this.rgb = rgb;
    }

    public static final int COUNT = values().length;
}
