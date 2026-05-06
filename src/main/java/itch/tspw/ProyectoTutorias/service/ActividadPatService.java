package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActividadPatService {
    @Autowired
    private ActividadPatRepository actividadRepository;


    public void guardar(ActividadPat actividad) {
        actividadRepository.save(actividad);
    }

    public void eliminar(Integer id) {
        actividadRepository.deleteById(id);
    }
    
 // 1. Actualiza los llamados a los métodos nuevos del repositorio
    public List<ActividadPat> listarPorPat(Integer idPat) {
        return actividadRepository.findByPat_IdPatAndActivoTrueOrderBySemanaProgramadaAsc(idPat);
    }

    public boolean existeActividadEnSemana(Integer idPat, Integer semana) {
        return actividadRepository.existsByPat_IdPatAndSemanaProgramadaAndActivoTrue(idPat, semana);
    }

    public boolean existeActividadConTitulo(Integer idPat, String titulo) {
        return actividadRepository.existsByPat_IdPatAndTituloIgnoreCaseAndActivoTrue(idPat, titulo);
    }

    // 2. CAMBIA el borrado físico por LÓGICO
    public void eliminarLogico(Integer idActividad) {
        ActividadPat act = actividadRepository.findById(idActividad)
            .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        act.setActivo(false);
        actividadRepository.save(act);
    }
}