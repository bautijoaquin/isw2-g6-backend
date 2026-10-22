package com.isw2.liga.dto;

import com.isw2.liga.model.EventoPartido;
import com.isw2.liga.model.Jugador;
import com.isw2.liga.model.TipoEvento;

public record EventoDTO(Integer id, TipoEvento tipo, int minuto, Integer jugadorId, String jugador,
                        Integer numeroCamiseta, Integer equipoId) {

    public static EventoDTO desde(EventoPartido e) {
        Jugador j = e.getJugador();
        return new EventoDTO(e.getId(), e.getTipo(), e.getMinuto(), j.getId(),
                j.getNombre() + " " + j.getApellido(), j.getNumeroCamiseta(), j.getEquipo().getId());
    }
}
