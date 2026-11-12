package com.proyecto.vistobueno.services;

import com.proyecto.vistobueno.entities.BitacoraAuditoria;
import com.proyecto.vistobueno.entities.Usuario;
import com.proyecto.vistobueno.repositories.BitacoraAuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BitacoraAuditoriaService {

    private final BitacoraAuditoriaRepository repository;

    public BitacoraAuditoriaService(BitacoraAuditoriaRepository repository) {
        this.repository = repository;
    }

    public List<BitacoraAuditoria> listarTodas() {
        return repository.findAllByOrderByFechaHoraDesc();
    }

    @Transactional
    public void registrar(BitacoraAuditoria bitacora) {
        if (bitacora.getFechaHora() == null) {
            bitacora.setFechaHora(LocalDateTime.now());
        }
        repository.save(bitacora);
    }

    @Transactional
    public void registrar(Usuario usuario, String modulo, String accion, String detalle, String ipOrigen) {
        BitacoraAuditoria bitacora = new BitacoraAuditoria();
        bitacora.setUsuario(usuario);
        bitacora.setModulo(modulo);
        bitacora.setAccion(accion);
        bitacora.setDetalle(detalle);
        bitacora.setIpOrigen(ipOrigen);
        bitacora.setFechaHora(LocalDateTime.now());
        repository.save(bitacora);
    }
}
