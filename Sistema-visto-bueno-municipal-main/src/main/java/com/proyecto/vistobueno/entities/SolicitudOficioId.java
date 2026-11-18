package com.proyecto.vistobueno.entities;

import java.io.Serializable;
import java.util.Objects;

public class SolicitudOficioId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String folioSolicitud;
    private Long oficioId;

    public SolicitudOficioId() {
    }

    public SolicitudOficioId(String folioSolicitud, Long oficioId) {
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
        SolicitudOficioId that = (SolicitudOficioId) o;
        return Objects.equals(folioSolicitud, that.folioSolicitud) &&
               Objects.equals(oficioId, that.oficioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folioSolicitud, oficioId);
    }
}
