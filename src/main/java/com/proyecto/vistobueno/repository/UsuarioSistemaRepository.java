package com.proyecto.vistobueno.repository;

import com.proyecto.vistobueno.model.UsuarioSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, Long> {

    Optional<UsuarioSistema> findByNumeroEmpleado(String numeroEmpleado);

    @Query("SELECT u FROM UsuarioSistema u WHERE " +
            "(:busqueda IS NULL OR :busqueda = '' OR " +
            " LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            " LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            " LOWER(u.numeroEmpleado) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            " LOWER(u.correo) LIKE LOWER(CONCAT('%', :busqueda, '%'))) AND " +
            "(:activo IS NULL OR u.activo = :activo)")
    List<UsuarioSistema> buscarConFiltro(@Param("busqueda") String busqueda, @Param("activo") Boolean activo);

    boolean existsByNumeroEmpleado(String numeroEmpleado);
    boolean existsByCorreo(String correo);
}
