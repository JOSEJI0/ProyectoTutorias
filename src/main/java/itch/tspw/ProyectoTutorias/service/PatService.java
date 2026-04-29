package itch.tspw.ProyectoTutorias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tspw.ProyectoTutorias.model.PatInstitucional;
import itch.tspw.ProyectoTutorias.repository.PatRepository;

import java.util.List;

@Service
public class PatService {
    @Autowired
    private PatRepository patRepository;

    public List<PatInstitucional> listarTodos() {
        return patRepository.findAll();
    }

    public PatInstitucional obtenerPorId(Integer id) {
        return patRepository.findById(id).orElseThrow(() -> new RuntimeException("PAT no encontrado"));
    }

    public void guardar(PatInstitucional pat) {
        patRepository.save(pat);
    }
}