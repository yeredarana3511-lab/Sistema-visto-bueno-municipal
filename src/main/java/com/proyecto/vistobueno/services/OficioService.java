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
     * Envía un correo electrónico básico con los datos del oficio (Integración Gmail / JavaMailSender).
     */
    public void enviarCorreoOficio(String destinatario, String folioOficio, String contenidoDocumento) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Nuevo Oficio de Visto Bueno Generado - Folio: " + folioOficio);
            mensaje.setText("Se ha generado un nuevo oficio de Visto Bueno Municipal.\n\n"
                    + "Folio del Oficio: " + folioOficio + "\n"
                    + "Contenido / Detalles:\n" + contenidoDocumento + "\n\n"
                    + "Atentamente,\nGobierno Municipal - Sistema de Oficios");
            mensaje.setFrom(correoRemitente);

            mailSender.send(mensaje);
            log.info("Correo enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar el correo electrónico a {}: {}", destinatario, e.getMessage());
            // No interrumpimos bruscamente la creación del oficio si el servidor SMTP falla temporalmente
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
        }

        return nuevoOficio;
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
