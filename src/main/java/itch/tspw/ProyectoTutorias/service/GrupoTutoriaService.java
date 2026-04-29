package itch.tspw.ProyectoTutorias.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tspw.ProyectoTutorias.model.Estudiante;
import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.repository.EstudianteRepository;
import itch.tspw.ProyectoTutorias.repository.GrupoTutoriaRepository;

import java.util.List;

@Service
public class GrupoTutoriaService {
	


    @Autowired
    private GrupoTutoriaRepository grupoTutoriaRepository;
    @Autowired
    private EstudianteRepository estudianteRepository;
    
	public List<GrupoTutoria> listarTodos() {
        return grupoTutoriaRepository.findAll();
    }

	@Transactional
    public void asignarGrupo(GrupoTutoria grupo, List<Integer> idEstudiantes) {
        // 1. Guardamos el grupo (con su tutor, carrera y periodo)
        GrupoTutoria grupoGuardado = grupoTutoriaRepository.save(grupo);

        // 2. Buscamos a los estudiantes y los vinculamos
        List<Estudiante> estudiantes = estudianteRepository.findAllById(idEstudiantes);
        grupoGuardado.setEstudiantes(estudiantes);

        // 3. Guardamos la relación en la tabla intermedia
        grupoTutoriaRepository.save(grupoGuardado);
    }
    // Obtener un grupo por ID
    public GrupoTutoria obtenerPorId(Integer idGrupo) {
		return grupoTutoriaRepository.findById(idGrupo).orElse(null);
	}
    // Obtener grupos activos asignados a un tutor
    public List<GrupoTutoria> obtenerGruposActivosPorTutor(Integer idTutor) {
        return grupoTutoriaRepository.findByTutor_IdTutorAndPeriodo_EstatusActivoTrue(idTutor);
    }

    // Obtener grupos de un periodo
    public List<GrupoTutoria> buscarTutoriasPorPeriodo(Integer idPeriodo) {
        return grupoTutoriaRepository.findByPeriodo_IdPeriodo(idPeriodo);
    }

    // Obtener historial de tutorías de un estudiante
    public List<GrupoTutoria> buscarHistorialTutoriasDeEstudiante(Integer idEstudiante) {
        return grupoTutoriaRepository.findByEstudiantes_IdEstudiante(idEstudiante);
    }
}
/*
package tspw.tutorias.service;

import tspw.tutorias.model.Estudiante;
import tspw.tutorias.model.GrupoTutoria;
import tspw.tutorias.model.Tutor;
import tspw.tutorias.repository.EstudianteRepository;
import tspw.tutorias.repository.GrupoTutoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GrupoTutoriaService {

    @Autowired
    private GrupoTutoriaRepository grupoTutoriaRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

   
}
*/