package com.isw2.liga.calculo;

import com.isw2.liga.model.TipoEvento;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RN-03: un jugador que en un partido FINALIZADO de la fecha N recibe 1 roja, o 2 amarillas
 * en ese mismo partido, queda suspendido para la fecha N+1. Las amarillas de partidos
 * distintos no se acumulan.
 */
public final class Suspensiones {

    public enum Motivo { ROJA, DOBLE_AMARILLA }

    public record Tarjeta(int partidoId, int jugadorId, TipoEvento tipo) {
    }

    /** Las amarillas se cuentan por partido y jugador juntos. */
    private record PartidoYJugador(int partidoId, int jugadorId) {
    }

    private Suspensiones() {
    }

    /**
     * @param tarjetas las tarjetas de los partidos FINALIZADOS de una sola fecha
     * @return los jugadores sancionados (id del jugador → motivo), en el orden de las tarjetas
     */
    public static Map<Integer, Motivo> sancionados(List<Tarjeta> tarjetas) {
        // Se cuenta por partido y jugador, para que no se sumen amarillas de partidos distintos
        Map<PartidoYJugador, Integer> amarillas = new LinkedHashMap<>();
        Map<Integer, Motivo> resultado = new LinkedHashMap<>();
        for (Tarjeta t : tarjetas) {
            if (t.tipo() == TipoEvento.ROJA) {
                resultado.put(t.jugadorId(), Motivo.ROJA);
            } else if (t.tipo() == TipoEvento.AMARILLA) {
                int cantidad = amarillas.merge(new PartidoYJugador(t.partidoId(), t.jugadorId()), 1, Integer::sum);
                if (cantidad >= 2) {
                    // Si además tiene una roja, la roja tiene prioridad como motivo
                    resultado.putIfAbsent(t.jugadorId(), Motivo.DOBLE_AMARILLA);
                }
            }
        }
        return resultado;
    }
}
