package com.isw2.liga.repository;

import com.isw2.liga.model.EstadoPartido;
import com.isw2.liga.model.Partido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartidoRepository extends JpaRepository<Partido, Integer> {

    /** Fixture con equipos, cancha y árbitro en la misma consulta (RF-10). */
    @EntityGraph(attributePaths = {"equipoLocal", "equipoVisitante", "cancha", "arbitro"})
    List<Partido> findByTorneoIdAndNumeroFechaOrderByFechaHoraInicioAsc(Integer torneoId, int numeroFecha);

    @EntityGraph(attributePaths = {"equipoLocal", "equipoVisitante", "cancha", "arbitro"})
    List<Partido> findByTorneoIdOrderByNumeroFechaAscFechaHoraInicioAsc(Integer torneoId);

    List<Partido> findByTorneoIdAndEstado(Integer torneoId, EstadoPartido estado);

    /** El partido con equipos, cancha y árbitro en una sola consulta (para el detalle). */
    @EntityGraph(attributePaths = {"equipoLocal", "equipoVisitante", "cancha", "arbitro"})
    Optional<Partido> findConDetalleById(Integer id);
}
