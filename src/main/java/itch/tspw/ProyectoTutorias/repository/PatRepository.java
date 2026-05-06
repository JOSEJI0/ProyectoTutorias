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
    
    Optional<PatInstitucional> findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraOrderByIdPatDesc(Integer idPeriodo, Integer idCarrera);
    
 // Filtra por Periodo, Carrera, que esté ACTIVO y trae el más reciente (ID Descendente)
    Optional<PatInstitucional> findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(Integer idPeriodo, Integer idCarrera);
    
    // Para listar solo los activos en las tablas generales del coordinador/tutor
    List<PatInstitucional> findByActivoTrueOrderByIdPatDesc();
}