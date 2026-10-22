package com.isw2.liga.model;

import jakarta.persistence.*;

/** Precargado por data.sql, sin CRUD. */
@Entity
@Table(name = "torneo")
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private boolean activo = true;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public int getAnio() { return anio; }
    public boolean isActivo() { return activo; }
}
