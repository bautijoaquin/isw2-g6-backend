package com.isw2.liga.calculo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultadoTest {

    @Test
    void sinGolesEsCeroACero() {
        assertEquals(new Resultado(0, 0), Resultado.calcular(1, 2, List.of()));
    }

    @Test
    void cadaGolSumaParaElEquipoDeSuAutor() {
        assertEquals(new Resultado(2, 1), Resultado.calcular(1, 2, List.of(1, 2, 1)));
    }
}
