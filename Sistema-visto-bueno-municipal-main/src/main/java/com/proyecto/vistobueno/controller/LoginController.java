package com.proyecto.vistobueno.controller;

import com.proyecto.vistobueno.model.UsuarioSistema;
import com.proyecto.vistobueno.repository.UsuarioSistemaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class LoginController {

    private final UsuarioSistemaRepository usuarioRepository;

    public LoginController(UsuarioSistemaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/inicio";
    }

    @GetMapping("/inicio")
    public String inicio(Authentication authentication, Model model) {
        if (authentication != null) {
            String numeroEmpleado = authentication.getName();
            Optional<UsuarioSistema> usuarioOpt = usuarioRepository.findByNumeroEmpleado(numeroEmpleado);
            usuarioOpt.ifPresent(u -> model.addAttribute("usuarioActual", u));
        }
        return "inicio";
    }
}
