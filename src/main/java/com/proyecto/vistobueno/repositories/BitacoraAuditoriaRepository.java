package com.proyecto.vistobueno.repositories;

import com.proyecto.vistobueno.entities.BitacoraAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraAuditoriaRepository extends JpaRepository<BitacoraAuditoria, Long> {

    @Query("SELECT b FROM BitacoraAuditoria b LEFT JOIN FETCH b.usuario ORDER BY b.fechaHora DESC")
    List<BitacoraAuditoria> findAllByOrderByFechaHoraDesc();
}
