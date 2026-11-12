package com.proyecto.vistobueno.controllers;

import com.proyecto.vistobueno.entities.BitacoraAuditoria;
import com.proyecto.vistobueno.entities.Usuario;
import com.proyecto.vistobueno.repositories.UsuarioRepository;
import com.proyecto.vistobueno.services.BitacoraAuditoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/auditoria")
public class BitacoraAuditoriaController {

    private final BitacoraAuditoriaService service;
    private final UsuarioRepository usuarioRepository;

    public BitacoraAuditoriaController(BitacoraAuditoriaService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("registros", service.listarTodas());
        return "auditoria/lista";
    }

    @GetMapping("/crear-prueba")
    public String crearPrueba(RedirectAttributes redirectAttributes) {
        try {
            // Asegurar un usuario por defecto si la base de datos está vacía
            Usuario usuario = usuarioRepository.findAll().stream().findFirst().orElseGet(() -> {
                Usuario nuevo = new Usuario();
                nuevo.setNumeroEmpleado("EMP-001");
                nuevo.setNombre("Funcionario Municipal");
                return usuarioRepository.save(nuevo);
            });

            BitacoraAuditoria log = new BitacoraAuditoria();
            log.setUsuario(usuario);
            log.setModulo("SOLICITUDES");
            log.setAccion("CONFIRMAR_OCR");
            log.setDetalle("Se validaron manualmente los datos de la solicitud");
            log.setIpOrigen("127.0.0.1");
            log.setFechaHora(LocalDateTime.now());

            service.registrar(log);
            redirectAttributes.addFlashAttribute("mensajeExito", "Registro de auditoría de prueba generado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear registro de auditoría de prueba: " + e.getMessage());
        }
        return "redirect:/auditoria";
    }
}
