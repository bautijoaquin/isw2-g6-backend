package com.isw2.liga.controller;

import com.isw2.liga.dto.CambiarActivoRequest;
import com.isw2.liga.dto.EquipoCrearRequest;
import com.isw2.liga.dto.EquipoDTO;
import com.isw2.liga.dto.EquipoModificarRequest;
import com.isw2.liga.dto.JugadorDTO;
import com.isw2.liga.service.EquipoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** RF-03: los GET son públicos (RF-05), el resto solo ADMIN. */
@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping
    public List<EquipoDTO> listar(@RequestParam(required = false) Integer torneoId,
                                  @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return equipoService.listar(torneoId, incluirInactivos);
    }

    @GetMapping("/{id}")
    public EquipoDTO obtener(@PathVariable Integer id) {
        return equipoService.obtener(id);
    }

    @GetMapping("/{id}/jugadores")
    public List<JugadorDTO> jugadores(@PathVariable Integer id,
                                      @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return equipoService.jugadores(id, incluirInactivos);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EquipoDTO crear(@Valid @RequestBody EquipoCrearRequest request) {
        return equipoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EquipoDTO modificar(@PathVariable Integer id, @Valid @RequestBody EquipoModificarRequest request) {
        return equipoService.modificar(id, request);
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public EquipoDTO cambiarActivo(@PathVariable Integer id, @Valid @RequestBody CambiarActivoRequest request) {
        return equipoService.cambiarActivo(id, request.activo());
    }
}
