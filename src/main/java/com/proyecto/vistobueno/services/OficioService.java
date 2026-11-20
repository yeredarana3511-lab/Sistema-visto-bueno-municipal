package com.proyecto.vistobueno.services;

import com.proyecto.vistobueno.dto.OficioResumenDTO;
import com.proyecto.vistobueno.entities.Dependencia;
import com.proyecto.vistobueno.entities.Oficio;
import com.proyecto.vistobueno.entities.SolicitudOficio;
import com.proyecto.vistobueno.repositories.DependenciaRepository;
import com.proyecto.vistobueno.repositories.DiaInhabilRepository;
import com.proyecto.vistobueno.repositories.OficioRepository;
import com.proyecto.vistobueno.repositories.SolicitudOficioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OficioService {

    private static final Logger log = LoggerFactory.getLogger(OficioService.class);

    @Autowired
    private OficioRepository oficioRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private DependenciaRepository dependenciaRepository;

    @Autowired
    private SolicitudOficioRepository solicitudOficioRepository;

    @Autowired(required = false)
    private DiaInhabilRepository diaInhabilRepository;

    @Value("${spring.mail.username:yeredarana29@gmail.com}")
    private String correoRemitente;

    // --- MÉTODOS DE LÓGICA DE NEGOCIO ---

    /**
     * Calcula los días hábiles transcurridos entre dos fechas (omite sábados, domingos y días inhábiles oficiales).
     */
    public long calcularDiasHabiles(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null || fechaInicio.isAfter(fechaFin)) {
            return 0;
        }

        long diasHabiles = 0;
        LocalDate fechaIterador = fechaInicio;

        while (!fechaIterador.isAfter(fechaFin)) {
            DayOfWeek dia = fechaIterador.getDayOfWeek();
            boolean esFinDeSemana = (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY);
            boolean esDiaInhabilRegistrado = (diaInhabilRepository != null && diaInhabilRepository.existsById(fechaIterador));

            if (!esFinDeSemana && !esDiaInhabilRegistrado) {
                diasHabiles++;
            }
            fechaIterador = fechaIterador.plusDays(1);
        }

        return diasHabiles;
    }

    /**
     * Determina el color del semáforo basado en los días hábiles transcurridos:
     * - Verde: 1-20 días
     * - Amarillo: 21-29 días
     * - Rojo: 30-37 días
     * - Morado: 38+ días
     */
    public String determinarColorSemaforo(long diasHabiles) {
        if (diasHabiles >= 1 && diasHabiles <= 20) {
            return "VERDE";
        } else if (diasHabiles >= 21 && diasHabiles <= 29) {
            return "AMARILLO";
        } else if (diasHabiles >= 30 && diasHabiles <= 37) {
            return "ROJO";
        } else if (diasHabiles >= 38) {
            return "MORADO";
        } else {
            return "SIN_ESTADO";
        }
    }

    /**
     * Retorna la clase CSS correspondiente para el color de semáforo.
     */
    public String obtenerClaseCssSemaforo(String color) {
        if (color == null) return "bg-secondary text-white";
        return switch (color.toUpperCase()) {
            case "VERDE" -> "bg-success text-white";
            case "AMARILLO" -> "bg-warning text-dark";
            case "ROJO" -> "bg-danger text-white";
            case "MORADO" -> "semaforo-morado text-white";
            default -> "bg-secondary text-white";
        };
    }

    /**
     * Envía un correo electrónico profesional con formato institucional del Gobierno de Tonalá
     * (Integración Gmail / JavaMailSender en formato HTML).
     */
    public boolean enviarCorreoOficio(String destinatario, String folioOficio, String contenidoDocumento) {
        try {
            if (mailSender != null) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                if (mimeMessage != null) {
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    helper.setTo(destinatario);
                    helper.setSubject("H. Ayuntamiento de Tonalá - Oficio de Visto Bueno: " + folioOficio);
                    helper.setFrom(correoRemitente, "Gobierno de Tonalá - Visto Bueno");

                    String htmlBody = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 650px; margin: 0 auto; border: 2px solid #822828; border-radius: 6px; overflow: hidden; background: #ffffff;\">"
                            + "<div style=\"background-color: #ffffff; text-align: center; padding: 14px 20px; border-bottom: 2px solid #c59b27;\">"
                            + "<img src=\"cid:logoTonala\" alt=\"Gobierno de Tonalá - Ciudad de la Transformación\" style=\"max-height: 60px; width: auto;\" />"
                            + "</div>"
                            + "<div style=\"background-color: #822828; color: #ffffff; padding: 16px 24px; text-align: center;\">"
                            + "<h2 style=\"margin: 0; font-size: 20px; letter-spacing: 1px;\">GOBIERNO DE TONALÁ, JALISCO</h2>"
                            + "<p style=\"margin: 4px 0 0; font-size: 13px; color: #f8fafc;\">COORDINACIÓN DE PADRÓN, LICENCIAS Y VISTO BUENO MUNICIPAL</p>"
                            + "</div>"
                            + "<div style=\"padding: 24px; color: #212529; line-height: 1.6;\">"
                            + "<div style=\"background: #f8f9fa; border-left: 4px solid #822828; padding: 10px 14px; margin-bottom: 18px;\">"
                            + "<strong style=\"color: #822828;\">OFICIO OFICIAL:</strong> <span>" + folioOficio + "</span>"
                            + "</div>"
                            + "<div style=\"background: #ffffff; border: 1px solid #dee2e6; border-radius: 4px; padding: 16px; margin: 15px 0;\">"
                            + contenidoDocumento
                            + "</div>"
                            + "<div style=\"background-color: #f8f9fa; border-left: 4px solid #c59b27; padding: 10px; margin-top: 20px; font-size: 11px; color: #6c757d;\">"
                            + "<strong>Aviso Legal Institucional:</strong> Documento oficial emitido a través del Sistema de Visto Bueno Municipal del H. Ayuntamiento Constitucional de Tonalá, Jalisco. La confirmación de aceptación en el servidor de correo Gmail no garantiza la entrega ni la lectura final por parte del destinatario."
                            + "</div>"
                            + "</div>"
                            + "<div style=\"background-color: #212529; color: #ffffff; padding: 12px; text-align: center; font-size: 11px;\">"
                            + "H. Ayuntamiento Constitucional de Tonalá, Jalisco &bull; Calle Hidalgo #21, Tonalá Centro &bull; Tel. (33) 3586 6000"
                            + "</div>"
                            + "</div>";

                    helper.setText(htmlBody, true);

                    // Adjuntar imagen inline del logotipo institucional oficial
                    org.springframework.core.io.ClassPathResource logoRes = new org.springframework.core.io.ClassPathResource("static/images/logo-tonala.png");
                    if (logoRes.exists()) {
                        try {
                            helper.addInline("logoTonala", logoRes, "image/png");
                        } catch (Exception ex) {
                            log.debug("Aviso al adjuntar logo inline: {}", ex.getMessage());
                        }
                    }

                    mailSender.send(mimeMessage);
                    log.info("Oficio {} enviado exitosamente vía correo a: {}", folioOficio, destinatario);
                    return true;
                } else {
                    // Fallback para entornos de prueba / mock donde createMimeMessage() es null
                    SimpleMailMessage mensaje = new SimpleMailMessage();
                    mensaje.setTo(destinatario);
                    mensaje.setSubject("Nuevo Oficio de Visto Bueno Generado - Folio: " + folioOficio);
                    mensaje.setText("Folio: " + folioOficio + "\n" + contenidoDocumento);
                    mensaje.setFrom(correoRemitente);
                    mailSender.send(mensaje);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Aviso SMTP al enviar oficio a {}: {}. Verifique credenciales de Gmail en application.properties.", destinatario, e.getMessage());
            return false;
        }
    }

    /**
     * Envía la resolución oficial del dictamen (Aprobado o Rechazado) por correo electrónico.
     */
    public boolean enviarCorreoDictamen(String destinatario, String folioSolicitud, String razonSocial, String decision, String observaciones) {
        if (destinatario == null || destinatario.trim().isEmpty()) {
            return false;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario.trim());
            helper.setSubject("H. Ayuntamiento de Tonalá - Resolución de Visto Bueno: " + folioSolicitud);
            helper.setFrom(correoRemitente, "Gobierno de Tonalá - Dictamen Oficial");

            String colorBadge = "APROBADO".equalsIgnoreCase(decision) ? "#198754" : "#dc3545";
            String decisionTexto = "APROBADO".equalsIgnoreCase(decision) ? "DICTAMEN FAVORABLE / APROBADO" : "DICTAMEN NO FAVORABLE / RECHAZADO";

            String htmlBody = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 650px; margin: 0 auto; border: 2px solid #822828; border-radius: 6px; overflow: hidden; background: #ffffff;\">"
                    + "<div style=\"background-color: #ffffff; text-align: center; padding: 14px 20px; border-bottom: 2px solid #c59b27;\">"
                    + "<img src=\"cid:logoTonala\" alt=\"Gobierno de Tonalá - Ciudad de la Transformación\" style=\"max-height: 60px; width: auto;\" />"
                    + "</div>"
                    + "<div style=\"background-color: #822828; color: #ffffff; padding: 16px 24px; text-align: center;\">"
                    + "<h2 style=\"margin: 0; font-size: 20px; letter-spacing: 1px;\">GOBIERNO DE TONALÁ, JALISCO</h2>"
                    + "<p style=\"margin: 4px 0 0; font-size: 13px; color: #f8fafc;\">DIRECCIÓN DE PADRÓN, LICENCIAS Y VISTO BUENO MUNICIPAL</p>"
                    + "</div>"
                    + "<div style=\"padding: 24px; color: #212529; line-height: 1.6;\">"
                    + "<h3 style=\"color: #822828; margin-top: 0; border-bottom: 2px solid #e9ecef; padding-bottom: 8px;\">NOTIFICACIÓN OFICIAL DE RESOLUCIÓN</h3>"
                    + "<p>Por medio del presente, se hace de su conocimiento el resultado del análisis y evaluación técnica emitido por la Coordinación de Supervisión del H. Ayuntamiento de Tonalá:</p>"
                    + "<table style=\"width: 100%; border-collapse: collapse; margin: 15px 0; font-size: 14px;\">"
                    + "<tr style=\"background: #f8f9fa;\"><td style=\"padding: 8px 12px; font-weight: bold; width: 38%;\">Folio de Solicitud:</td><td style=\"padding: 8px 12px;\"><strong>" + folioSolicitud + "</strong></td></tr>"
                    + "<tr><td style=\"padding: 8px 12px; font-weight: bold;\">Establecimiento:</td><td style=\"padding: 8px 12px;\">" + (razonSocial != null ? razonSocial : "Establecimiento Solicitante") + "</td></tr>"
                    + "<tr style=\"background: #f8f9fa;\"><td style=\"padding: 8px 12px; font-weight: bold;\">Sentido del Dictamen:</td><td style=\"padding: 8px 12px;\"><span style=\"display: inline-block; padding: 4px 12px; border-radius: 4px; font-weight: bold; color: #ffffff; background-color: " + colorBadge + ";\">" + decisionTexto + "</span></td></tr>"
                    + "<tr><td style=\"padding: 8px 12px; font-weight: bold;\">Observaciones Oficiales:</td><td style=\"padding: 8px 12px;\">" + (observaciones != null && !observaciones.isBlank() ? observaciones : "Sin observaciones adicionales.") + "</td></tr>"
                    + "<tr style=\"background: #f8f9fa;\"><td style=\"padding: 8px 12px; font-weight: bold;\">Fecha de Emisión:</td><td style=\"padding: 8px 12px;\">" + LocalDate.now() + "</td></tr>"
                    + "</table>"
                    + "<div style=\"background-color: #f8f9fa; border-left: 4px solid #c59b27; padding: 10px; margin-top: 20px; font-size: 11px; color: #6c757d;\">"
                    + "<strong>Aviso Legal Institucional:</strong> Notificación generada de conformidad con el Reglamento de Comercio y Servicios del Municipio de Tonalá, Jalisco. La confirmación de aceptación en el servidor de correo Gmail no garantiza la entrega ni la lectura final por parte del destinatario."
                    + "</div>"
                    + "</div>"
                    + "<div style=\"background-color: #212529; color: #ffffff; padding: 12px; text-align: center; font-size: 11px;\">"
                    + "H. Ayuntamiento Constitucional de Tonalá, Jalisco &bull; Calle Hidalgo #21, Tonalá Centro &bull; Tel. (33) 3586 6000"
                    + "</div>"
                    + "</div>";

            helper.setText(htmlBody, true);

            // Adjuntar imagen inline del logotipo oficial
            org.springframework.core.io.ClassPathResource logoRes = new org.springframework.core.io.ClassPathResource("static/images/logo-tonala.png");
            if (logoRes.exists()) {
                try {
                    helper.addInline("logoTonala", logoRes, "image/png");
                } catch (Exception ex) {
                    log.debug("Aviso al adjuntar logo inline: {}", ex.getMessage());
                }
            }

            mailSender.send(mimeMessage);
            log.info("Dictamen de la solicitud {} enviado exitosamente a: {}", folioSolicitud, destinatario);
            return true;
        } catch (Exception e) {
            log.warn("Aviso SMTP al enviar dictamen a {}: {}. Verifique credenciales de Gmail en application.properties.", destinatario, e.getMessage());
            return false;
        }
    }

    /**
     * Genera un nuevo oficio en la base de datos y lo vincula a una solicitud.
     */
    @Transactional
    public Oficio generarYGuardarOficio(String folioSolicitud, Long dependenciaId, String contenido) {
        return generarYGuardarOficio(folioSolicitud, dependenciaId, contenido, null, LocalDate.now());
    }

    /**
     * Sobrecarga completa para guardar oficio con email de destino y fecha de conteo personalizada.
     */
    @Transactional
    public Oficio generarYGuardarOficio(String folioSolicitud, Long dependenciaId, String contenido,
                                       String emailDestino, LocalDate fechaInicioConteo) {
        // 1. Buscar la dependencia a la que va dirigida
        Dependencia dependencia = dependenciaRepository.findById(dependenciaId)
                .orElseThrow(() -> new RuntimeException("Dependencia no encontrada con ID: " + dependenciaId));

        // 2. Incrementar secuencia y generar folio
        int siguienteSecuencia = (dependencia.getSecuenciaActual() != null ? dependencia.getSecuenciaActual() : 0) + 1;
        dependencia.setSecuenciaActual(siguienteSecuencia);
        dependenciaRepository.save(dependencia);

        String prefijo = (dependencia.getPrefijoFolio() != null && !dependencia.getPrefijoFolio().isBlank())
                ? dependencia.getPrefijoFolio()
                : "OF";
        String folioGenerado = prefijo + "-" + LocalDate.now().getYear() + "-" + String.format("%04d", siguienteSecuencia);

        // 3. Crear el objeto Oficio con los datos iniciales
        Oficio nuevoOficio = new Oficio();
        nuevoOficio.setFolioOficio(folioGenerado);
        nuevoOficio.setDependencia(dependencia);
        nuevoOficio.setContenidoDoc(contenido);
        nuevoOficio.setFechaGeneracion(LocalDateTime.now());
        nuevoOficio.setFechaInicioConteo(fechaInicioConteo != null ? fechaInicioConteo : LocalDate.now());
        nuevoOficio.setEstadoCierre(false);
        nuevoOficio.setEmailDestino(emailDestino);

        // Guardamos el oficio en la base de datos
        nuevoOficio = oficioRepository.save(nuevoOficio);

        // 4. Crear el vínculo en la tabla intermedia solicitud_oficio
        SolicitudOficio vinculo = new SolicitudOficio(folioSolicitud, nuevoOficio.getId());
        solicitudOficioRepository.save(vinculo);

        log.info("Oficio {} guardado y vinculado con Solicitud {}", folioGenerado, folioSolicitud);

        // 5. Enviar correo de notificación si se especificó destinatario
        if (emailDestino != null && !emailDestino.trim().isEmpty()) {
            enviarCorreoOficio(emailDestino.trim(), folioGenerado, contenido);
            nuevoOficio.setFechaEnvio(LocalDateTime.now());
            nuevoOficio = oficioRepository.save(nuevoOficio);
        }

        return nuevoOficio;
    }

    /**
     * Permite agrupar múltiples solicitudes compatibles en un solo oficio dirigido a una dependencia.
     */
    @Transactional
    public Oficio generarYGuardarOficioAgrupado(List<String> foliosSolicitudes, Long dependenciaId, String contenido,
                                               String emailDestino, LocalDate fechaInicioConteo) {
        Dependencia dependencia = dependenciaRepository.findById(dependenciaId)
                .orElseThrow(() -> new RuntimeException("Dependencia no encontrada con ID: " + dependenciaId));

        int siguienteSecuencia = (dependencia.getSecuenciaActual() != null ? dependencia.getSecuenciaActual() : 0) + 1;
        dependencia.setSecuenciaActual(siguienteSecuencia);
        dependenciaRepository.save(dependencia);

        String prefijo = (dependencia.getPrefijoFolio() != null && !dependencia.getPrefijoFolio().isBlank())
                ? dependencia.getPrefijoFolio()
                : "OF";
        String folioGenerado = prefijo + "-" + LocalDate.now().getYear() + "-" + String.format("%04d", siguienteSecuencia);

        Oficio nuevoOficio = new Oficio();
        nuevoOficio.setFolioOficio(folioGenerado);
        nuevoOficio.setDependencia(dependencia);
        nuevoOficio.setContenidoDoc(contenido);
        nuevoOficio.setFechaGeneracion(LocalDateTime.now());
        nuevoOficio.setFechaInicioConteo(fechaInicioConteo != null ? fechaInicioConteo : LocalDate.now());
        nuevoOficio.setEstadoCierre(false);
        nuevoOficio.setEmailDestino(emailDestino);

        nuevoOficio = oficioRepository.save(nuevoOficio);

        for (String folio : foliosSolicitudes) {
            if (folio != null && !folio.isBlank()) {
                solicitudOficioRepository.save(new SolicitudOficio(folio.trim(), nuevoOficio.getId()));
            }
        }

        if (emailDestino != null && !emailDestino.trim().isEmpty()) {
            enviarCorreoOficio(emailDestino.trim(), folioGenerado, contenido);
            nuevoOficio.setFechaEnvio(LocalDateTime.now());
            nuevoOficio = oficioRepository.save(nuevoOficio);
        }

        return nuevoOficio;
    }

    /**
     * Registra la respuesta formal recibida de una dependencia y opcionalmente cierra el oficio.
     */
    @Transactional
    public Oficio registrarRespuesta(Long oficioId, String numeroRespuesta, String observaciones, boolean cerrarOficio) {
        Oficio oficio = oficioRepository.findById(oficioId)
                .orElseThrow(() -> new RuntimeException("Oficio no encontrado con ID: " + oficioId));

        oficio.setNumeroRespuesta(numeroRespuesta);
        oficio.setObservacionesRespuesta(observaciones);
        oficio.setFechaRespuesta(LocalDateTime.now());

        if (cerrarOficio) {
            oficio.setEstadoCierre(true);
            oficio.setFechaCierre(LocalDateTime.now());
        }

        return oficioRepository.save(oficio);
    }

    /**
     * Reenvía un oficio existente por correo electrónico (Gmail) y actualiza el registro.
     */
    @Transactional
    public boolean enviarCorreoOficioExistente(Long oficioId, String correoDestino) {
        Oficio oficio = oficioRepository.findById(oficioId)
                .orElseThrow(() -> new RuntimeException("Oficio no encontrado: " + oficioId));
        boolean exito = enviarCorreoOficio(correoDestino, oficio.getFolioOficio(), oficio.getContenidoDoc());
        oficio.setEmailDestino(correoDestino);
        oficio.setFechaEnvio(LocalDateTime.now());
        oficioRepository.save(oficio);
        return exito;
    }

    /**
     * Inicializa las 3 dependencias oficiales del proyecto si la tabla está vacía:
     * - Ecología
     * - Inspección y Reglamentos
     * - Protección Civil y Bomberos
     */
    @Transactional
    public String inicializarDependencias() {
        if (dependenciaRepository.count() == 0) {
            Dependencia dep1 = new Dependencia("Ecología", "ecologia@municipio.gob.mx", "ECO");
            Dependencia dep2 = new Dependencia("Inspección y Reglamentos", "reglamentos@municipio.gob.mx", "IR");
            Dependencia dep3 = new Dependencia("Protección Civil y Bomberos", "proteccioncivil@municipio.gob.mx", "PCB");

            dependenciaRepository.saveAll(List.of(dep1, dep2, dep3));
            log.info("Las 3 dependencias oficiales fueron inicializadas con éxito.");
            return "¡Las 3 dependencias oficiales se registraron con éxito en la base de datos!";
        }

        return "Las dependencias ya estaban registradas en la base de datos, no se hicieron cambios.";
    }

    /**
     * Retorna todas las dependencias disponibles.
     */
    public List<Dependencia> listarDependencias() {
        return dependenciaRepository.findAll();
    }

    /**
     * Retorna todos los oficios registrados.
     */
    public List<Oficio> listarOficios() {
        return oficioRepository.findAll();
    }

    /**
     * Retorna lista de resumen con DTO que incluye días hábiles transcurridos y semáforo.
     */
    public List<OficioResumenDTO> obtenerResumenOficios() {
        List<Oficio> oficios = oficioRepository.findAll();
        List<OficioResumenDTO> resumenes = new ArrayList<>();

        for (Oficio of : oficios) {
            String folioSolicitud = solicitudOficioRepository.findByOficioId(of.getId())
                    .map(SolicitudOficio::getFolioSolicitud)
                    .orElse("N/A");

            LocalDate fechaFin = (of.getEstadoCierre() != null && of.getEstadoCierre() && of.getFechaCierre() != null)
                    ? of.getFechaCierre().toLocalDate()
                    : LocalDate.now();

            long dias = 0;
            if (of.getFechaInicioConteo() != null) {
                dias = calcularDiasHabiles(of.getFechaInicioConteo(), fechaFin);
            }

            String color = determinarColorSemaforo(dias);
            String badgeClass = obtenerClaseCssSemaforo(color);

            resumenes.add(new OficioResumenDTO(of, folioSolicitud, dias, color, badgeClass));
        }

        return resumenes;
    }
}
