package itch.tspw.ProyectoTutorias.repository;

import itch.tspw.ProyectoTutorias.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
    
    List<Asistencia> findByEstudiante_IdEstudiante(Integer idEstudiante);
    long countByEstudiante_IdEstudianteAndPresenteTrue(Integer idEstudiante);
    long countByEstudiante_IdEstudianteAndPresenteFalse(Integer idEstudiante);
    List<Asistencia> findBySesion_IdSesion(Integer idSesion);
    List<Asistencia> findBySesion_Grupo_IdGrupo(Integer idGrupo);
    


}