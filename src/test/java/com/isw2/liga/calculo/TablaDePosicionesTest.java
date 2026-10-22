package com.isw2.liga.calculo;

import com.isw2.liga.calculo.TablaDePosiciones.EquipoTabla;
import com.isw2.liga.calculo.TablaDePosiciones.Fila;
import com.isw2.liga.calculo.TablaDePosiciones.PartidoJugado;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TablaDePosicionesTest {

    private static PartidoJugado partido(int local, int visitante, int gl, int gv) {
        return new PartidoJugado(local, visitante, new Resultado(gl, gv));
    }

    private static List<String> orden(List<Fila> filas) {
        return filas.stream().map(Fila::equipo).toList();
    }

    @Test
    void calculaTodasLasColumnasYLosPuntos() {
        var equipos = List.of(new EquipoTabla(1, "A"), new EquipoTabla(2, "B"), new EquipoTabla(3, "C"));
        var filas = TablaDePosiciones.calcular(equipos, List.of(partido(1, 2, 3, 1), partido(3, 1, 0, 0)));

        assertEquals(new Fila(1, "A", 2, 1, 1, 0, 3, 1), filas.get(0));
        assertEquals(4, filas.get(0).pts());
        assertEquals(2, filas.get(0).dg());
        assertEquals(new Fila(3, "C", 1, 0, 1, 0, 0, 0), filas.get(1));
        assertEquals(new Fila(2, "B", 1, 0, 0, 1, 1, 3), filas.get(2));
        assertEquals(0, filas.get(2).pts());
    }

    @Test
    void losEquiposSinPartidosAparecenConCeros() {
        var filas = TablaDePosiciones.calcular(List.of(new EquipoTabla(1, "A")), List.of());
        assertEquals(new Fila(1, "A", 0, 0, 0, 0, 0, 0), filas.get(0));
    }

    @Test
    void desempataPorDiferenciaDeGol() {
        // A y B ganan un partido cada uno; B con mejor diferencia
        var equipos = List.of(new EquipoTabla(1, "A"), new EquipoTabla(2, "B"),
                new EquipoTabla(3, "C"), new EquipoTabla(4, "D"));
        var filas = TablaDePosiciones.calcular(equipos, List.of(partido(1, 3, 1, 0), partido(2, 4, 3, 0)));
        assertEquals(List.of("B", "A", "C", "D"), orden(filas));
    }

    @Test
    void desempataPorGolesAFavor() {
        // A gana 3-2 y B gana 1-0: misma diferencia, A hizo más goles
        var equipos = List.of(new EquipoTabla(1, "B"), new EquipoTabla(2, "A"),
                new EquipoTabla(3, "C"), new EquipoTabla(4, "D"));
        var filas = TablaDePosiciones.calcular(equipos, List.of(partido(1, 3, 1, 0), partido(2, 4, 3, 2)));
        assertEquals(List.of("A", "B", "D", "C"), orden(filas));
    }

    @Test
    void desempataPorOrdenAlfabeticoIgnorandoTildesYMayusculas() {
        var equipos = List.of(new EquipoTabla(1, "Unión"), new EquipoTabla(2, "atlético"), new EquipoTabla(3, "Boca"));
        assertEquals(List.of("atlético", "Boca", "Unión"), orden(TablaDePosiciones.calcular(equipos, List.of())));
    }

    @Test
    void fallaSiUnPartidoTieneUnEquipoQueNoEstaEnLaTabla() {
        var equipos = List.of(new EquipoTabla(1, "A"));
        assertThrows(IllegalArgumentException.class,
                () -> TablaDePosiciones.calcular(equipos, List.of(partido(1, 9, 1, 0))));
    }
}
