package com.proyecto.vistobueno.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "solicitud_oficio")
@IdClass(SolicitudOficioId.class)
public class SolicitudOficio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "folio_solicitud", length = 50, nullable = false)
    private String folioSolicitud;

    @Id
    @Column(name = "oficio_id", nullable = false)
    private Long oficioId;

    public SolicitudOficio() {
    }

    public SolicitudOficio(String folioSolicitud, Long oficioId) {
        this.folioSolicitud = folioSolicitud;
        this.oficioId = oficioId;
    }

    public String getFolioSolicitud() {
        return folioSolicitud;
    }

    public void setFolioSolicitud(String folioSolicitud) {
        this.folioSolicitud = folioSolicitud;
    }

    public Long getOficioId() {
        return oficioId;
    }

    public void setOficioId(Long oficioId) {
        this.oficioId = oficioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolicitudOficio that = (SolicitudOficio) o;
        return Objects.equals(folioSolicitud, that.folioSolicitud) &&
               Objects.equals(oficioId, that.oficioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folioSolicitud, oficioId);
    }
}
