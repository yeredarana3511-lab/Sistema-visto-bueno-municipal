package com.sistema.vistobueno.repository;

import com.sistema.vistobueno.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    boolean existsByClave(String clave);
}