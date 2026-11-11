package com.sistema.vistobueno.repository;

import com.sistema.vistobueno.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método para autenticación en Spring Security
    Optional<Usuario> findByNumeroEmpleado(String numeroEmpleado);

    // Método para búsquedas y filtros de la tabla de usuarios
    @Query("SELECT u FROM Usuario u WHERE " +
            "(:busqueda IS NULL OR :busqueda = '' OR " +
            " LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            " LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            " LOWER(u.numeroEmpleado) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            " LOWER(u.correo) LIKE LOWER(CONCAT('%', :busqueda, '%'))) AND " +
            "(:activo IS NULL OR u.activo = :activo)")
    List<Usuario> buscarConFiltro(@Param("busqueda") String busqueda, @Param("activo") Boolean activo);

    // Verificaciones adicionales
    boolean existsByNumeroEmpleado(String numeroEmpleado);
    boolean existsByCorreo(String correo);
}