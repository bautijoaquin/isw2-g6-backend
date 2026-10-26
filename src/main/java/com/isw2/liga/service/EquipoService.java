package com.isw2.liga.service;

import com.isw2.liga.dto.EquipoCrearRequest;
import com.isw2.liga.dto.EquipoDTO;
import com.isw2.liga.dto.EquipoModificarRequest;
import com.isw2.liga.dto.JugadorDTO;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.Equipo;
import com.isw2.liga.model.Torneo;
import com.isw2.liga.repository.EquipoRepository;
import com.isw2.liga.repository.JugadorRepository;
import com.isw2.liga.repository.TorneoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF-03 Gestionar equipos y su parte de RF-05 (consulta pública). */
@Service
@Transactional
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final TorneoRepository torneoRepository;
    private final JugadorRepository jugadorRepository;

    public EquipoService(EquipoRepository equipoRepository, TorneoRepository torneoRepository,
                         JugadorRepository jugadorRepository) {
        this.equipoRepository = equipoRepository;
        this.torneoRepository = torneoRepository;
        this.jugadorRepository = jugadorRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipoDTO> listar(Integer torneoId, boolean incluirInactivos) {
        var equipos = torneoId == null
                ? equipoRepository.findAll(Sort.by("nombre"))
                : equipoRepository.findByTorneoIdOrderByNombreAsc(torneoId);
        return equipos.stream()
                .filter(e -> incluirInactivos || e.isActivo())
                .map(EquipoDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipoDTO obtener(Integer id) {
        return EquipoDTO.desde(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<JugadorDTO> jugadores(Integer equipoId, boolean incluirInactivos) {
        buscar(equipoId);
        return jugadorRepository.findByEquipoIdOrderByApellidoAscNombreAsc(equipoId).stream()
                .filter(j -> incluirInactivos || j.isActivo())
                .map(JugadorDTO::desde)
                .toList();
    }

    public EquipoDTO crear(EquipoCrearRequest request) {
        Torneo torneo = torneoRepository.findById(request.torneoId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe el torneo " + request.torneoId()));
        if (!torneo.isActivo()) {
            throw ApiException.conflicto("El torneo " + torneo.getNombre() + " no está activo");
        }
        String nombre = request.nombre().trim();
        // RF-03: nombre único dentro del torneo
        if (equipoRepository.existsByTorneoIdAndNombreIgnoreCase(torneo.getId(), nombre)) {
            throw ApiException.conflicto("Ya existe un equipo llamado " + nombre + " en el torneo");
        }
        Equipo equipo = new Equipo();
        equipo.setTorneo(torneo);
        equipo.setNombre(nombre);
        equipo.setBarrio(limpiar(request.barrio()));
        return EquipoDTO.desde(equipoRepository.save(equipo));
    }

    public EquipoDTO modificar(Integer id, EquipoModificarRequest request) {
        Equipo equipo = buscar(id);
        String nombre = request.nombre().trim();
        if (equipoRepository.existsByTorneoIdAndNombreIgnoreCaseAndIdNot(equipo.getTorneo().getId(), nombre, id)) {
            throw ApiException.conflicto("Ya existe un equipo llamado " + nombre + " en el torneo");
        }
        equipo.setNombre(nombre);
        equipo.setBarrio(limpiar(request.barrio()));
        return EquipoDTO.desde(equipo);
    }

    /** RN-09: los equipos no se borran, se desactivan. */
    public EquipoDTO cambiarActivo(Integer id, boolean activo) {
        Equipo equipo = buscar(id);
        equipo.setActivo(activo);
        return EquipoDTO.desde(equipo);
    }

    Equipo buscar(Integer id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el equipo " + id));
    }

    // El barrio es opcional: un texto vacío se guarda como null
    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
