package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Al usar EAGER en la relación, esto consultará al usuario Y a sus perfiles automáticamente
    Optional<Usuario> findByCorreoInstitucional(String correoInstitucional);
}