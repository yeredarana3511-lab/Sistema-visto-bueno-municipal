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

    /**
     * Inicializa automáticamente las 3 dependencias oficiales al arrancar la aplicación si está vacía.
     */
    @PostConstruct
    public void autoInicializarDependencias() {
        try {
            String resultado = oficioService.inicializarDependencias();
            log.info("Inicialización de dependencias: {}", resultado);
        } catch (Exception e) {
            log.warn("No se pudo auto-inicializar dependencias al inicio (la base de datos podría estar iniciando): {}", e.getMessage());
        }
    }

    /**
     * Vista principal (MVC): listado de oficios con su semáforo y formulario de registro.
     */
    @GetMapping({"/", "/oficios"})
    public String verOficios(Model model) {
        // Asegurar que las dependencias estén cargadas
        try {
            oficioService.inicializarDependencias();
        } catch (Exception ignored) {
        }

        List<Dependencia> dependencias = oficioService.listarDependencias();
        model.addAttribute("dependencias", dependencias);
        model.addAttribute("oficios", oficioService.obtenerResumenOficios());
        model.addAttribute("fechaHoy", LocalDate.now());

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

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "¡Oficio " + nuevo.getFolioOficio() + " generado exitosamente! Semáforo actual: " + color + " (" + dias + " días hábiles).");

        } catch (Exception e) {
            log.error("Error al guardar oficio: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear el oficio: " + e.getMessage());
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
