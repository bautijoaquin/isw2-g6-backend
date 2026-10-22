package com.isw2.liga.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jugador")
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Un jugador no cambia de equipo durante el torneo (RF-04)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false, updatable = false)
    private Equipo equipo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @Column(nullable = false, unique = true, length = 10)
    private String dni;

    @Column(name = "numero_camiseta")
    private Integer numeroCamiseta;

    @Column(nullable = false)
    private boolean activo = true;

    public Integer getId() { return id; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public Integer getNumeroCamiseta() { return numeroCamiseta; }
    public void setNumeroCamiseta(Integer numeroCamiseta) { this.numeroCamiseta = numeroCamiseta; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
