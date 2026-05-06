package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.PeriodoEscolar;

import java.util.Optional;

@Repository
public interface PeriodoEscolarRepository extends JpaRepository<PeriodoEscolar, Integer> {
    Optional<PeriodoEscolar> findByEstatusActivoTrue();
}