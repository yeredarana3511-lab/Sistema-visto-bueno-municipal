package com.proyecto.vistobueno.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "oficios")
public class Oficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folio_oficio", unique = true, length = 100)
    private String folioOficio;

    // Relación con tabla Dependencia
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dependencia_id")
    private Dependencia dependencia;

    @Lob
    @Column(name = "contenido_doc", columnDefinition = "LONGTEXT")
    private String contenidoDoc;

    // Mapeamos el ID directamente
    @Column(name = "usuario_creador_id")
    private Long usuarioCreadorId;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "gmail_message_id", length = 100)
    private String gmailMessageId;

    @Column(name = "email_destino", length = 150)
    private String emailDestino;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "ruta_pdf_enviado", length = 500)
    private String rutaPdfEnviado;

    @Column(name = "numero_respuesta", length = 100)
    private String numeroRespuesta;

    @Lob
    @Column(name = "observaciones_respuesta", columnDefinition = "TEXT")
    private String observacionesRespuesta;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(name = "ruta_pdf_respuesta", length = 500)
    private String rutaPdfRespuesta;

    @Column(name = "fecha_inicio_conteo")
    private LocalDate fechaInicioConteo;

    @Column(name = "estado_cierre")
    private Boolean estadoCierre = false;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    public Oficio() {
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolioOficio() {
        return folioOficio;
    }

    public void setFolioOficio(String folioOficio) {
        this.folioOficio = folioOficio;
    }

    public Dependencia getDependencia() {
        return dependencia;
    }

    public void setDependencia(Dependencia dependencia) {
        this.dependencia = dependencia;
    }

    public String getContenidoDoc() {
        return contenidoDoc;
    }

    public void setContenidoDoc(String contenidoDoc) {
        this.contenidoDoc = contenidoDoc;
    }

    public Long getUsuarioCreadorId() {
        return usuarioCreadorId;
    }

    public void setUsuarioCreadorId(Long usuarioCreadorId) {
        this.usuarioCreadorId = usuarioCreadorId;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getGmailMessageId() {
        return gmailMessageId;
    }

    public void setGmailMessageId(String gmailMessageId) {
        this.gmailMessageId = gmailMessageId;
    }

    public String getEmailDestino() {
        return emailDestino;
    }

    public void setEmailDestino(String emailDestino) {
        this.emailDestino = emailDestino;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getRutaPdfEnviado() {
        return rutaPdfEnviado;
    }

    public void setRutaPdfEnviado(String rutaPdfEnviado) {
        this.rutaPdfEnviado = rutaPdfEnviado;
    }

    public String getNumeroRespuesta() {
        return numeroRespuesta;
    }

    public void setNumeroRespuesta(String numeroRespuesta) {
        this.numeroRespuesta = numeroRespuesta;
    }

    public String getObservacionesRespuesta() {
        return observacionesRespuesta;
    }

    public void setObservacionesRespuesta(String observacionesRespuesta) {
        this.observacionesRespuesta = observacionesRespuesta;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public String getRutaPdfRespuesta() {
        return rutaPdfRespuesta;
    }

    public void setRutaPdfRespuesta(String rutaPdfRespuesta) {
        this.rutaPdfRespuesta = rutaPdfRespuesta;
    }

    public LocalDate getFechaInicioConteo() {
        return fechaInicioConteo;
    }

    public void setFechaInicioConteo(LocalDate fechaInicioConteo) {
        this.fechaInicioConteo = fechaInicioConteo;
    }

    public Boolean getEstadoCierre() {
        return estadoCierre;
    }

    public void setEstadoCierre(Boolean estadoCierre) {
        this.estadoCierre = estadoCierre;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
}
