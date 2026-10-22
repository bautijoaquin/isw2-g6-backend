package com.isw2.liga.controller;

import com.isw2.liga.dto.ArbitroDTO;
import com.isw2.liga.dto.CanchaDTO;
import com.isw2.liga.dto.TorneoDTO;
import com.isw2.liga.service.CatalogoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/torneos")
    public List<TorneoDTO> torneos(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return catalogoService.torneos(incluirInactivos);
    }

    @GetMapping("/canchas")
    public List<CanchaDTO> canchas(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return catalogoService.canchas(incluirInactivos);
    }

    @GetMapping("/arbitros")
    public List<ArbitroDTO> arbitros(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return catalogoService.arbitros(incluirInactivos);
    }
}
