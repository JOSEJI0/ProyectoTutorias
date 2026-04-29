package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.GrupoTutoria;

import java.util.List;

public interface GrupoTutoriaRepository extends JpaRepository<GrupoTutoria, Integer> {
    
    // Grupos activos asignados a un tutor
    List<GrupoTutoria> findByTutor_IdTutorAndPeriodo_EstatusActivoTrue(Integer idTutor);
    // Grupos de un periodo
    List<GrupoTutoria> findByPeriodo_IdPeriodo(Integer idPeriodo);

    // Grupos en los que ha estado un estudiante
    List<GrupoTutoria> findByEstudiantes_IdEstudiante(Integer idEstudiante);
}