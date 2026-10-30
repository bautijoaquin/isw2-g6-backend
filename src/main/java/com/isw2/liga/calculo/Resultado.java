package com.isw2.liga.calculo;

import java.util.List;

/**
 * Marcador de un partido. El resultado no se carga: se calcula siempre con los goles (RN-06).
 */
public record Resultado(int golesLocal, int golesVisitante) {

    /**
     * Cada GOL suma para el equipo del jugador que lo hizo. No hay goles en contra (sección 7).
     *
     * @param equiposDeLosGoles el id del equipo del autor de cada gol
     */
    public static Resultado calcular(int localId, int visitanteId, List<Integer> equiposDeLosGoles) {
        int local = 0;
        int visitante = 0;
        for (int equipoId : equiposDeLosGoles) {
            if (equipoId == localId) {
                local++;
            } else if (equipoId == visitanteId) {
                visitante++;
            }
        }
        return new Resultado(local, visitante);
    }
}
