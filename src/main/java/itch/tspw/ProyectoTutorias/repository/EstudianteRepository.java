package itch.tspw.ProyectoTutorias.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.Carrera;
import itch.tspw.ProyectoTutorias.model.Estudiante;
import itch.tspw.ProyectoTutorias.model.Usuario;


public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
	
    
    Optional<Estudiante> findByNumeroControl(String numeroControl);
    long countByCarrera(Carrera carrera);    
    List<Estudiante> findByGrupoIsNull();
    List<Estudiante> findByCarrera_IdCarreraAndGrupoIsNull(Integer idCarrera);
    List<Estudiante> findByActivoTrue();
    List<Estudiante> findBySemestreActualAndActivoTrue(Integer semestre);
    List<Estudiante> findByGrupoIsNullAndActivoTrue();
    List<Estudiante> findBySemestreActualAndCarrera_IdCarreraAndActivoTrue(Integer semestre, Integer idCarrera);
    List<Estudiante> findByCarrera_IdCarreraAndActivoTrue(Integer idCarrera);
    Optional<Estudiante> findByUsuario(Usuario usuario);
    
}