package com.proyecto.vistobueno.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "dependencias")
public class Dependencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String nombre;

    @Column(name = "correo_contacto", length = 150)
    private String correoContacto;

    @Column(name = "prefijo_folio", length = 20)
    private String prefijoFolio;

    @Column(name = "secuencia_actual")
    private Integer secuenciaActual = 0;

    @Lob
    @Column(name = "plantilla_html", columnDefinition = "LONGTEXT")
    private String plantillaHtml;

    public Dependencia() {
    }

    public Dependencia(String nombre) {
        this.nombre = nombre;
    }

    public Dependencia(String nombre, String correoContacto, String prefijoFolio) {
        this.nombre = nombre;
        this.correoContacto = correoContacto;
        this.prefijoFolio = prefijoFolio;
        this.secuenciaActual = 0;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
    }

    public String getPrefijoFolio() {
        return prefijoFolio;
    }

    public void setPrefijoFolio(String prefijoFolio) {
        this.prefijoFolio = prefijoFolio;
    }

    public Integer getSecuenciaActual() {
        return secuenciaActual;
    }

    public void setSecuenciaActual(Integer secuenciaActual) {
        this.secuenciaActual = secuenciaActual;
    }

    public String getPlantillaHtml() {
        return plantillaHtml;
    }

    public void setPlantillaHtml(String plantillaHtml) {
        this.plantillaHtml = plantillaHtml;
    }
}
