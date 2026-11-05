package com.colorwheel;

import java.util.List;

/**
 * Una dificultad de la ruleta. Los comandos se ejecutan en el servidor (con permisos de consola)
 * cuando la ruleta se detiene. Si duracionSeg > 0, al pasar ese tiempo se ejecutan los comandos de deshacer.
 */
public record Dificultad(String nombre, String descripcion, List<String> comandos, int duracionSeg, List<String> deshacer) {
}
