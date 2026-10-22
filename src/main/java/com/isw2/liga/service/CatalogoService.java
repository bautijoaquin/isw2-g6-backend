package com.isw2.liga.service;

import com.isw2.liga.dto.ArbitroDTO;
import com.isw2.liga.dto.CanchaDTO;
import com.isw2.liga.dto.TorneoDTO;
import com.isw2.liga.repository.ArbitroRepository;
import com.isw2.liga.repository.CanchaRepository;
import com.isw2.liga.repository.TorneoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RF-05 (parte precargada): torneo, canchas y árbitros solo se consultan.
 * Por defecto se muestran solo los activos.
 */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final TorneoRepository torneoRepository;
    private final CanchaRepository canchaRepository;
    private final ArbitroRepository arbitroRepository;

    public CatalogoService(TorneoRepository torneoRepository, CanchaRepository canchaRepository,
                           ArbitroRepository arbitroRepository) {
        this.torneoRepository = torneoRepository;
        this.canchaRepository = canchaRepository;
        this.arbitroRepository = arbitroRepository;
    }

    public List<TorneoDTO> torneos(boolean incluirInactivos) {
        var torneos = incluirInactivos
                ? torneoRepository.findAll(Sort.by("id"))
                : torneoRepository.findByActivoTrueOrderByIdAsc();
        return torneos.stream().map(TorneoDTO::desde).toList();
    }

    public List<CanchaDTO> canchas(boolean incluirInactivas) {
        var canchas = incluirInactivas
                ? canchaRepository.findAll(Sort.by("nombre"))
                : canchaRepository.findByActivoTrueOrderByNombreAsc();
        return canchas.stream().map(CanchaDTO::desde).toList();
    }

    public List<ArbitroDTO> arbitros(boolean incluirInactivos) {
        var arbitros = incluirInactivos
                ? arbitroRepository.findAll(Sort.by("apellido", "nombre"))
                : arbitroRepository.findByActivoTrueOrderByApellidoAscNombreAsc();
        return arbitros.stream().map(ArbitroDTO::desde).toList();
    }
}
