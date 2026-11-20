package com.proyecto.vistobueno.controllers;

import com.proyecto.vistobueno.dto.OcrResultDTO;
import com.proyecto.vistobueno.entities.Solicitud;
import com.proyecto.vistobueno.model.UsuarioSistema;
import com.proyecto.vistobueno.repository.UsuarioSistemaRepository;
import com.proyecto.vistobueno.services.OcrService;
import com.proyecto.vistobueno.services.SolicitudService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    private static final Logger log = LoggerFactory.getLogger(SolicitudController.class);

    private final SolicitudService solicitudService;
    private final OcrService ocrService;
    private final UsuarioSistemaRepository usuarioRepository;

    public SolicitudController(SolicitudService solicitudService,
                               OcrService ocrService,
                               UsuarioSistemaRepository usuarioRepository) {
        this.solicitudService = solicitudService;
        this.ocrService = ocrService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Catálogo / Listado de solicitudes registradas en el sistema
     */
    @GetMapping
    public String listar(Model model, Authentication auth) {
        cargarUsuarioActual(model, auth);
        List<Solicitud> lista = solicitudService.listarTodas();
        model.addAttribute("solicitudes", lista);
        return "solicitudes/lista";
    }

    /**
     * Formulario para subir imagen de solicitud (Paso 1 del flujo OCR)
     */
    @GetMapping("/nueva")
    public String nuevaSolicitud(Model model, Authentication auth) {
        cargarUsuarioActual(model, auth);
        model.addAttribute("ocr", new OcrResultDTO());
        model.addAttribute("mostrarValidacion", false);
        return "solicitudes/nueva";
    }

    /**
     * Procesa la imagen mediante OCR y despliega la pantalla de REVISIÓN Y VALIDACIÓN HUMANA
     */
    @PostMapping("/procesar-ocr")
    public String procesarOcr(@RequestParam("imagen") MultipartFile imagen,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              Authentication auth) {
        // Exclusivo de Secretaría / Operador
        if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_SECRETARIA") || a.getAuthority().equals("ROLE_CAJERO"))) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Acceso Denegado: Solo el personal de Secretaría / Ventanilla puede subir y procesar solicitudes con OCR.");
            return "redirect:/solicitudes";
        }
        cargarUsuarioActual(model, auth);
        try {
            if (imagen.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensajeError", "Debe seleccionar un archivo de imagen válido (PNG, JPG, PDF).");
                return "redirect:/solicitudes/nueva";
            }

            OcrResultDTO ocrResultado = ocrService.procesarImagenSolicitud(imagen);
            model.addAttribute("ocr", ocrResultado);
            model.addAttribute("mostrarValidacion", true);

            return "solicitudes/nueva";
        } catch (Exception e) {
            log.error("Error al procesar OCR: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar la imagen con OCR: " + e.getMessage());
            return "redirect:/solicitudes/nueva";
        }
    }

    /**
     * Guarda la solicitud tras la confirmación o corrección humana
     */
    @PostMapping("/confirmar")
    public String confirmarSolicitud(@ModelAttribute OcrResultDTO ocrDto,
                                    HttpServletRequest request,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        // Exclusivo de Secretaría / Operador
        if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_SECRETARIA") || a.getAuthority().equals("ROLE_CAJERO"))) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Acceso Denegado: La captura y validación de solicitudes es exclusiva del personal de Secretaría / Ventanilla.");
            return "redirect:/solicitudes";
        }
        try {
            Long usuarioId = obtenerIdUsuarioActual(auth);
            String ipOrigen = request.getRemoteAddr();

            Solicitud guardada = solicitudService.confirmarYGuardarSolicitud(ocrDto, usuarioId, ipOrigen);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "¡Solicitud " + guardada.getFolioSolicitud() + " validada humanamente y registrada con éxito! Oficios vinculados y listos para seguimiento.");
            return "redirect:/solicitudes";
        } catch (Exception e) {
            log.error("Error al confirmar solicitud: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar la solicitud: " + e.getMessage());
            return "redirect:/solicitudes/nueva";
        }
    }

    /**
     * Vista de detalle de una solicitud
     */
    @GetMapping("/detalle/{folio}")
    public String detalle(@PathVariable("folio") String folio, Model model, Authentication auth) {
        cargarUsuarioActual(model, auth);
        Solicitud solicitud = solicitudService.buscarPorFolio(folio)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + folio));
        model.addAttribute("solicitud", solicitud);
        return "solicitudes/detalle";
    }

    /**
     * Acción del Supervisor: Dictamen de Visto Bueno (Aprobar / Rechazar)
     */
    @PostMapping("/dictaminar")
    public String dictaminar(@RequestParam("folioSolicitud") String folio,
                             @RequestParam("decision") String decision,
                             @RequestParam(value = "observaciones", required = false) String observaciones,
                             @RequestParam(value = "correoDestino", required = false) String correoDestino,
                             HttpServletRequest request,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        // Validación estricta de segregación de funciones: El Administrador NO puede dictaminar
        if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR") || a.getAuthority().equals("ROLE_DIRECTOR"))) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Acceso Denegado: El perfil Administrador no tiene facultades para dictaminar solicitudes. Esta función es exclusiva del Supervisor.");
            return "redirect:/solicitudes";
        }

        try {
            Long usuarioId = obtenerIdUsuarioActual(auth);
            boolean correoEnviado = solicitudService.dictaminarSolicitud(folio, decision, observaciones, correoDestino, usuarioId, request.getRemoteAddr());

            String mensaje = "Dictamen de Visto Bueno registrado como: " + decision + " para la solicitud " + folio + ".";
            if (correoDestino != null && !correoDestino.trim().isEmpty()) {
                if (correoEnviado) {
                    mensaje += " Notificación oficial enviada con éxito al correo " + correoDestino + ".";
                } else {
                    mensaje += " (Aviso sobre correo: No se pudo entregar a " + correoDestino + " vía Gmail. Configure su contraseña de aplicación en application.properties).";
                }
            }
            redirectAttributes.addFlashAttribute("mensajeExito", mensaje);
        } catch (Exception e) {
            log.error("Error al emitir dictamen: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Error al dictaminar: " + e.getMessage());
        }
        return "redirect:/solicitudes";
    }

    private void cargarUsuarioActual(Model model, Authentication auth) {
        if (auth != null) {
            usuarioRepository.findByNumeroEmpleado(auth.getName())
                    .ifPresent(u -> model.addAttribute("usuarioActual", u));
        }
    }

    private Long obtenerIdUsuarioActual(Authentication auth) {
        if (auth != null) {
            return usuarioRepository.findByNumeroEmpleado(auth.getName())
                    .map(UsuarioSistema::getId)
                    .orElse(null);
        }
        return null;
    }
}
