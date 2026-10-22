package com.isw2.liga.repository;

import com.isw2.liga.model.EstadoPartido;
import com.isw2.liga.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Integer> {

    List<Partido> findByTorneoIdAndNumeroFechaOrderByFechaHoraInicioAsc(Integer torneoId, int numeroFecha);

    List<Partido> findByTorneoIdOrderByNumeroFechaAscFechaHoraInicioAsc(Integer torneoId);

    List<Partido> findByTorneoIdAndEstado(Integer torneoId, EstadoPartido estado);
}
