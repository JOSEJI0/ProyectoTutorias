package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatService {
	
	private final PatRepository patRepository;

    public PatService(PatRepository patRepository) {
        this.patRepository = patRepository;
    }

    public PatInstitucional guardar(PatInstitucional pat) {
        return patRepository.save(pat);
    }

    public void eliminar(Integer idPat) {
        patRepository.deleteById(idPat);
    }
    
    public List<PatInstitucional> listarTodos() {
        return patRepository.findByActivoTrueOrderByIdPatDesc();
    }
    
    public PatInstitucional obtenerPorId(Integer idPat) {
        return patRepository.findById(idPat).orElseThrow(() -> new RuntimeException("PAT no encontrado"));
    }

    public boolean existePatParaCarreraEnPeriodo(Integer idPeriodo, Integer idCarrera) {
        return patRepository.findByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrue(idPeriodo, idCarrera).isPresent();
    }

    public void eliminarLogico(Integer idPat) {
    	PatInstitucional pat = obtenerPorId(idPat);
        pat.setActivo(false);
        patRepository.save(pat);
    }
}