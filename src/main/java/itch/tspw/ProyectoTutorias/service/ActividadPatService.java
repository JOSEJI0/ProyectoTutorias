package itch.tspw.ProyectoTutorias.service;

import org.springframework.stereotype.Service;
import itch.tspw.ProyectoTutorias.model.ActividadPat;
import itch.tspw.ProyectoTutorias.repository.ActividadPatRepository;

import java.util.List;

@Service
public class ActividadPatService {
	
	private final ActividadPatRepository actividadRepository;

    public ActividadPatService(ActividadPatRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    public void guardar(ActividadPat actividad) {
        actividadRepository.save(actividad);
    }

    public void eliminar(Integer id) {
        actividadRepository.deleteById(id);
    }
    
    public List<ActividadPat> listarPorPat(Integer idPat) {
        return actividadRepository.findByPat_IdPatAndActivoTrueOrderBySemanaProgramadaAsc(idPat);
    }

    public boolean existeActividadEnSemana(Integer idPat, Integer semana) {
        return actividadRepository.existsByPat_IdPatAndSemanaProgramadaAndActivoTrue(idPat, semana);
    }

    public boolean existeActividadConTitulo(Integer idPat, String titulo) {
        return actividadRepository.existsByPat_IdPatAndTituloIgnoreCaseAndActivoTrue(idPat, titulo);
    }

    public void eliminarLogico(Integer idActividad) {
        ActividadPat act = actividadRepository.findById(idActividad)
            .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        act.setActivo(false);
        actividadRepository.save(act);
    }
}