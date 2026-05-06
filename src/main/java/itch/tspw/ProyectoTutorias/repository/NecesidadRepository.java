package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.NecesidadEstudiante;

import java.util.List;

@Repository
public interface NecesidadRepository extends JpaRepository<NecesidadEstudiante, Integer> {
    
    List<NecesidadEstudiante> findByEstudiante_Grupo_Tutor_IdTutorOrderByFechaSolicitudDesc(Integer idTutor);
    
    List<NecesidadEstudiante> findAllByOrderByFechaSolicitudDesc();
}