package com.proyecto.vistobueno.services;

import com.proyecto.vistobueno.entities.DiaInhabil;
import com.proyecto.vistobueno.repositories.DiaInhabilRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaInhabilServiceTest {

    @Mock
    private DiaInhabilRepository repository;

    @InjectMocks
    private DiaInhabilService service;

    @Test
    @DisplayName("Debe listar todos los días inhábiles")
    void testListarTodos() {
        DiaInhabil dia1 = new DiaInhabil(LocalDate.of(2026, 1, 1), "Año Nuevo");
        DiaInhabil dia2 = new DiaInhabil(LocalDate.of(2026, 2, 5), "Día de la Constitución");

        when(repository.findAll()).thenReturn(List.of(dia1, dia2));

        List<DiaInhabil> lista = service.listarTodos();
        assertEquals(2, lista.size());
        assertEquals("Año Nuevo", lista.get(0).getDescripcion());
    }

    @Test
    @DisplayName("Debe guardar un día inhábil")
    void testGuardar() {
        DiaInhabil dia = new DiaInhabil(LocalDate.of(2026, 5, 1), "Día del Trabajo");
        when(repository.save(dia)).thenReturn(dia);

        DiaInhabil guardado = service.guardar(dia);
        assertNotNull(guardado);
        assertEquals(LocalDate.of(2026, 5, 1), guardado.getFecha());
    }

    @Test
    @DisplayName("Debe eliminar un día inhábil por su fecha")
    void testEliminar() {
        LocalDate fecha = LocalDate.of(2026, 11, 20);
        doNothing().when(repository).deleteById(fecha);

        service.eliminar(fecha);
        verify(repository, times(1)).deleteById(fecha);
    }

    @Test
    @DisplayName("Debe verificar si una fecha es día inhábil")
    void testEsDiaInhabil() {
        LocalDate fecha = LocalDate.of(2026, 12, 25);
        when(repository.existsById(fecha)).thenReturn(true);

        assertTrue(service.esDiaInhabil(fecha));
        verify(repository, times(1)).existsById(fecha);
    }
}
