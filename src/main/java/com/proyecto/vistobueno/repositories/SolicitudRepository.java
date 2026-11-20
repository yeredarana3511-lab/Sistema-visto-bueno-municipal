package com.proyecto.vistobueno.repositories;

import com.proyecto.vistobueno.entities.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, String> {
    List<Solicitud> findAllByOrderByFechaRegistroDesc();
    List<Solicitud> findByEstadoOrderByFechaRegistroDesc(String estado);
}
