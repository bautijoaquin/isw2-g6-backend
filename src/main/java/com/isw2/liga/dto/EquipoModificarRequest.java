package com.isw2.liga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** No trae torneoId: un equipo no se pasa a otro torneo. */
public record EquipoModificarRequest(
        @NotBlank @Size(max = 80) String nombre,
        @Size(max = 80) String barrio) {
}
