package com.colorwheel;

import java.util.ArrayList;
import java.util.List;

/** Las 75 dificultades: 5 por cada día, del día 1 (suave) al día 15 (brutal). */
public final class Dificultades {
    public static final int DIAS = 15;
    public static final int POR_DIA = 5;
    /** Índice del color amarillo en WheelColor: la ruleta siempre cae aquí con una dificultad. */
    public static final int COLOR_AMARILLO = WheelColor.AMARILLO.ordinal();

    public static final Dificultad[][] LISTA = new Dificultad[DIAS][];

    // Posiciones alrededor de cada jugador para los mobs que aparecen
    private static final int[][] OFFSETS = {
            {4, 0}, {-4, 0}, {0, 4}, {0, -4}, {5, 5}, {-5, 5}, {5, -5}, {-5, -5},
            {7, 0}, {-7, 0}, {0, 7}, {0, -7}, {3, 6}, {-3, -6}, {6, -3}, {-6, 3}};

    private Dificultades() {}

    // ---------- constructores de ayuda ----------
    private static Dificultad ef(String n, String d, String efecto, int seg, int nivel) {
        return new Dificultad(n, d, List.of("effect give @a minecraft:" + efecto + " " + seg + " " + nivel + " true"), 0, List.of());
    }

    private static Dificultad efs(String n, String d, int seg, String... efectosYNiveles) {
        List<String> c = new ArrayList<>();
        for (int i = 0; i < efectosYNiveles.length; i += 2) {
            c.add("effect give @a minecraft:" + efectosYNiveles[i] + " " + seg + " " + efectosYNiveles[i + 1] + " true");
        }
        return new Dificultad(n, d, c, 0, List.of());
    }

    private static List<String> spawn(String mob, int cantidad, int desde) {
        List<String> c = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            int[] o = OFFSETS[(desde + i) % OFFSETS.length];
            c.add("execute at @a run summon minecraft:" + mob + " ~" + o[0] + " ~ ~" + o[1]);
        }
        return c;
    }

    private static Dificultad mob(String n, String d, String mob, int cantidad) {
        return new Dificultad(n, d, spawn(mob, cantidad, 0), 0, List.of());
    }

    private static Dificultad mobs(String n, String d, String... mobYCantidad) {
        List<String> c = new ArrayList<>();
        int desde = 0;
        for (int i = 0; i < mobYCantidad.length; i += 2) {
            int cant = Integer.parseInt(mobYCantidad[i + 1]);
            c.addAll(spawn(mobYCantidad[i], cant, desde));
            desde += cant;
        }
        return new Dificultad(n, d, c, 0, List.of());
    }

    private static Dificultad conMobs(String n, String d, List<String> extra, String... mobYCantidad) {
        List<String> c = new ArrayList<>(extra);
        int desde = 0;
        for (int i = 0; i < mobYCantidad.length; i += 2) {
            int cant = Integer.parseInt(mobYCantidad[i + 1]);
            c.addAll(spawn(mobYCantidad[i], cant, desde));
            desde += cant;
        }
        return new Dificultad(n, d, c, 0, List.of());
    }

    private static Dificultad dmg(String n, String d, int puntos) {
        return new Dificultad(n, d, List.of("damage @a " + puntos + " minecraft:magic"), 0, List.of());
    }

    private static Dificultad cmd(String n, String d, String... comandos) {
        return new Dificultad(n, d, List.of(comandos), 0, List.of());
    }

    private static Dificultad vida(String n, String d, int vidaMax, int seg) {
        return new Dificultad(n, d,
                List.of("attribute @a minecraft:generic.max_health base set " + vidaMax),
                seg, List.of("attribute @a minecraft:generic.max_health base set 20"));
    }

    private static Dificultad temporal(String n, String d, int seg, List<String> comandos, List<String> deshacer) {
        return new Dificultad(n, d, comandos, seg, deshacer);
    }

    static {
        LISTA[0] = new Dificultad[]{
                ef("Piernas pesadas", "Lentitud I durante 3 minutos.", "slowness", 180, 0),
                ef("Estómago vacío", "Hambre I durante 3 minutos.", "hunger", 180, 0),
                mob("Visitas nocturnas", "Aparecen 3 zombis junto a cada jugador.", "zombie", 3),
                ef("Manos torpes", "Fatiga de minado I durante 3 minutos.", "mining_fatigue", 180, 0),
                ef("Mareo", "Náusea durante 15 segundos.", "nausea", 15, 0)};
        LISTA[1] = new Dificultad[]{
                ef("Sin saltos", "Nadie puede saltar durante 1 minuto.", "jump_boost", 60, 200),
                ef("Debilidad", "Debilidad I durante 3 minutos.", "weakness", 180, 0),
                mob("Esqueletos", "Aparecen 3 esqueletos junto a cada jugador.", "skeleton", 3),
                cmd("Lluvia", "Llueve durante 5 minutos.", "weather rain 300"),
                dmg("Golpe sorpresa", "Cada jugador pierde 1,5 corazones.", 3)};
        LISTA[2] = new Dificultad[]{
                mob("Arañas", "Aparecen 3 arañas junto a cada jugador.", "spider", 3),
                ef("Veneno leve", "Veneno I durante 10 segundos.", "poison", 10, 0),
                cmd("Noche cerrada", "Se hace de noche de golpe.", "time set midnight"),
                temporal("Sin regeneración", "No se regenera vida durante 5 minutos.", 300,
                        List.of("gamerule naturalRegeneration false"), List.of("gamerule naturalRegeneration true")),
                mob("Creepers", "Aparecen 2 creepers junto a cada jugador.", "creeper", 2)};
        LISTA[3] = new Dificultad[]{
                ef("Ceguera", "Ceguera durante 15 segundos.", "blindness", 15, 0),
                ef("Hambre extrema", "Hambre III durante 2 minutos.", "hunger", 120, 2),
                cmd("Tormenta", "Tormenta eléctrica durante 5 minutos.", "weather thunder 300"),
                ef("Piernas de plomo", "Lentitud II durante 3 minutos.", "slowness", 180, 1),
                mob("Patrulla de zombis", "Aparecen 5 zombis junto a cada jugador.", "zombie", 5)};
        LISTA[4] = new Dificultad[]{
                vida("Vida a la mitad", "Vida máxima de 5 corazones durante 5 minutos.", 10, 300),
                ef("Oscuridad", "Oscuridad durante 1 minuto.", "darkness", 60, 0),
                mob("Brujas", "Aparecen 2 brujas junto a cada jugador.", "witch", 2),
                temporal("Golpes débiles", "Tu daño de ataque baja a la mitad durante 5 minutos.", 300,
                        List.of("attribute @a minecraft:generic.attack_damage base set 0.5"),
                        List.of("attribute @a minecraft:generic.attack_damage base set 1")),
                mob("Fantasmas", "Aparecen 3 fantasmas junto a cada jugador.", "phantom", 3)};
        LISTA[5] = new Dificultad[]{
                mobs("Banda mixta", "2 zombis, 2 esqueletos y 1 araña por jugador.", "zombie", "2", "skeleton", "2", "spider", "1"),
                ef("Mala suerte", "Mala suerte durante 10 minutos.", "unluck", 600, 4),
                efs("Lento y débil", "Lentitud II y Debilidad II durante 2 minutos.", 120, "slowness", "1", "weakness", "1"),
                mob("Endermen", "Aparecen 2 endermen junto a cada jugador.", "enderman", 2),
                dmg("Golpe fuerte", "Cada jugador pierde 3 corazones.", 6)};
        LISTA[6] = new Dificultad[]{
                mob("Saqueadores", "Aparecen 3 saqueadores junto a cada jugador.", "pillager", 3),
                vida("Vida reducida", "Vida máxima de 7 corazones durante 5 minutos.", 14, 300),
                ef("Veneno", "Veneno II durante 15 segundos.", "poison", 15, 1),
                mob("Vindicadores", "Aparecen 2 vindicadores junto a cada jugador.", "vindicator", 2),
                cmd("Mundo oscuro", "Noche y tormenta durante 5 minutos.", "time set midnight", "weather thunder 300")};
        LISTA[7] = new Dificultad[]{
                ef("Casi parálisis", "Lentitud III durante 1 minuto.", "slowness", 60, 2),
                mob("Lluvia de slimes", "Aparecen 4 slimes junto a cada jugador.", "slime", 4),
                ef("Fatiga severa", "Fatiga de minado III durante 3 minutos.", "mining_fatigue", 180, 2),
                mob("Cubos de magma", "Aparecen 3 cubos de magma junto a cada jugador.", "magma_cube", 3),
                efs("Caos visual", "Náusea y ceguera durante 10 segundos.", 10, "nausea", "0", "blindness", "0")};
        LISTA[8] = new Dificultad[]{
                mob("Devastador", "Aparece 1 devastador junto a cada jugador.", "ravager", 1),
                ef("Hambre brutal", "Hambre V durante 2 minutos.", "hunger", 120, 4),
                vida("Vida de cristal", "Vida máxima de 3 corazones durante 3 minutos.", 6, 180),
                mob("Lluvia de flechas", "Aparecen 6 esqueletos junto a cada jugador.", "skeleton", 6),
                ef("Oscuridad total", "Oscuridad durante 2 minutos.", "darkness", 120, 0)};
        LISTA[9] = new Dificultad[]{
                mobs("Brigada", "5 zombis, 3 esqueletos y 2 creepers por jugador.", "zombie", "5", "skeleton", "3", "creeper", "2"),
                ef("Piernas de piedra", "Lentitud IV durante 2 minutos.", "slowness", 120, 3),
                temporal("Sin regeneración larga", "No se regenera vida durante 10 minutos.", 600,
                        List.of("gamerule naturalRegeneration false"), List.of("gamerule naturalRegeneration true")),
                mob("Evocadores", "Aparecen 2 evocadores junto a cada jugador.", "evoker", 2),
                dmg("Golpe devastador", "Cada jugador pierde 4 corazones.", 8)};
        LISTA[10] = new Dificultad[]{
                mob("Invasión de arañas", "Aparecen 8 arañas junto a cada jugador.", "spider", 8),
                ef("Marchitamiento", "Wither I durante 5 segundos.", "wither", 5, 0),
                temporal("Manos de papel", "Tu daño de ataque baja mucho durante 4 minutos.", 240,
                        List.of("attribute @a minecraft:generic.attack_damage base set 0.25"),
                        List.of("attribute @a minecraft:generic.attack_damage base set 1")),
                mob("Marea de zombis", "Aparecen 10 zombis junto a cada jugador.", "zombie", 10),
                temporal("Noche infinita", "Siempre de noche durante 5 minutos.", 300,
                        List.of("gamerule doDaylightCycle false", "time set midnight"), List.of("gamerule doDaylightCycle true"))};
        LISTA[11] = new Dificultad[]{
                mob("Aquelarre", "Aparecen 5 brujas junto a cada jugador.", "witch", 5),
                efs("Todo mal", "Lentitud, debilidad, fatiga y hambre II durante 2 minutos.", 120,
                        "slowness", "1", "weakness", "1", "mining_fatigue", "1", "hunger", "1"),
                mob("Plaga de fantasmas", "Aparecen 6 fantasmas junto a cada jugador.", "phantom", 6),
                mob("Creepers en masa", "Aparecen 5 creepers junto a cada jugador.", "creeper", 5),
                cmd("Eclipse", "Oscuridad durante 3 minutos y ceguera 5 segundos.",
                        "effect give @a minecraft:darkness 180 0 true", "effect give @a minecraft:blindness 5 0 true")};
        LISTA[12] = new Dificultad[]{
                mob("Asalto", "Aparecen 8 saqueadores junto a cada jugador.", "pillager", 8),
                vida("Vida mínima", "Vida máxima de 2 corazones durante 2 minutos.", 4, 120),
                mob("Doble devastador", "Aparecen 2 devastadores junto a cada jugador.", "ravager", 2),
                ef("Veneno mortal", "Veneno III durante 20 segundos.", "poison", 20, 2),
                mob("Enjambre de vex", "Aparecen 6 vex junto a cada jugador.", "vex", 6)};
        LISTA[13] = new Dificultad[]{
                mob("Marea de endermen", "Aparecen 6 endermen junto a cada jugador.", "enderman", 6),
                dmg("Golpe mortal", "Cada jugador pierde 6 corazones.", 12),
                conMobs("Apocalipsis suave", "Noche, tormenta, 4 zombis y 4 esqueletos por jugador.",
                        List.of("time set midnight", "weather thunder 600"), "zombie", "4", "skeleton", "4"),
                efs("Parálisis", "Lentitud VII y fatiga de minado durante 30 segundos.", 30, "slowness", "6", "mining_fatigue", "2"),
                mob("Manada de zoglins", "Aparecen 3 zoglins junto a cada jugador.", "zoglin", 3)};
        LISTA[14] = new Dificultad[]{
                mob("El Guardián", "Aparece 1 warden junto a cada jugador.", "warden", 1),
                mobs("Ejército", "8 zombis, 6 esqueletos, 3 creepers y 2 brujas por jugador.", "zombie", "8", "skeleton", "6", "creeper", "3", "witch", "2"),
                vida("Un solo corazón", "Vida máxima de 1 corazón durante 1 minuto.", 2, 60),
                cmd("Fin del mundo", "Noche, tormenta y oscuridad durante 5 minutos.", "time set midnight", "weather thunder 300",
                        "effect give @a minecraft:darkness 300 0 true"),
                mobs("Jefes", "3 evocadores y 2 devastadores por jugador.", "evoker", "3", "ravager", "2")};
    }
}
