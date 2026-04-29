package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.Perfil;

import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Integer> {
    Optional<Perfil> findByNombre(String nombre); // Nos servirá para buscar "ROLE_TUTOR", etc.
}