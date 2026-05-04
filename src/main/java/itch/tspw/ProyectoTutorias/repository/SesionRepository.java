package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.Sesion;

import java.util.List;
import java.time.LocalDate;

public interface SesionRepository extends JpaRepository<Sesion, Integer> {
    
    // Sesiones de un grupo (CORRECTO)
    List<Sesion> findByGrupo_IdGrupo(Integer idGrupo);
    
    // Sesiones impartidas por un tutor
    List<Sesion> findByGrupo_Tutor_IdTutor(Integer idTutor);
    
    // Sesiones de un periodo escolar
    List<Sesion> findByGrupo_Periodo_IdPeriodo(Integer idPeriodo);
    
    // Sesiones por fecha
    List<Sesion> findByFechaImparticion(LocalDate fechaImparticion);
    
    // (ELIMINADA LA LÍNEA QUE ROMPÍA EL PROGRAMA: findByGrupoTutoria_IdGrupo)

    long countByGrupo_IdGrupo(Integer idGrupo);
}