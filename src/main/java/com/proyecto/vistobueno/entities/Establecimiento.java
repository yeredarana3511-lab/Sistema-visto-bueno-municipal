package com.proyecto.vistobueno.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "establecimientos")
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folio_contribuyente", nullable = false, unique = true, length = 50)
    private String folioContribuyente;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "giro", nullable = false, length = 150)
    private String giro;

    @Column(name = "direccion", columnDefinition = "TEXT", nullable = false)
    private String direccion;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }

    public Establecimiento() {}

    public Establecimiento(String folioContribuyente, String razonSocial, String giro, String direccion) {
        this.folioContribuyente = folioContribuyente;
        this.razonSocial = razonSocial;
        this.giro = giro;
        this.direccion = direccion;
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFolioContribuyente() { return folioContribuyente; }
    public void setFolioContribuyente(String folioContribuyente) { this.folioContribuyente = folioContribuyente; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getGiro() { return giro; }
    public void setGiro(String giro) { this.giro = giro; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
