package com.isw2.liga.service;

import com.isw2.liga.dto.EventoDTO;
import com.isw2.liga.dto.EventoRequest;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.EstadoPartido;
import com.isw2.liga.model.EventoPartido;
import com.isw2.liga.model.Jugador;
import com.isw2.liga.model.Partido;
import com.isw2.liga.repository.EventoPartidoRepository;
import com.isw2.liga.repository.JugadorRepository;
import com.isw2.liga.repository.PartidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF-08 y RF-09: cargar y borrar los goles y tarjetas de un partido.
 * El resultado, la tabla y los suspendidos se recalculan solos porque salen de estos eventos (RN-06).
 */
@Service
@Transactional
public class EventoService {

    private final PartidoRepository partidoRepository;
    private final JugadorRepository jugadorRepository;
    private final EventoPartidoRepository eventoRepository;
    private final SuspensionService suspensionService;

    public EventoService(PartidoRepository partidoRepository, JugadorRepository jugadorRepository,
                         EventoPartidoRepository eventoRepository, SuspensionService suspensionService) {
        this.partidoRepository = partidoRepository;
        this.jugadorRepository = jugadorRepository;
        this.eventoRepository = eventoRepository;
        this.suspensionService = suspensionService;
    }

    public EventoDTO registrar(Integer partidoId, EventoRequest pedido) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el partido " + partidoId));
        exigirEnJuego(partido);

        // RN-04: solo jugadores activos de uno de los dos equipos del partido
        Jugador jugador = jugadorRepository.findById(pedido.jugadorId())
                .orElseThrow(() -> ApiException.datosInvalidos("No existe el jugador " + pedido.jugadorId()));
        Integer equipoId = jugador.getEquipo().getId();
        if (!equipoId.equals(partido.getEquipoLocal().getId())
                && !equipoId.equals(partido.getEquipoVisitante().getId())) {
            throw ApiException.datosInvalidos("El jugador no es del equipo local ni del visitante (RN-04)");
        }
        if (!jugador.isActivo()) {
            throw ApiException.datosInvalidos("El jugador está dado de baja (RN-04)");
        }
        // RN-03: un suspendido para esta fecha no puede tener eventos
        if (suspensionService.estaSuspendido(partido.getTorneo().getId(), partido.getNumeroFecha(), jugador.getId())) {
            throw ApiException.conflicto("El jugador está suspendido para la fecha "
                    + partido.getNumeroFecha() + " (RN-03)");
        }

        EventoPartido evento = new EventoPartido();
        evento.setPartido(partido);
        evento.setJugador(jugador);
        evento.setTipo(pedido.tipo());
        evento.setMinuto(pedido.minuto());
        return EventoDTO.desde(eventoRepository.save(evento));
    }

    /** RF-09: corregir es borrar el evento cargado por error, solo mientras se juega. */
    public void borrar(Integer eventoId) {
        EventoPartido evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el evento " + eventoId));
        exigirEnJuego(evento.getPartido());
        eventoRepository.delete(evento);
    }

    private static void exigirEnJuego(Partido partido) {
        if (partido.getEstado() != EstadoPartido.EN_JUEGO) {
            throw ApiException.conflicto("El partido está " + partido.getEstado()
                    + ": los eventos solo se cargan o corrigen con el partido EN_JUEGO");
        }
    }
}
