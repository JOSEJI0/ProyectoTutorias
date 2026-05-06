package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EvidenciaService {

    @Autowired
    private EvidenciaSesionRepository evidenciaRepository;

    // Obtener evidencias pendientes de validación
    public List<EvidenciaSesion> obtenerEvidenciasPendientes() {
        return evidenciaRepository.findByEstatusValidacion("PENDIENTE");
    }

    // Validar (aprobar/rechazar) una evidencia y guardar notas
    public boolean validarEvidencia(Integer idEvidencia, String estatus, String notas) {
        Optional<EvidenciaSesion> evidenciaOpt = evidenciaRepository.findById(idEvidencia);
        
        if (evidenciaOpt.isPresent()) {
            EvidenciaSesion evidencia = evidenciaOpt.get();
            evidencia.setEstatusValidacion(estatus); 
            evidencia.setNotasCoordinador(notas);
            evidenciaRepository.save(evidencia);
            return true;
        }
        return false;
    }
}