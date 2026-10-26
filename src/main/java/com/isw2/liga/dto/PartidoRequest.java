package com.isw2.liga.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Lo que manda el admin al programar un partido (RF-06). fechaHoraInicio: "2026-11-07T15:00". */
public record PartidoRequest(
        @NotNull Integer torneoId,
        @NotNull @Min(1) Integer numeroFecha,
        @NotNull Integer equipoLocalId,
        @NotNull Integer equipoVisitanteId,
        @NotNull Integer canchaId,
        @NotNull Integer arbitroId,
        @NotNull LocalDateTime fechaHoraInicio) {
}
