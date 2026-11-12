package com.proyecto.vistobueno.repositories;

import com.proyecto.vistobueno.entities.Dependencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DependenciaRepository extends JpaRepository<Dependencia, Long> {
}