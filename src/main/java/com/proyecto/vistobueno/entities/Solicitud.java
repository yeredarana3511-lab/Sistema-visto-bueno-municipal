package com.proyecto.vistobueno.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @Column(name = "folio_solicitud", length = 50, nullable = false)
    private String folioSolicitud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @Column(name = "ruta_imagen", length = 500)
    private String rutaImagen;

    @Lob
    @Column(name = "datos_ocr_json", columnDefinition = "LONGTEXT")
    private String datosOcrJson;

    @Column(name = "porcentaje_confiabilidad", precision = 5, scale = 2)
    private BigDecimal porcentajeConfiabilidad = BigDecimal.ZERO;

    @Column(name = "ocr_confirmado", nullable = false)
    private Boolean ocrConfirmado = false;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @Column(name = "usuario_captura_id")
    private Long usuarioCapturaId;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
        if (this.porcentajeConfiabilidad == null) {
            this.porcentajeConfiabilidad = BigDecimal.ZERO;
        }
        if (this.ocrConfirmado == null) {
            this.ocrConfirmado = false;
        }
        if (this.estado == null) {
            this.estado = "PENDIENTE";
        }
    }

    public Solicitud() {}

    public Solicitud(String folioSolicitud, Establecimiento establecimiento, String rutaImagen,
                     String datosOcrJson, BigDecimal porcentajeConfiabilidad, Long usuarioCapturaId) {
        this.folioSolicitud = folioSolicitud;
        this.establecimiento = establecimiento;
        this.rutaImagen = rutaImagen;
        this.datosOcrJson = datosOcrJson;
        this.porcentajeConfiabilidad = porcentajeConfiabilidad;
        this.ocrConfirmado = false;
        this.estado = "PENDIENTE";
        this.usuarioCapturaId = usuarioCapturaId;
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public String getFolioSolicitud() { return folioSolicitud; }
    public void setFolioSolicitud(String folioSolicitud) { this.folioSolicitud = folioSolicitud; }

    public Establecimiento getEstablecimiento() { return establecimiento; }
    public void setEstablecimiento(Establecimiento establecimiento) { this.establecimiento = establecimiento; }

    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }

    public String getDatosOcrJson() { return datosOcrJson; }
    public void setDatosOcrJson(String datosOcrJson) { this.datosOcrJson = datosOcrJson; }

    public BigDecimal getPorcentajeConfiabilidad() { return porcentajeConfiabilidad; }
    public void setPorcentajeConfiabilidad(BigDecimal porcentajeConfiabilidad) { this.porcentajeConfiabilidad = porcentajeConfiabilidad; }

    public Boolean getOcrConfirmado() { return ocrConfirmado; }
    public void setOcrConfirmado(Boolean ocrConfirmado) { this.ocrConfirmado = ocrConfirmado; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getUsuarioCapturaId() { return usuarioCapturaId; }
    public void setUsuarioCapturaId(Long usuarioCapturaId) { this.usuarioCapturaId = usuarioCapturaId; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
