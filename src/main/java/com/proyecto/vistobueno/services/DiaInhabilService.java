package com.proyecto.vistobueno.services;

import com.proyecto.vistobueno.entities.DiaInhabil;
import com.proyecto.vistobueno.repositories.DiaInhabilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DiaInhabilService {

    private final DiaInhabilRepository repository;

    public DiaInhabilService(DiaInhabilRepository repository) {
        this.repository = repository;
    }

    public List<DiaInhabil> listarTodos() {
        return repository.findAll();
    }

    @Transactional
    public DiaInhabil guardar(DiaInhabil diaInhabil) {
        return repository.save(diaInhabil);
    }

    @Transactional
    public void eliminar(LocalDate fecha) {
        repository.deleteById(fecha);
    }

    public boolean esDiaInhabil(LocalDate fecha) {
        return repository.existsById(fecha);
    }
}
