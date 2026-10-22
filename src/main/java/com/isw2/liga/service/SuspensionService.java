package com.isw2.liga.service;

import com.isw2.liga.calculo.Suspensiones;
import com.isw2.liga.calculo.Suspensiones.Motivo;
import com.isw2.liga.calculo.Suspensiones.Tarjeta;
import com.isw2.liga.dto.SuspendidoDTO;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.EventoPartido;
import com.isw2.liga.model.Jugador;
import com.isw2.liga.repository.EventoPartidoRepository;
import com.isw2.liga.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RF-12 y RN-03: los suspendidos de la fecha N salen de las tarjetas de los partidos
 * FINALIZADOS de la fecha N-1. No se guardan: se calculan en cada consulta.
 */
@Service
@Transactional(readOnly = true)
public class SuspensionService {

    private final TorneoRepository torneoRepository;
    private final EventoPartidoRepository eventoRepository;

    public SuspensionService(TorneoRepository torneoRepository, EventoPartidoRepository eventoRepository) {
        this.torneoRepository = torneoRepository;
        this.eventoRepository = eventoRepository;
    }

    public List<SuspendidoDTO> suspendidos(Integer torneoId, int fecha) {
        if (!torneoRepository.existsById(torneoId)) {
            throw ApiException.noEncontrado("No existe el torneo " + torneoId);
        }
        if (fecha < 1) {
            throw ApiException.datosInvalidos("La fecha tiene que ser 1 o mayor");
        }
        int fechaSancion = fecha - 1;
        if (fechaSancion < 1) {
            return List.of();
        }
        List<EventoPartido> tarjetas = eventoRepository.findTarjetasDeFechaFinalizada(torneoId, fechaSancion);
        Map<Integer, Jugador> jugadores = new HashMap<>();
        tarjetas.forEach(e -> jugadores.put(e.getJugador().getId(), e.getJugador()));

        Map<Integer, Motivo> sancionados = sancionadosDe(tarjetas);
        return sancionados.entrySet().stream()
                .map(s -> SuspendidoDTO.desde(jugadores.get(s.getKey()), s.getValue(), fechaSancion))
                .toList();
    }

    /** RN-03 aplicado a un jugador: lo usa EventoService para no aceptar eventos de un suspendido (RF-08). */
    public boolean estaSuspendido(Integer torneoId, int fecha, Integer jugadorId) {
        if (fecha <= 1) {
            return false;
        }
        return sancionadosDe(eventoRepository.findTarjetasDeFechaFinalizada(torneoId, fecha - 1))
                .containsKey(jugadorId);
    }

    private static Map<Integer, Motivo> sancionadosDe(List<EventoPartido> tarjetas) {
        return Suspensiones.sancionados(tarjetas.stream()
                .map(e -> new Tarjeta(e.getPartido().getId(), e.getJugador().getId(), e.getTipo()))
                .toList());
    }
}
