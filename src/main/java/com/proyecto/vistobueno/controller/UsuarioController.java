package com.proyecto.vistobueno.controller;

import com.proyecto.vistobueno.model.UsuarioSistema;
import com.proyecto.vistobueno.service.RolService;
import com.proyecto.vistobueno.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolService rolService;

    public UsuarioController(UsuarioService usuarioService, RolService rolService) {
        this.usuarioService = usuarioService;
        this.rolService = rolService;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            @RequestParam(name = "activo", required = false) Boolean activo,
            Model model) {
        model.addAttribute("usuarios", usuarioService.buscarUsuarios(filtro, activo));
        model.addAttribute("filtro", filtro);
        model.addAttribute("activo", activo);
        return "usuarios/listado";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("usuario", new UsuarioSistema());
        model.addAttribute("roles", rolService.listarTodos());
        return "usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable("id") Long id, Model model) {
        UsuarioSistema usuario = usuarioService.buscarPorId(id).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolService.listarTodos());
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("usuario") UsuarioSistema usuario, @RequestParam(name = "password", required = false) String password) {
        usuarioService.guardar(usuario, password);
        return "redirect:/usuarios";
    }
}
