package com.isw2.liga.dto;

import com.isw2.liga.model.EstadoPartido;

import java.time.LocalDateTime;
import java.util.List;

/** Detalle de un partido con su resultado calculado y sus eventos en orden (RF-10). */
public record PartidoDetalleDTO(Integer id, Integer torneoId, int numeroFecha,
                                ResumenDTO local, ResumenDTO visitante,
                                int golesLocal, int golesVisitante,
                                ResumenDTO cancha, ResumenDTO arbitro,
                                LocalDateTime fechaHoraInicio, EstadoPartido estado,
                                List<EventoDTO> eventos) {
}
