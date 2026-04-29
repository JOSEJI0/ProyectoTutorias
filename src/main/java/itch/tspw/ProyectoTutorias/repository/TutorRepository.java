package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.Tutor;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {
    
    Optional<Tutor> findByRfcEmpleado(String rfcEmpleado);
}