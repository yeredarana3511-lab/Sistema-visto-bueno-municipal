package com.proyecto.vistobueno.services;

import com.proyecto.vistobueno.entities.Dependencia;
import com.proyecto.vistobueno.entities.Oficio;
import com.proyecto.vistobueno.entities.SolicitudOficio;
import com.proyecto.vistobueno.repositories.DependenciaRepository;
import com.proyecto.vistobueno.repositories.OficioRepository;
import com.proyecto.vistobueno.repositories.SolicitudOficioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OficioServiceTest {

    @Mock
    private OficioRepository oficioRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private DependenciaRepository dependenciaRepository;

    @Mock
    private SolicitudOficioRepository solicitudOficioRepository;

    @InjectMocks
    private OficioService oficioService;

    private Dependencia dependenciaEcologia;

    @BeforeEach
    void setUp() {
        dependenciaEcologia = new Dependencia("Ecología", "ecologia@municipio.gob.mx", "ECO");
        dependenciaEcologia.setId(1L);
        dependenciaEcologia.setSecuenciaActual(5);
    }

    @Test
    @DisplayName("Debe calcular correctamente los días hábiles excluyendo sábados y domingos")
    void testCalcularDiasHabiles() {
        // Lunes 2026-11-09 a Viernes 2026-11-13 (5 días hábiles)
        LocalDate lunes = LocalDate.of(2026, 11, 9);
        LocalDate viernes = LocalDate.of(2026, 11, 13);
        assertEquals(5, oficioService.calcularDiasHabiles(lunes, viernes));

        // Viernes 2026-11-13 a Lunes 2026-11-16 (2 días hábiles: viernes y lunes)
        LocalDate siguienteLunes = LocalDate.of(2026, 11, 16);
        assertEquals(2, oficioService.calcularDiasHabiles(viernes, siguienteLunes));

        // Sábado a Domingo (0 días hábiles)
        LocalDate sabado = LocalDate.of(2026, 11, 14);
        LocalDate domingo = LocalDate.of(2026, 11, 15);
        assertEquals(0, oficioService.calcularDiasHabiles(sabado, domingo));

        // Casos nulos o fechas invertidas
        assertEquals(0, oficioService.calcularDiasHabiles(null, viernes));
        assertEquals(0, oficioService.calcularDiasHabiles(viernes, null));
        assertEquals(0, oficioService.calcularDiasHabiles(viernes, lunes));
    }

    @Test
    @DisplayName("Debe determinar el color correcto del semáforo según las reglas del negocio")
    void testDeterminarColorSemaforo() {
        // Verde (1-20 días)
        assertEquals("VERDE", oficioService.determinarColorSemaforo(1));
        assertEquals("VERDE", oficioService.determinarColorSemaforo(10));
        assertEquals("VERDE", oficioService.determinarColorSemaforo(20));

        // Amarillo (21-29 días)
        assertEquals("AMARILLO", oficioService.determinarColorSemaforo(21));
        assertEquals("AMARILLO", oficioService.determinarColorSemaforo(25));
        assertEquals("AMARILLO", oficioService.determinarColorSemaforo(29));

        // Rojo (30-37 días)
        assertEquals("ROJO", oficioService.determinarColorSemaforo(30));
        assertEquals("ROJO", oficioService.determinarColorSemaforo(35));
        assertEquals("ROJO", oficioService.determinarColorSemaforo(37));

        // Morado (38+ días)
        assertEquals("MORADO", oficioService.determinarColorSemaforo(38));
        assertEquals("MORADO", oficioService.determinarColorSemaforo(60));

        // Menor a 1
        assertEquals("SIN_ESTADO", oficioService.determinarColorSemaforo(0));
    }

    @Test
    @DisplayName("Debe generar y guardar un nuevo Oficio vinculado a una SolicitudOficio")
    void testGenerarYGuardarOficio() {
        when(dependenciaRepository.findById(1L)).thenReturn(Optional.of(dependenciaEcologia));
        when(dependenciaRepository.save(any(Dependencia.class))).thenReturn(dependenciaEcologia);

        when(oficioRepository.save(any(Oficio.class))).thenAnswer(invocation -> {
            Oficio of = invocation.getArgument(0);
            of.setId(100L);
            return of;
        });

        when(solicitudOficioRepository.save(any(SolicitudOficio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Oficio resultado = oficioService.generarYGuardarOficio(
                "SOL-2026-999",
                1L,
                "Solicitud de dictamen ambiental",
                "contacto@ciudadano.mx",
                LocalDate.of(2026, 11, 1)
        );

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertTrue(resultado.getFolioOficio().startsWith("ECO-"));
        assertEquals("Solicitud de dictamen ambiental", resultado.getContenidoDoc());
        assertFalse(resultado.getEstadoCierre());

        // Verificar que el vínculo intermedio SolicitudOficio se guardó correctamente
        ArgumentCaptor<SolicitudOficio> vinculoCaptor = ArgumentCaptor.forClass(SolicitudOficio.class);
        verify(solicitudOficioRepository).save(vinculoCaptor.capture());
        SolicitudOficio vinculoGuardado = vinculoCaptor.getValue();
        assertEquals("SOL-2026-999", vinculoGuardado.getFolioSolicitud());
        assertEquals(100L, vinculoGuardado.getOficioId());

        // Verificar intento de envío de correo
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debe inicializar las 3 dependencias oficiales cuando la tabla está vacía")
    void testInicializarDependencias() {
        when(dependenciaRepository.count()).thenReturn(0L);

        String mensaje = oficioService.inicializarDependencias();

        assertTrue(mensaje.contains("se registraron con éxito"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Dependencia>> captor = ArgumentCaptor.forClass(List.class);
        verify(dependenciaRepository).saveAll(captor.capture());

        List<Dependencia> guardadas = captor.getValue();
        assertEquals(3, guardadas.size());

        List<String> nombres = guardadas.stream().map(Dependencia::getNombre).toList();
        assertTrue(nombres.contains("Ecología"));
        assertTrue(nombres.contains("Inspección y Reglamentos"));
        assertTrue(nombres.contains("Protección Civil y Bomberos"));
    }
}
