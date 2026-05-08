package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EvidenciaService {

	private final EvidenciaSesionRepository evidenciaRepository;

    public EvidenciaService(EvidenciaSesionRepository evidenciaRepository) {
        this.evidenciaRepository = evidenciaRepository;
    }
    public List<EvidenciaSesion> obtenerEvidenciasPendientes() {
        return evidenciaRepository.findByEstatusValidacion("PENDIENTE");
    }

    public boolean validarEvidencia(Integer idEvidencia, String estatus, String notas) {
        return evidenciaRepository.findById(idEvidencia)
                .map(evidencia -> {
                    evidencia.setEstatusValidacion(estatus); 
                    evidencia.setNotasCoordinador(notas);
                    evidenciaRepository.save(evidencia);
                    return true;
                })
                .orElse(false);
    }
}