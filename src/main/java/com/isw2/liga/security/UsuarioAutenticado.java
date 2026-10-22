package com.isw2.liga.security;

/**
 * Quién hizo la petición. Se obtiene en un controller con
 * {@code @AuthenticationPrincipal UsuarioAutenticado usuario}
 * (por ejemplo para RN-08: un admin no puede desactivarse a sí mismo).
 */
public record UsuarioAutenticado(Integer id, String username, String rol) {
}
