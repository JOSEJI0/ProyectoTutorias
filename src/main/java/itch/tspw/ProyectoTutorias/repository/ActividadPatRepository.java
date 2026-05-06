package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.ActividadPat;

import java.util.List;

@Repository
public interface ActividadPatRepository extends JpaRepository<ActividadPat, Integer> {
    List<ActividadPat> findByPat_IdPatOrderBySemanaProgramadaAsc(Integer idPat);
    List<ActividadPat> findByPat_IdPat(Integer idPat);
    boolean existsByPat_IdPatAndSemanaProgramada(Integer idPat, Integer semanaProgramada);
    boolean existsByPat_IdPatAndTituloIgnoreCase(Integer idPat, String titulo);
    List<ActividadPat> findByPat_IdPatAndActivoTrueOrderBySemanaProgramadaAsc(Integer idPat);
    boolean existsByPat_IdPatAndSemanaProgramadaAndActivoTrue(Integer idPat, Integer semanaProgramada);
    boolean existsByPat_IdPatAndTituloIgnoreCaseAndActivoTrue(Integer idPat, String titulo);
}