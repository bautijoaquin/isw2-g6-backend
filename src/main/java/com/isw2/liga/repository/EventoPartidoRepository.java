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

    /** Goles de varios partidos en una sola consulta, para calcular resultados en lote. */
    @Query("""
            select e from EventoPartido e join fetch e.jugador j
            where e.partido in :partidos and e.tipo = com.isw2.liga.model.TipoEvento.GOL""")
    List<EventoPartido> findGolesDe(@Param("partidos") Collection<Partido> partidos);
}
