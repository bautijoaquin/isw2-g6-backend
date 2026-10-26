package com.isw2.liga.controller;

import com.isw2.liga.dto.CambiarActivoRequest;
import com.isw2.liga.dto.JugadorCrearRequest;
import com.isw2.liga.dto.JugadorDTO;
import com.isw2.liga.dto.JugadorModificarRequest;
import com.isw2.liga.service.JugadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** RF-04: escrituras solo ADMIN. La lista por equipo está en GET /api/equipos/{id}/jugadores. */
@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {

    private final JugadorService jugadorService;

    public JugadorController(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    @GetMapping("/{id}")
    public JugadorDTO obtener(@PathVariable Integer id) {
        return jugadorService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public JugadorDTO crear(@Valid @RequestBody JugadorCrearRequest request) {
        return jugadorService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public JugadorDTO modificar(@PathVariable Integer id, @Valid @RequestBody JugadorModificarRequest request) {
        return jugadorService.modificar(id, request);
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public JugadorDTO cambiarActivo(@PathVariable Integer id, @Valid @RequestBody CambiarActivoRequest request) {
        return jugadorService.cambiarActivo(id, request.activo());
    }
}
