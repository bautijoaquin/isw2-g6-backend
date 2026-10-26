package com.isw2.liga.dto;

import com.isw2.liga.calculo.Resultado;
import com.isw2.liga.model.EstadoPartido;
import com.isw2.liga.model.Partido;

import java.time.LocalDateTime;

/** Un partido del fixture con su resultado calculado (RF-10). El detalle con eventos es PartidoDetalleDTO. */
public record PartidoDTO(Integer id, Integer torneoId, int numeroFecha,
                         ResumenDTO local, ResumenDTO visitante,
                         int golesLocal, int golesVisitante,
                         ResumenDTO cancha, ResumenDTO arbitro,
                         LocalDateTime fechaHoraInicio, EstadoPartido estado) {

    public static PartidoDTO desde(Partido p, Resultado r) {
        return new PartidoDTO(p.getId(), p.getTorneo().getId(), p.getNumeroFecha(),
                new ResumenDTO(p.getEquipoLocal().getId(), p.getEquipoLocal().getNombre()),
                new ResumenDTO(p.getEquipoVisitante().getId(), p.getEquipoVisitante().getNombre()),
                r.golesLocal(), r.golesVisitante(),
                new ResumenDTO(p.getCancha().getId(), p.getCancha().getNombre()),
                new ResumenDTO(p.getArbitro().getId(), p.getArbitro().getNombre() + " " + p.getArbitro().getApellido()),
                p.getFechaHoraInicio(), p.getEstado());
    }
}
