package itch.tspw.ProyectoTutorias.service;

import org.springframework.stereotype.Service;
import itch.tspw.ProyectoTutorias.model.Carrera;
import itch.tspw.ProyectoTutorias.repository.CarreraRepository;

import java.util.List;

@Service
public class CarreraService {

	private final CarreraRepository carreraRepository;

    public CarreraService(CarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
    }
    public List<Carrera> listarTodas() {
        return carreraRepository.findAll();
    }

    public Carrera obtenerPorId(Integer id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));
    }

    public void guardar(Carrera carrera) {
        carreraRepository.save(carrera);
    }

    public void eliminar(Integer id) {
        carreraRepository.deleteById(id);
    }
}