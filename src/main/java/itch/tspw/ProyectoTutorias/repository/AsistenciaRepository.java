package itch.tspw.ProyectoTutorias.repository;

import itch.tspw.ProyectoTutorias.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
    
    // Asistencias de un estudiante
    List<Asistencia> findByEstudiante_IdEstudiante(Integer idEstudiante);
    
    // Conteo de asistencias presentes
    long countByEstudiante_IdEstudianteAndPresenteTrue(Integer idEstudiante);
    
    // Conteo de ausencias
    long countByEstudiante_IdEstudianteAndPresenteFalse(Integer idEstudiante);
    
    //Buscar asistencias por sesión
    List<Asistencia> findBySesion_IdSesion(Integer idSesion);
	
	 // Buscar asistencias por grupo
    List<Asistencia> findBySesion_Grupo_IdGrupo(Integer idGrupo);
    


}