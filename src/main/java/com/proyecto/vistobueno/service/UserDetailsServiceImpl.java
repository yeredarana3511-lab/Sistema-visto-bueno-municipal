package com.proyecto.vistobueno.service;

import com.proyecto.vistobueno.model.UsuarioSistema;
import com.proyecto.vistobueno.repository.UsuarioSistemaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioSistemaRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioSistemaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuarioSistema usuario = usuarioRepository.findByNumeroEmpleado(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (usuario.getRol() != null) {
            String claveRol = usuario.getRol().getClave();
            if (claveRol != null && !claveRol.isBlank()) {
                authorities.add(new SimpleGrantedAuthority(claveRol));
                if (!claveRol.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + claveRol));
                }
            }
            if (usuario.getRol().getPermisos() != null) {
                for (String permiso : usuario.getRol().getPermisos()) {
                    if (permiso != null && !permiso.isBlank()) {
                        authorities.add(new SimpleGrantedAuthority(permiso.trim()));
                    }
                }
            }
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        String passwordHash = (usuario.getPasswordHash() != null) ? usuario.getPasswordHash() : "";

        return new User(
                usuario.getNumeroEmpleado(),
                passwordHash,
                usuario.isActivo(),
                true,
                true,
                true,
                authorities
        );
    }
}
