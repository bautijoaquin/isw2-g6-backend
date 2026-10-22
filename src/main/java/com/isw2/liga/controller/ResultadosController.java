package com.isw2.liga.controller;

import com.isw2.liga.dto.FilaTablaDTO;
import com.isw2.liga.dto.PartidoDetalleDTO;
import com.isw2.liga.service.ResultadoService;
import com.isw2.liga.service.TablaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consultas públicas de resultados (RF-10, RF-11). Son GET, así que no piden token. */
@RestController
@RequestMapping("/api")
public class ResultadosController {

    private final ResultadoService resultadoService;
    private final TablaService tablaService;

    public ResultadosController(ResultadoService resultadoService, TablaService tablaService) {
        this.resultadoService = resultadoService;
        this.tablaService = tablaService;
    }

    @GetMapping("/partidos/{id}")
    public PartidoDetalleDTO detalle(@PathVariable Integer id) {
        return resultadoService.detalle(id);
    }

    @GetMapping("/torneos/{torneoId}/tabla")
    public List<FilaTablaDTO> tabla(@PathVariable Integer torneoId) {
        return tablaService.tabla(torneoId);
    }
}
