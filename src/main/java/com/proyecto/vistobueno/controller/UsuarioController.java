package com.proyecto.vistobueno.controller;

import com.proyecto.vistobueno.entities.Usuario;
import com.proyecto.vistobueno.model.UsuarioSistema;
import com.proyecto.vistobueno.repositories.UsuarioRepository;
import com.proyecto.vistobueno.repository.UsuarioSistemaRepository;
import com.proyecto.vistobueno.service.RolService;
import com.proyecto.vistobueno.service.UsuarioService;
import com.proyecto.vistobueno.services.BitacoraAuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final BitacoraAuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioSistemaRepository usuarioSistemaRepository;

    public UsuarioController(UsuarioService usuarioService,
                             RolService rolService,
                             BitacoraAuditoriaService auditoriaService,
                             UsuarioRepository usuarioRepository,
                             UsuarioSistemaRepository usuarioSistemaRepository) {
        this.usuarioService = usuarioService;
        this.rolService = rolService;
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            @RequestParam(name = "activo", required = false) Boolean activo,
            Model model,
            Authentication auth) {
        model.addAttribute("usuarios", usuarioService.buscarUsuarios(filtro, activo));
        model.addAttribute("filtro", filtro);
        model.addAttribute("activo", activo);
        cargarUsuarioActual(model, auth);
        return "usuarios/listado";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model, Authentication auth) {
        model.addAttribute("usuario", new UsuarioSistema());
        model.addAttribute("roles", rolService.listarTodos());
        cargarUsuarioActual(model, auth);
        return "usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable("id") Long id, Model model, Authentication auth) {
        UsuarioSistema usuario = usuarioService.buscarPorId(id).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolService.listarTodos());
        cargarUsuarioActual(model, auth);
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("usuario") UsuarioSistema usuario,
                          @RequestParam(name = "password", required = false) String password,
                          HttpServletRequest request,
                          Authentication auth,
                          RedirectAttributes redirectAttributes) {
        try {
            String anterior = "Nuevo Usuario";
            if (usuario.getId() != null) {
                Optional<UsuarioSistema> usrPrevio = usuarioService.buscarPorId(usuario.getId());
                if (usrPrevio.isPresent()) {
                    UsuarioSistema p = usrPrevio.get();
                    anterior = "No. Empleado: " + p.getNumeroEmpleado() + ", Nombre: " + p.getNombre() +
                               ", Correo: " + p.getCorreo() + ", Rol: " + (p.getRol() != null ? p.getRol().getNombre() : "Sin Rol") +
                               ", Activo: " + p.isActivo();
                }
            }

            usuarioService.guardar(usuario, password);

            String nuevo = "No. Empleado: " + usuario.getNumeroEmpleado() + ", Nombre: " + usuario.getNombre() +
                           ", Correo: " + usuario.getCorreo() + ", Rol: " + (usuario.getRol() != null ? usuario.getRol().getNombre() : "Sin Rol") +
                           ", Activo: " + usuario.isActivo();

            Usuario usuarioResponsable = (auth != null)
                    ? usuarioRepository.findByNumeroEmpleado(auth.getName()).orElse(null)
                    : null;

            auditoriaService.registrar(
                    usuarioResponsable,
                    "USUARIOS",
                    (usuario.getId() != null ? "MODIFICAR_USUARIO" : "CREAR_USUARIO"),
                    "Usuario: " + usuario.getNumeroEmpleado() + " | [Anterior: " + anterior + "] -> [Nuevo: " + nuevo + "]",
                    request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario '" + usuario.getNumeroEmpleado() + "' guardado y auditado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el usuario: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    private void cargarUsuarioActual(Model model, Authentication auth) {
        if (auth != null) {
            usuarioSistemaRepository.findByNumeroEmpleado(auth.getName())
                    .ifPresent(u -> model.addAttribute("usuarioActual", u));
        }
    }
}
