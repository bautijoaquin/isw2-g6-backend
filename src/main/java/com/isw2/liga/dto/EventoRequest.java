package com.isw2.liga.dto;

import com.isw2.liga.model.TipoEvento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Lo que manda el delegado al cargar un gol o una tarjeta (RF-08). */
public record EventoRequest(
        @NotNull Integer jugadorId,
        @NotNull TipoEvento tipo,
        @NotNull @Min(1) @Max(120) Integer minuto) {
}
