package com.sistema.vistobueno.service;

import com.sistema.vistobueno.model.Usuario;
import com.sistema.vistobueno.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> buscarUsuarios(String filtro, Boolean activo) {
        if ((filtro == null || filtro.isBlank()) && activo == null) {
            return usuarioRepository.findAll();
        }
        String busqueda = (filtro != null && !filtro.isBlank()) ? filtro.trim() : null;
        return usuarioRepository.buscarConFiltro(busqueda, activo);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public void guardar(Usuario usuario, String rawPassword) {
        if (rawPassword != null && !rawPassword.isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(rawPassword.trim()));
        } else if (usuario.getId() != null) {
            // Edición de usuario sin cambio de contraseña:
            // Recuperar el hash existente de la base de datos para no sobreescribirlo
            usuarioRepository.findById(usuario.getId()).ifPresent(existente -> {
                usuario.setPasswordHash(existente.getPasswordHash());
            });
        }
        usuarioRepository.save(usuario);
    }
}
