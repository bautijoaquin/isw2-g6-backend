package com.isw2.liga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(
        @NotBlank @Size(min = 8, message = "debe tener al menos 8 caracteres") String password) {
}
