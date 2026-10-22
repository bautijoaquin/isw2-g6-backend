package com.isw2.liga.exception;

import org.springframework.http.HttpStatus;

/**
 * Error de negocio con su código HTTP. Los services la lanzan y
 * GlobalExceptionHandler la convierte en una respuesta JSON (RNF-08).
 *
 * Ejemplo: throw ApiException.conflicto("La cancha está ocupada en ese horario (RN-01)");
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException noEncontrado(String mensaje) {
        return new ApiException(HttpStatus.NOT_FOUND, mensaje);
    }

    public static ApiException datosInvalidos(String mensaje) {
        return new ApiException(HttpStatus.BAD_REQUEST, mensaje);
    }

    public static ApiException conflicto(String mensaje) {
        return new ApiException(HttpStatus.CONFLICT, mensaje);
    }

    public static ApiException noAutorizado(String mensaje) {
        return new ApiException(HttpStatus.UNAUTHORIZED, mensaje);
    }
}
