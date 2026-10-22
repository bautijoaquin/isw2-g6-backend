package com.isw2.liga.service;

import com.isw2.liga.calculo.Resultado;
import com.isw2.liga.calculo.TablaDePosiciones;
import com.isw2.liga.calculo.TablaDePosiciones.EquipoTabla;
import com.isw2.liga.calculo.TablaDePosiciones.PartidoJugado;
import com.isw2.liga.dto.FilaTablaDTO;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.EstadoPartido;
import com.isw2.liga.model.Partido;
import com.isw2.liga.repository.EquipoRepository;
import com.isw2.liga.repository.PartidoRepository;
import com.isw2.liga.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/** RF-11: tabla de posiciones calculada con los partidos FINALIZADOS del torneo. */
@Service
@Transactional(readOnly = true)
public class TablaService {

    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;
    private final PartidoRepository partidoRepository;
    private final ResultadoService resultadoService;

    public TablaService(TorneoRepository torneoRepository, EquipoRepository equipoRepository,
                        PartidoRepository partidoRepository, ResultadoService resultadoService) {
        this.torneoRepository = torneoRepository;
        this.equipoRepository = equipoRepository;
        this.partidoRepository = partidoRepository;
        this.resultadoService = resultadoService;
    }

    public List<FilaTablaDTO> tabla(Integer torneoId) {
        if (!torneoRepository.existsById(torneoId)) {
            throw ApiException.noEncontrado("No existe el torneo " + torneoId);
        }
        List<Partido> finalizados = partidoRepository.findByTorneoIdAndEstado(torneoId, EstadoPartido.FINALIZADO);
        Map<Integer, Resultado> resultados = resultadoService.resultadosDe(finalizados);

        List<PartidoJugado> jugados = finalizados.stream()
                .map(p -> new PartidoJugado(p.getEquipoLocal().getId(), p.getEquipoVisitante().getId(),
                        resultados.get(p.getId())))
                .toList();
        Set<Integer> jugaron = new HashSet<>();
        for (PartidoJugado p : jugados) {
            jugaron.add(p.localId());
            jugaron.add(p.visitanteId());
        }

        // Un equipo dado de baja sigue en la tabla si llegó a jugar: sus partidos ya cuentan para los demás
        List<EquipoTabla> equipos = equipoRepository.findByTorneoIdOrderByNombreAsc(torneoId).stream()
                .filter(e -> e.isActivo() || jugaron.contains(e.getId()))
                .map(e -> new EquipoTabla(e.getId(), e.getNombre()))
                .toList();

        List<TablaDePosiciones.Fila> filas = TablaDePosiciones.calcular(equipos, jugados);
        return IntStream.range(0, filas.size())
                .mapToObj(i -> FilaTablaDTO.desde(i + 1, filas.get(i)))
                .toList();
    }
}
