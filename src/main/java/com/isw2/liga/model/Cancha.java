package com.isw2.liga.model;

import jakarta.persistence.*;

/** Precargada por data.sql, sin CRUD. */
@Entity
@Table(name = "cancha")
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 120)
    private String direccion;

    @Column(nullable = false)
    private boolean activo = true;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public boolean isActivo() { return activo; }
}
