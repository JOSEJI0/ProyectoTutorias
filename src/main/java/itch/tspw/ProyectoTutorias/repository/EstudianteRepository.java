package itch.tspw.ProyectoTutorias.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.Carrera;
import itch.tspw.ProyectoTutorias.model.Estudiante;
import itch.tspw.ProyectoTutorias.model.Usuario;


public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
	
    
    // Buscar estudiante por número de control
    Optional<Estudiante> findByNumeroControl(String numeroControl);
    //Contar estudiantes por carrera
    long countByCarrera(Carrera carrera);
    
    //Mostrar solo los estudiantes sin grupo
    List<Estudiante> findByGrupoIsNull();
    
    //Filtrar por carrera y sin grupo
    List<Estudiante> findByCarrera_IdCarreraAndGrupoIsNull(Integer idCarrera);
    //Mostrar estudiantes activos
    List<Estudiante> findByActivoTrue();
    //Filtrar por semestre y activos
    List<Estudiante> findBySemestreActualAndActivoTrue(Integer semestre);
    //Mostrar estudiantes sin grupo y activos
    List<Estudiante> findByGrupoIsNullAndActivoTrue();
    //Filtrar por carrera y semestre y activos
    List<Estudiante> findBySemestreActualAndCarrera_IdCarreraAndActivoTrue(Integer semestre, Integer idCarrera);
    List<Estudiante> findByCarrera_IdCarreraAndActivoTrue(Integer idCarrera);
    //Buscar al estudiante por usuario LOGIN 
    Optional<Estudiante> findByUsuario(Usuario usuario);
    
}