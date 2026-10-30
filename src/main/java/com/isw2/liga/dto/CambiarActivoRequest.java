package com.isw2.liga.dto;

import jakarta.validation.constraints.NotNull;

/** Baja o alta lógica (RN-09). Lo pueden reutilizar equipos y jugadores. */
public record CambiarActivoRequest(@NotNull Boolean activo) {
}
