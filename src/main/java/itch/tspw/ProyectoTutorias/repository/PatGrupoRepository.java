package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.PatGrupo;

import java.util.Optional;

@Repository
public interface PatGrupoRepository extends JpaRepository<PatGrupo, Integer> {
    Optional<PatGrupo> findByGrupo_IdGrupo(Integer idGrupo);
}