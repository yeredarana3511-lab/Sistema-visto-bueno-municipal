package com.proyecto.vistobueno.controllers;

import com.proyecto.vistobueno.entities.DiaInhabil;
import com.proyecto.vistobueno.services.DiaInhabilService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/dias-inhabiles")
public class DiaInhabilController {

    private final DiaInhabilService service;
    private final com.proyecto.vistobueno.repository.UsuarioSistemaRepository usuarioRepository;

    public DiaInhabilController(DiaInhabilService service, com.proyecto.vistobueno.repository.UsuarioSistemaRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listar(Model model, org.springframework.security.core.Authentication auth) {
        model.addAttribute("diasInhabiles", service.listarTodos());
        model.addAttribute("nuevoDia", new DiaInhabil());
        model.addAttribute("fechaHoy", LocalDate.now());
        if (auth != null) {
            usuarioRepository.findByNumeroEmpleado(auth.getName())
                    .ifPresent(u -> model.addAttribute("usuarioActual", u));
        }
        return "dias_inhabiles/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("nuevoDia") DiaInhabil diaInhabil, RedirectAttributes redirectAttributes) {
        try {
            if (diaInhabil.getFecha() == null) {
                redirectAttributes.addFlashAttribute("mensajeError", "La fecha del día inhábil es obligatoria.");
                return "redirect:/dias-inhabiles";
            }
            if (diaInhabil.getDescripcion() == null || diaInhabil.getDescripcion().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("mensajeError", "La descripción o motivo es obligatorio.");
                return "redirect:/dias-inhabiles";
            }
            service.guardar(diaInhabil);
            redirectAttributes.addFlashAttribute("mensajeExito", "Día inhábil (" + diaInhabil.getFecha() + ") registrado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar día inhábil: " + e.getMessage());
        }
        return "redirect:/dias-inhabiles";
    }

    @GetMapping("/eliminar/{fecha}")
    public String eliminar(@PathVariable("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                           RedirectAttributes redirectAttributes) {
        try {
            service.eliminar(fecha);
            redirectAttributes.addFlashAttribute("mensajeExito", "Día inhábil (" + fecha + ") eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar día inhábil: " + e.getMessage());
        }
        return "redirect:/dias-inhabiles";
    }
}
