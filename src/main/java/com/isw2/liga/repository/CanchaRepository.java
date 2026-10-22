package com.isw2.liga.repository;

import com.isw2.liga.model.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CanchaRepository extends JpaRepository<Cancha, Integer> {

    List<Cancha> findByActivoTrueOrderByNombreAsc();
}
