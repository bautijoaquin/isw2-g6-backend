package com.isw2.liga.dto;

import com.isw2.liga.model.Torneo;

public record TorneoDTO(Integer id, String nombre, int anio, boolean activo) {

    public static TorneoDTO desde(Torneo t) {
        return new TorneoDTO(t.getId(), t.getNombre(), t.getAnio(), t.isActivo());
    }
}
