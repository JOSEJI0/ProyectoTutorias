package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.Estudiante;

import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
	
    
    // Buscar estudiante por número de control
    Optional<Estudiante> findByNumeroControl(String numeroControl);
}