package com.isw2.liga.repository;

import com.isw2.liga.model.EventoPartido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoPartidoRepository extends JpaRepository<EventoPartido, Integer> {

    List<EventoPartido> findByPartidoIdOrderByMinutoAscIdAsc(Integer partidoId);
}
