package com.isw2.liga.service;

import com.isw2.liga.calculo.Resultado;
import com.isw2.liga.dto.EventoDTO;
import com.isw2.liga.dto.PartidoDetalleDTO;
import com.isw2.liga.dto.ResumenDTO;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.EventoPartido;
import com.isw2.liga.model.Partido;
import com.isw2.liga.model.TipoEvento;
import com.isw2.liga.repository.EventoPartidoRepository;
import com.isw2.liga.repository.PartidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RF-10 y RN-06: el resultado de un partido nunca se guarda, se calcula con sus goles.
 * resultadosDe() también sirve para el listado de partidos de una fecha.
 */
@Service
@Transactional(readOnly = true)
public class ResultadoService {

    private final PartidoRepository partidoRepository;
    private final EventoPartidoRepository eventoRepository;

    public ResultadoService(PartidoRepository partidoRepository, EventoPartidoRepository eventoRepository) {
        this.partidoRepository = partidoRepository;
        this.eventoRepository = eventoRepository;
    }

    public PartidoDetalleDTO detalle(Integer partidoId) {
        Partido p = partidoRepository.findConDetalleById(partidoId)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el partido " + partidoId));
        List<EventoPartido> eventos = eventoRepository.findEventosDePartido(partidoId);
        Resultado r = resultado(p, eventos);
        return new PartidoDetalleDTO(p.getId(), p.getTorneo().getId(), p.getNumeroFecha(),
                new ResumenDTO(p.getEquipoLocal().getId(), p.getEquipoLocal().getNombre()),
                new ResumenDTO(p.getEquipoVisitante().getId(), p.getEquipoVisitante().getNombre()),
                r.golesLocal(), r.golesVisitante(),
                new ResumenDTO(p.getCancha().getId(), p.getCancha().getNombre()),
                new ResumenDTO(p.getArbitro().getId(), p.getArbitro().getNombre() + " " + p.getArbitro().getApellido()),
                p.getFechaHoraInicio(), p.getEstado(),
                eventos.stream().map(EventoDTO::desde).toList());
    }

    /** Resultado de cada partido (id del partido → resultado), con una sola consulta de goles. */
    public Map<Integer, Resultado> resultadosDe(List<Partido> partidos) {
        if (partidos.isEmpty()) {
            return Map.of();
        }
        Map<Integer, List<EventoPartido>> golesPorPartido = eventoRepository.findGolesDe(partidos).stream()
                .collect(Collectors.groupingBy(e -> e.getPartido().getId()));
        Map<Integer, Resultado> resultados = new HashMap<>();
        for (Partido p : partidos) {
            resultados.put(p.getId(), resultado(p, golesPorPartido.getOrDefault(p.getId(), List.of())));
        }
        return resultados;
    }

    /** Único lugar donde los eventos se convierten en resultado: el detalle y la tabla usan el mismo. */
    private static Resultado resultado(Partido p, List<EventoPartido> eventos) {
        List<Integer> equiposDeLosGoles = eventos.stream()
                .filter(e -> e.getTipo() == TipoEvento.GOL)
                .map(e -> e.getJugador().getEquipo().getId())
                .toList();
        return Resultado.calcular(p.getEquipoLocal().getId(), p.getEquipoVisitante().getId(), equiposDeLosGoles);
    }
}
