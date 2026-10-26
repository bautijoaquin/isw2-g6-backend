package com.isw2.liga.repository;

import com.isw2.liga.model.EstadoPartido;
import com.isw2.liga.model.Partido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PartidoRepository extends JpaRepository<Partido, Integer> {

    /** Fixture con equipos, cancha y árbitro en la misma consulta (RF-10). */
    @EntityGraph(attributePaths = {"equipoLocal", "equipoVisitante", "cancha", "arbitro"})
    List<Partido> findByTorneoIdAndNumeroFechaOrderByFechaHoraInicioAsc(Integer torneoId, int numeroFecha);

    @EntityGraph(attributePaths = {"equipoLocal", "equipoVisitante", "cancha", "arbitro"})
    List<Partido> findByTorneoIdOrderByNumeroFechaAscFechaHoraInicioAsc(Integer torneoId);

    List<Partido> findByTorneoIdAndEstado(Integer torneoId, EstadoPartido estado);

    // Consultas de RN-01 y RN-02. Los partidos SUSPENDIDOS no ocupan cancha, árbitro ni fecha.
    // excluirId deja afuera al propio partido cuando se modifica (al programar uno nuevo se pasa 0).

    /** RN-01: otro partido en la misma cancha que empieza entre desde y hasta (sin incluir los bordes). */
    @Query("""
            select count(p) > 0 from Partido p
            where p.cancha.id = :canchaId and p.id <> :excluirId
              and p.estado <> com.isw2.liga.model.EstadoPartido.SUSPENDIDO
              and p.fechaHoraInicio > :desde and p.fechaHoraInicio < :hasta""")
    boolean canchaOcupada(@Param("canchaId") Integer canchaId, @Param("desde") LocalDateTime desde,
                          @Param("hasta") LocalDateTime hasta, @Param("excluirId") Integer excluirId);

    /** RN-02: el árbitro ya dirige otro partido que empieza entre desde y hasta. */
    @Query("""
            select count(p) > 0 from Partido p
            where p.arbitro.id = :arbitroId and p.id <> :excluirId
              and p.estado <> com.isw2.liga.model.EstadoPartido.SUSPENDIDO
              and p.fechaHoraInicio > :desde and p.fechaHoraInicio < :hasta""")
    boolean arbitroOcupado(@Param("arbitroId") Integer arbitroId, @Param("desde") LocalDateTime desde,
                           @Param("hasta") LocalDateTime hasta, @Param("excluirId") Integer excluirId);

    /** RN-02: el equipo ya juega otro partido en esa fecha del torneo. */
    @Query("""
            select count(p) > 0 from Partido p
            where p.torneo.id = :torneoId and p.numeroFecha = :fecha and p.id <> :excluirId
              and p.estado <> com.isw2.liga.model.EstadoPartido.SUSPENDIDO
              and (p.equipoLocal.id = :equipoId or p.equipoVisitante.id = :equipoId)""")
    boolean equipoJuegaEnLaFecha(@Param("torneoId") Integer torneoId, @Param("fecha") int fecha,
                                 @Param("equipoId") Integer equipoId, @Param("excluirId") Integer excluirId);

    /** El partido con equipos, cancha y árbitro en una sola consulta (para el detalle). */
    @EntityGraph(attributePaths = {"equipoLocal", "equipoVisitante", "cancha", "arbitro"})
    Optional<Partido> findConDetalleById(Integer id);
}
