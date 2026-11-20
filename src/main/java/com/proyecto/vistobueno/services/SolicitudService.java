package com.proyecto.vistobueno.services;

import com.proyecto.vistobueno.dto.OcrResultDTO;
import com.proyecto.vistobueno.entities.Dependencia;
import com.proyecto.vistobueno.entities.Establecimiento;
import com.proyecto.vistobueno.entities.Solicitud;
import com.proyecto.vistobueno.entities.Usuario;
import com.proyecto.vistobueno.repositories.DependenciaRepository;
import com.proyecto.vistobueno.repositories.EstablecimientoRepository;
import com.proyecto.vistobueno.repositories.SolicitudRepository;
import com.proyecto.vistobueno.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudService.class);

    private final SolicitudRepository solicitudRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final DependenciaRepository dependenciaRepository;
    private final OficioService oficioService;
    private final OcrService ocrService;
    private final BitacoraAuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;

    public SolicitudService(SolicitudRepository solicitudRepository,
                            EstablecimientoRepository establecimientoRepository,
                            DependenciaRepository dependenciaRepository,
                            OficioService oficioService,
                            OcrService ocrService,
                            BitacoraAuditoriaService auditoriaService,
                            UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.dependenciaRepository = dependenciaRepository;
        this.oficioService = oficioService;
        this.ocrService = ocrService;
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Solicitud> listarTodas() {
        return solicitudRepository.findAllByOrderByFechaRegistroDesc();
    }

    public Optional<Solicitud> buscarPorFolio(String folio) {
        return solicitudRepository.findById(folio);
    }

    /**
     * Guarda la solicitud tras la REVISIÓN Y VALIDACIÓN HUMANA OBLIGATORIA de los datos de OCR.
     */
    @Transactional
    public Solicitud confirmarYGuardarSolicitud(OcrResultDTO dto, Long usuarioId, String ipOrigen) {
        // 1. Guardar o actualizar Establecimiento
        Establecimiento est = establecimientoRepository.findByFolioContribuyente(dto.getFolioContribuyente().trim())
                .orElseGet(() -> new Establecimiento(
                        dto.getFolioContribuyente().trim(),
                        dto.getRazonSocial().trim(),
                        dto.getGiro().trim(),
                        dto.getDireccion().trim()
                ));

        est.setRazonSocial(dto.getRazonSocial().trim());
        est.setGiro(dto.getGiro().trim());
        est.setDireccion(dto.getDireccion().trim());
        est = establecimientoRepository.save(est);

        // 2. Crear y guardar la Solicitud confirmada
        Solicitud solicitud = new Solicitud();
        solicitud.setFolioSolicitud(dto.getFolioSolicitud().trim());
        solicitud.setEstablecimiento(est);
        solicitud.setRutaImagen(dto.getRutaImagen());
        solicitud.setDatosOcrJson(ocrService.convertirOcrAJson(dto));
        solicitud.setPorcentajeConfiabilidad(dto.getPorcentajeConfiabilidad());
        solicitud.setOcrConfirmado(true); // Validación humana confirmada
        solicitud.setEstado("CONFIRMADO");
        solicitud.setUsuarioCapturaId(usuarioId);
        solicitud.setFechaRegistro(LocalDateTime.now());

        solicitud = solicitudRepository.save(solicitud);
        log.info("Solicitud {} guardada y validada humanamente con éxito.", solicitud.getFolioSolicitud());

        // 3. Generar oficios automáticos para las dependencias seleccionadas si están activas
        List<Dependencia> deps = dependenciaRepository.findAll();
        for (Dependencia dep : deps) {
            boolean requerida = false;
            String depName = dep.getNombre().toUpperCase();
            if (depName.contains("ECOLOG") && dto.isRequiereEcologia()) requerida = true;
            if (depName.contains("REGLA") && dto.isRequiereReglamentos()) requerida = true;
            if (depName.contains("PROTECCI") && dto.isRequiereProteccionCivil()) requerida = true;

            if (requerida) {
                try {
                    String contenido = (dep.getPlantillaHtml() != null && !dep.getPlantillaHtml().isBlank())
                            ? dep.getPlantillaHtml()
                            : "Oficio oficial de solicitud de Visto Bueno Municipal para el establecimiento: " + est.getRazonSocial();

                    // Reemplazo de etiquetas dinámicas
                    contenido = contenido.replace("{{RAZON_SOCIAL}}", est.getRazonSocial())
                                         .replace("{{GIRO}}", est.getGiro())
                                         .replace("{{DOMICILIO}}", est.getDireccion())
                                         .replace("{{FOLIO_SOLICITUD}}", solicitud.getFolioSolicitud())
                                         .replace("{{FECHA}}", LocalDate.now().toString());

                    oficioService.generarYGuardarOficio(
                            solicitud.getFolioSolicitud(),
                            dep.getId(),
                            contenido,
                            dep.getCorreoContacto(),
                            LocalDate.now()
                    );
                } catch (Exception e) {
                    log.error("No se pudo autogenerar oficio para {}: {}", dep.getNombre(), e.getMessage());
                }
            }
        }

        // 4. Registro en Bitácora de Auditoría
        Usuario usuario = (usuarioId != null) ? usuarioRepository.findById(usuarioId).orElse(null) : null;
        auditoriaService.registrar(
                usuario,
                "SOLICITUDES",
                "CONFIRMAR_OCR",
                "Se validó y confirmó la solicitud " + solicitud.getFolioSolicitud() + " para el contribuyente " + est.getFolioContribuyente(),
                ipOrigen
        );

        return solicitud;
    }

    /**
     * Acción del Supervisor: Dictaminar Visto Bueno (Aprobar / Rechazar) con notificación por correo opcional.
     */
    @Transactional
    public boolean dictaminarSolicitud(String folio, String decision, String observaciones, String correoDestino, Long usuarioSupervisorId, String ipOrigen) {
        Solicitud solicitud = solicitudRepository.findById(folio)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + folio));

        if ("APROBADO".equalsIgnoreCase(decision)) {
            solicitud.setEstado("EN_PROCESO");
        } else if ("RECHAZADO".equalsIgnoreCase(decision)) {
            solicitud.setEstado("CERRADO");
        }

        solicitudRepository.save(solicitud);

        String razonSocial = (solicitud.getEstablecimiento() != null) ? solicitud.getEstablecimiento().getRazonSocial() : "Establecimiento";

        boolean correoEnviado = false;
        if (correoDestino != null && !correoDestino.trim().isEmpty()) {
            correoEnviado = oficioService.enviarCorreoDictamen(correoDestino.trim(), folio, razonSocial, decision, observaciones);
        }

        Usuario supervisor = (usuarioSupervisorId != null) ? usuarioRepository.findById(usuarioSupervisorId).orElse(null) : null;
        String detalleAuditoria = "Dictamen emitido para solicitud " + folio + ": " + decision + ". Observaciones: " + observaciones
                + (correoDestino != null && !correoDestino.isBlank() ? " | Correo de notificación: " + correoDestino : "");

        auditoriaService.registrar(
                supervisor,
                "VISTO_BUENO",
                "DICTAMEN_" + decision.toUpperCase(),
                detalleAuditoria,
                ipOrigen
        );

        return correoEnviado;
    }
}
