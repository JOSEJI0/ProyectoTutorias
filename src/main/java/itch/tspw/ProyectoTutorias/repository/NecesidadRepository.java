package itch.tspw.ProyectoTutorias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.NecesidadEstudiante;

import java.util.List;

@Repository
public interface NecesidadRepository extends JpaRepository<NecesidadEstudiante, Integer> {
    
    // Para el TUTOR: Buscar solo las necesidades de los alumnos de sus grupos
    List<NecesidadEstudiante> findByEstudiante_Grupo_Tutor_IdTutorOrderByFechaSolicitudDesc(Integer idTutor);
    
    // Para el COORDINADOR: Buscar todas las de la escuela
    List<NecesidadEstudiante> findAllByOrderByFechaSolicitudDesc();
}