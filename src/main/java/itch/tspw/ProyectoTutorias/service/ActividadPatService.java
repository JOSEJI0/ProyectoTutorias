package itch.tspw.ProyectoTutorias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tspw.ProyectoTutorias.model.ActividadPat;
import itch.tspw.ProyectoTutorias.repository.ActividadPatRepository;

import java.util.List;

@Service
public class ActividadPatService {
    @Autowired
    private ActividadPatRepository actividadRepository;

    public List<ActividadPat> listarPorPat(Integer idPat) {
        return actividadRepository.findByPat_IdPatOrderBySemanaProgramadaAsc(idPat);
    }

    public void guardar(ActividadPat actividad) {
        actividadRepository.save(actividad);
    }

    public void eliminar(Integer id) {
        actividadRepository.deleteById(id);
    }
}