package com.isw2.liga.controller;

import com.isw2.liga.dto.PartidoDTO;
import com.isw2.liga.dto.PartidoRequest;
import com.isw2.liga.service.PartidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** RF-10: fixture público. RF-06: programar partidos, solo ADMIN. El detalle de un partido está en ResultadosController. */
@RestController
@RequestMapping("/api/partidos")
public class PartidoController {

    private final PartidoService partidoService;

    public PartidoController(PartidoService partidoService) {
        this.partidoService = partidoService;
    }

    @GetMapping
    public List<PartidoDTO> listar(@RequestParam Integer torneoId, @RequestParam(required = false) Integer fecha) {
        return partidoService.listar(torneoId, fecha);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PartidoDTO programar(@Valid @RequestBody PartidoRequest request) {
        return partidoService.programar(request);
    }
}
