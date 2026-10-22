package com.isw2.liga.dto;

import com.isw2.liga.calculo.TablaDePosiciones;

public record FilaTablaDTO(int posicion, Integer equipoId, String equipo,
                           int pj, int pg, int pe, int pp, int gf, int gc, int dg, int pts) {

    public static FilaTablaDTO desde(int posicion, TablaDePosiciones.Fila f) {
        return new FilaTablaDTO(posicion, f.equipoId(), f.equipo(),
                f.pj(), f.pg(), f.pe(), f.pp(), f.gf(), f.gc(), f.dg(), f.pts());
    }
}
