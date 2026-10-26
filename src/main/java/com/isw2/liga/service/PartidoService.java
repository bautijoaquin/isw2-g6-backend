package com.isw2.liga.service;

import com.isw2.liga.calculo.Resultado;
import com.isw2.liga.dto.PartidoDTO;
import com.isw2.liga.dto.PartidoRequest;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.Arbitro;
import com.isw2.liga.model.Cancha;
import com.isw2.liga.model.Equipo;
import com.isw2.liga.model.EstadoPartido;
import com.isw2.liga.model.Partido;
import com.isw2.liga.model.Torneo;
import com.isw2.liga.repository.ArbitroRepository;
import com.isw2.liga.repository.CanchaRepository;
import com.isw2.liga.repository.EquipoRepository;
import com.isw2.liga.repository.PartidoRepository;
import com.isw2.liga.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RF-06 y RF-10: programar partidos y armar el fixture.
 * El resultado de cada partido se calcula con sus goles (RN-06).
 */
@Service
@Transactional
public class PartidoService {

    /** RN-01: cada partido reserva la cancha (y el árbitro, RN-02) 2 horas desde su inicio. */
    static final Duration SEPARACION_MINIMA = Duration.ofHours(2);

    private final PartidoRepository partidoRepository;
    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;
    private final CanchaRepository canchaRepository;
    private final ArbitroRepository arbitroRepository;
    private final ResultadoService resultadoService;

    public PartidoService(PartidoRepository partidoRepository, TorneoRepository torneoRepository,
                          EquipoRepository equipoRepository, CanchaRepository canchaRepository,
                          ArbitroRepository arbitroRepository, ResultadoService resultadoService) {
        this.partidoRepository = partidoRepository;
        this.torneoRepository = torneoRepository;
        this.equipoRepository = equipoRepository;
        this.canchaRepository = canchaRepository;
        this.arbitroRepository = arbitroRepository;
        this.resultadoService = resultadoService;
    }

    /** Sin fecha devuelve todo el torneo, ordenado por fecha y horario. */
    @Transactional(readOnly = true)
    public List<PartidoDTO> listar(Integer torneoId, Integer fecha) {
        if (!torneoRepository.existsById(torneoId)) {
            throw ApiException.noEncontrado("No existe el torneo " + torneoId);
        }
        if (fecha != null && fecha < 1) {
            throw ApiException.datosInvalidos("La fecha tiene que ser 1 o mayor");
        }
        List<Partido> partidos = fecha == null
                ? partidoRepository.findByTorneoIdOrderByNumeroFechaAscFechaHoraInicioAsc(torneoId)
                : partidoRepository.findByTorneoIdAndNumeroFechaOrderByFechaHoraInicioAsc(torneoId, fecha);
        Map<Integer, Resultado> resultados = resultadoService.resultadosDe(partidos);
        return partidos.stream()
                .map(p -> PartidoDTO.desde(p, resultados.get(p.getId())))
                .toList();
    }

    /** RF-06: el partido nuevo queda PROGRAMADO (es el valor por defecto de la entidad). */
    public PartidoDTO programar(PartidoRequest request) {
        Torneo torneo = torneoRepository.findById(request.torneoId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe el torneo " + request.torneoId()));
        if (!torneo.isActivo()) {
            throw ApiException.conflicto("El torneo " + torneo.getNombre() + " no está activo");
        }
        Partido partido = new Partido();
        partido.setTorneo(torneo);
        completar(partido, request, 0);
        return PartidoDTO.desde(partidoRepository.save(partido), new Resultado(0, 0));
    }

    /** RF-06 y RN-07: solo se reprograma un partido que todavía no empezó. El torneo no se cambia. */
    public PartidoDTO modificar(Integer id, PartidoRequest request) {
        Partido partido = buscarProgramado(id, "modificar");
        if (!partido.getTorneo().getId().equals(request.torneoId())) {
            throw ApiException.datosInvalidos("El torneo de un partido no se puede cambiar");
        }
        completar(partido, request, id);
        return PartidoDTO.desde(partido, new Resultado(0, 0));
    }

    /** RF-06 y RN-07: un partido PROGRAMADO todavía no tiene eventos, así que se puede borrar. */
    public void eliminar(Integer id) {
        partidoRepository.delete(buscarProgramado(id, "eliminar"));
    }

    /**
     * Valida el pedido contra los criterios de RF-06 y las reglas RN-01, RN-02 y RN-07, y si está todo bien
     * carga los datos en el partido.
     *
     * @param excluirId id del partido que se está modificando, para no chocar consigo mismo (0 si es nuevo)
     */
    private void completar(Partido partido, PartidoRequest request, Integer excluirId) {
        // RF-06: local y visitante distintos, del torneo y activos; cancha y árbitro activos
        if (request.equipoLocalId().equals(request.equipoVisitanteId())) {
            throw ApiException.datosInvalidos("El equipo local y el visitante tienen que ser distintos (RF-06)");
        }
        Integer torneoId = partido.getTorneo().getId();
        Equipo local = equipoDelTorneo(request.equipoLocalId(), torneoId);
        Equipo visitante = equipoDelTorneo(request.equipoVisitanteId(), torneoId);
        Cancha cancha = canchaRepository.findById(request.canchaId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe la cancha " + request.canchaId()));
        if (!cancha.isActivo()) {
            throw ApiException.conflicto("La cancha " + cancha.getNombre() + " no está activa (RF-06)");
        }
        Arbitro arbitro = arbitroRepository.findById(request.arbitroId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe el árbitro " + request.arbitroId()));
        if (!arbitro.isActivo()) {
            throw ApiException.conflicto("El árbitro " + arbitro.getApellido() + " no está activo (RF-06)");
        }

        LocalDateTime inicio = request.fechaHoraInicio();
        LocalDateTime desde = inicio.minus(SEPARACION_MINIMA);
        LocalDateTime hasta = inicio.plus(SEPARACION_MINIMA);
        // RN-01
        if (partidoRepository.canchaOcupada(cancha.getId(), desde, hasta, excluirId)) {
            throw ApiException.conflicto("La cancha ya tiene un partido a menos de 2 horas (RN-01)");
        }
        // RF-06: un equipo no juega dos veces en la misma fecha
        int fecha = request.numeroFecha();
        for (Equipo e : List.of(local, visitante)) {
            if (partidoRepository.equipoJuegaEnLaFecha(torneoId, fecha, e.getId(), excluirId)) {
                throw ApiException.conflicto(e.getNombre() + " ya juega en la fecha " + fecha + " (RF-06)");
            }
        }
        // RN-02
        if (partidoRepository.arbitroOcupado(arbitro.getId(), desde, hasta, excluirId)) {
            throw ApiException.conflicto("El árbitro ya dirige un partido a menos de 2 horas (RN-02)");
        }
        // RN-07: las fechas se juegan en orden. Cada partido ocupa 2 horas desde su inicio (RN-01),
        // asi que una fecha termina 2 horas despues de que empieza su ultimo partido.
        LocalDateTime ultimoDeLaAnterior = partidoRepository.ultimoInicioDeLaFecha(torneoId, fecha - 1, excluirId);
        if (ultimoDeLaAnterior != null && inicio.isBefore(ultimoDeLaAnterior.plus(SEPARACION_MINIMA))) {
            throw ApiException.conflicto("La fecha " + fecha + " no puede empezar antes de que termine la fecha "
                    + (fecha - 1) + " (RN-07)");
        }
        LocalDateTime primeroDeLaSiguiente = partidoRepository.primerInicioDeLaFecha(torneoId, fecha + 1, excluirId);
        if (primeroDeLaSiguiente != null && hasta.isAfter(primeroDeLaSiguiente)) {
            throw ApiException.conflicto("La fecha " + fecha + " tiene que terminar antes de que empiece la fecha "
                    + (fecha + 1) + " (RN-07)");
        }

        partido.setNumeroFecha(fecha);
        partido.setEquipoLocal(local);
        partido.setEquipoVisitante(visitante);
        partido.setCancha(cancha);
        partido.setArbitro(arbitro);
        partido.setFechaHoraInicio(inicio);
    }

    private Partido buscarProgramado(Integer id, String accion) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el partido " + id));
        if (partido.getEstado() != EstadoPartido.PROGRAMADO) {
            throw ApiException.conflicto("El partido está " + partido.getEstado()
                    + ": solo se puede " + accion + " un partido PROGRAMADO (RN-07)");
        }
        return partido;
    }

    private Equipo equipoDelTorneo(Integer equipoId, Integer torneoId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el equipo " + equipoId));
        if (!equipo.getTorneo().getId().equals(torneoId)) {
            throw ApiException.datosInvalidos("El equipo " + equipo.getNombre() + " no es de este torneo (RF-06)");
        }
        if (!equipo.isActivo()) {
            throw ApiException.conflicto("El equipo " + equipo.getNombre() + " está desactivado (RF-06)");
        }
        return equipo;
    }
}
