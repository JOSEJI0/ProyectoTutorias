package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tspw.ProyectoTutorias.model.Sesion;

import java.util.List;
import java.time.LocalDate;

public interface SesionRepository extends JpaRepository<Sesion, Integer> {
    
    List<Sesion> findByGrupo_IdGrupo(Integer idGrupo);
    List<Sesion> findByGrupo_Tutor_IdTutor(Integer idTutor);
    List<Sesion> findByGrupo_Periodo_IdPeriodo(Integer idPeriodo);
    List<Sesion> findByFechaImparticion(LocalDate fechaImparticion);
    long countByGrupo_IdGrupo(Integer idGrupo);
    long countByGrupo_Tutor_IdTutor(Integer idTutor);
    List<Sesion> findByGrupo_IdGrupoAndEstatusRegistro(Integer idGrupo, String estatus);
    
    
}