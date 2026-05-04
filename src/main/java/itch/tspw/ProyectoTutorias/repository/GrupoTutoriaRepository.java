package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.GrupoTutoria;

import java.util.List;

@Repository
public interface GrupoTutoriaRepository extends JpaRepository<GrupoTutoria, Integer> {
    
    // Grupos activos asignados a un tutor Y que no estén eliminados
    List<GrupoTutoria> findByTutor_IdTutorAndPeriodo_EstatusActivoTrueAndActivoTrue(Integer idTutor);
    
    // Grupos de un periodo Y que no estén eliminados
    List<GrupoTutoria> findByPeriodo_IdPeriodoAndActivoTrue(Integer idPeriodo);

    // Grupos en los que ha estado un estudiante Y que no estén eliminados
    List<GrupoTutoria> findByEstudiantes_IdEstudianteAndActivoTrue(Integer idEstudiante); 
    
    // Buscar grupos por el estado del periodo Y que no estén eliminados
    List<GrupoTutoria> findByPeriodo_EstatusActivoAndActivoTrue(Boolean estatusActivo);
}