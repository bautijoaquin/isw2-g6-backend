package com.isw2.liga.dto;

import com.isw2.liga.model.Arbitro;

public record ArbitroDTO(Integer id, String nombre, String apellido, String dni, boolean activo) {

    public static ArbitroDTO desde(Arbitro a) {
        return new ArbitroDTO(a.getId(), a.getNombre(), a.getApellido(), a.getDni(), a.isActivo());
    }
}
