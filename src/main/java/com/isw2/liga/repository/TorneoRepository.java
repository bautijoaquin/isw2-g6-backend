package com.isw2.liga.repository;

import com.isw2.liga.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TorneoRepository extends JpaRepository<Torneo, Integer> {

    List<Torneo> findByActivoTrueOrderByIdAsc();
}
