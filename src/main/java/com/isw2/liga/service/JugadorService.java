package com.isw2.liga.service;

import com.isw2.liga.dto.JugadorCrearRequest;
import com.isw2.liga.dto.JugadorDTO;
import com.isw2.liga.dto.JugadorModificarRequest;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.Equipo;
import com.isw2.liga.model.Jugador;
import com.isw2.liga.repository.JugadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-04 Gestionar jugadores. */
@Service
@Transactional
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final EquipoService equipoService;

    public JugadorService(JugadorRepository jugadorRepository, EquipoService equipoService) {
        this.jugadorRepository = jugadorRepository;
        this.equipoService = equipoService;
    }

    @Transactional(readOnly = true)
    public JugadorDTO obtener(Integer id) {
        return JugadorDTO.desde(buscar(id));
    }

    public JugadorDTO crear(JugadorCrearRequest request) {
        Equipo equipo = equipoService.buscar(request.equipoId());
        if (!equipo.isActivo()) {
            throw ApiException.conflicto("El equipo " + equipo.getNombre() + " está desactivado");
        }
        // RF-04: el DNI no se repite en todo el sistema
        if (jugadorRepository.existsByDni(request.dni())) {
            throw ApiException.conflicto("Ya existe un jugador con el DNI " + request.dni());
        }
        Jugador jugador = new Jugador();
        jugador.setEquipo(equipo);
        jugador.setNombre(request.nombre().trim());
        jugador.setApellido(request.apellido().trim());
        jugador.setDni(request.dni());
        jugador.setNumeroCamiseta(request.numeroCamiseta());
        return JugadorDTO.desde(jugadorRepository.save(jugador));
    }

    public JugadorDTO modificar(Integer id, JugadorModificarRequest request) {
        Jugador jugador = buscar(id);
        if (jugadorRepository.existsByDniAndIdNot(request.dni(), id)) {
            throw ApiException.conflicto("Ya existe un jugador con el DNI " + request.dni());
        }
        jugador.setNombre(request.nombre().trim());
        jugador.setApellido(request.apellido().trim());
        jugador.setDni(request.dni());
        jugador.setNumeroCamiseta(request.numeroCamiseta());
        return JugadorDTO.desde(jugador);
    }

    /** RN-09: los jugadores no se borran, se desactivan. */
    public JugadorDTO cambiarActivo(Integer id, boolean activo) {
        Jugador jugador = buscar(id);
        jugador.setActivo(activo);
        return JugadorDTO.desde(jugador);
    }

    private Jugador buscar(Integer id) {
        return jugadorRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el jugador " + id));
    }
}
