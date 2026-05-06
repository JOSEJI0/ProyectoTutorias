package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.EvidenciaSesion;

import java.util.List;

public interface EvidenciaSesionRepository extends JpaRepository<EvidenciaSesion, Integer> {
    
    // Evidencias por estatus (p.ej. PENDIENTE)
    List<EvidenciaSesion> findByEstatusValidacion(String estatusValidacion);
}