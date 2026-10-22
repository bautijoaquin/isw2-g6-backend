package com.isw2.liga.dto;

import com.isw2.liga.calculo.Suspensiones.Motivo;
import com.isw2.liga.model.Jugador;

/** Un jugador que no puede jugar la fecha consultada (RF-12). fechaSancion es la fecha de la tarjeta. */
public record SuspendidoDTO(Integer jugadorId, String nombre, String apellido, Integer numeroCamiseta,
                            Integer equipoId, String equipo, Motivo motivo, int fechaSancion) {

    public static SuspendidoDTO desde(Jugador j, Motivo motivo, int fechaSancion) {
        return new SuspendidoDTO(j.getId(), j.getNombre(), j.getApellido(), j.getNumeroCamiseta(),
                j.getEquipo().getId(), j.getEquipo().getNombre(), motivo, fechaSancion);
    }
}
