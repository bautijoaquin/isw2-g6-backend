package com.isw2.liga.dto;

/** Formato único de error para toda la API. */
public record ErrorResponse(int status, String mensaje) {
}
