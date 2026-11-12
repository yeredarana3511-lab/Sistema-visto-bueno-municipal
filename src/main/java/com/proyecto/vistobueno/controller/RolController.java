package com.proyecto.vistobueno.controller;

import com.proyecto.vistobueno.model.Rol;
import com.proyecto.vistobueno.service.RolService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;

    // Catálogo base de permisos por módulo según el flujo operativo del sistema
    private static final List<String> PERMISOS_DISPONIBLES = Arrays.asList(
            // Módulo de Mantenimiento y Administración (Administrador)
            "USUARIOS:GESTIONAR", "ROLES:GESTIONAR",
            // Módulo de Captura y OCR (Secretaria)
            "SOLICITUDES:CONSULTAR", "SOLICITUDES:CREAR", "SOLICITUDES:EDITAR", "SOLICITUDES:CONFIRMAR_OCR", "SOLICITUDES:ENVIAR",
            // Módulo de Visto Bueno (Supervisor)
            "VISTO_BUENO:APROBAR_RECHAZAR",
            // Módulo de Oficios y Plantillas
            "OFICIOS:CONSULTAR", "OFICIOS:GENERAR", "OFICIOS:ENVIAR",
            // Módulo de Seguimiento y Auditoría
            "SEGUIMIENTO:CONSULTAR", "SEGUIMIENTO:REGISTRAR_RESPUESTA"
    );

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("roles", rolService.listarTodos());
        return "roles/listado";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("rol", new Rol());
        model.addAttribute("permisosDisponibles", PERMISOS_DISPONIBLES);
        return "roles/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable("id") Long id, Model model) {
        Rol rol = rolService.buscarPorId(id).orElseThrow();
        model.addAttribute("rol", rol);
        model.addAttribute("permisosDisponibles", PERMISOS_DISPONIBLES);
        return "roles/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("rol") Rol rol) {
        rolService.guardar(rol);
        return "redirect:/roles";
    }
}
