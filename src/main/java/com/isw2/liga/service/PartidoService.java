package com.isw2.liga.service;

import com.isw2.liga.calculo.Resultado;
import com.isw2.liga.dto.PartidoDTO;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.Partido;
import com.isw2.liga.repository.PartidoRepository;
import com.isw2.liga.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** RF-10: fixture del torneo, con el resultado de cada partido calculado con sus goles (RN-06). */
@Service
@Transactional
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final TorneoRepository torneoRepository;
    private final ResultadoService resultadoService;

    public PartidoService(PartidoRepository partidoRepository, TorneoRepository torneoRepository,
                          ResultadoService resultadoService) {
        this.partidoRepository = partidoRepository;
        this.torneoRepository = torneoRepository;
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
}
