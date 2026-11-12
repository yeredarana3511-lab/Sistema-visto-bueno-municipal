package com.proyecto.vistobueno.repositories;

import com.proyecto.vistobueno.entities.DiaInhabil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DiaInhabilRepository extends JpaRepository<DiaInhabil, LocalDate> {
}
