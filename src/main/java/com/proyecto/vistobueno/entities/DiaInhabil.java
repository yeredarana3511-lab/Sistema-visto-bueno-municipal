package com.proyecto.vistobueno.entities;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "dias_inhabiles")
public class DiaInhabil {

    @Id
    @Column(name = "fecha")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    public DiaInhabil() {}

    public DiaInhabil(LocalDate fecha, String descripcion) {
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // Métodos de compatibilidad para plantillas y vistas
    public LocalDate getId() { return fecha; }
    public String getMotivo() { return descripcion; }
    public void setMotivo(String motivo) { this.descripcion = motivo; }
}
