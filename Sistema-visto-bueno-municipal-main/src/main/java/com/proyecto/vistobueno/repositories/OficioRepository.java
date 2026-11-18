package com.proyecto.vistobueno.repositories;

import com.proyecto.vistobueno.entities.Oficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OficioRepository extends JpaRepository<Oficio, Long> {
}