package itch.tspw.ProyectoTutorias.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.PatInstitucional;

@Repository
public interface PatRepository extends JpaRepository<PatInstitucional, Integer> {
    
    // Buscar si ya existe un PAT para una carrera en un periodo específico
    Optional<PatInstitucional> findByPeriodo_IdPeriodoAndCarrera_IdCarrera(Integer idPeriodo, Integer idCarrera);
    
 // Solo traer los activos
    List<PatInstitucional> findByActivoTrue();
    
    // Verificar duplicados solo entre los activos
    Optional<PatInstitucional> findByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrue(Integer idPeriodo, Integer idCarrera);
}