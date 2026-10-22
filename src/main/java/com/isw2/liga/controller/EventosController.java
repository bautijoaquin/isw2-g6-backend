package com.isw2.liga.controller;

import com.isw2.liga.dto.EventoDTO;
import com.isw2.liga.dto.EventoRequest;
import com.isw2.liga.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Carga y corrección de eventos durante el partido (RF-08, RF-09). Requiere token de delegado o admin. */
@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('ADMIN','DELEGADO')")
public class EventosController {

    private final EventoService eventoService;

    public EventosController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @PostMapping("/partidos/{partidoId}/eventos")
    @ResponseStatus(HttpStatus.CREATED)
    public EventoDTO registrar(@PathVariable Integer partidoId, @Valid @RequestBody EventoRequest pedido) {
        return eventoService.registrar(partidoId, pedido);
    }

    @DeleteMapping("/eventos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(@PathVariable Integer id) {
        eventoService.borrar(id);
    }
}
