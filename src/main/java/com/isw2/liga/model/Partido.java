package com.isw2.liga.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "partido")
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @Column(name = "numero_fecha", nullable = false)
    private int numeroFecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_local_id", nullable = false)
    private Equipo equipoLocal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_visitante_id", nullable = false)
    private Equipo equipoVisitante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cancha_id", nullable = false)
    private Cancha cancha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arbitro_id", nullable = false)
    private Arbitro arbitro;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPartido estado = EstadoPartido.PROGRAMADO;

    public Integer getId() { return id; }
    public Torneo getTorneo() { return torneo; }
    public void setTorneo(Torneo torneo) { this.torneo = torneo; }
    public int getNumeroFecha() { return numeroFecha; }
    public void setNumeroFecha(int numeroFecha) { this.numeroFecha = numeroFecha; }
    public Equipo getEquipoLocal() { return equipoLocal; }
    public void setEquipoLocal(Equipo equipoLocal) { this.equipoLocal = equipoLocal; }
    public Equipo getEquipoVisitante() { return equipoVisitante; }
    public void setEquipoVisitante(Equipo equipoVisitante) { this.equipoVisitante = equipoVisitante; }
    public Cancha getCancha() { return cancha; }
    public void setCancha(Cancha cancha) { this.cancha = cancha; }
    public Arbitro getArbitro() { return arbitro; }
    public void setArbitro(Arbitro arbitro) { this.arbitro = arbitro; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public EstadoPartido getEstado() { return estado; }
    public void setEstado(EstadoPartido estado) { this.estado = estado; }
}
