package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.PatInstitucional;

@Repository
public interface PatRepository extends JpaRepository<PatInstitucional, Integer> {
}