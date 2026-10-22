package com.isw2.liga.model;

import jakarta.persistence.*;

/** Precargado por data.sql, sin CRUD. */
@Entity
@Table(name = "arbitro")
public class Arbitro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @Column(nullable = false, unique = true, length = 10)
    private String dni;

    @Column(nullable = false)
    private boolean activo = true;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public boolean isActivo() { return activo; }
}
