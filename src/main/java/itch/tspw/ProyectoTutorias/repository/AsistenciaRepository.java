package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.Asistencia;

import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
    
    // Asistencias de un estudiante
    List<Asistencia> findByEstudiante_IdEstudiante(Integer idEstudiante);
    
    // Conteo de asistencias presentes
    long countByEstudiante_IdEstudianteAndPresenteTrue(Integer idEstudiante);
    
    // Conteo de ausencias
    long countByEstudiante_IdEstudianteAndPresenteFalse(Integer idEstudiante);
}