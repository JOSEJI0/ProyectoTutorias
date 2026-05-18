package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SesionService {

    private final SesionRepository sesionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final GrupoTutoriaRepository grupoTutoriaRepository;
    private final ActividadPatRepository actividadPatRepository;

    public SesionService(SesionRepository sesionRepository, 
                         AsistenciaRepository asistenciaRepository,
                         GrupoTutoriaRepository grupoTutoriaRepository, 
                         ActividadPatRepository actividadPatRepository) {
        this.sesionRepository = sesionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.grupoTutoriaRepository = grupoTutoriaRepository;
        this.actividadPatRepository = actividadPatRepository;
    }

    @Transactional(readOnly = true)
    public List<Sesion> obtenerSesionesPorGrupo(Integer idGrupo) {
        return sesionRepository.findByGrupo_IdGrupo(idGrupo);
    }
    @Transactional(readOnly = true)
    public List<Sesion> obtenerSesionesPorTutor(Integer idTutor) {
        return sesionRepository.findByGrupo_Tutor_IdTutor(idTutor);
    }

    @Transactional(readOnly = true)
    public List<Sesion> buscarActividadesPorFecha(LocalDate fecha) {
        return sesionRepository.findByFechaImparticion(fecha);
    }

    @Transactional
    public Sesion registrarAsistenciaCompleta(Integer idGrupo, Integer semana, Integer idActividad, List<Integer> idEstudiantesPresentes) {
        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado con ID: " + idGrupo));
                
        ActividadPat actividad = actividadPatRepository.findById(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + idActividad));
        
        Set<Integer> presentes = idEstudiantesPresentes == null ? Set.of() : new HashSet<>(idEstudiantesPresentes);

        Sesion nuevaSesion = new Sesion();
        nuevaSesion.setFechaImparticion(LocalDate.now());
        nuevaSesion.setSemanaNumero(semana);
        nuevaSesion.setEstatusRegistro("COMPLETADO");
        nuevaSesion.setGrupo(grupo);
        nuevaSesion.setActividad(actividad);
        final Sesion sesionGuardada = sesionRepository.save(nuevaSesion);

        List<Asistencia> listaAsistencia = new ArrayList<>();
        
        for (Estudiante estudiante : grupo.getEstudiantes()) {
            // SEGURIDAD: Se ignora y omite el pase de lista para alumnos que no se encuentran activos en el sistema
            if (!Boolean.TRUE.equals(estudiante.getActivo())) {
                continue;
            }

            Asistencia registro = new Asistencia();
            registro.setSesion(sesionGuardada);
            registro.setEstudiante(estudiante);
            
            boolean asistio = presentes.contains(estudiante.getIdEstudiante());
            registro.setPresente(asistio);            
            listaAsistencia.add(registro);
        }
        
        if (!listaAsistencia.isEmpty()) {
            asistenciaRepository.saveAll(listaAsistencia);
        }
        
        return sesionGuardada;
    }

    @Transactional(readOnly = true)
    public long contarSesionesPorTutor(Integer idTutor) {
        if (idTutor == null) return 0;
        return sesionRepository.countByGrupo_Tutor_IdTutor(idTutor);
    }
}