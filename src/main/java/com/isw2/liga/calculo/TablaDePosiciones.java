package com.isw2.liga.calculo;

import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tabla de posiciones (RF-11). Función pura: recibe los equipos y los partidos
 * FINALIZADOS ya con su resultado, y no toca la base. Así se testea sin Spring.
 */
public final class TablaDePosiciones {

    public static final int PUNTOS_GANADO = 3;
    public static final int PUNTOS_EMPATADO = 1;
    public static final int PUNTOS_PERDIDO = 0;

    public record EquipoTabla(int id, String nombre) {
    }

    public record PartidoJugado(int localId, int visitanteId, Resultado resultado) {
    }

    public record Fila(int equipoId, String equipo, int pj, int pg, int pe, int pp, int gf, int gc) {

        public int dg() {
            return gf - gc;
        }

        public int pts() {
            return pg * PUNTOS_GANADO + pe * PUNTOS_EMPATADO + pp * PUNTOS_PERDIDO;
        }
    }

    /** Orden de la sección 7: puntos, diferencia de gol, goles a favor y nombre. */
    private static final Comparator<Fila> ORDEN = Comparator
            .<Fila>comparingInt(Fila::pts).reversed()
            .thenComparing(Comparator.comparingInt(Fila::dg).reversed())
            .thenComparing(Comparator.comparingInt(Fila::gf).reversed())
            .thenComparing(Fila::equipo, Collator.getInstance(Locale.forLanguageTag("es")));

    private TablaDePosiciones() {
    }

    /**
     * @param equipos     todos los equipos que van en la tabla; los que no jugaron quedan en cero
     * @param finalizados solo partidos FINALIZADOS, los demás no suman
     */
    public static List<Fila> calcular(List<EquipoTabla> equipos, List<PartidoJugado> finalizados) {
        Map<Integer, Acumulado> porEquipo = new LinkedHashMap<>();
        for (EquipoTabla e : equipos) {
            porEquipo.put(e.id(), new Acumulado(e));
        }
        for (PartidoJugado p : finalizados) {
            int local = p.resultado().golesLocal();
            int visitante = p.resultado().golesVisitante();
            sumar(porEquipo.get(p.localId()), local, visitante);
            sumar(porEquipo.get(p.visitanteId()), visitante, local);
        }
        return porEquipo.values().stream().map(Acumulado::fila).sorted(ORDEN).toList();
    }

    private static void sumar(Acumulado a, int aFavor, int enContra) {
        if (a == null) {
            throw new IllegalArgumentException("Hay un partido con un equipo que no está en la tabla");
        }
        a.pj++;
        a.gf += aFavor;
        a.gc += enContra;
        if (aFavor > enContra) {
            a.pg++;
        } else if (aFavor == enContra) {
            a.pe++;
        } else {
            a.pp++;
        }
    }

    private static final class Acumulado {
        private final EquipoTabla equipo;
        private int pj, pg, pe, pp, gf, gc;

        private Acumulado(EquipoTabla equipo) {
            this.equipo = equipo;
        }

        private Fila fila() {
            return new Fila(equipo.id(), equipo.nombre(), pj, pg, pe, pp, gf, gc);
        }
    }
}
