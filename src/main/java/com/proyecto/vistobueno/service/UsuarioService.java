package com.proyecto.vistobueno.service;

import com.proyecto.vistobueno.model.UsuarioSistema;
import com.proyecto.vistobueno.repository.UsuarioSistemaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioSistemaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioSistemaRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioSistema> buscarUsuarios(String filtro, Boolean activo) {
        if ((filtro == null || filtro.isBlank()) && activo == null) {
            return usuarioRepository.findAll();
        }
        String busqueda = (filtro != null && !filtro.isBlank()) ? filtro.trim() : null;
        return usuarioRepository.buscarConFiltro(busqueda, activo);
    }

    public Optional<UsuarioSistema> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public void guardar(UsuarioSistema usuario, String rawPassword) {
        if (rawPassword != null && !rawPassword.isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(rawPassword.trim()));
        } else if (usuario.getId() != null) {
            usuarioRepository.findById(usuario.getId()).ifPresent(existente -> {
                usuario.setPasswordHash(existente.getPasswordHash());
            });
        }
        usuarioRepository.save(usuario);
    }
}
