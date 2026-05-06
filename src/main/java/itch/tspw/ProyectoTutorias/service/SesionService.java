package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SesionService {

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private GrupoTutoriaRepository grupoTutoriaRepository;

    @Autowired
    private ActividadPatRepository actividadPatRepository;

    public List<Sesion> obtenerSesionesPorGrupo(Integer idGrupo) {
        return sesionRepository.findByGrupo_IdGrupo(idGrupo);
    }
    
    public List<Sesion> obtenerSesionesPorTutor(Integer idTutor) {
        return sesionRepository.findByGrupo_Tutor_IdTutor(idTutor);
    }

    public List<Sesion> buscarActividadesPorFecha(LocalDate fecha) {
        return sesionRepository.findByFechaImparticion(fecha);
    }

    @Transactional
    public Sesion registrarAsistenciaCompleta(Integer idGrupo, Integer semana, Integer idActividad, List<Integer> idEstudiantesPresentes) {
        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
                
        ActividadPat actividad = actividadPatRepository.findById(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        Sesion nuevaSesion = new Sesion();
        nuevaSesion.setFechaImparticion(LocalDate.now());
        nuevaSesion.setSemanaNumero(semana);
        nuevaSesion.setEstatusRegistro("COMPLETADO");
        nuevaSesion.setGrupo(grupo);
        nuevaSesion.setActividad(actividad);
        
        // Guardamos y recuperamos el ID
        nuevaSesion = sesionRepository.save(nuevaSesion);

        for (Estudiante estudiante : grupo.getEstudiantes()) {
            Asistencia registro = new Asistencia();
            registro.setSesion(nuevaSesion);
            registro.setEstudiante(estudiante);
            
            boolean asistio = idEstudiantesPresentes != null && idEstudiantesPresentes.contains(estudiante.getIdEstudiante());
            registro.setPresente(asistio);
            
            asistenciaRepository.save(registro);
        }
        
        return nuevaSesion;
    }

    public long contarSesionesPorTutor(Integer idTutor) {
        if (idTutor == null) return 0;
        return sesionRepository.countByGrupo_Tutor_IdTutor(idTutor);
    }
}