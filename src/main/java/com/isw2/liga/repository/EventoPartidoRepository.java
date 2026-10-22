package com.isw2.liga.repository;

import com.isw2.liga.model.EventoPartido;
import com.isw2.liga.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EventoPartidoRepository extends JpaRepository<EventoPartido, Integer> {

    List<EventoPartido> findByPartidoIdOrderByMinutoAscIdAsc(Integer partidoId);

    /** Eventos de un partido en orden, con el jugador en la misma consulta (para el detalle). */
    @Query("""
            select e from EventoPartido e join fetch e.jugador j
            where e.partido.id = :partidoId order by e.minuto, e.id""")
    List<EventoPartido> findEventosDePartido(@Param("partidoId") Integer partidoId);

    /** Goles de varios partidos en una sola consulta, para calcular resultados en lote. */
    @Query("""
            select e from EventoPartido e join fetch e.jugador j
            where e.partido in :partidos and e.tipo = com.isw2.liga.model.TipoEvento.GOL""")
    List<EventoPartido> findGolesDe(@Param("partidos") Collection<Partido> partidos);

    /** Amarillas y rojas de los partidos FINALIZADOS de una fecha (RN-03). */
    @Query("""
            select e from EventoPartido e join fetch e.jugador j join fetch j.equipo
            where e.partido.torneo.id = :torneoId and e.partido.numeroFecha = :fecha
              and e.partido.estado = com.isw2.liga.model.EstadoPartido.FINALIZADO
              and e.tipo in (com.isw2.liga.model.TipoEvento.AMARILLA, com.isw2.liga.model.TipoEvento.ROJA)
            order by e.id""")
    List<EventoPartido> findTarjetasDeFechaFinalizada(@Param("torneoId") Integer torneoId,
                                                      @Param("fecha") int fecha);
}
