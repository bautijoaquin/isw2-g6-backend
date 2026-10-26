package com.isw2.liga.controller;

import com.isw2.liga.dto.PartidoDTO;
import com.isw2.liga.service.PartidoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** RF-10: fixture público. El detalle de un partido está en ResultadosController. */
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
}
