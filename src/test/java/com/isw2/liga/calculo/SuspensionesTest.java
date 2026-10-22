package com.isw2.liga.calculo;

import com.isw2.liga.calculo.Suspensiones.Motivo;
import com.isw2.liga.calculo.Suspensiones.Tarjeta;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.isw2.liga.model.TipoEvento.AMARILLA;
import static com.isw2.liga.model.TipoEvento.GOL;
import static com.isw2.liga.model.TipoEvento.ROJA;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SuspensionesTest {

    @Test
    void unaRojaSuspende() {
        assertEquals(Map.of(7, Motivo.ROJA), Suspensiones.sancionados(List.of(new Tarjeta(1, 7, ROJA))));
    }

    @Test
    void dosAmarillasEnElMismoPartidoSuspenden() {
        var tarjetas = List.of(new Tarjeta(1, 7, AMARILLA), new Tarjeta(1, 7, AMARILLA));
        assertEquals(Map.of(7, Motivo.DOBLE_AMARILLA), Suspensiones.sancionados(tarjetas));
    }

    @Test
    void unaSolaAmarillaNoSuspende() {
        var tarjetas = List.of(new Tarjeta(1, 7, AMARILLA), new Tarjeta(1, 8, AMARILLA), new Tarjeta(1, 7, GOL));
        assertEquals(Map.of(), Suspensiones.sancionados(tarjetas));
    }

    @Test
    void lasAmarillasDePartidosDistintosNoSeAcumulan() {
        var tarjetas = List.of(new Tarjeta(1, 7, AMARILLA), new Tarjeta(2, 7, AMARILLA));
        assertEquals(Map.of(), Suspensiones.sancionados(tarjetas));
    }

    @Test
    void siTieneRojaYDobleAmarillaElMotivoEsRoja() {
        var amarillasPrimero = List.of(new Tarjeta(1, 7, AMARILLA), new Tarjeta(1, 7, AMARILLA), new Tarjeta(1, 7, ROJA));
        var rojaPrimero = List.of(new Tarjeta(1, 7, ROJA), new Tarjeta(1, 7, AMARILLA), new Tarjeta(1, 7, AMARILLA));
        assertEquals(Map.of(7, Motivo.ROJA), Suspensiones.sancionados(amarillasPrimero));
        assertEquals(Map.of(7, Motivo.ROJA), Suspensiones.sancionados(rojaPrimero));
    }
}
