package com.isw2.liga.dto;

import com.isw2.liga.model.Jugador;

public record JugadorDTO(Integer id, Integer equipoId, String equipo, String nombre, String apellido, String dni,
                         Integer numeroCamiseta, boolean activo) {

    public static JugadorDTO desde(Jugador j) {
        return new JugadorDTO(j.getId(), j.getEquipo().getId(), j.getEquipo().getNombre(), j.getNombre(),
                j.getApellido(), j.getDni(), j.getNumeroCamiseta(), j.isActivo());
    }
}
