package com.isw2.liga.repository;

import com.isw2.liga.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Integer> {

    List<Jugador> findByEquipoIdOrderByApellidoAscNombreAsc(Integer equipoId);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Integer id);
}
