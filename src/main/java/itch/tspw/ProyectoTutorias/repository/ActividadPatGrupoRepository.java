package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.ActividadPatGrupo;

import java.util.List;

@Repository
public interface ActividadPatGrupoRepository extends JpaRepository<ActividadPatGrupo, Integer> {
    List<ActividadPatGrupo> findByPatGrupo_IdPatGrupoOrderBySemanaProgramadaAsc(Integer idPatGrupo);
}