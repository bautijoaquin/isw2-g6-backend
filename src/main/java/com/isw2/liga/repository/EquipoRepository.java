package com.isw2.liga.repository;

import com.isw2.liga.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Integer> {

    List<Equipo> findByTorneoIdOrderByNombreAsc(Integer torneoId);

    boolean existsByTorneoIdAndNombreIgnoreCase(Integer torneoId, String nombre);

    boolean existsByTorneoIdAndNombreIgnoreCaseAndIdNot(Integer torneoId, String nombre, Integer id);
}
