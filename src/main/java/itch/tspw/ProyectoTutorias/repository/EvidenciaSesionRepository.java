package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.EvidenciaSesion;

import java.util.List;

public interface EvidenciaSesionRepository extends JpaRepository<EvidenciaSesion, Integer> {
    
    List<EvidenciaSesion> findByEstatusValidacion(String estatusValidacion);
}