package com.isw2.liga.dto;

import com.isw2.liga.model.Rol;

public record LoginResponse(String token, String nombre, Rol rol) {
}
