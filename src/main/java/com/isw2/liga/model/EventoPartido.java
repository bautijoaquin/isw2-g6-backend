package com.isw2.liga.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evento_partido")
public class EventoPartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvento tipo;

    @Column(nullable = false)
    private int minuto;

    public Integer getId() { return id; }
    public Partido getPartido() { return partido; }
    public void setPartido(Partido partido) { this.partido = partido; }
    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }
    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }
    public int getMinuto() { return minuto; }
    public void setMinuto(int minuto) { this.minuto = minuto; }
}
