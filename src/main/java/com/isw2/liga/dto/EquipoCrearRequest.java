package com.isw2.liga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** RF-03: el nombre es obligatorio y el barrio opcional. */
public record EquipoCrearRequest(
        @NotNull Integer torneoId,
        @NotBlank @Size(max = 80) String nombre,
        @Size(max = 80) String barrio) {
}
