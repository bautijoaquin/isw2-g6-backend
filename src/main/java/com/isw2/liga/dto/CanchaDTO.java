package com.isw2.liga.dto;

import com.isw2.liga.model.Cancha;

public record CanchaDTO(Integer id, String nombre, String direccion, boolean activo) {

    public static CanchaDTO desde(Cancha c) {
        return new CanchaDTO(c.getId(), c.getNombre(), c.getDireccion(), c.isActivo());
    }
}
