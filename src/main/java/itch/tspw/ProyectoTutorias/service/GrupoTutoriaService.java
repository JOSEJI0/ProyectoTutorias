package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrupoTutoriaService {

    @Autowired
    private GrupoTutoriaRepository grupoTutoriaRepository;
    
    @Autowired
    private EstudianteRepository estudianteRepository;
    
    @Autowired
    private SesionRepository sesionRepository;
    
    public List<GrupoTutoria> listarTodos() {
        return grupoTutoriaRepository.findAll();
    }

    @Transactional
    public GrupoTutoria asignarGrupo(GrupoTutoria grupo, List<Integer> idEstudiantes) {
        GrupoTutoria grupoGuardado;

        if (grupo.getIdGrupo() != null) {
            grupoGuardado = grupoTutoriaRepository.findById(grupo.getIdGrupo()).orElse(grupo);
            // Actualizamos los campos vitales por si es una edición
            grupoGuardado.setNombreGrupo(grupo.getNombreGrupo());
            grupoGuardado.setCarrera(grupo.getCarrera());
            grupoGuardado.setSemestre(grupo.getSemestre());
            grupoGuardado.setHorario(grupo.getHorario());
            grupoGuardado.setTutor(grupo.getTutor());
            grupoGuardado = grupoTutoriaRepository.save(grupoGuardado);
        } else {
            grupoGuardado = grupoTutoriaRepository.save(grupo);
        }

        if (idEstudiantes != null && !idEstudiantes.isEmpty()) {
            List<Estudiante> estudiantesSeleccionados = estudianteRepository.findAllById(idEstudiantes);
            for (Estudiante estudiante : estudiantesSeleccionados) {
                estudiante.setGrupo(grupoGuardado);
            }
            estudianteRepository.saveAll(estudiantesSeleccionados);
        }
        
        return grupoGuardado; // Devolvemos el objeto con su ID real de la BD
    }
    
    @Transactional
    public void eliminarGrupoSeguro(Integer idGrupo) {
        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));

        // 1. Desvincular a los estudiantes (Se quedan sin grupo, pero NO se borran del sistema)
        List<Estudiante> estudiantes = grupo.getEstudiantes();
        if (estudiantes != null && !estudiantes.isEmpty()) {
            for (Estudiante e : estudiantes) {
                e.setGrupo(null);
            }
            estudianteRepository.saveAll(estudiantes);
        }

        // 2. En lugar de borrar las sesiones dependientes, las marcamos como Canceladas
        List<Sesion> sesiones = sesionRepository.findByGrupo_IdGrupo(idGrupo);
        if (!sesiones.isEmpty()) {
            for (Sesion s : sesiones) {
                s.setEstatusRegistro("Cancelada");
            }
            sesionRepository.saveAll(sesiones);
        }

        // 3. BORRADO LÓGICO: Cambiamos el estado del grupo a inactivo y guardamos
        grupo.setActivo(false);
        grupoTutoriaRepository.save(grupo);
    }
    
    // Obtener un grupo por ID
    public GrupoTutoria obtenerPorId(Integer idGrupo) {
        return grupoTutoriaRepository.findById(idGrupo).orElse(null);
    }
    
    // Obtener grupos activos asignados a un tutor
    public List<GrupoTutoria> obtenerGruposActivosPorTutor(Integer idTutor) {
        return grupoTutoriaRepository.findByTutor_IdTutorAndPeriodo_EstatusActivoTrueAndActivoTrue(idTutor);
    }

    // Obtener grupos de un periodo
    public List<GrupoTutoria> buscarTutoriasPorPeriodo(Integer idPeriodo) {
        return grupoTutoriaRepository.findByPeriodo_IdPeriodoAndActivoTrue(idPeriodo);
    }

    // Obtener historial de tutorías de un estudiante
    public List<GrupoTutoria> buscarHistorialTutoriasDeEstudiante(Integer idEstudiante) {
        return grupoTutoriaRepository.findByEstudiantes_IdEstudianteAndActivoTrue(idEstudiante);
    }
    
    // Obtener grupos por estatus del periodo
    public List<GrupoTutoria> listarGruposPorEstatus(boolean activo) {
        return grupoTutoriaRepository.findByPeriodo_EstatusActivoAndActivoTrue(activo);
    }
}