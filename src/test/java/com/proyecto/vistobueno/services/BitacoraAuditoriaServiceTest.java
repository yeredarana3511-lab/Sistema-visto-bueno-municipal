package com.proyecto.vistobueno.services;

import com.proyecto.vistobueno.entities.BitacoraAuditoria;
import com.proyecto.vistobueno.entities.Usuario;
import com.proyecto.vistobueno.repositories.BitacoraAuditoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BitacoraAuditoriaServiceTest {

    @Mock
    private BitacoraAuditoriaRepository repository;

    @InjectMocks
    private BitacoraAuditoriaService service;

    @Test
    @DisplayName("Debe listar todos los registros ordenados por fecha y hora descendente")
    void testListarTodas() {
        BitacoraAuditoria b1 = new BitacoraAuditoria();
        b1.setId(1L);
        b1.setModulo("OFICIOS");

        when(repository.findAllByOrderByFechaHoraDesc()).thenReturn(List.of(b1));

        List<BitacoraAuditoria> lista = service.listarTodas();
        assertEquals(1, lista.size());
        assertEquals("OFICIOS", lista.get(0).getModulo());
    }

    @Test
    @DisplayName("Debe registrar un objeto BitacoraAuditoria asignando fechaHora si es nula")
    void testRegistrarObjeto() {
        BitacoraAuditoria log = new BitacoraAuditoria();
        log.setModulo("SOLICITUDES");
        log.setAccion("VALIDAR");

        service.registrar(log);

        ArgumentCaptor<BitacoraAuditoria> captor = ArgumentCaptor.forClass(BitacoraAuditoria.class);
        verify(repository).save(captor.capture());
        assertNotNull(captor.getValue().getFechaHora());
        assertEquals("SOLICITUDES", captor.getValue().getModulo());
    }

    @Test
    @DisplayName("Debe registrar un evento con parámetros individuales")
    void testRegistrarParametros() {
        Usuario usuario = new Usuario("EMP-100", "Carlos Perez");
        service.registrar(usuario, "DEPENDENCIAS", "ACTUALIZAR", "Se editó la dependencia", "192.168.1.50");

        ArgumentCaptor<BitacoraAuditoria> captor = ArgumentCaptor.forClass(BitacoraAuditoria.class);
        verify(repository).save(captor.capture());
        BitacoraAuditoria guardado = captor.getValue();

        assertEquals(usuario, guardado.getUsuario());
        assertEquals("DEPENDENCIAS", guardado.getModulo());
        assertEquals("ACTUALIZAR", guardado.getAccion());
        assertEquals("Se editó la dependencia", guardado.getDetalle());
        assertEquals("192.168.1.50", guardado.getIpOrigen());
        assertNotNull(guardado.getFechaHora());
    }
}
