package com.isw2.liga.dto;

import com.isw2.liga.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCrearRequest(
        @NotBlank @Size(max = 40) String username,
        @NotBlank @Size(min = 8, message = "debe tener al menos 8 caracteres") String password,
        @NotBlank @Size(max = 100) String nombreCompleto,
        @NotNull Rol rol) {
}
