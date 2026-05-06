package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PeriodoEscolarService {

    @Autowired
    private PeriodoEscolarRepository periodoRepository;

    public List<PeriodoEscolar> listarTodos() {
        return periodoRepository.findAll();
    }

    // AGREGAR O CORREGIR ESTE MÉTODO:
    public PeriodoEscolar obtenerPorId(Integer id) {
        return periodoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Periodo no encontrado"));
    }
    	//Obtener periodos activos
	public PeriodoEscolar obtenerActivo() {
		
		return periodoRepository.findByEstatusActivoTrue()
				.orElseThrow(() -> new IllegalArgumentException("No hay periodos activos"));
	}
}