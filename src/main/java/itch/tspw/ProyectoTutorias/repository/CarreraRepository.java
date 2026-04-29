package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.Carrera;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {
}