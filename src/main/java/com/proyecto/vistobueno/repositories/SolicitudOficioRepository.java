package com.proyecto.vistobueno.repositories;

import com.proyecto.vistobueno.entities.SolicitudOficio;
import com.proyecto.vistobueno.entities.SolicitudOficioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudOficioRepository extends JpaRepository<SolicitudOficio, SolicitudOficioId> {

    Optional<SolicitudOficio> findByOficioId(Long oficioId);

    List<SolicitudOficio> findByFolioSolicitud(String folioSolicitud);
}
