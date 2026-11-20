package com.proyecto.vistobueno.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_empleado", nullable = false, unique = true)
    private String numeroEmpleado;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    public Usuario() {}

    public Usuario(String numeroEmpleado, String nombre) {
        this.numeroEmpleado = numeroEmpleado;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroEmpleado() { return numeroEmpleado; }
    public void setNumeroEmpleado(String numeroEmpleado) { this.numeroEmpleado = numeroEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
