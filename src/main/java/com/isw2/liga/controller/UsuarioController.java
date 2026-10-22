package com.isw2.liga.controller;

import com.isw2.liga.dto.CambiarActivoRequest;
import com.isw2.liga.dto.CambiarPasswordRequest;
import com.isw2.liga.dto.UsuarioCrearRequest;
import com.isw2.liga.dto.UsuarioDTO;
import com.isw2.liga.dto.UsuarioModificarRequest;
import com.isw2.liga.security.UsuarioAutenticado;
import com.isw2.liga.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** RF-02: todo el controller es solo para ADMIN. */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioDTO> listar(@RequestParam(defaultValue = "true") boolean incluirInactivos) {
        return usuarioService.listar(incluirInactivos);
    }

    @GetMapping("/{id}")
    public UsuarioDTO obtener(@PathVariable Integer id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDTO crear(@Valid @RequestBody UsuarioCrearRequest request) {
        return usuarioService.crear(request);
    }

    @PutMapping("/{id}")
    public UsuarioDTO modificar(@PathVariable Integer id, @Valid @RequestBody UsuarioModificarRequest request,
                                @AuthenticationPrincipal UsuarioAutenticado quien) {
        return usuarioService.modificar(id, request, quien.id());
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@PathVariable Integer id, @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(id, request.password());
    }

    @PatchMapping("/{id}/activo")
    public UsuarioDTO cambiarActivo(@PathVariable Integer id, @Valid @RequestBody CambiarActivoRequest request,
                                    @AuthenticationPrincipal UsuarioAutenticado quien) {
        return usuarioService.cambiarActivo(id, request.activo(), quien.id());
    }
}
