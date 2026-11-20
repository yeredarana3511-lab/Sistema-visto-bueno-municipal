package com.proyecto.vistobueno.dto;

import java.math.BigDecimal;

public class OcrResultDTO {

    private String nombreArchivo;
    private String rutaImagen;
    private String folioSolicitud;
    private String folioContribuyente;
    private String razonSocial;
    private String giro;
    private String direccion;
    private String propietario;
    private BigDecimal porcentajeConfiabilidad;
    private String textoExtraido;
    private boolean requiereEcologia = true;
    private boolean requiereReglamentos = true;
    private boolean requiereProteccionCivil = true;

    public OcrResultDTO() {}

    // Getters y Setters
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }

    public String getFolioSolicitud() { return folioSolicitud; }
    public void setFolioSolicitud(String folioSolicitud) { this.folioSolicitud = folioSolicitud; }

    public String getFolioContribuyente() { return folioContribuyente; }
    public void setFolioContribuyente(String folioContribuyente) { this.folioContribuyente = folioContribuyente; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getGiro() { return giro; }
    public void setGiro(String giro) { this.giro = giro; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }

    public BigDecimal getPorcentajeConfiabilidad() { return porcentajeConfiabilidad; }
    public void setPorcentajeConfiabilidad(BigDecimal porcentajeConfiabilidad) { this.porcentajeConfiabilidad = porcentajeConfiabilidad; }

    public String getTextoExtraido() { return textoExtraido; }
    public void setTextoExtraido(String textoExtraido) { this.textoExtraido = textoExtraido; }

    public boolean isRequiereEcologia() { return requiereEcologia; }
    public void setRequiereEcologia(boolean requiereEcologia) { this.requiereEcologia = requiereEcologia; }

    public boolean isRequiereReglamentos() { return requiereReglamentos; }
    public void setRequiereReglamentos(boolean requiereReglamentos) { this.requiereReglamentos = requiereReglamentos; }

    public boolean isRequiereProteccionCivil() { return requiereProteccionCivil; }
    public void setRequiereProteccionCivil(boolean requiereProteccionCivil) { this.requiereProteccionCivil = requiereProteccionCivil; }
}
