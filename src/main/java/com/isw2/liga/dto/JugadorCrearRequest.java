package com.isw2.liga.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** RF-04: nombre, apellido y DNI obligatorios; la camiseta es opcional y va de 1 a 99. */
public record JugadorCrearRequest(
        @NotNull Integer equipoId,
        @NotBlank @Size(max = 60) String nombre,
        @NotBlank @Size(max = 60) String apellido,
        @NotBlank @Pattern(regexp = "[0-9]{7,8}", message = "debe tener 7 u 8 dígitos, sin puntos") String dni,
        @Min(1) @Max(99) Integer numeroCamiseta) {
}
