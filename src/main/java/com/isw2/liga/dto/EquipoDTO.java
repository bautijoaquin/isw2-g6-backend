package com.isw2.liga.dto;

import com.isw2.liga.model.Equipo;

public record EquipoDTO(Integer id, Integer torneoId, String nombre, String barrio, boolean activo) {

    public static EquipoDTO desde(Equipo e) {
        return new EquipoDTO(e.getId(), e.getTorneo().getId(), e.getNombre(), e.getBarrio(), e.isActivo());
    }
}
