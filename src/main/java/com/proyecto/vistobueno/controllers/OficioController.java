package com.proyecto.vistobueno.controllers;

import com.proyecto.vistobueno.entities.Dependencia;
import com.proyecto.vistobueno.entities.Oficio;
import com.proyecto.vistobueno.services.OficioService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OficioController {

    private static final Logger log = LoggerFactory.getLogger(OficioController.class);

    @Autowired
    private OficioService oficioService;

    @Autowired
    private com.proyecto.vistobueno.repositories.SolicitudRepository solicitudRepository;

    @Autowired
    private com.proyecto.vistobueno.repository.UsuarioSistemaRepository usuarioSistemaRepository;

    @Autowired
    private com.proyecto.vistobueno.repositories.UsuarioRepository usuarioRepository;

    @Autowired
    private com.proyecto.vistobueno.services.BitacoraAuditoriaService auditoriaService;

    /**
     * Inicializa automáticamente las 3 dependencias oficiales al arrancar la aplicación si está vacía.
     */
    @PostConstruct
    public void autoInicializarDependencias() {
        try {
            String resultado = oficioService.inicializarDependencias();
            log.info("Inicialización de dependencias: {}", resultado);
        } catch (Exception e) {
            log.warn("No se pudo auto-inicializar dependencias al inicio: {}", e.getMessage());
        }
    }

    /**
     * Vista principal (MVC): listado de oficios con su semáforo, bandejas y formulario de registro.
     */
    @GetMapping("/oficios")
    public String verOficios(Model model, org.springframework.security.core.Authentication auth) {
        try {
            oficioService.inicializarDependencias();
        } catch (Exception ignored) {}

        List<Dependencia> dependencias = oficioService.listarDependencias();
        model.addAttribute("dependencias", dependencias);
        model.addAttribute("oficios", oficioService.obtenerResumenOficios());
        model.addAttribute("solicitudes", solicitudRepository.findAllByOrderByFechaRegistroDesc());
        model.addAttribute("fechaHoy", LocalDate.now());

        if (auth != null) {
            usuarioSistemaRepository.findByNumeroEmpleado(auth.getName())
                    .ifPresent(u -> model.addAttribute("usuarioActual", u));
        }

        return "oficios";
    }

    /**
     * Procesa el formulario para registrar y vincular un nuevo oficio a una solicitud.
     */
    @PostMapping("/oficios/guardar")
    public String guardarOficio(
            @RequestParam("folioSolicitud") String folioSolicitud,
            @RequestParam("dependenciaId") Long dependenciaId,
            @RequestParam("contenidoDoc") String contenidoDoc,
            @RequestParam(value = "emailDestino", required = false) String emailDestino,
            @RequestParam(value = "fechaInicioConteo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioConteo,
            jakarta.servlet.http.HttpServletRequest request,
            org.springframework.security.core.Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            if (folioSolicitud == null || folioSolicitud.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("mensajeError", "El folio de la solicitud es obligatorio.");
                return "redirect:/oficios";
            }

            LocalDate fechaInicio = (fechaInicioConteo != null) ? fechaInicioConteo : LocalDate.now();
            Oficio nuevo = oficioService.generarYGuardarOficio(
                    folioSolicitud.trim(),
                    dependenciaId,
                    contenidoDoc,
                    emailDestino,
                    fechaInicio
            );

            long dias = oficioService.calcularDiasHabiles(fechaInicio, LocalDate.now());
            String color = oficioService.determinarColorSemaforo(dias);

            com.proyecto.vistobueno.entities.Usuario usuario = (auth != null)
                    ? usuarioRepository.findByNumeroEmpleado(auth.getName()).orElse(null)
                    : null;
            auditoriaService.registrar(
                    usuario,
                    "OFICIOS",
                    "GENERAR",
                    "Oficio generado " + nuevo.getFolioOficio() + " para solicitud " + folioSolicitud + " con semáforo " + color,
                    request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "¡Oficio " + nuevo.getFolioOficio() + " generado exitosamente! Semáforo actual: " + color + " (" + dias + " días hábiles).");

        } catch (Exception e) {
            log.error("Error al guardar oficio: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear el oficio: " + e.getMessage());
        }

        return "redirect:/oficios";
    }

    /**
     * Registra la respuesta oficial de la dependencia y opcionalmente cierra el seguimiento.
     */
    @PostMapping("/oficios/registrar-respuesta")
    public String registrarRespuesta(
            @RequestParam("oficioId") Long oficioId,
            @RequestParam("numeroRespuesta") String numeroRespuesta,
            @RequestParam(value = "observacionesRespuesta", required = false) String observaciones,
            @RequestParam(value = "cerrarOficio", defaultValue = "false") boolean cerrarOficio,
            jakarta.servlet.http.HttpServletRequest request,
            org.springframework.security.core.Authentication auth,
            RedirectAttributes redirectAttributes) {

        // Segregación estricta de funciones: El Superadministrador NO puede registrar respuestas a oficios de dependencias
        if (auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Acceso Denegado: El Superadministrador sólo realiza tareas de mantenimiento técnico y no tiene facultades para registrar respuestas a los oficios de dependencias.");
            return "redirect:/oficios";
        }

        try {
            Oficio oficio = oficioService.registrarRespuesta(oficioId, numeroRespuesta, observaciones, cerrarOficio);

            com.proyecto.vistobueno.entities.Usuario usuario = (auth != null)
                    ? usuarioRepository.findByNumeroEmpleado(auth.getName()).orElse(null)
                    : null;
            auditoriaService.registrar(
                    usuario,
                    "OFICIOS",
                    "REGISTRAR_RESPUESTA",
                    "Respuesta registrada para oficio " + oficio.getFolioOficio() + ": " + numeroRespuesta + ". Cierre definitivo: " + cerrarOficio,
                    request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Respuesta registrada correctamente para el oficio " + oficio.getFolioOficio() + (cerrarOficio ? " (Seguimiento cerrado)." : "."));
        } catch (Exception e) {
            log.error("Error al registrar respuesta: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar respuesta: " + e.getMessage());
        }

        return "redirect:/oficios";
    }

    /**
     * Envía un oficio por correo electrónico mediante Gmail y registra la acción.
     */
    @PostMapping("/oficios/enviar-correo")
    public String enviarCorreo(
            @RequestParam("oficioId") Long oficioId,
            @RequestParam("emailDestino") String emailDestino,
            jakarta.servlet.http.HttpServletRequest request,
            org.springframework.security.core.Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            boolean enviado = oficioService.enviarCorreoOficioExistente(oficioId, emailDestino.trim());

            com.proyecto.vistobueno.entities.Usuario usuario = (auth != null)
                    ? usuarioRepository.findByNumeroEmpleado(auth.getName()).orElse(null)
                    : null;
            auditoriaService.registrar(
                    usuario,
                    "OFICIOS",
                    "ENVIAR_GMAIL",
                    "Oficio ID " + oficioId + " enviado a " + emailDestino + " vía Gmail. Estado entrega: " + (enviado ? "EXITOSO" : "PENDIENTE_CONFIG_SMTP"),
                    request.getRemoteAddr()
            );

            if (enviado) {
                redirectAttributes.addFlashAttribute("mensajeExito",
                        "¡Oficio enviado exitosamente a " + emailDestino + "! (Nota: El registro de aceptación por Gmail no garantiza entrega o lectura final).");
            } else {
                redirectAttributes.addFlashAttribute("mensajeError",
                        "Aviso de Envío: El oficio se registró para envío a " + emailDestino + ", pero el servidor de Gmail no aceptó la conexión (credenciales temporales en application.properties). Configure una Contraseña de Aplicación de 16 caracteres de Google para entrega en buzón.");
            }
        } catch (Exception e) {
            log.error("Error al enviar correo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar envío de correo: " + e.getMessage());
        }

        return "redirect:/oficios";
    }

    /**
     * Endpoint web para invocar la inicialización manual de dependencias desde la interfaz.
     */
    @GetMapping("/inicializar-dependencias")
    public String inicializarDependenciasWeb(RedirectAttributes redirectAttributes) {
        try {
            String mensaje = oficioService.inicializarDependencias();
            redirectAttributes.addFlashAttribute("mensajeExito", mensaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al inicializar dependencias: " + e.getMessage());
        }
        return "redirect:/oficios";
    }

    // =========================================================================
    // ENDPOINTS REST / API (Compatibilidad y pruebas directas)
    // =========================================================================

    /**
     * Endpoint REST para probar el cálculo de días hábiles y el semáforo.
     */
    @GetMapping("/api/oficios/prueba-semaforo")
    @ResponseBody
    public Map<String, Object> probarSemaforo(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);

        long dias = oficioService.calcularDiasHabiles(inicio, fin);
        String color = oficioService.determinarColorSemaforo(dias);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha_inicio_ingresada", inicio);
        respuesta.put("fecha_fin_ingresada", fin);
        respuesta.put("dias_habiles_calculados", dias);
        respuesta.put("color_del_semaforo", color);

        return respuesta;
    }

    /**
     * Endpoint REST para probar el envío de correos vía JavaMailSender.
     */
    @GetMapping("/api/oficios/prueba-correo")
    @ResponseBody
    public String probarCorreo(@RequestParam String correoDestino) {
        try {
            oficioService.enviarCorreoOficio(
                    correoDestino,
                    "OFICIO-TEST-001",
                    "Este es un mensaje de prueba para el proyecto de Visto Bueno Municipal."
            );
            return "¡Correo enviado con éxito a " + correoDestino + "!";
        } catch (Exception e) {
            return "Fallo al enviar el correo: " + e.getMessage();
        }
    }

    /**
     * Endpoint REST para insertar las dependencias en la base de datos si está vacía.
     */
    @GetMapping("/api/oficios/inicializar-dependencias")
    @ResponseBody
    public String inicializarDependenciasApi() {
        return oficioService.inicializarDependencias();
    }
}
