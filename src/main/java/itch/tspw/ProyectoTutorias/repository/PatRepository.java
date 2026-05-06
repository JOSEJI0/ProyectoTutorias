package itch.tspw.ProyectoTutorias.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.PatInstitucional;


@Repository
public interface PatRepository extends JpaRepository<PatInstitucional, Integer> {
    
    Optional<PatInstitucional> findByPeriodo_IdPeriodoAndCarrera_IdCarrera(Integer idPeriodo, Integer idCarrera);
    List<PatInstitucional> findByActivoTrue();
    Optional<PatInstitucional> findByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrue(Integer idPeriodo, Integer idCarrera);
    Optional<PatInstitucional> findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraOrderByIdPatDesc(Integer idPeriodo, Integer idCarrera);
    Optional<PatInstitucional> findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(Integer idPeriodo, Integer idCarrera);
    List<PatInstitucional> findByActivoTrueOrderByIdPatDesc();
}