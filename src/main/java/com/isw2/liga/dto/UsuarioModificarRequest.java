package com.isw2.liga.dto;

import com.isw2.liga.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** No trae username: el nombre de usuario no se puede cambiar (RF-02). */
public record UsuarioModificarRequest(
        @NotBlank @Size(max = 100) String nombreCompleto,
        @NotNull Rol rol) {
}
