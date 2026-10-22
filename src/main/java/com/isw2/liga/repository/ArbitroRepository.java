package com.isw2.liga.repository;

import com.isw2.liga.model.Arbitro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArbitroRepository extends JpaRepository<Arbitro, Integer> {

    List<Arbitro> findByActivoTrueOrderByApellidoAscNombreAsc();
}
