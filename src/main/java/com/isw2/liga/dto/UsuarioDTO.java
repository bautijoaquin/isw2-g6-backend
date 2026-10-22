package com.isw2.liga.dto;

import com.isw2.liga.model.Rol;
import com.isw2.liga.model.Usuario;

import java.time.LocalDateTime;

/** Usuario hacia afuera: nunca incluye la contraseña ni su hash (RNF-06). */
public record UsuarioDTO(Integer id, String username, String nombreCompleto, Rol rol, boolean activo,
                         LocalDateTime fechaAlta) {

    public static UsuarioDTO desde(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getRol(), u.isActivo(),
                u.getFechaAlta());
    }
}
