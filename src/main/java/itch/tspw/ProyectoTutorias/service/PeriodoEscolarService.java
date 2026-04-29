package itch.tspw.ProyectoTutorias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tspw.ProyectoTutorias.model.PeriodoEscolar;
import itch.tspw.ProyectoTutorias.repository.PeriodoEscolarRepository;

import java.util.List;

@Service
public class PeriodoEscolarService {

    @Autowired
    private PeriodoEscolarRepository periodoRepository;

    public List<PeriodoEscolar> listarTodos() {
        return periodoRepository.findAll();
    }

    public PeriodoEscolar obtenerActivo() {
        return periodoRepository.findByEstatusActivoTrue()
                .orElseThrow(() -> new RuntimeException("No hay un periodo escolar activo configurado."));
    }
}