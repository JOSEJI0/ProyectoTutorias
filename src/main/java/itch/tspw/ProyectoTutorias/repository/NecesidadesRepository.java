package itch.tspw.ProyectoTutorias.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itch.tspw.ProyectoTutorias.model.NecesidadEstudiante;

@Repository
public interface NecesidadesRepository extends JpaRepository<NecesidadEstudiante, Integer> {
	
	List<NecesidadEstudiante> findByEstudiante_Grupo_Tutor_IdTutorOrderByFechaSolicitudDesc(Integer idTutor);
	
	List<NecesidadEstudiante> findAllByOrderByFechaSolicitudDesc();

}

