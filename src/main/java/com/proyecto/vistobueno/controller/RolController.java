package com.proyecto.vistobueno.controller;

import com.proyecto.vistobueno.entities.Usuario;
import com.proyecto.vistobueno.model.Rol;
import com.proyecto.vistobueno.model.UsuarioSistema;
import com.proyecto.vistobueno.repositories.UsuarioRepository;
import com.proyecto.vistobueno.repository.UsuarioSistemaRepository;
import com.proyecto.vistobueno.service.RolService;
import com.proyecto.vistobueno.services.BitacoraAuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;
    private final BitacoraAuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioSistemaRepository usuarioSistemaRepository;

    // Catálogo base de permisos por módulo según el flujo operativo del sistema
    private static final List<String> PERMISOS_DISPONIBLES = Arrays.asList(
            // Módulo de Mantenimiento y Administración (Administrador)
            "USUARIOS:GESTIONAR", "ROLES:GESTIONAR", "SISTEMA:MANTENIMIENTO",
            // Módulo de Captura y OCR (Secretaria)
            "SOLICITUDES:CONSULTAR", "SOLICITUDES:CREAR", "SOLICITUDES:EDITAR", "SOLICITUDES:CONFIRMAR_OCR", "SOLICITUDES:ENVIAR",
            // Módulo de Visto Bueno (Supervisor)
            "VISTO_BUENO:APROBAR_RECHAZAR",
            // Módulo de Oficios y Plantillas
            "OFICIOS:CONSULTAR", "OFICIOS:GENERAR", "OFICIOS:ENVIAR",
            // Módulo de Seguimiento y Auditoría
            "SEGUIMIENTO:CONSULTAR", "SEGUIMIENTO:REGISTRAR_RESPUESTA"
    );

    public RolController(RolService rolService,
                         BitacoraAuditoriaService auditoriaService,
                         UsuarioRepository usuarioRepository,
                         UsuarioSistemaRepository usuarioSistemaRepository) {
        this.rolService = rolService;
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
    }

    @GetMapping
    public String listar(Model model, Authentication auth) {
        model.addAttribute("roles", rolService.listarTodos());
        cargarUsuarioActual(model, auth);
        return "roles/listado";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model, Authentication auth) {
        model.addAttribute("rol", new Rol());
        model.addAttribute("permisosDisponibles", PERMISOS_DISPONIBLES);
        cargarUsuarioActual(model, auth);
        return "roles/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable("id") Long id, Model model, Authentication auth) {
        Rol rol = rolService.buscarPorId(id).orElseThrow();
        model.addAttribute("rol", rol);
        model.addAttribute("permisosDisponibles", PERMISOS_DISPONIBLES);
        cargarUsuarioActual(model, auth);
        return "roles/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("rol") Rol rol,
                          HttpServletRequest request,
                          Authentication auth,
                          RedirectAttributes redirectAttributes) {
        try {
            String anterior = "Nuevo Rol";
            if (rol.getId() != null) {
                Optional<Rol> rolPrevio = rolService.buscarPorId(rol.getId());
                if (rolPrevio.isPresent()) {
                    anterior = "Clave: " + rolPrevio.get().getClave() + ", Nombre: " + rolPrevio.get().getNombre() +
                               ", Permisos: " + rolPrevio.get().getPermisos();
                }
            }

            rolService.guardar(rol);
            String nuevo = "Clave: " + rol.getClave() + ", Nombre: " + rol.getNombre() + ", Permisos: " + rol.getPermisos();

            Usuario usuarioResponsable = (auth != null)
                    ? usuarioRepository.findByNumeroEmpleado(auth.getName()).orElse(null)
                    : null;

            auditoriaService.registrar(
                    usuarioResponsable,
                    "ROLES",
                    (rol.getId() != null ? "MODIFICAR_ROL" : "CREAR_ROL"),
                    "Rol: " + rol.getNombre() + " | [Anterior: " + anterior + "] -> [Nuevo: " + nuevo + "]",
                    request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute("mensajeExito", "Rol '" + rol.getNombre() + "' guardado y auditado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el rol: " + e.getMessage());
        }
        return "redirect:/roles";
    }

    private void cargarUsuarioActual(Model model, Authentication auth) {
        if (auth != null) {
            usuarioSistemaRepository.findByNumeroEmpleado(auth.getName())
                    .ifPresent(u -> model.addAttribute("usuarioActual", u));
        }
    }
}
