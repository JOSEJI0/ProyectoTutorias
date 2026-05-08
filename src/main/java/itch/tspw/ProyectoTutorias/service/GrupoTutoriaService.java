package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GrupoTutoriaService {

    private final GrupoTutoriaRepository grupoTutoriaRepository;
    private final EstudianteRepository estudianteRepository;
    private final SesionRepository sesionRepository;

    public GrupoTutoriaService(GrupoTutoriaRepository grupoTutoriaRepository,
                               EstudianteRepository estudianteRepository,
                               SesionRepository sesionRepository) {
        this.grupoTutoriaRepository = grupoTutoriaRepository;
        this.estudianteRepository = estudianteRepository;
        this.sesionRepository = sesionRepository;
    }

    @Transactional(readOnly = true)
    public List<GrupoTutoria> listarTodos() {
        return grupoTutoriaRepository.findAll();
    }

    @Transactional
    public GrupoTutoria asignarGrupo(GrupoTutoria grupo, List<Integer> idEstudiantes) {
        final GrupoTutoria grupoGuardado = grupoTutoriaRepository.save(grupo);

        if (idEstudiantes != null && !idEstudiantes.isEmpty()) {
            List<Estudiante> estudiantes = estudianteRepository.findAllById(idEstudiantes);
            estudiantes.forEach(e -> e.setGrupo(grupoGuardado));
            estudianteRepository.saveAll(estudiantes);
        }
        
        return grupoGuardado;
    }
    
    @Transactional
    public void eliminarGrupoSeguro(Integer idGrupo) {
        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));

        if (grupo.getEstudiantes() != null) {
            grupo.getEstudiantes().forEach(e -> e.setGrupo(null));
            estudianteRepository.saveAll(grupo.getEstudiantes());
        }

        List<Sesion> sesiones = sesionRepository.findByGrupo_IdGrupo(idGrupo);
        if (!sesiones.isEmpty()) {
            sesiones.forEach(s -> s.setEstatusRegistro("Cancelada"));
            sesionRepository.saveAll(sesiones);
        }

        grupo.setActivo(false);
        grupoTutoriaRepository.save(grupo);
    }
    
    @Transactional(readOnly = true)
    public GrupoTutoria obtenerPorId(Integer idGrupo) {
        return grupoTutoriaRepository.findById(idGrupo).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<GrupoTutoria> obtenerGruposActivosPorTutor(Integer idTutor) {
        return grupoTutoriaRepository.findByTutor_IdTutorAndPeriodo_EstatusActivoTrueAndActivoTrue(idTutor);
    }

    @Transactional(readOnly = true)
    public List<GrupoTutoria> buscarTutoriasPorPeriodo(Integer idPeriodo) {
        return grupoTutoriaRepository.findByPeriodo_IdPeriodoAndActivoTrue(idPeriodo);
    }

    @Transactional(readOnly = true)
    public List<GrupoTutoria> buscarHistorialTutoriasDeEstudiante(Integer idEstudiante) {
        return grupoTutoriaRepository.findByEstudiantes_IdEstudianteAndActivoTrue(idEstudiante);
    }
    
    @Transactional(readOnly = true)
    public List<GrupoTutoria> listarGruposPorEstatus(boolean activo) {
        return grupoTutoriaRepository.findByPeriodo_EstatusActivoAndActivoTrue(activo);
    }
}