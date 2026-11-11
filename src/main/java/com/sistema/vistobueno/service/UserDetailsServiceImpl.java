package com.sistema.vistobueno.service;

import com.sistema.vistobueno.model.Usuario;
import com.sistema.vistobueno.repository.UsuarioRepository;
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

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNumeroEmpleado(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con número de empleado: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (usuario.getRol() != null) {
            String claveRol = usuario.getRol().getClave();
            if (claveRol != null && !claveRol.isBlank()) {
                authorities.add(new SimpleGrantedAuthority(claveRol));
                if (!claveRol.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + claveRol));
                }
            }

            // Mapear permisos granulares asociados al rol si están definidos
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

        // Obtener directamente el hash almacenado en la entidad (columna password_hash)
        String passwordHash = (usuario.getPasswordHash() != null) ? usuario.getPasswordHash() : "";

        return new User(
                usuario.getNumeroEmpleado(),
                passwordHash,
                usuario.isActivo(), // Si el usuario no está activo, la autenticación se deniega automáticamente
                true,               // accountNonExpired
                true,               // credentialsNonExpired
                true,               // accountNonLocked
                authorities
        );
    }
}
