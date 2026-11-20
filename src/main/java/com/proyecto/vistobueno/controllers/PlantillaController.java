package com.proyecto.vistobueno.controllers;

import com.proyecto.vistobueno.entities.Dependencia;
import com.proyecto.vistobueno.entities.Usuario;
import com.proyecto.vistobueno.model.UsuarioSistema;
import com.proyecto.vistobueno.repositories.DependenciaRepository;
import com.proyecto.vistobueno.repositories.UsuarioRepository;
import com.proyecto.vistobueno.repository.UsuarioSistemaRepository;
import com.proyecto.vistobueno.services.BitacoraAuditoriaService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/plantillas")
public class PlantillaController {

    private final DependenciaRepository dependenciaRepository;
    private final BitacoraAuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioSistemaRepository usuarioSistemaRepository;

    public PlantillaController(DependenciaRepository dependenciaRepository,
                               BitacoraAuditoriaService auditoriaService,
                               UsuarioRepository usuarioRepository,
                               UsuarioSistemaRepository usuarioSistemaRepository) {
        this.dependenciaRepository = dependenciaRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
    }

    @PostConstruct
    public void asegurarPlantillasBase() {
        try {
            List<Dependencia> lista = dependenciaRepository.findAll();
            for (Dependencia dep : lista) {
                if (dep.getPlantillaHtml() == null || dep.getPlantillaHtml().isBlank()) {
                    dep.setPlantillaHtml(generarPlantillaPredeterminada(dep.getNombre()));
                    dependenciaRepository.save(dep);
                }
            }
        } catch (Exception ignored) {}
    }

    @GetMapping
    public String listar(Model model, Authentication auth) {
        asegurarPlantillasBase();
        model.addAttribute("dependencias", dependenciaRepository.findAll());
        cargarUsuarioActual(model, auth);
        return "plantillas/lista";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model, Authentication auth) {
        Dependencia dep = dependenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dependencia no encontrada: " + id));
        if (dep.getPlantillaHtml() == null || dep.getPlantillaHtml().isBlank()) {
            dep.setPlantillaHtml(generarPlantillaPredeterminada(dep.getNombre()));
        }
        model.addAttribute("dependencia", dep);
        cargarUsuarioActual(model, auth);
        return "plantillas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Dependencia dependencia,
                          HttpServletRequest request,
                          Authentication auth,
                          RedirectAttributes redirectAttributes) {
        try {
            Dependencia existente = dependenciaRepository.findById(dependencia.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Dependencia no encontrada"));

            existente.setNombre(dependencia.getNombre());
            existente.setCorreoContacto(dependencia.getCorreoContacto());
            existente.setPrefijoFolio(dependencia.getPrefijoFolio());
            existente.setPlantillaHtml(dependencia.getPlantillaHtml());

            dependenciaRepository.save(existente);

            Usuario usuario = null;
            if (auth != null) {
                usuario = usuarioRepository.findByNumeroEmpleado(auth.getName()).orElse(null);
            }

            auditoriaService.registrar(
                    usuario,
                    "PLANTILLAS",
                    "ACTUALIZAR",
                    "Plantilla HTML actualizada para la dependencia: " + existente.getNombre(),
                    request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Plantilla oficial de " + existente.getNombre() + " guardada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar plantilla: " + e.getMessage());
        }
        return "redirect:/plantillas";
    }

    private String generarPlantillaPredeterminada(String nombreDependencia) {
        return "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 700px; margin: 0 auto; border: 2px solid #822828; border-radius: 6px; overflow: hidden; background: #ffffff;\">\n" +
                "  <div style=\"background-color: #822828; color: #ffffff; padding: 18px 24px; text-align: center; border-bottom: 3px solid #c59b27;\">\n" +
                "    <h3 style=\"color: #ffffff; margin: 0; font-size: 1.3rem; letter-spacing: 1px;\">GOBIERNO MUNICIPAL DE TONALÁ, JALISCO</h3>\n" +
                "    <h4 style=\"margin: 6px 0; color: #ffffff; font-weight: 600;\">" + nombreDependencia.toUpperCase() + "</h4>\n" +
                "    <p style=\"margin: 0; font-size: 0.82rem; color: #f8fafc;\">COORDINACIÓN DE PADRÓN, LICENCIAS Y VISTO BUENO MUNICIPAL</p>\n" +
                "  </div>\n" +
                "  <div style=\"padding: 24px; color: #212529; line-height: 1.6;\">\n" +
                "    <p style=\"text-align: right; font-size: 0.9rem; margin-bottom: 20px;\"><strong>Fecha:</strong> {{FECHA}} | <strong>Folio:</strong> {{FOLIO}}</p>\n" +
                "    <p><strong>C. TITULAR DE " + nombreDependencia.toUpperCase() + "</strong><br>PRESENTE.</p>\n" +
                "    <p>Por medio del presente oficio se solicita a su digno cargo la emisión del dictamen técnico correspondiente a la solicitud oficial <strong>{{FOLIO}}</strong> para el siguiente establecimiento comercial:</p>\n" +
                "    <table style=\"width: 100%; border-collapse: collapse; margin: 15px 0; font-size: 14px;\">\n" +
                "      <tr style=\"background: #f8f9fa;\"><td style=\"padding: 8px 12px; border: 1px solid #dee2e6; width: 32%;\"><strong>Razón Social:</strong></td><td style=\"padding: 8px 12px; border: 1px solid #dee2e6;\">{{RAZON_SOCIAL}}</td></tr>\n" +
                "      <tr><td style=\"padding: 8px 12px; border: 1px solid #dee2e6;\"><strong>Giro Comercial:</strong></td><td style=\"padding: 8px 12px; border: 1px solid #dee2e6;\">{{GIRO}}</td></tr>\n" +
                "      <tr style=\"background: #f8f9fa;\"><td style=\"padding: 8px 12px; border: 1px solid #dee2e6;\"><strong>Domicilio:</strong></td><td style=\"padding: 8px 12px; border: 1px solid #dee2e6;\">{{DOMICILIO}}</td></tr>\n" +
                "    </table>\n" +
                "    <p>Agradecemos remitir la respuesta formal dentro del cómputo de días hábiles normativos para la emisión del Visto Bueno Municipal.</p>\n" +
                "    <br>\n" +
                "    <div style=\"text-align: center; margin-top: 25px;\">\n" +
                "      <p style=\"margin: 0;\"><strong>ATENTAMENTE</strong><br>Coordinación de Visto Bueno Municipal<br>H. Ayuntamiento Constitucional de Tonalá, Jalisco</p>\n" +
                "    </div>\n" +
                "    <div style=\"background-color: #f8f9fa; border-left: 4px solid #c59b27; padding: 10px; margin-top: 25px; font-size: 11px; color: #6c757d;\">\n" +
                "      <strong>Aviso Institucional:</strong> Documento oficial emitido bajo los lineamientos del Gobierno de Tonalá.\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <div style=\"background-color: #212529; color: #ffffff; padding: 10px; text-align: center; font-size: 11px;\">\n" +
                "    H. Ayuntamiento Constitucional de Tonalá, Jalisco &bull; Sistema de Visto Bueno Municipal\n" +
                "  </div>\n" +
                "</div>";
    }

    private void cargarUsuarioActual(Model model, Authentication auth) {
        if (auth != null) {
            usuarioSistemaRepository.findByNumeroEmpleado(auth.getName())
                    .ifPresent(u -> model.addAttribute("usuarioActual", u));
        }
    }
}
