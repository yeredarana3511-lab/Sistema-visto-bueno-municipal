package com.sistema.vistobueno.config;

import com.sistema.vistobueno.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Protección CSRF habilitada por defecto
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos públicos
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                        // Acceso público a /login
                        .requestMatchers("/login", "/login?error", "/login?logout").permitAll()
                        // Pantalla de Inicio accesible para cualquier usuario autenticado (Admin, Cajero, etc.)
                        .requestMatchers("/", "/inicio").authenticated()
                        // Restricción de operaciones de gestión de usuarios y roles SOLO a Administradores
                        .requestMatchers("/usuarios/**", "/roles/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        // Redirigir SIEMPRE a la pantalla de Inicio para ambos roles al iniciar sesión
                        .defaultSuccessUrl("/inicio", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        // Si un rol no autorizado intenta entrar por URL directa a /usuarios, se le devuelve al inicio con aviso
                        .accessDeniedPage("/inicio?denegado=true")
                );

        return http.build();
    }
}
