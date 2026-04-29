package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.ActividadPat;

import java.util.List;

@Repository
public interface ActividadPatRepository extends JpaRepository<ActividadPat, Integer> {
    List<ActividadPat> findByPat_IdPatOrderBySemanaProgramadaAsc(Integer idPat);
}