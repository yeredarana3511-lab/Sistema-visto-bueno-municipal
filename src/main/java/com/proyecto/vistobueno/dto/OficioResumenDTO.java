package com.proyecto.vistobueno.dto;

import com.proyecto.vistobueno.entities.Oficio;

public class OficioResumenDTO {

    private Oficio oficio;
    private String folioSolicitud;
    private long diasHabiles;
    private String colorSemaforo;
    private String badgeClass;

    public OficioResumenDTO() {
    }

    public OficioResumenDTO(Oficio oficio, String folioSolicitud, long diasHabiles, String colorSemaforo, String badgeClass) {
        this.oficio = oficio;
        this.folioSolicitud = folioSolicitud;
        this.diasHabiles = diasHabiles;
        this.colorSemaforo = colorSemaforo;
        this.badgeClass = badgeClass;
    }

    public Oficio getOficio() {
        return oficio;
    }

    public void setOficio(Oficio oficio) {
        this.oficio = oficio;
    }

    public String getFolioSolicitud() {
        return folioSolicitud;
    }

    public void setFolioSolicitud(String folioSolicitud) {
        this.folioSolicitud = folioSolicitud;
    }

    public long getDiasHabiles() {
        return diasHabiles;
    }

    public void setDiasHabiles(long diasHabiles) {
        this.diasHabiles = diasHabiles;
    }

    public String getColorSemaforo() {
        return colorSemaforo;
    }

    public void setColorSemaforo(String colorSemaforo) {
        this.colorSemaforo = colorSemaforo;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }
}
