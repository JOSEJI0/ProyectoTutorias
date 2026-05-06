package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.GrupoTutoria;

import java.util.List;

@Repository
public interface GrupoTutoriaRepository extends JpaRepository<GrupoTutoria, Integer> {
    
    List<GrupoTutoria> findByTutor_IdTutorAndPeriodo_EstatusActivoTrueAndActivoTrue(Integer idTutor);
    
    List<GrupoTutoria> findByPeriodo_IdPeriodoAndActivoTrue(Integer idPeriodo);

    List<GrupoTutoria> findByEstudiantes_IdEstudianteAndActivoTrue(Integer idEstudiante); 
    
    List<GrupoTutoria> findByPeriodo_EstatusActivoAndActivoTrue(Boolean estatusActivo);
    
    List<GrupoTutoria> findByTutor_IdTutor(Integer idTutor);
}