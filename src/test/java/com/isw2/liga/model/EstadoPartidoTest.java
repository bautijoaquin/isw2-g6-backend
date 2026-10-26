package com.isw2.liga.model;

import org.junit.jupiter.api.Test;

import static com.isw2.liga.model.EstadoPartido.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RN-05: qué cambios de estado se permiten. Test sin Spring. */
class EstadoPartidoTest {

    @Test
    void elCaminoNormalEsProgramadoEnJuegoFinalizado() {
        assertTrue(PROGRAMADO.puedePasarA(EN_JUEGO));
        assertTrue(EN_JUEGO.puedePasarA(FINALIZADO));
    }

    @Test
    void seSuspendeAntesODuranteElPartido() {
        assertTrue(PROGRAMADO.puedePasarA(SUSPENDIDO));
        assertTrue(EN_JUEGO.puedePasarA(SUSPENDIDO));
    }

    @Test
    void noSeSaltaNiSeVuelveAtras() {
        assertFalse(PROGRAMADO.puedePasarA(FINALIZADO));
        assertFalse(EN_JUEGO.puedePasarA(PROGRAMADO));
        assertFalse(EN_JUEGO.puedePasarA(EN_JUEGO));
    }

    @Test
    void finalizadoYSuspendidoSonFinales() {
        for (EstadoPartido nuevo : EstadoPartido.values()) {
            assertFalse(FINALIZADO.puedePasarA(nuevo));
            assertFalse(SUSPENDIDO.puedePasarA(nuevo));
        }
    }
}
