package com.isw2.liga.model;

/** Etapas de un partido (sección 6 de los requerimientos). */
public enum EstadoPartido {
    PROGRAMADO,
    EN_JUEGO,
    FINALIZADO,
    SUSPENDIDO;

    /**
     * RN-05: PROGRAMADO → EN_JUEGO → FINALIZADO, y desde PROGRAMADO o EN_JUEGO se puede suspender.
     * FINALIZADO y SUSPENDIDO son finales.
     */
    public boolean puedePasarA(EstadoPartido nuevo) {
        return switch (this) {
            case PROGRAMADO -> nuevo == EN_JUEGO || nuevo == SUSPENDIDO;
            case EN_JUEGO -> nuevo == FINALIZADO || nuevo == SUSPENDIDO;
            case FINALIZADO, SUSPENDIDO -> false;
        };
    }
}
