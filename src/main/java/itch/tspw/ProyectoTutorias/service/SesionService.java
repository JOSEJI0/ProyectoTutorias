package itch.tspw.ProyectoTutorias.service;

import org.springframework.transaction.annotation.Transactional;

import itch.tspw.ProyectoTutorias.model.ActividadPat;
import itch.tspw.ProyectoTutorias.model.Asistencia;
import itch.tspw.ProyectoTutorias.model.Estudiante;
import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.model.Sesion;
import itch.tspw.ProyectoTutorias.repository.ActividadPatRepository;
import itch.tspw.ProyectoTutorias.repository.AsistenciaRepository;
import itch.tspw.ProyectoTutorias.repository.GrupoTutoriaRepository;
import itch.tspw.ProyectoTutorias.repository.SesionRepository;
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

    // Obtener sesiones de un grupo
    public List<Sesion> obtenerSesionesPorGrupo(Integer idGrupo) {
        return sesionRepository.findByGrupo_IdGrupo(idGrupo);
    }
    
    // Obtener sesiones de un tutor
    public List<Sesion> obtenerSesionesPorTutor(Integer idTutor) {
        return sesionRepository.findByGrupo_Tutor_IdTutor(idTutor);
    }

    // Buscar actividades por fecha
    public List<Sesion> buscarActividadesPorFecha(LocalDate fecha) {
        return sesionRepository.findByFechaImparticion(fecha);
    }

    // Registra asistencias para todos los alumnos de un grupo en una sesión
    @Transactional
    public void registrarAsistenciaCompleta(Integer idGrupo, Integer semana, Integer idActividad, List<Integer> idEstudiantesPresentes) {
        
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
        
        nuevaSesion = sesionRepository.save(nuevaSesion);

        for (Estudiante estudiante : grupo.getEstudiantes()) {
            Asistencia registro = new Asistencia();
            registro.setSesion(nuevaSesion);
            registro.setEstudiante(estudiante);
            
            boolean asistio = idEstudiantesPresentes != null && idEstudiantesPresentes.contains(estudiante.getIdEstudiante());
            registro.setPresente(asistio);
            
            asistenciaRepository.save(registro);
        }
    }
}