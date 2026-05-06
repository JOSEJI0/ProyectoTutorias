package itch.tspw.ProyectoTutorias.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.Tutor;
import itch.tspw.ProyectoTutorias.model.Usuario;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {
    
    Optional<Tutor> findByRfcEmpleado(String rfcEmpleado);
    Optional<Tutor> findByUsuario(Usuario usuario);
}